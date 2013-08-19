package au.swin.ict.research.cs3.road.road4ws.core.deployer;

import au.edu.swin.ict.road.composite.Composite;
import au.edu.swin.ict.road.composite.IRole;
import au.edu.swin.ict.road.composite.exceptions.RoleDescriptionGenerationException;
import au.swin.ict.research.cs3.road.road4ws.core.ROADConstants;
import javassist.*;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;

import java.io.File;
import java.io.IOException;

/**
 * This is the implementation based on javassist where we try to deploy a new
 * .class file as a POJO and then use Axis2's existing support for POJO. Caused
 * some issues related to role re-deployment, Hence currently not in use.
 */
public class JavaAssistBasedDeployer extends ROADDeployer {
    private ClassPool pool;

    @Override
    public void init(ConfigurationContext configurationContext) {
	super.init(configurationContext);
	this.pool = ClassPool.getDefault();
    }

    @Override
    protected void deployANewRole(IRole role) throws AxisFault {
	// Here we add the Role aka Service
	writeRoleFile(role);
    }

    /**
     * To delete the class files created for different roles.
     * 
     * @param compositeName
     */
    protected void unDeployAComposite(String compositeName) {
	try {
	    compositeName = compositeName.toLowerCase();
	    log.info("Removing class files for composition " + compositeName);
	    File rolesDir = new File(axisHome
		    + ROADConstants.ROAD4WS_ROLE_FILES_DIR);
	    log.info(rolesDir.listFiles().length + " file)s) found");
	    for (File f : rolesDir.listFiles()) {
		String fName = f.getName().toLowerCase();
		log.info("File " + fName + "compare with " + compositeName);
		if (fName.startsWith(compositeName)) {
		    f.delete();
		    log.info("Deleted file " + fName + " of " + compositeName
			    + " composition");
		}
	    }
	} catch (NullPointerException e) {
	    log.debug(e.getMessage());
	}
    }

    protected void unDeployARole(String compositeName, String roleName) {
	String fileName = this
		.getNoramlizedServiceName(compositeName, roleName);
	File rolesDir = new File(axisHome
		+ ROADConstants.ROAD4WS_ROLE_FILES_DIR);
	for (File f : rolesDir.listFiles()) {
	    String fName = f.getName().toLowerCase();

	    if (fName.equalsIgnoreCase(fileName + ".class")) {
		f.delete();
		log.info("Deleted file " + fName);

	    }
	}
    }

    /**
     * Write Role class file to Roles folder, so it will be deployed by
     * RoleDeployer
     * 
     * @param role
     * @return
     * @throws AxisFault
     */
    private void writeRoleFile(IRole role) throws AxisFault {
	AxisService service = null;
	Composite composite = role.getComposite();

	String axisServiceName = getNoramlizedServiceName(role.getComposite()
		.getName(), role.getName());

	try {
	    pool.insertClassPath(new ClassClassPath(this.getClass()));

	    CtClass cc = pool.getCtClass(role.getProvidedOperationObject()
		    .getClass().getCanonicalName());
	    cc.defrost();
	    cc.setName(axisServiceName);

	    cc.writeFile(axisHome + ROADConstants.ROAD4WS_ROLE_FILES_DIR);

	} catch (NotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (CannotCompileException e) {
	    e.printStackTrace();
	} catch (RoleDescriptionGenerationException e) {
	    e.printStackTrace();
	}
	// Register the message pusher
	role.registerNewPushListener(this.messagePusher);
    }
}
