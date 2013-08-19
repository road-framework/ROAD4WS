package au.swin.ict.research.cs3.road.road4ws.roadmodule;

import java.io.File;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.modules.Module;
import org.apache.log4j.Logger;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;

import au.swin.ict.research.cs3.road.road4ws.core.ROADConstants;

/**
 * 
 * @author Malinda Kapuruge
 * 
 */
public class ROADModule implements Module {

    private static final Logger log = Logger.getLogger(ROADMsgHandler.class);

    // initialize the module
    public void init(ConfigurationContext configContext, AxisModule module)
	    throws AxisFault {
	log.debug("ROAD Module initialized");
	AxisConfiguration ac = configContext.getAxisConfiguration();

    }

    public void engageNotify(AxisDescription axisDescription) throws AxisFault {
	// log.debug("engageNotify");
    }

    // shutdown the module
    public void shutdown(ConfigurationContext configurationContext)
	    throws AxisFault {
	log.info("Shutting down ROAD Module");
	// Clean
	String roleFileDir = System.getenv("AXIS2_HOME")
		+ ROADConstants.ROAD4WS_ROLE_FILES_DIR;
	log.info("Deleting temporary files");

	boolean success = (new File(roleFileDir)).delete();
	if (!success) {
	    log.error("Could not delete temporary files in " + roleFileDir);
	}
    }

    public String[] getPolicyNamespaces() {
	return null;
    }

    public void applyPolicy(Policy policy, AxisDescription axisDescription)
	    throws AxisFault {
    }

    public boolean canSupportAssertion(Assertion assertion) {
	return true;
    }

}
