package au.swin.ict.research.cs3.road.road4ws.core.deployer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentErrorMsgs;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.Loader;
import org.apache.log4j.Logger;

import au.swin.ict.research.cs3.road.road4ws.core.ROADConstants;

/**
 * Deploys the role class in Axis2 web services engine
 * 
 * @author
 * 
 */

public class RoleDeployer implements Deployer {

    private static final Logger log = Logger.getLogger(RoleDeployer.class);
    private AxisConfiguration axisConfig;
    private ConfigurationContext configCtx;
    private String compositeName;
    private String roleName;

    @Override
    public void init(ConfigurationContext configCtx) {
	this.configCtx = configCtx;
	this.axisConfig = configCtx.getAxisConfiguration();

    }

    @Override
    public void deploy(DeploymentFileData deploymentFileData)
	    throws DeploymentException {
	log.info("[ROAD4WS]Role deployer called");
	// Get the class loader from current thread
	// The class loader will be changed with another class loader
	ClassLoader threadClassLoader = Thread.currentThread()
		.getContextClassLoader();
	AxisService axisService;

	try {
	    // Set the class loader from deployment file on current Thread
	    File file = deploymentFileData.getFile();
	    File parentFile = file.getParentFile();

	    ClassLoader classLoader = Utils
		    .getClassLoader(configCtx.getAxisConfiguration()
			    .getSystemClassLoader(), parentFile, configCtx
			    .getAxisConfiguration().isChildFirstClassLoading());

	    Thread.currentThread().setContextClassLoader(classLoader);
	    // Deploy the service from java class file
	    // File format: CompositeName_RoleName.class
	    String className = file.getName();
	    String[] compositeRoleName = className
		    .split(ROADConstants.ROAD4WS_SVC_NAME_SEPERATOR);
	    compositeName = compositeRoleName[0];
	    roleName = compositeRoleName[1];
	    try {
		className = className.replaceAll(".class", "");
		Class clazz = Loader.loadClass(className);
		axisService = createAxisServiceUsingAnnogen(className,
			classLoader, deploymentFileData.getFile().toURI()
				.toURL());
		// We need to remove the service first
		String axisServiceName = ROADDeployer.getNoramlizedServiceName(
			compositeName, roleName);
		if (null != (this.axisConfig.getService(axisServiceName))) {
		    this.axisConfig.removeService(axisServiceName);
		    log.info("[ROAD4WS] Service interface replaced : "
			    + axisServiceName);
		}

		this.axisConfig.addService(axisService);

		String svcDescription = "Role " + roleName + " of composite "
			+ compositeName + "  [ROAD4WS]";

		axisService.setDocumentation(svcDescription);
		axisService.setServiceDescription(svcDescription);
		log.info("[ROAD4WS] Service added : " + axisService.getName()
			+ " of " + compositeName);
		axisService.setScope(Constants.SCOPE_APPLICATION);
	    } catch (MalformedURLException e) {
		e.printStackTrace();
	    } catch (AxisFault e) {
		e.printStackTrace();
	    } catch (ClassNotFoundException e) {
		e.printStackTrace();
	    } catch (InstantiationException e) {
		e.printStackTrace();
	    } catch (IllegalAccessException e) {
		e.printStackTrace();
	    }
	} finally {
	    // Set the class loader as default class loader
	    if (threadClassLoader != null) {
		Thread.currentThread().setContextClassLoader(threadClassLoader);
	    }
	}

    }

    private AxisService createAxisServiceUsingAnnogen(String className,
	    ClassLoader classLoader, URL serviceLocation)
	    throws ClassNotFoundException, InstantiationException,
	    IllegalAccessException, AxisFault {
	// Create a hash map that contains message receiver to create axis
	// service
	HashMap messageReciverMap = new HashMap();

	MessageReceiver messageReceiver = new au.swin.ict.research.cs3.road.road4ws.core.mgsrcvr.ROADInOnlyMessageReceiver();
	messageReciverMap.put(WSDL2Constants.MEP_URI_IN_ONLY, messageReceiver);

	MessageReceiver inOutmessageReceiver = new au.swin.ict.research.cs3.road.road4ws.core.mgsrcvr.ROADInOutMessageReceiver();
	messageReciverMap.put(WSDL2Constants.MEP_URI_IN_OUT,
		inOutmessageReceiver);
	messageReciverMap.put(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY,
		inOutmessageReceiver);

	// Create axis service
	AxisService axisService = AxisService.createService(className,
		configCtx.getAxisConfiguration(), messageReciverMap, null,
		null, classLoader);

	axisService.setFileName(serviceLocation);

	return this.addAxisServiceInformation(axisService);
    }

    private AxisService addAxisServiceInformation(AxisService service)
	    throws AxisFault {

	// Very important. All services created must be engaged to ROADModule.
	// In that way the Handler can intercept
	// the message directed to them
	AxisModule roadModule = this.getRoadModule();
	if (null == roadModule) {
	    log.error("Cannot find ROAD Module. Make sure that it is being engaged properly");
	    return null;
	} else {
	    service.engageModule(roadModule);
	    log.debug("[ROAD4WS]: Engaged service " + service.getName()
		    + " of composite " + compositeName + " to "
		    + ROADConstants.ROAD4WS_ROADMODULE);
	    return service;
	}
    }

    /**
     * Return the ROAD Module which is a parameter in the axis config
     * 
     * @return
     */
    private AxisModule getRoadModule() {
	AxisModule roadModule = null;
	roadModule = this.axisConfig
		.getModule(ROADConstants.ROAD4WS_ROADMODULE);
	return roadModule;
    }

    @Override
    public void setDirectory(String directory) {

    }

    @Override
    public void setExtension(String extension) {

    }

    @Override
    public void undeploy(String fileName) throws DeploymentException {

	fileName = Utils.getShortFileName(fileName);
	String className = fileName.replaceAll(".class", "");
	try {
	    AxisServiceGroup serviceGroup = configCtx.getAxisConfiguration()
		    .removeServiceGroup(className);
	    configCtx.removeServiceGroupContext(serviceGroup);
	    log.info(Messages.getMessage(DeploymentErrorMsgs.SERVICE_REMOVED,
		    fileName));
	} catch (AxisFault axisFault) {
	    // May be a faulty service
	    log.debug(Messages.getMessage(
		    DeploymentErrorMsgs.FAULTY_SERVICE_REMOVAL,
		    axisFault.getMessage()), axisFault);
	    configCtx.getAxisConfiguration().removeFaultyService(fileName);
	}
    }

    @Override
    public void cleanup() throws DeploymentException {
	// TODO Auto-generated method stub

    }

}
