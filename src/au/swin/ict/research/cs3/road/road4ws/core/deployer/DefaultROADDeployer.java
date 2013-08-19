package au.swin.ict.research.cs3.road.road4ws.core.deployer;

import au.edu.swin.ict.road.composite.IRole;
import au.edu.swin.ict.road.composite.contract.Operation;
import au.swin.ict.research.cs3.road.road4ws.core.ROADConstants;
import au.swin.ict.research.cs3.road.road4ws.core.util.ServiceWSDLBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.wsdl.WSDLConstants;

import javax.xml.namespace.QName;

/**
 * This is the default road composite deployer we use for Axis2. Need to be
 * referenced in the axis2.xml (See README.txt)
 * 
 */
public class DefaultROADDeployer extends ROADDeployer {

    protected void deployANewRole(IRole role) throws AxisFault {

	String axisServiceName = getNoramlizedServiceName(role.getComposite()
		.getName(), role.getName());
	// We create a service
	AxisService service = new AxisService(axisServiceName);
	ServiceWSDLBuilder serviceWSDLBuilder = new ServiceWSDLBuilder(service,
		role);
	serviceWSDLBuilder.startBuild();

	// We add operations based on provider operation list
	for (Operation op : role.getProvidedOperationsList()) {
	    // We create a new Axis operation depending on sync/async of
	    // operation
	    AxisOperation axOp = null;
	    if (op.getReturnType() != null
		    && !"void".equals(op.getReturnType())) {// This operation is
							    // in-out (there is
							    // a return type)
		axOp = new InOutAxisOperation(new QName(op.getName()));
		axOp.setMessageExchangePattern(WSDL2Constants.MEP_URI_IN_OUT);
		axOp.setMessageReceiver(new au.swin.ict.research.cs3.road.road4ws.core.mgsrcvr.ROADInOutMessageReceiver());
		AxisMessage outMsg = new AxisMessage();
		outMsg.setParent(axOp);
		outMsg.setName(op.getName() + "Response");
		axOp.addMessage(outMsg, WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
		serviceWSDLBuilder.buildOutMessage(axOp, op.getReturnType());
	    } else {// This operation is in only
		axOp = new InOnlyAxisOperation(new QName(op.getName()));
		axOp.setMessageExchangePattern(WSDL2Constants.MEP_URI_IN_ONLY);
		axOp.setMessageReceiver(new au.swin.ict.research.cs3.road.road4ws.core.mgsrcvr.ROADInOnlyMessageReceiver());
	    }
	    axOp.setStyle(WSDLConstants.STYLE_RPC);
	    AxisMessage inMsg = new AxisMessage();
	    inMsg.setName(op.getName() + "Request");
	    inMsg.setParent(axOp);
	    axOp.addMessage(inMsg, WSDLConstants.MESSAGE_LABEL_IN_VALUE);
	    serviceWSDLBuilder.buildInMessage(axOp, op.getParameters());
	    service.addOperation(axOp);
	    serviceWSDLBuilder.endBuild();

	}
	// Push listener need to be registered irrespective of whether there are
	// operations or not.
	// Because, in the future role might add new operations.
	role.registerNewPushListener(this.messagePusher);

	// We add message receivers
	MessageReceiver messageReceiver = new au.swin.ict.research.cs3.road.road4ws.core.mgsrcvr.ROADInOnlyMessageReceiver();
	MessageReceiver inOutmessageReceiver = new au.swin.ict.research.cs3.road.road4ws.core.mgsrcvr.ROADInOutMessageReceiver();

	service.addMessageReceiver(WSDL2Constants.MEP_URI_IN_ONLY,
		messageReceiver);
	service.addMessageReceiver(WSDL2Constants.MEP_URI_IN_OUT,
		inOutmessageReceiver);
	service.addMessageReceiver(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY,
		inOutmessageReceiver);
	// then we add the service to the axis cnfg
	this.axisConfig.addService(service);
	addAxisServiceInformation(service);
    }

    // TODO copied from RoleDeployer
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
		    + " to " + ROADConstants.ROAD4WS_ROADMODULE);
	    return service;
	}
    }

    @Override
    protected void unDeployAComposite(String compositeName) {
    }

    @Override
    protected void unDeployARole(String compositeName, String roleName) {
    }
}
