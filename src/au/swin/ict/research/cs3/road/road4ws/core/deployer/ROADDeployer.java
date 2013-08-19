package au.swin.ict.research.cs3.road.road4ws.core.deployer;

import au.edu.swin.ict.road.composite.Composite;
import au.edu.swin.ict.road.composite.IRole;
import au.edu.swin.ict.road.composite.ROADDeploymentEnv;
import au.edu.swin.ict.road.composite.exceptions.CompositeInstantiationException;
import au.edu.swin.ict.road.composite.exceptions.ConsistencyViolationException;
import au.edu.swin.ict.road.composite.listeners.CompositeAddRoleListener;
import au.edu.swin.ict.road.composite.listeners.CompositeRemoveRoleListener;
import au.edu.swin.ict.road.composite.listeners.CompositeUpdateRoleListener;
import au.edu.swin.ict.road.demarshalling.CompositeDemarshaller;
import au.edu.swin.ict.road.demarshalling.exceptions.CompositeDemarshallingException;
import au.swin.ict.research.cs3.road.road4ws.core.ROADConstants;
import au.swin.ict.research.cs3.road.road4ws.message.MessagePusher;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentErrorMsgs;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.log4j.Logger;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Deploys the ROAD composites in Axis2 web services engine. This class is
 * abstract and need to be extended using a suitable technology to deploy ROAD
 * composites.
 * 
 * @author Malinda Kapuruge
 */
public abstract class ROADDeployer implements Deployer,
	CompositeAddRoleListener, CompositeRemoveRoleListener,
	CompositeUpdateRoleListener, ROADDeploymentEnv {

    protected static final Logger log = Logger.getLogger(ROADDeployer.class
	    .getName());
    protected AxisConfiguration axisConfig;
    private ConfigurationContext configCtx;
    private CompositeDemarshaller demarsheller;
    private List<Composite> compositeList;
    protected String axisHome;
    // Keep one message pusher to be notified upon message push requirements
    // One instance for all.
    // Is this a bottleneck? - Malinda. 26/10/2012
    protected MessagePusher messagePusher;

    /**
     * The init methos of the deployer
     */
    @Override
    public void init(ConfigurationContext configurationContext) {
	this.configCtx = configurationContext;
	this.axisConfig = configCtx.getAxisConfiguration();
	this.demarsheller = new CompositeDemarshaller();
	this.compositeList = new ArrayList<Composite>();
	this.axisHome = System.getenv("AXIS2_HOME");
	this.messagePusher = new MessagePusher(configurationContext);
	this.configCtx.setProperty(ROADConstants.ROAD4WS_COMPOSITE_LIST,
		this.compositeList);// To be accessed by other axis2 runtime
				    // tools:In Future

	// Object property =
	// configCtx.getProperty(ROADConstants.ROAD4WS_COMPOSITE_LIST);
	// if(null != property){
	// ArrayList<Composite> road4wsCompositeList =
	// (ArrayList<Composite>)property;
	// for(Composite composite : road4wsCompositeList){
	//
	// }
	// }
    }

    /**
     * The deploy method of the deployer
     */
    @Override
    public void deploy(DeploymentFileData deploymentFileData)
	    throws DeploymentException {

	Composite composite = null;
	File file = deploymentFileData.getFile();

	log.info("[ROAD4WS] Deploying composite from the file : "
		+ file.getAbsoluteFile());

	try {
	    log.debug("Loading SMC from " + file.getAbsolutePath());
	    composite = this.demarsheller.demarshalSMC(file.getAbsolutePath());
	    if (null == composite) {
		throw new DeploymentException(
			"ROAD4WS: Cannot instantiate the composite from file "
				+ file.getAbsoluteFile());
	    }
	    this.startListeningToCompositeChanges(composite);// Here we start
	    // listening to
	    // the changes
	    // of the
	    // composite
	} catch (CompositeDemarshallingException e) {
	    e.printStackTrace();
	} catch (ConsistencyViolationException e) {
	    e.printStackTrace();
	} catch (CompositeInstantiationException e) {
	    e.printStackTrace();
	}

	// Now we have a composite. Let's start a new thread
	// Add the composite to compositeList
	compositeList.add(composite);
	composite.setRoadDepEnv(this);
	Thread compo = new Thread(composite);
	compo.start();

	// Set the organizer service
	OrganizerDeployer od = new OrganizerDeployer(this.configCtx, composite);
	try {
	    od.createOrgService();
	} catch (AxisFault e1) {
	    log.error("Could not create the Organizer service for the composite"
		    + composite.getName());
	    e1.printStackTrace();
	}

	// Then we add it to the axis config
	try {

	    // Check if there is already created composite
	    if (null != this.axisConfig.getParameter(composite.getName())) {
		log.debug("Cannot add composite  " + composite.getName()
			+ " to the axis config. It is already exist");
	    } else {
		log.debug("Adding composite to the axis config "
			+ composite.getName());
		this.axisConfig.addParameter(composite.getName().toLowerCase(),
			composite);
	    }
	} catch (AxisFault e) {
	    e.printStackTrace();
	    log.debug("Could not add composte to the axisConfig "
		    + composite.getName());
	}

	// Then we create our services [START READING FROM HERE]
	try {
	    List<IRole> roles = composite.getCompositeRoles();
	    /* Iterate through all the roles and expose them as services */

	    for (IRole role : roles) {
		if (role != null) {
		    deployANewRole(role);// call the child's method
		}
	    }
	} catch (AxisFault e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}

	try {
	    // We are doing this for future use.
	    // e.g., if a ROAD runtime monitor require visualizing the composite
	    // it can retrieve the composite from the axisConfig by giving the
	    // composite name.
	    this.axisConfig.addParameter(composite.getName(), composite);
	} catch (AxisFault e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    /**
     * This method let the deployer to start listening to changes in the
     * ROADfactory
     * 
     * @param composite
     */
    private void startListeningToCompositeChanges(Composite composite) {
	composite.addCompositeAddRoleListner(this);
	composite.addCompositerRemoveRoleListner(this);
	composite.addCompositeUpdateRoleListner(this);
    }

    /**
     * This method let the deployer to stop listening to changes in the
     * ROADfactory. Of No use yet.
     * 
     * @param composite
     */
    private void stopListeningToCompositeChanges(Composite composite) {
	composite.removeCompositeAddRoleListner(this);
	composite.removeCompositerRemoveRoleListner(this);
	composite.removeCompositeUpdateRoleListner(this);
    }

    @Override
    public void setDirectory(String arg0) {
	// TODO Do nothing

    }

    @Override
    public void setExtension(String arg0) {
	// TODO Do nothing

    }

    @Override
    public void cleanup() throws DeploymentException {
	// TODO Auto-generated method stub

    }

    /**
     * Undeply method of the deployer
     */
    @Override
    public void undeploy(String fileName) throws DeploymentException {
	// Get the composite name from xml file
	fileName = Utils.getShortFileName(fileName);
	// WRONG Assumption? Filename and the composition name are the same?
	// Issue: The file is already deleted so we cannot parse the contents to
	// get the SMC name
	String compositeName = fileName.replaceAll(".xml", "");
	compositeName = compositeName.toLowerCase();

	// Get the role class file that related with the composite and delete it
	this.unDeployAComposite(compositeName);
	log.info("Removed functional services for composition " + compositeName);
	// Get the composite Organizer and remove the services from axis2 engine
	String organizerName = compositeName
		+ ROADConstants.ROAD4WS_SVC_NAME_SEPERATOR
		+ ROADConstants.ROAD4WS_ORGANIZER_NAME;
	organizerName = organizerName.toLowerCase();
	try {
	    AxisServiceGroup serviceGroup = configCtx.getAxisConfiguration()
		    .removeServiceGroup(organizerName);
	    configCtx.removeServiceGroupContext(serviceGroup);
	    log.info(Messages.getMessage(DeploymentErrorMsgs.SERVICE_REMOVED,
		    fileName));
	    log.info("Removed organizer proxy for composition " + compositeName);
	} catch (AxisFault axisFault) {
	    // May be a faulty service
	    log.info(Messages.getMessage(
		    DeploymentErrorMsgs.FAULTY_SERVICE_REMOVAL,
		    axisFault.getMessage()), axisFault);
	    configCtx.getAxisConfiguration().removeFaultyService(fileName);
	}

	// Finally get a composite from composite from compositeList and shut
	// down it
	for (Composite c : compositeList) {
	    if (compositeName.equalsIgnoreCase(c.getName())) {
		compositeList.remove(c);
		c.stop();
		for (String roleID : c.getRoleMap().keySet()) {
		    String serviceName = getNoramlizedServiceName(
			    compositeName, roleID);
		    try {
			configCtx.getAxisConfiguration().removeService(
				serviceName);
		    } catch (AxisFault axisFault) {
			log.error(
				"Error removing the axis2 service related to te role : "
					+ roleID, axisFault);
		    }
		}
	    }
	}

    }

    /**
     * Implement how the role is deployed.
     * 
     * @param role
     * @throws AxisFault
     */
    protected abstract void deployANewRole(IRole role) throws AxisFault;

    /**
     * Implement how the composite is undepoyed
     * 
     * @param compositeName
     */
    protected abstract void unDeployAComposite(String compositeName);

    /**
     * Implement how the role is undeployed
     * 
     * @param compositeName
     * @param roleName
     */
    protected abstract void unDeployARole(String compositeName, String roleName);

    private HashMap getMsgRcvrMap() {
	HashMap messageReciverMap = new HashMap();

	MessageReceiver messageReceiver = new au.swin.ict.research.cs3.road.road4ws.core.mgsrcvr.ROADInOnlyMessageReceiver();
	messageReciverMap.put(WSDL2Constants.MEP_URI_IN_ONLY, messageReceiver);

	MessageReceiver inOutmessageReceiver = new au.swin.ict.research.cs3.road.road4ws.core.mgsrcvr.ROADInOutMessageReceiver();
	messageReciverMap.put(WSDL2Constants.MEP_URI_IN_OUT,
		inOutmessageReceiver);
	messageReciverMap.put(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY,
		inOutmessageReceiver);

	return messageReciverMap;

    }

    public static String getNoramlizedServiceName(String compositeName,
	    String roleName) {
	String s = compositeName + ROADConstants.ROAD4WS_SVC_NAME_SEPERATOR
		+ roleName;
	return s.toLowerCase();
    }

    /**
     * Return the ROAD Module which is a parameter in the axis config
     * 
     * @return
     */
    protected AxisModule getRoadModule() {
	return this.axisConfig.getModule(ROADConstants.ROAD4WS_ROADMODULE);
    }

    /**
     * remove a role/service
     * 
     * @param roleName
     * @throws AxisFault
     */
    private void removeRole(String compositeName, String roleName)
	    throws AxisFault {
	/*
	 * Remove a specific service from the axis2 configuration context. We
	 * check for the Role name if a match found remove it
	 */

	String svcName = this.getNoramlizedServiceName(compositeName, roleName);

	AxisService svc = this.axisConfig.getService(svcName);
	if (null != svc) {
	    this.axisConfig.removeService(svcName);
	    // svc.setName(svcName+"_old");
	    log.info("Service removed from Axis2");
	    // We need to physically remove the file from the repo. So that Role
	    // deployer will pick the change
	    this.unDeployARole(compositeName, roleName);

	} else {
	    log.error("A request is made to remove service " + svcName
		    + ". But ROAD4WS cannot find the service in Axis2");
	    log.error("Following is the list of all available services");
	    HashMap<String, AxisService> serviceSet = this.axisConfig
		    .getServices();
	    for (Object key : serviceSet.keySet()) {
		AxisService tempSvc = (AxisService) serviceSet.get(key);
		log.error("\t" + tempSvc.getName());
	    }
	}

    }

    /**
     * Adds a specific operation to a service. Of no use with the javassist
     * update. But we keep this in case we need to add an operation dynamically
     * in the future.
     * 
     * @param service
     * @param operationName
     * @param async
     */
    private void addOperationToService(AxisService service,
	    String operationName, boolean async) {
	AxisOperation axisOp = null;
	if (true == async) {
	    axisOp = new InOnlyAxisOperation(new QName(operationName));
	    axisOp.setMessageExchangePattern(WSDL2Constants.MEP_URI_IN_ONLY);
	    axisOp.setMessageReceiver(new au.swin.ict.research.cs3.road.road4ws.core.mgsrcvr.ROADInOnlyMessageReceiver());
	} else {
	    axisOp = new InOutAxisOperation(new QName(operationName));
	    axisOp.setMessageExchangePattern(WSDL2Constants.MEP_URI_IN_OUT);
	    axisOp.setMessageReceiver(new au.swin.ict.research.cs3.road.road4ws.core.mgsrcvr.ROADInOutMessageReceiver());
	}
	axisOp.setSoapAction(operationName);
	axisOp.setStyle(WSDLConstants.STYLE_DOC);
	service.addOperation(axisOp);
    }

    /**
     * Removes a specific operation to a service. Of no use with the javassist
     * update. But we keep this in case we need to add an operation dynamically
     * in the future.
     * 
     * @param service
     * @param operationName
     */
    private void removeOperationFromService(AxisService service,
	    String operationName) {
	service.removeOperation(new QName(operationName));
    }

    // ----------- ROADFactory Interface
    @Override
    public void roleAdded(IRole role) {
	try {
	    deployANewRole(role);
	} catch (AxisFault e) {
	    e.printStackTrace();
	    log.error("Cannot add service " + role.getName() + " of "
		    + role.getComposite().getName());
	}
    }

    @Override
    public void roleRemoved(IRole removedRole) {
	// TODO Auto-generated method stub
	try {
	    this.removeRole(removedRole.getComposite().getName(),
		    removedRole.getName());
	} catch (AxisFault e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    log.error("Cannot remove service " + removedRole.getName());
	}
    }

    @Override
    public void roleUpdated(IRole role) {
	/*
	 * TODO This need to be fixed. The change in ROADfactory gives less
	 * information about the update. Earlier it was public void
	 * roleUpdated(RoleChangeDescription changeDescription) But now there is
	 * no change description. So we just remove the role and re-deploy it.
	 */
	try {
	    this.removeRole(role.getComposite().getName(), role.getName());
	    deployANewRole(role);
	} catch (AxisFault e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    @Override
    public Object getDepEnvProperty(String key) {
	if (key.equals(ROADDeploymentEnv.RDE_ALL_COMPOSITES)) {
	    return this.configCtx
		    .getProperty(ROADConstants.ROAD4WS_COMPOSITE_LIST);
	} else {
	    return null;
	}
    }

    @Override
    public boolean isCompositeDeployed(String compositeName) {
	for (Composite c : this.compositeList) {
	    if (c.getName().equals(compositeName)) {
		return true;
	    }
	}

	return false;
    }

}
