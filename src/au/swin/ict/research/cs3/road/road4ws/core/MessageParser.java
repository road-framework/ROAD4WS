package au.swin.ict.research.cs3.road.road4ws.core;

import java.util.Iterator;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.log4j.Logger;

import au.edu.swin.ict.road.composite.Composite;
import au.edu.swin.ict.road.composite.IRole;
import au.swin.ict.research.cs3.road.road4ws.roadmodule.ROADMsgHandler;

/**
 * Parse a message to identify the composite name and the originated Role name.
 * The class is a place holder for such utility methods that parses a SOAP
 * message
 * 
 * @author Malinda Kapuruge
 * 
 */
public class MessageParser {
    private static final Logger log = Logger.getLogger(ROADMsgHandler.class
	    .getName());

    /**
     * Parse a message to identify the composite name and the originated Role
     * name. <code>Array[1] = Composite name</code>
     * <code>Array[2] = Role name</code>
     * 
     * @param msgContext
     *            the message context of the Axis2.
     * @return an array of string containing the composite and the role name
     */
    public static String[] findCompositeAndRole(MessageContext msgContext) {
	AxisConfiguration axConfig = msgContext.getConfigurationContext()
		.getAxisConfiguration();

	String svcName = null;

	log.debug("Parsing message for "
		+ msgContext.getAxisService().getEndpointName());

	// Get the service name that the msg is heading to
	svcName = msgContext.getAxisService().getName();
	if (null == svcName) {
	    log.error("Cannot identify the service name for the message ");
	    return null;
	}

	String[] arr = svcName.split(ROADConstants.ROAD4WS_SVC_NAME_SEPERATOR);

	return arr;
    }

    /**
     * Given the composite name, return the composite object from the axis
     * configuration
     * 
     * @param msgContext
     *            the message context of the Axis2
     * @param compName
     *            the composite name
     * @return a Composite object
     * @throws AxisFault
     */
    public static Composite getComposite(MessageContext msgContext,
	    String compName) throws AxisFault {
	Composite composite = null;
	AxisConfiguration axConfig = msgContext.getConfigurationContext()
		.getAxisConfiguration();

	// Get the composite
	Parameter param = axConfig.getParameter(compName);
	if (null == param) {
	    throw new AxisFault("Cannot find the composite");
	}

	composite = (Composite) param.getValue();

	return composite;
    }

    /**
     * Given the service name, return the Role from a composite
     * 
     * @param msgContext
     *            the message context of the Axis2
     * @param composite
     *            Composite object
     * @param roleName
     *            Name of the role. In the sense the name of the corresponding
     *            service
     * @return a Role Object
     * @throws AxisFault
     */
    public static IRole getRole(MessageContext msgContext, Composite composite,
	    String roleName) throws AxisFault {

	// Get all the roles
	List<IRole> iRoles = composite.getCompositeRoles();
	Iterator<IRole> iterator = iRoles.iterator();
	while (iterator.hasNext()) {
	    IRole role = iterator.next();
	    String tempRoleName = role.getName().toLowerCase();
	    log.debug("Checking if " + roleName + " == " + tempRoleName);
	    if (roleName.equals(tempRoleName)) {
		log.debug("Identified the service[Role] : " + roleName);
		return role;
	    }
	}
	log.error("Cannot find the role " + roleName + " in composite "
		+ composite.getName());
	throw new AxisFault("Cannot find the composite");
    }
}
