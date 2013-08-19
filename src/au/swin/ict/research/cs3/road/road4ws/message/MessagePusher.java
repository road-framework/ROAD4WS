package au.swin.ict.research.cs3.road.road4ws.message;

import java.util.LinkedList;
import java.util.Queue;

import au.edu.swin.ict.road.composite.IRole;
import au.edu.swin.ict.road.composite.listeners.RolePushMessageListener;
import au.edu.swin.ict.road.composite.message.MessageWrapper;
import au.edu.swin.ict.road.composite.message.MessageWrapper.SyncType;
import au.swin.ict.research.cs3.road.road4ws.core.ROADConstants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.util.IdleConnectionTimeoutThread;
import org.apache.log4j.Logger;

/**
 * Pushes message to a player specified by the endpoint. Collect the response
 * message if any and returns it back to roadfactory
 * 
 * @author Malinda Kapuruge
 */
public class MessagePusher implements RolePushMessageListener {
    private static final Logger log = Logger.getLogger(MessagePusher.class);
    private ConfigurationContext configurationContext = null;
    private IdleConnectionTimeoutThread idleConnectionTimeoutThread;
    private MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager;

    public MessagePusher(ConfigurationContext configurationContext) {
	String axis2HomeUrlStr = System.getenv("AXIS2_HOME");
	try {
	    this.configurationContext = ConfigurationContextFactory
		    .createConfigurationContextFromFileSystem(axis2HomeUrlStr
			    + "/conf/axis2.xml");
	    MultiThreadedHttpConnectionManager connManager = (MultiThreadedHttpConnectionManager) configurationContext
		    .getProperty(HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER);

	    if (connManager == null) {
		connManager = new MultiThreadedHttpConnectionManager();
		configurationContext.setProperty(
			HTTPConstants.MULTITHREAD_HTTP_CONNECTION_MANAGER,
			connManager);
		connManager.setParams(new HttpConnectionManagerParams());
	    }
	    connManager.getParams().setMaxTotalConnections(100);
	    connManager.getParams().setMaxConnectionsPerHost(
		    HostConfiguration.ANY_HOST_CONFIGURATION, 100);
	    idleConnectionTimeoutThread = new IdleConnectionTimeoutThread();
	    this.multiThreadedHttpConnectionManager = connManager;
	    idleConnectionTimeoutThread
		    .setName("Http_Idle_Connection_Timeout_Thread");
	    idleConnectionTimeoutThread.setConnectionTimeout(30000);
	    idleConnectionTimeoutThread.setTimeoutInterval(30000);
	    idleConnectionTimeoutThread.addConnectionManager(connManager);
	    idleConnectionTimeoutThread.start();

	} catch (AxisFault e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}// configurationContext;
    }

    @Override
    public void pushMessageRecieved(IRole role) {
	this.multiThreadedPush(role);
	// this.singleThreadedPush(role);
    }

    public void roleBasedPush(IRole role) {

    }

    public void multiThreadedPush(IRole role) {
	log.info("MULTI THREADED EXEC>>>>>>>>>>>>>> >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
	Thread mpt = new Thread(new MessagePusherThread(role,
		this.configurationContext));
	mpt.start();
	return;
    }

    public void singleThreadedPush(IRole role) {
	log.info("SINGLE THREADED EXEC>>>>>>>>>>>>>> >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
	new MessagePusherThread(role, this.configurationContext).run();// No
								       // multi
								       // threads
	return;
    }

    public void shutDown() {
	idleConnectionTimeoutThread.shutdown();
    }

    /*
     * The thread class that invokes external web services. If there is a
     * request then the message will be placed in the InQ of the role.
     */
    class MessagePusherThread implements Runnable {
	private IRole role = null;
	private ConfigurationContext configurationContext = null;

	public MessagePusherThread(IRole role,
		ConfigurationContext configurationContext) {
	    super();
	    this.role = role;
	    this.configurationContext = configurationContext;
	}

	@Override
	public void run() {
	    // We have predefined number of push attepmts
	    int pushAttempts = ROADConstants.ROAD4WS_DEFAULT_PUSH_ATTEMPTS;
	    log.info("A message need to be pushed as there is one in the queue of Role="
		    + role.getName());
	    // Get the message that need to pushed
	    MessageWrapper mw = role.getNextPushMessage();

	    // Quit if this is a false alarm
	    if (null == mw) {
		if (log.isInfoEnabled()) {
		    log.info("No more messages in " + role.getName() + " queue");
		}
		// break;
		return;
	    }

	    // If this is as a response, well.. let the message reciever to
	    // handle
	    if (true == mw.isResponse()) {// DO we still need this check????
		// break;
		return;
	    }

	    // Get the epr from the Role and the operation
	    String playerBinding = role.getPlayerBinding()
		    .replaceAll("(\\r|\\n)", "").trim();
	    if (log.isDebugEnabled()) {
		log.debug("player binding " + playerBinding);
	    }
	    String eprStr = playerBinding + "/" + mw.getOperationName();
	    if (log.isDebugEnabled()) {
		log.debug("eprStr " + eprStr);
	    }
	    // Unwrap
	    Object msg = mw.getMessage();

	    if (mw.getSyncType() == SyncType.OUT) {
		SOAPMessageSender messageSender = new SOAPMessageSender();
		try {
		    messageSender.sendSOAPMsg(msg, eprStr, null,
			    this.configurationContext, false,
			    multiThreadedHttpConnectionManager);
		} catch (MessageDeliveryException e) {
		    e.printStackTrace();
		}
		return;
	    }

	    if (null != msg) {
		int attempt = 1;
		boolean success = false;
		// We use SOAP message delivery
		SOAPMessageSender messageSender = new SOAPMessageSender();
		// Try to send three times if not success
		while (!success && attempt < pushAttempts) {
		    try {
			if (log.isInfoEnabled()) {
			    log.info("[Attempt =" + attempt
				    + "]Pushing message of Role ="
				    + role.getName() + ",to end point ="
				    + eprStr + ",with action ="
				    + mw.getOperationName());
			}
			attempt++;
			// This is where we send the message
			// ResponseMsg responseMsg = messageSender
			// .sendSOAPMsg(
			// msg,
			// eprStr,
			// (String) mw
			// .getProperty(ROADConstants.ROAD4WS_MW_SOAP_ACTION));
			ResponseMsg responseMsg = messageSender.sendSOAPMsg(
				msg, eprStr, null, this.configurationContext,
				true, multiThreadedHttpConnectionManager);
			// If there is no exception that means we have delivered
			// the message
			success = responseMsg.isSuccessDelivery();
			// If there is a response route it to the roadfactory
			if ((true == success)
				&& (null != responseMsg.getEnvelope())) {
			    // TODO route the SOAP Envelope to the roadfactory
			    if (log.isInfoEnabled()) {
				log.info("Message delivered to " + eprStr);
			    }
			    if (log.isInfoEnabled()) {
				log.info("There is a response"
					+ responseMsg.getEnvelope().toString());
			    }
			    // We will remember the operation name
			    String opName = mw.getOperationName();//
			    MessageWrapper smwResponse = new MessageWrapper(
				    responseMsg.getEnvelope(), opName, true);
			    // Set the SOAP Action
			    // smwResponse.setMessageClassifier(mw.getMessageClassifier());
			    // //TODO Indika
			    // smwResponse.setClientID(mw.getClientID());
			    smwResponse.setProperty(
				    ROADConstants.ROAD4WS_MW_SOAP_ACTION,
				    responseMsg.getMessageContext()
					    .getSoapAction());
			    // Set originated role
			    smwResponse.setOriginRole(role);
			    // Set the correlation that of the request
			    smwResponse.setCorrelationId(mw.getCorrelationId());
			    if (log.isDebugEnabled()) {
				log.debug("Crrelation id set ."
					+ smwResponse.getCorrelationId() + " ");
			    }
			    // Do we still need this?
			    smwResponse.setProperty(
				    ROADConstants.ROAD4WS_MW_IS_RESPONSE,
				    ROADConstants.ROAD4WS_MW_RESPONSE);
			    smwResponse.setTaskId(mw.getTaskId());
			    // Drop message to the composite
			    role.putMessage(smwResponse);
			    if (log.isDebugEnabled()) {
				log.debug("Response message successfully dropped back to ."
					+ role.getId());
			    }

			} else {
			    if (log.isInfoEnabled()) {
				log.info("There is no response");
			    }
			}
		    } catch (MessageDeliveryException e) {
			success = false;
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}// eof while loop (!success && attempt < pushAttempts)

		if (!success) {
		    if (log.isDebugEnabled()) {
			log.debug("Message delivery permanantly failed");
		    }

		} else {
		    // return true;
		}

	    } else {
		log.error("Message is NULL. Cannot be pushed to " + eprStr);
	    }

	}
    }
}
