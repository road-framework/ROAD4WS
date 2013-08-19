package au.swin.ict.research.cs3.road.road4ws.message;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.log4j.Logger;

/**
 * Class to handle sending SOAP messages.
 * 
 * @author Malinda Kapuruge
 */
public class SOAPMessageSender {
    private static final Logger log = Logger.getLogger(SOAPMessageSender.class);

    //
    public ResponseMsg sendSOAPMsg(Object SOAPMessage, String eprStr,
	    String action, ConfigurationContext configCtxt,
	    boolean isResponseExpected,
	    MultiThreadedHttpConnectionManager connManager)
	    throws MessageDeliveryException {
	ResponseMsg responseMsg = null;
	ServiceClient client = null;
	SOAPEnvelope envelope = null;
	OperationClient operationClient = null;
	try {
	    // Create a new config context
	    // 2407: We do not create new config context
	    // String axis2HomeUrlStr = System.getenv("AXIS2_HOME");
	    // ConfigurationContext configCtxt =
	    // ConfigurationContextFactory.createConfigurationContextFromFileSystem(axis2HomeUrlStr+"/conf/axis2.xml");

	    // Create service client
	    client = new ServiceClient(configCtxt, null);// null=service
	    client.getServiceContext().setProperty(
		    HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER,
		    connManager);
	    Options options = new Options();
	    options.setAction(action);
	    options.setSenderTransport(Constants.TRANSPORT_HTTP,
		    configCtxt.getAxisConfiguration());

	    options.setTo(new EndpointReference(eprStr));
	    options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
	    options.setProperty(HTTPConstants.SO_TIMEOUT, 60000000);
	    options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, 100000000);
	    client.setOptions(options);

	    // creating message context
	    MessageContext outMsgCtx = new MessageContext();

	    // Set the envelope
	    outMsgCtx.setEnvelope((SOAPEnvelope) SOAPMessage);
	    outMsgCtx.setConfigurationContext(configCtxt);// twice?

	    // Create opclient from the svc client
	    // Ref:http://today.java.net/pub/a/today/2006/12/13/invoking-web-services-using-apache-axis2.html
	    if (isResponseExpected) {
		operationClient = client
			.createClient(ServiceClient.ANON_ROBUST_OUT_ONLY_OP);
	    } else {
		operationClient = client
			.createClient(ServiceClient.ANON_OUT_IN_OP);
	    }

	    operationClient.addMessageContext(outMsgCtx);

	    // Execute
	    operationClient.execute(true);

	    // Process the response for two way interactions
	    if (isResponseExpected) {
		MessageContext inMsgtCtx = operationClient
			.getMessageContext("In");
		envelope = inMsgtCtx.getEnvelope();
		// Now we create our response

		responseMsg = new ResponseMsg(inMsgtCtx, true, envelope);
	    }

	    client.cleanup();
	    client.cleanupTransport();

	} catch (AxisFault e) {
	    // TODO Auto-generated catch block
	    log.error(e);
	    throw new MessageDeliveryException(eprStr);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    log.error(e);
	    e.printStackTrace();
	}

	return responseMsg;
    }
}