package au.swin.ict.research.cs3.road.road4ws.core.util;

import java.util.Iterator;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;
import org.apache.axis2.wsdl.WSDLConstants;

public class ServiceCreator {

    public static AxisService createSampleRoleAsAService(String roleName,
	    List<String> operationNames) throws AxisFault {
	AxisService service = new AxisService(roleName);

	Iterator<String> opIter = operationNames.iterator();
	while (opIter.hasNext()) {
	    String operationName = opIter.next();
	    javax.xml.namespace.QName opName = new javax.xml.namespace.QName(
		    operationName);
	    AxisOperation axisOp = new InOutAxisOperation(opName);
	    MessageReceiver messageReceiver = new RawXMLINOutMessageReceiver();

	    axisOp.setMessageReceiver(messageReceiver);
	    axisOp.setStyle(WSDLConstants.STYLE_RPC);

	    service.addOperation(axisOp);
	    service.mapActionToOperation(Constants.AXIS2_NAMESPACE_URI + "/"
		    + opName.getLocalPart(), axisOp);

	}

	return service;
    }

    public static AxisService createService(String serviceName) {
	javax.xml.namespace.QName serviceNameQ = new javax.xml.namespace.QName(
		serviceName);
	/* More TODO */
	AxisService service = new AxisService(serviceNameQ.getLocalPart());

	return service;
    }

    public static AxisOperation createOperation(String operationName) {
	javax.xml.namespace.QName opName = new javax.xml.namespace.QName(
		operationName);
	MessageReceiver messageReceiver = new RawXMLINOutMessageReceiver();
	AxisOperation axisOp = new OutInAxisOperation(opName);
	axisOp.setMessageReceiver(messageReceiver);
	axisOp.setStyle(WSDLConstants.STYLE_RPC);

	return axisOp;
    }

}
