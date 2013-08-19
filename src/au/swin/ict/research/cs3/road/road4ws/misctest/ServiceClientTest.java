package au.swin.ict.research.cs3.road.road4ws.misctest;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.dom.soap11.SOAP11Factory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;

import au.swin.ict.research.cs3.road.road4ws.message.MessageDeliveryException;

public class ServiceClientTest {
    public static void main(String[] args) {
	String eprStr = "http://localhost:7070/axis2/services/DavidChef/";
	ServiceClient client = null;
	SOAPEnvelope envelope = null;
	try {
	    // Create a new config context
	    String axis2HomeUrlStr = System.getenv("AXIS2_HOME");
	    ConfigurationContext configCtxt = ConfigurationContextFactory
		    .createConfigurationContextFromFileSystem(axis2HomeUrlStr
			    + "conf/axis2.xml");

	    // Create service client
	    client = new ServiceClient(configCtxt, null);// null service
	    Options options = new Options();
	    // options.setAction(action);
	    options.setTo(new EndpointReference(eprStr));
	    options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
	    client.setOptions(options);

	    // creating message context
	    MessageContext outMsgCtx = new MessageContext();

	    // Set the envelope
	    outMsgCtx.setEnvelope(getEnvelope());// (SOAPEnvelope)SOAPMessage
	    outMsgCtx.setConfigurationContext(configCtxt);// twice?

	    // Create opclient from the svc client
	    // Ref:http://today.java.net/pub/a/today/2006/12/13/invoking-web-services-using-apache-axis2.html
	    OperationClient operationClient = client
		    .createClient(ServiceClient.ANON_OUT_IN_OP);
	    operationClient.addMessageContext(outMsgCtx);

	    // Execute
	    operationClient.execute(true);

	    // Process the response
	    MessageContext inMsgtCtx = operationClient.getMessageContext("In");
	    envelope = inMsgtCtx.getEnvelope();

	} catch (AxisFault e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();

	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public static SOAPEnvelope getEnvelope() {
	SOAP11Factory factory = new SOAP11Factory();
	SOAPEnvelope envelope = factory.createSOAPEnvelope();

	return envelope;
    }
}
