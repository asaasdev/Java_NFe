/**
 *
 */
package br.com.swconsultoria.nfe.util;

import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.AmbienteEnum;
import br.com.swconsultoria.nfe.dom.enuns.DocumentoEnum;
import br.com.swconsultoria.nfe.dom.enuns.EstadosEnum;
import br.com.swconsultoria.nfe.dom.enuns.ServicosEnum;
import br.com.swconsultoria.nfe.exception.NfeException;
import lombok.extern.java.Log;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.*;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * @author Samuel Oliveira
 *
 * Classe responsávelem montar as URL's de consulta de serviços do SEFAZ.
 */
@Log
public class WebServiceUtil {

    private final static Logger logger = Logger.getLogger(WebServiceUtil.class.getName());

    private static INIConfiguration loadIniConfiguration(ConfiguracoesNfe config) throws NfeException {
        InputStream is;
        try {
            if (ObjetoUtil.verifica(config.getArquivoWebService()).isPresent()) {
                File arquivo = new File(config.getArquivoWebService());
                if (!arquivo.exists()) {
                    throw new FileNotFoundException("Arquivo WebService " + config.getArquivoWebService() + " não encontrado");
                }
                is = new FileInputStream(arquivo);
                logger.info("[ARQUIVO INI CUSTOMIZADO]: " + config.getArquivoWebService());
            } else {
                is = WebServiceUtil.class.getResourceAsStream("/WebServicesNfe.ini");
                if (is == null) {
                    throw new NfeException("Arquivo WebServicesNfe.ini não encontrado no classpath.");
                }
            }

            INIConfiguration iniConfig = new INIConfiguration();
            try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                iniConfig.read(reader);
            } finally {
                // InputStreamReader typically closes the underlying stream, but a redundant close is safe.
                try {
                    is.close();
                } catch (IOException e) {
                    logger.fine("Error closing InputStream: " + e.getMessage());
                }
            }
            return iniConfig;
        } catch (IOException | ConfigurationException e) {
            throw new NfeException("Erro ao carregar arquivo de configuração WebService: " + e.getMessage(), e);
        }
    }

    private static String determineLookupSectionKey(INIConfiguration iniConfig, ConfiguracoesNfe config, DocumentoEnum tipoDocumento, ServicosEnum tipoServico, String initialSecao) throws NfeException {
        String lookupSectionKey = initialSecao;

        SubnodeConfiguration initialSectionConfig = iniConfig.getSection(initialSecao);
        String usarValue = null;
        if (initialSectionConfig != null && !initialSectionConfig.isEmpty()) {
            usarValue = getStringIgnoreCase(initialSectionConfig, "Usar");
        }

        // Logic to determine the final 'lookupSectionKey'
        if (tipoServico.equals(ServicosEnum.CONSULTA_CADASTRO) && (
                config.getEstado().equals(EstadosEnum.PA) ||
                        config.getEstado().equals(EstadosEnum.AM) ||
                        config.getEstado().equals(EstadosEnum.AL) ||
                        config.getEstado().equals(EstadosEnum.AP) ||
                        config.getEstado().equals(EstadosEnum.DF) ||
                        config.getEstado().equals(EstadosEnum.PI) ||
                        config.getEstado().equals(EstadosEnum.RJ) ||
                        config.getEstado().equals(EstadosEnum.RO) ||
                        config.getEstado().equals(EstadosEnum.SE) ||
                        config.getEstado().equals(EstadosEnum.TO))) {
            throw new NfeException("Estado não possui Consulta Cadastro.");
        } else if (tipoServico.equals(ServicosEnum.DISTRIBUICAO_DFE) ||
                tipoServico.equals(ServicosEnum.MANIFESTACAO) ||
                tipoServico.equals(ServicosEnum.EPEC)) {
            lookupSectionKey = config.getAmbiente().equals(AmbienteEnum.HOMOLOGACAO) ? "NFe_AN_H" : "NFe_AN_P";
        } else if (!tipoServico.equals(ServicosEnum.URL_CONSULTANFCE) &&
                !tipoServico.equals(ServicosEnum.URL_QRCODE) &&
                config.isContigenciaSVC() && tipoDocumento.equals(DocumentoEnum.NFE)) {
            if (config.getEstado().equals(EstadosEnum.GO) || config.getEstado().equals(EstadosEnum.AM) ||
                    config.getEstado().equals(EstadosEnum.BA) || config.getEstado().equals(EstadosEnum.CE) ||
                    config.getEstado().equals(EstadosEnum.MA) || config.getEstado().equals(EstadosEnum.MS) ||
                    config.getEstado().equals(EstadosEnum.MT) || config.getEstado().equals(EstadosEnum.PA) ||
                    config.getEstado().equals(EstadosEnum.PE) || config.getEstado().equals(EstadosEnum.PI) ||
                    config.getEstado().equals(EstadosEnum.PR)) {
                lookupSectionKey = tipoDocumento.getTipo() + "_SVRS_" + (config.getAmbiente().equals(AmbienteEnum.HOMOLOGACAO) ? "H" : "P");
            } else {
                lookupSectionKey = tipoDocumento.getTipo() + "_SVC-AN_" + (config.getAmbiente().equals(AmbienteEnum.HOMOLOGACAO) ? "H" : "P");
            }
        } else if (ObjetoUtil.verifica(usarValue).isPresent() &&
                !tipoServico.equals(ServicosEnum.URL_CONSULTANFCE) &&
                !tipoServico.equals(ServicosEnum.URL_QRCODE)) {
            lookupSectionKey = usarValue;
        }

        // If none of the above, lookupSectionKey remains initialSecao
        return lookupSectionKey;
    }

    private static String getStringIgnoreCase(SubnodeConfiguration sectionConfig, String targetKey) {
        if (sectionConfig == null || sectionConfig.isEmpty() || targetKey == null) {
            return null;
        }

        // First, try direct access with the targetKey, relying on SubnodeConfiguration's case-insensitivity
        String value = sectionConfig.getString(targetKey, null);
        if (value != null) {
            return value;
        }

        // If direct access failed, iterate and check with equalsIgnoreCase
        for (Iterator<String> it = sectionConfig.getKeys(); it.hasNext(); ) {
            String key = it.next();
            String normalizedIteratedKey = key.replace("..", "."); // Normalize ".." to "." from iterated key
            if (targetKey.equalsIgnoreCase(normalizedIteratedKey)) {
                return sectionConfig.getString(key); // Use original iterated key
            }
        }
        return null;
    }

    public static String getUrl(ConfiguracoesNfe config, DocumentoEnum tipoDocumento, ServicosEnum tipoServico) throws NfeException {
        String initialSecao = tipoDocumento.getTipo() + "_" + config.getEstado() + "_"
                + (config.getAmbiente().equals(AmbienteEnum.HOMOLOGACAO) ? "H" : "P");

        INIConfiguration iniConfig = loadIniConfiguration(config);

        String lookupSectionKey = determineLookupSectionKey(iniConfig, config, tipoDocumento, tipoServico, initialSecao);

        SubnodeConfiguration finalSectionConfig = iniConfig.getSection(lookupSectionKey);
        String finalUrl = getStringIgnoreCase(finalSectionConfig, tipoServico.getServico());

        final String finalSectionKeyForLambda = lookupSectionKey; // Essential for lambda
        ObjetoUtil.verifica(finalUrl).orElseThrow(() -> new NfeException(
                "WebService de " + tipoServico + " não encontrado para " + config.getEstado().getNome() + " na seção " + finalSectionKeyForLambda));

        logger.info("[URL]: " + tipoServico + ": " + finalUrl);
        return finalUrl;
    }
}
