package au.swin.ict.research.cs3.road.road4ws.misctest;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.log4j.Logger;

public class RESTClient {
    private static Logger log = Logger.getLogger(RESTClient.class.getName());

    public static void main(String[] args) {
	Options options = new Options();
	options.setTo(new EndpointReference(
		"http://localhost:7070/axis2/services/DavidChef/"));

	options.setProperty(Constants.Configuration.ENABLE_REST,
		Constants.VALUE_TRUE);
	ServiceClient sender = null;
	try {
	    sender = new ServiceClient();
	} catch (AxisFault e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}
	sender.setOptions(options);
	try {
	    OMElement result = sender.sendReceive(createPayLoad());
	    log.debug(result.toString());
	} catch (AxisFault e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    public static OMElement createPayLoad() {
	OMFactory fac = OMAbstractFactory.getOMFactory();
	OMNamespace omNs = fac.createOMNamespace("http://ws.apache.org/axis2 ",
		"ns");
	OMElement method = fac.createOMElement("orderFood", omNs);
	method.setText("Chinese");
	return method;

    }
}
