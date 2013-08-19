package au.swin.ict.research.cs3.road.road4ws.core.util;

import org.apache.axis2.context.MessageContext;
import org.apache.log4j.Logger;

public class Axis2Util {
    private static final Logger log = Logger.getLogger(Axis2Util.class);

    public static String getMappingOperation(MessageContext msgContext) {
	String opName = null;
	String inputAction = msgContext.getAxisOperation().getInputAction();

	opName = inputAction;
	opName = opName.replace("urn:", "");
	// opName = inputAction.substring(0, inputAction.length()
	// - "Request".length());

	log.debug("Operation found : " + opName + " truncated from "
		+ inputAction);
	return opName;
    }
}
