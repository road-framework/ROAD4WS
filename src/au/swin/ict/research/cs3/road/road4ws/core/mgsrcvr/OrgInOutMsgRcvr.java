package au.swin.ict.research.cs3.road.road4ws.core.mgsrcvr;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.log4j.Logger;

import au.edu.swin.ict.road.composite.IOrganiserRole;

/**
 * TODO: Use this in the organizer deployer
 * 
 * @author Malinda Kapuruge
 * 
 */
public class OrgInOutMsgRcvr extends
	org.apache.axis2.rpc.receivers.RPCInOutAsyncMessageReceiver {
    private static final Logger log = Logger.getLogger(OrgInOutMsgRcvr.class);
    IOrganiserRole organizer = null;

    public OrgInOutMsgRcvr(IOrganiserRole organizer) {
	this.organizer = organizer;
    }

    public void invokeBusinessLogic(MessageContext msgContext,
	    MessageContext newmsgContext) throws AxisFault {
	log.debug("OrgInOutMsgRcvr invokeBusinessLogic");
	Object obj = this.getTheImplementationObject(msgContext);
	if (null != obj) {
	    IOrganiserRole orgObj = (IOrganiserRole) obj;
	    log.info("[ROAD4WS]The organizer has been found for "
		    + orgObj.toString());
	} else {
	    log.info("[ROAD4WS]The organizer object is NULL");
	    throw new AxisFault(
		    "[ROAD4WS]Configuration error: Cannot find the organizer object");
	}

	super.invokeBusinessLogic(msgContext, newmsgContext);
    }

    /* Overiding the parent method */
    protected Object makeNewServiceObject(MessageContext msgContext)
	    throws AxisFault {
	return this.organizer;
    }
}
