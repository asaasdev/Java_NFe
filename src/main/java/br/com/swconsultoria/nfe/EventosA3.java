package br.com.swconsultoria.nfe;

import java.rmi.RemoteException;
import javax.xml.stream.XMLStreamException;
import br.com.swconsultoria.nfe.ws.RetryParameter;
import lombok.extern.java.Log;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.transport.http.HTTPConstants;
import br.com.swconsultoria.nfe.dom.ConfiguracoesNfe;
import br.com.swconsultoria.nfe.dom.enuns.DocumentoEnum;
import br.com.swconsultoria.nfe.dom.enuns.ServicosEnum;
import br.com.swconsultoria.nfe.exception.NfeException;
import br.com.swconsultoria.nfe.util.ObjetoUtil;
import br.com.swconsultoria.nfe.util.WebServiceUtil;
import br.com.swconsultoria.nfe.wsdl.NFeRecepcaoEvento.NFeRecepcaoEvento4Stub;

@Log
public class EventosA3 {
    static String enviarEvento(ConfiguracoesNfe config, String xmlAssinado, ServicosEnum tipoEvento, boolean valida, DocumentoEnum tipoDocumento)
            throws NfeException {
        try {

            log.info("[XML-ENVIO-" + tipoEvento + "]: " + xmlAssinado);

            if (valida) {
                new Validar().validaXml(config, xmlAssinado, tipoEvento);
            }

            OMElement ome = AXIOMUtil.stringToOM(xmlAssinado);

            NFeRecepcaoEvento4Stub.NfeDadosMsg dadosMsg = new NFeRecepcaoEvento4Stub.NfeDadosMsg();
            dadosMsg.setExtraElement(ome);

            String url = WebServiceUtil.getUrl(config, tipoDocumento, tipoEvento);

            NFeRecepcaoEvento4Stub stub = new NFeRecepcaoEvento4Stub(url);

            if (ObjetoUtil.verifica(config.getTimeout()).isPresent()) {
                stub._getServiceClient().getOptions().setProperty(HTTPConstants.SO_TIMEOUT, config.getTimeout());
                stub._getServiceClient().getOptions().setProperty(HTTPConstants.CONNECTION_TIMEOUT, config.getTimeout());
            }

            if (ObjetoUtil.verifica(config.getRetry()).isPresent()) {
                RetryParameter.populateRetry(stub, config.getRetry());
            }

            NFeRecepcaoEvento4Stub.NfeResultMsg result = stub.nfeRecepcaoEvento(dadosMsg);

            log.info("[XML-RETORNO-" + tipoEvento + "]: " + result.getExtraElement().toString());
            return result.getExtraElement().toString();

        } catch (RemoteException | XMLStreamException e) {
            throw new NfeException(e.getMessage());
        }
    }
}
