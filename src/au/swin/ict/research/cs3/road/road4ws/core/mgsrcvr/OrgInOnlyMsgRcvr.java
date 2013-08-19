package au.swin.ict.research.cs3.road.road4ws.core.mgsrcvr;

import java.lang.reflect.Method;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.log4j.Logger;

import au.edu.swin.ict.road.composite.IOrganiserRole;

/**
 * The Message reciever to support MEP=In Only
 * 
 * @author Malinda Kapuruge
 * 
 */
public class OrgInOnlyMsgRcvr extends
	org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver {
    private static final Logger log = Logger.getLogger(OrgInOnlyMsgRcvr.class);
    IOrganiserRole organizer = null;

    public OrgInOnlyMsgRcvr(IOrganiserRole organizer) {
	this.organizer = organizer;
    }

    /**
     * Invoke the organizer object. Tip: We pass a reference of organizer to the
     * super via msg context
     */
    public void invokeBusinessLogic(MessageContext msgContext) throws AxisFault {

	log.debug("OrgInOutMsgRcvr invokeBusinessLogic");
	Object obj = this.getTheImplementationObject(msgContext);
	if (null != obj) {
	    IOrganiserRole orgObj = (IOrganiserRole) obj;

	    log.info("[ROAD4WS]The organizer has been found for "
		    + orgObj.getName());
	} else {
	    log.info("[ROAD4WS]The organizer object is NULL");
	    throw new AxisFault(
		    "[ROAD4WS]Configuration error: Cannot find the organizer object");
	}

	super.invokeBusinessLogic(msgContext);

    }

    /**
     * We do not create a new one. The organizer has to remain the same. Cannot
     * have two organizer objects for the same composition.
     */
    protected Object makeNewServiceObject(MessageContext msgContext)
	    throws AxisFault {
	return this.organizer;

    }

    /**
     * Find a given operation
     * 
     * @param op
     * @param implClass
     * @return
     */
    private Method findOperation(AxisOperation op, Class implClass) {
	Method method = (Method) (op.getParameterValue("myMethod"));
	if (method != null)
	    return method;

	String methodName = op.getName().getLocalPart();
	try {
	    // Looking for a method of the form "void method(OMElement)"
	    method = implClass.getMethod(methodName,
		    new Class[] { OMElement.class });
	    if (method.getReturnType().equals(void.class)) {
		try {
		    op.addParameter("myMethod", method);
		} catch (AxisFault axisFault) {
		    // Do nothing here
		}
		return method;
	    }
	} catch (NoSuchMethodException e) {
	    // Fall through
	}

	return null;
    }
}
