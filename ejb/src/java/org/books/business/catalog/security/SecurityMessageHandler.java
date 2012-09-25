package org.books.business.catalog.security;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 *
 * @author Christoph Horber
 */
public class SecurityMessageHandler implements SOAPHandler<SOAPMessageContext> {
    //TODO Set access key
    private static final String ACCESS_KEY = "...";
    //TODO Set secret key
    private static final String SECRET_KEY = "...";
    
    private static final Logger LOGGER = Logger.getLogger(SecurityMessageHandler.class.getName());
    
    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);  
        if (outbound) {
            try {
                SOAPMessage message = context.getMessage();
                SOAPEnvelope env = message.getSOAPPart().getEnvelope();
                SOAPHeader header = env.addHeader();
                
                String operation = env.getBody().getFirstChild().getNodeName();
                enrichHeaderWithSecurity(header, operation);
                context.getMessage().saveChanges();
            } catch (SOAPException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                return false;
            }
        }
        
        // no inbound handling
        
        printMsg(context);
        
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    @Override
    public void close(MessageContext context) {
        LOGGER.log(Level.FINE, String.format("Closed %s", this.getClass().getName()));
    }
    
    private static void enrichHeaderWithSecurity(SOAPHeader header, String operation) throws SOAPException {
        header.addNamespaceDeclaration("sec", "http://security.amazonaws.com/doc/2007-01-01/");
        SOAPElement eAwsAccessKeyId = header.addChildElement("AWSAccessKeyId", "sec");
        eAwsAccessKeyId.setValue(ACCESS_KEY);
        SOAPElement eTimestamp = header.addChildElement("Timestamp", "sec");
        String timestamp = SecurityHelper.getTimestamp();
        eTimestamp.setValue(timestamp);
        SOAPElement eSignature = header.addChildElement("Signature", "sec");
        eSignature.setValue(SecurityHelper.getSignature(SECRET_KEY, operation, timestamp));
    }
    
    private void printMsg(SOAPMessageContext ctx) {
        try {
            StringWriter buffer = new StringWriter();
            PrintWriter writer = new PrintWriter(buffer);
            boolean outbound = (Boolean) ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
            writer.println("SOAP " + (outbound ? "response" : "request") + ":");

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(new DOMSource(ctx.getMessage().getSOAPPart()), new StreamResult(writer));
            // Temporary disabled
            //LOGGER.info(buffer.toString());
        } catch (Exception ex) {
            // Could not log message
            // Ignore exception
        }
    } 
}