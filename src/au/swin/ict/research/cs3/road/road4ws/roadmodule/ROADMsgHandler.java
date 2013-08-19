package au.swin.ict.research.cs3.road.road4ws.roadmodule;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.log4j.Logger;

import au.edu.swin.ict.road.composite.Composite;
import au.edu.swin.ict.road.composite.IRole;
import au.swin.ict.research.cs3.road.road4ws.core.MessageParser;
import au.swin.ict.research.cs3.road.road4ws.core.ROADConstants;

public class ROADMsgHandler extends AbstractHandler implements Handler {
    private static final Logger log = Logger.getLogger(ROADMsgHandler.class);

    @Override
    public InvocationResponse invoke(MessageContext msgContext)
	    throws AxisFault {

	// Check if the service is engaged with the ROADModule
	if (!msgContext.isEngaged(ROADConstants.ROAD4WS_ROADMODULE)) {
	    log.debug("We do nothing. The mesasge not directed to a ROAD Composite");
	    return InvocationResponse.CONTINUE;
	} else {

	    log.debug(ROADConstants.ROAD4WS_ROADMODULE
		    + " is engaged. Continue...");
	    String[] arr = MessageParser.findCompositeAndRole(msgContext);

	    Composite composite = MessageParser
		    .getComposite(msgContext, arr[0]);
	    IRole role = MessageParser.getRole(msgContext, composite, arr[1]);

	    log.debug("Composite :" + composite.getName() + " and Role: "
		    + role.getName() + " found where the msg is heading");
	    msgContext.setProperty(ROADConstants.ROAD4WS_CURRENT_COMPOSITE,
		    composite);
	    msgContext.setProperty(ROADConstants.ROAD4WS_CURRENT_ROLE, role);
	    return InvocationResponse.CONTINUE;
	}
    }
}
