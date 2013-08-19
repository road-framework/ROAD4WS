package au.swin.ict.research.cs3.road.road4ws.message;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.context.MessageContext;

public class ResponseMsg {
    private MessageContext messageContext = null;
    private boolean successDelivery = false;
    private SOAPEnvelope envelope = null;

    public ResponseMsg() {
	super();
    }

    public ResponseMsg(MessageContext messageContext, boolean successDelivery,
	    SOAPEnvelope envelope) {
	super();
	this.messageContext = messageContext;
	this.successDelivery = successDelivery;
	this.envelope = envelope;
    }

    public MessageContext getMessageContext() {
	return messageContext;
    }

    public void setMessageContext(MessageContext messageContext) {
	this.messageContext = messageContext;
    }

    public boolean isSuccessDelivery() {
	return successDelivery;
    }

    public void setSuccessDelivery(boolean successDelivery) {
	this.successDelivery = successDelivery;
    }

    public SOAPEnvelope getEnvelope() {
	return envelope;
    }

    public void setEnvelope(SOAPEnvelope envelope) {
	this.envelope = envelope;
    }

}
