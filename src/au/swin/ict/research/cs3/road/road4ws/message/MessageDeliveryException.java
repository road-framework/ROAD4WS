package au.swin.ict.research.cs3.road.road4ws.message;

public class MessageDeliveryException extends Exception {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public MessageDeliveryException(String eprStr) {
	super("The message couldn't be delivered to the address " + eprStr);
    }

}
