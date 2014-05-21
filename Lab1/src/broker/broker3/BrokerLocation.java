import java.io.Serializable;
 /**
 * BrokerPacket
 * ============
 * 
 * Packet format of the packets exchanged between the Broker and the Client
 * 
 */


/* class to describe host/port combo */
public class BrokerLocation implements Serializable {
	public String  broker_host;
	public Integer broker_port;
	
	/* constructor */
	public BrokerLocation(String host, Integer port) {
		this.broker_host = host;
		this.broker_port = port;
	}
	
	/* printable output */
	public String toString() {
		return " HOST: " + broker_host + " PORT: " + broker_port; 
	}
	
}