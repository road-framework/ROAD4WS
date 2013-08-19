package au.swin.ict.research.cs3.road.road4ws.core.mgsrcvr;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;
import org.apache.log4j.Logger;

import au.edu.swin.ict.road.composite.Composite;
import au.edu.swin.ict.road.composite.IRole;
import au.edu.swin.ict.road.composite.message.MessageWrapper;
import au.swin.ict.research.cs3.road.road4ws.core.ROADConstants;

public class ROADInOutMessageReceiver extends RawXMLINOutMessageReceiver
	implements MessageReceiver {
    private SOAPEnvelope responseEnvelope = null;
    private MessageContext oldMsgContext = null;
    private boolean wait = false;
    private Composite composite = null;
    private String opName = null;
    private static int counter = 0;
    private static final Logger log = Logger
	    .getLogger(ROADInOutMessageReceiver.class.getName());

    public void invokeBusinessLogic(MessageContext msgContext,
	    MessageContext newmsgContext) throws AxisFault {

	IRole role = null;
	try {

	    this.oldMsgContext = msgContext;

	    this.opName = msgContext.getOperationContext().getOperationName();

	    this.composite = (Composite) msgContext
		    .getProperty(ROADConstants.ROAD4WS_CURRENT_COMPOSITE);
	    role = (IRole) msgContext
		    .getProperty(ROADConstants.ROAD4WS_CURRENT_ROLE);

	    log.debug("ROADInOutMessageReceiver invokeBusinessLogic for "
		    + opName + " of " + role.getId() + " in Composite: "
		    + composite.getName());

	    if (null == composite) {
		log.debug("ROAD4WS Exception: The mesg Reciever cannot get the composite from the axis2 message contxt");
		throw new AxisFault(
			"ROAD4WS Exception: The mesg Reciever cannot get the composite from the axis2 message contxt");
	    }
	    if (null == role) {
		log.debug("ROAD4WS Exception: The mesg Reciever cannot get the Role from the axis2 message contxt");
		throw new AxisFault(
			"ROAD4WS Exception: The mesg Reciever cannot get the Role from the axis2 message contxt");
	    }

	    MessageWrapper mwReturn = this.syncDrop(msgContext, role, opName);
	    if (null == mwReturn) {
		throw AxisFault.makeFault(new Exception(
			"[ROAD4WS]There is no return message"));
	    }

	    if (mwReturn.isAnErrorMessage()) {
		throw AxisFault.makeFault(new Exception(
			"[ROAD4WS]Message Processing Error. Reason:"
				+ mwReturn.getErrorMessage()));
	    }
	    if (null != mwReturn) {
		SOAPEnvelope returnEnvelope = (SOAPEnvelope) mwReturn
			.getMessage();
		returnEnvelope.build();
		newmsgContext.setEnvelope(returnEnvelope);
	    }

	} catch (Exception e) {
	    throw AxisFault.makeFault(e);
	}
    }

    /**
     * Drops a synchronous message
     * 
     * @param msgContext
     * @param role
     * @param opName
     * @return
     * @throws AxisFault
     */
    private MessageWrapper syncDrop(MessageContext msgContext, IRole role,
	    String opName) throws AxisFault {
	MessageWrapper mwReturn = null;

	SOAPEnvelope putEnvelope = null;
	String actionStr = null;
	if (null == opName) {
	    log.error("Cannot get the operation name >>>");
	    return null;
	} else {
	    log.debug("The operation name is " + opName);
	}

	// Create a SOAP Message Wrapper
	putEnvelope = msgContext.getEnvelope();
	putEnvelope.build();
	actionStr = msgContext.getSoapAction();
	log.debug("Dropping msg " + putEnvelope.toString()
		+ " \n to comp; Role =" + role.getName() + " -> Operation="
		+ opName);
	MessageWrapper mwRequest = new MessageWrapper(putEnvelope, opName,
		false);// Create new message
	// smw.setProperty(ROADConstants.ROAD4WS_MW_ID, counter++);
	mwRequest.setProperty(ROADConstants.ROAD4WS_MW_SOAP_ACTION, actionStr);
	// log.debug("Role=" + role.getName() + " -> Operation=" + opName);
	mwRequest.setOriginRole(role);
	log.debug("Message successfully dropped.");
	// Drop message to the composite. This should return a mw as this
	// invocation should have a return type
	mwReturn = role.putSyncMessage(mwRequest);

	// To create a dummy soap envelope
	// mwReturn = new MessageWrapper(this.createDummyEnvelope(msgContext),
	// opName, true);

	return mwReturn;
    }

    /**
     * Test method
     * 
     * @deprecated
     * @param msgContext
     * @return
     * @throws AxisFault
     */
    public SOAPEnvelope createDummyEnvelope(MessageContext msgContext)
	    throws AxisFault {
	SOAPFactory soapFac = getSOAPFactory(msgContext);
	SOAPEnvelope envelope = soapFac.getDefaultEnvelope();

	OMFactory fac = OMAbstractFactory.getOMFactory();
	OMNamespace omNs = fac.createOMNamespace("http://road4ws.com/xsd",
		"tns");

	OMElement method = fac.createOMElement("Empty", omNs);
	OMElement value = fac.createOMElement("EmptyMsg", omNs);
	value.addChild(fac.createOMText(value, "0"));
	method.addChild(value);

	envelope.getBody().addChild(method);

	return envelope;
    }

    // Send a message indicating that no mesages are available in the queue
    private OMElement createEmptyMsg() {

	OMFactory fac = OMAbstractFactory.getOMFactory();
	OMNamespace omNs = fac.createOMNamespace(
		"http://quickstart.samples/xsd", "tns");

	OMElement method = fac.createOMElement("NoMessage", omNs);
	OMElement value = fac.createOMElement("MesseageCount", omNs);
	value.addChild(fac.createOMText(value, "0"));
	method.addChild(value);
	return method;

    }

}
