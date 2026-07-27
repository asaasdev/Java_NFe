package br.com.swconsultoria.nfe;

import javax.xml.bind.JAXBException;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.DocumentoEnum;
import br.com.swconsultoria.nfe.dom.enuns.ServicosEnum;
import br.com.swconsultoria.nfe.exception.NfeException;
import br.com.swconsultoria.nfe.schemas_eventos.TEnvEventoGenerico;
import br.com.swconsultoria.nfe.schemas_eventos.TProcEventoGenerico;
import br.com.swconsultoria.nfe.schemas_eventos.TRetEnvEventoGenerico;
import br.com.swconsultoria.nfe.schemas_eventos.TRetEventoGenerico;
import br.com.swconsultoria.nfe.util.ConstantesUtil;
import br.com.swconsultoria.nfe.util.XmlNfeUtil;

class EventoGenericoA3 {

    private EventoGenericoA3() {}

    static TRetEnvEventoGenerico eventoGenericoA3(ConfiguracoesNfe config, boolean valida, String xmlAssinado)
            throws NfeException {
        String xmlRetorno = EventosA3.enviarEvento(config, xmlAssinado, ServicosEnum.EVENTO_GENERICO, valida, DocumentoEnum.NFE);
        return XmlNfeUtil.xmlToObject(xmlRetorno, TRetEnvEventoGenerico.class);
    }

    static String criaProcEventoGenericoA3(ConfiguracoesNfe config, String xmlAssinado, TRetEventoGenerico retorno)
            throws NfeException {
        try {
            TProcEventoGenerico procEvento = new TProcEventoGenerico();
            procEvento.setVersao(ConstantesUtil.VERSAO.EVENTO_GENERICO);
            procEvento.setEvento(XmlNfeUtil.xmlToObject(xmlAssinado, TEnvEventoGenerico.class).getEvento().get(0));
            procEvento.setRetEvento(retorno);
            return XmlNfeUtil.objectToXml(procEvento, config.getEncode());
        } catch (JAXBException e) {
            throw new NfeException(e.getMessage(), e);
        }
    }
}
