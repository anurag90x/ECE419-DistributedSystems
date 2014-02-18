import java.io.Serializable;


 /**
 * BrokerPacket
 * ============
 * 
 * Packet format of the packets exchanged between the Broker and the Client
 * 
 */




public class MazewarPacket implements Serializable,Comparable<MazewarPacket> {
	

	
	public int type;
	public int sequenceTag;
	public DirectedPoint startingPoint;
	public String player;
	@Override
	public int compareTo(MazewarPacket o) {
		// TODO Auto-generated method stub
		return this.sequenceTag - o.sequenceTag;
	}
	

}


