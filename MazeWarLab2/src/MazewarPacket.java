import java.io.Serializable;


 /**
 * BrokerPacket
 * ============
 * 
 * Packet format of the packets exchanged between the Mazewar client & server
 * 
 */




public class MazewarPacket implements Serializable,Comparable<MazewarPacket> {



	public int type;
	public int sequenceTag;
	public DirectedPoint startingPoint;
	public String player;
	public int renderReady;
	@Override
	public int compareTo(MazewarPacket o) {
		// TODO Auto-generated method stub
		return this.sequenceTag - o.sequenceTag;
	}


}