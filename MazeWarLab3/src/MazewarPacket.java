import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


 /**
 * MazewarPacket
 * ============
 * 
 * Packet format of the packets exchanged between the different elements of the system
 * 
 */




public class MazewarPacket implements Serializable {
	
	
	public static final int NULL   = 0;
	public static final int REQUEST = 100;
	public static final int REPLY   = 200;
	public static final int ENTER     = 300;
	public static final int BYE     = 400;
	public static final int ACTION = 500;
	public static final int TOKEN = 600;
	public static final int REQUEST_NEIGHBOUR = 700;
	public static final int REQUEST_GAME_STATE = 800;
	public static final int REPLY_GAME_STATE = 900;
	public static final int REPLY_ENTER = 1000;
	public static final int REQUEST_PREVIOUS=1100;

	
	
	public int mType;
	public int mActionType;
	public String mPlayer;
	public int mPort;
	
	
	public HashMap<String,DirectedPoint> clMap = new HashMap<String,DirectedPoint>();
	public long seed;
	
	
	public MazewarPacket(int type)
	{
		mType = type;
	}	

	public MazewarPacket(int type, int actionType)
	{
		mType = type;
		mActionType= actionType;
		
	}
	
	public void setGameState(Map clientMap,long seed)
	{
		//this.clMap = (HashMap) clientMap;
		for(Object cl : clientMap.keySet())
		{
			String name = ((Client)cl).getName();
			System.out.println("Name from map is "+name);
			clMap.put(name, (DirectedPoint) clientMap.get(cl));
			
		}
	
		this.seed = seed;
	}
	
	
	
	
}


