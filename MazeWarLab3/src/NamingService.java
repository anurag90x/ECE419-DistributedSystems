import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;


/**
 * @author Anurag
 * NamingService class is responsible for registering client addresses i.e their server addresses and then replying to lookup queries
 */
/**
 * @author Anurag
 *
 */
/**
 * @author Anurag
 *
 */
public class NamingService {
	
	private static LinkedHashMap<String,String> neighbourMap = new LinkedHashMap<String,String>();
	public static HashMap<String,Integer> socketMap = new HashMap<String, Integer>();
	public static HashMap<String, ArrayList<Object>> sockStreams = new HashMap<String,ArrayList<Object>>();
	
	public static final int NUMBER_OF_PLAYERS = 4;
	public static int playerCounter = 0;
	public static HashMap<String,DirectedPoint> playerMap = new HashMap<String,DirectedPoint>();
	public static HashMap<String,String> playerIPMap = new HashMap<String,String>();
	
	static ServerSocket serverSock ;
	static String firstKey = "";
	
	public static void initNamingService(int port)
	{
		try {
			serverSock = new ServerSocket(port);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param incoming
	 * @param port
	 * @param tempMap
	 */
	public static void registerService(Socket incoming, int port, HashMap<String,DirectedPoint> tempMap)
	{
		try
		{
			String ipAddress = incoming.getRemoteSocketAddress().toString().split(":")[0];
		//int port = incoming.getPort();
		String keyName = ipAddress+":"+port;
		if (neighbourMap.containsKey(keyName))
		{
			return;
		}
		
		
		if(neighbourMap.isEmpty())
		{
			neighbourMap.put(keyName,"");
			firstKey = keyName;
			System.out.println("Registered first client - give it token ");
			ObjectOutputStream outputStream = (ObjectOutputStream)NamingService.sockStreams.get(incoming.getRemoteSocketAddress().toString()).get(0);
			MazewarPacket firstToken = new MazewarPacket(MazewarPacket.TOKEN);
			try
			{
				outputStream.writeObject(firstToken);	
			}
			catch(IOException ex)
			{	
				ex.printStackTrace();
			}
			
			System.out.println("Registered first client - complete-gave token");

		}
		else
		{
			String prevKey="" ;
			for(String key : neighbourMap.keySet())
			{
				if(neighbourMap.get(key).equals(""))
				{
					neighbourMap.put(key, keyName);
					neighbourMap.put(keyName,key);
					
				}
				prevKey = key;
			}
			neighbourMap.put(prevKey, keyName);
			neighbourMap.put(keyName, firstKey);
			
		}
		
		playerCounter++;
		System.out.println("Player count is "+playerCounter);
		for(String s: tempMap.keySet())
		{
			playerMap.put(s,tempMap.get(s));
			playerIPMap.put(keyName,s);

		}
		
		if (playerCounter == NUMBER_OF_PLAYERS)
		{
			MazewarPacket enterPack = new MazewarPacket(MazewarPacket.REPLY_ENTER);
			enterPack.clMap = playerMap;
			for(String client : socketMap.keySet())
			{
				System.out.println("Client to write to is "+client);
				ArrayList<Object> streams = sockStreams.get(client);
				ObjectOutputStream out = (ObjectOutputStream) streams.get(0);
				ObjectInputStream in= (ObjectInputStream) streams.get(1);
				try {
					out.writeObject(enterPack);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			
			
		}

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		
	}
	
	public static String requestPrevious(Socket incoming,int port)
	{
		String keyName ="";
		try
		{
			System.out.println("ENTER Request Previous Service ");
			String ipAddress = incoming.getRemoteSocketAddress().toString().split(":")[0];
			//int port = incoming.getPort();
			keyName = ipAddress+":"+port;
			System.out.println("EXIT Request Neighbour Service "+keyName);
			for(String s : neighbourMap.keySet())
			{
				if(neighbourMap.get(s).equals(keyName))
				{
					System.out.println("PREVIOUS IS "+playerIPMap.get(s));
					return playerIPMap.get(s);
				}
			}

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return "";
	
	}
	
	public static void cancelService(Socket incoming)
	{
		try
		{
			System.out.println("ENTER Cancel Service ");
			String ipAddress = incoming.getRemoteSocketAddress().toString().split(":")[0];
			int port = incoming.getPort();
			String keyName = ipAddress+":"+port;
			String prev = "";
			
			System.out.println("To remove "+keyName);
			for(String key:neighbourMap.keySet())
			{
				String val = neighbourMap.get(key);
				System.out.println(val);
				if(val.equals(keyName))
				{
					prev = key;
					break;
				}
			}
			
			String next = neighbourMap.get(keyName);
			
			System.out.println("Previous is "+prev+"  and next is "+next);
			
			neighbourMap.put(prev, next);
			neighbourMap.remove(keyName); // remove the player
			System.out.println("EXIT Cancel Service ");
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		

	}
	
	public static String requestNeighbour(Socket incoming,int port)
	{
		String keyName ="";
		try
		{
			System.out.println("ENTER Request Neighbour Service ");
			String ipAddress = incoming.getRemoteSocketAddress().toString().split(":")[0];
			//int port = incoming.getPort();
			keyName = ipAddress+":"+port;
			System.out.println("EXIT Request Neighbour Service "+keyName);

		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return neighbourMap.get(keyName);

	}
	
	
	public static void registerServiceUTest()
	{
		System.out.println("ENTER ");
		for(String key : neighbourMap.keySet())
		{
			System.out.println(key+"    neighbour : "+neighbourMap.get(key));
		}
		System.out.println("EXIT ");
	}

	
	public static void main(String args[])
	{
		 int port = 4445;
		boolean isListening = true;
		initNamingService(port);
		while (isListening)
		{
			try {
				Socket sock = serverSock.accept();
			
				new Thread(new ServiceHandler(sock)).start();
				
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
	}
}

class ServiceHandler implements Runnable
{
	Socket incomingSock = null;
	ObjectOutputStream out = null;
	ObjectInputStream in = null;
	ServiceHandler(Socket inc)
	{
		incomingSock = inc;
		try
		{
			//System.out.println("Registering service of "+inc.getRemoteSocketAddress().toString()+":"+inc.getPort());
		NamingService.socketMap.put(inc.getRemoteSocketAddress().toString(), inc.getPort());
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		
	}
	@Override
	public void run() {


			try {
				 out  = new ObjectOutputStream(incomingSock.getOutputStream());
				 in = new ObjectInputStream(incomingSock.getInputStream());
				 ArrayList<Object> temp = new ArrayList<Object>();
				 temp.add(out);
				 temp.add(in);
				 NamingService.sockStreams.put(incomingSock.getRemoteSocketAddress().toString(),temp);

				MazewarPacket packet;
				while(true)
				{
					packet = (MazewarPacket) in.readObject();
					if(packet.mType==MazewarPacket.ENTER)
					{
						System.out.println("Registering");
						int port = packet.mPort;
						//Store board location
						HashMap<String,DirectedPoint> tempMap = packet.clMap;
						
						NamingService.registerService(incomingSock,port,tempMap);
						NamingService.registerServiceUTest();
					}
					else if(packet.mType==MazewarPacket.REQUEST_NEIGHBOUR)
					{
						//System.out.println("Get neighbour");
						int port = packet.mPort;
						String nbInfo = NamingService.requestNeighbour(incomingSock,port);
						out.writeObject(nbInfo);
						//break;
					}
					else if (packet.mType == MazewarPacket.REQUEST_PREVIOUS)
					{
						//System.out.println("Get previous neighbour");
						int port = packet.mPort;
						String nbInfo = NamingService.requestPrevious(incomingSock,port);
						out.writeObject(nbInfo);
					}
					else if(packet.mType==MazewarPacket.BYE)
					{
						//System.out.println("Leaving");
						NamingService.cancelService(incomingSock);
						NamingService.registerServiceUTest();
						break;
					}
				}
				out.close();
				in.close();
				incomingSock.close();
			}catch (ClassNotFoundException e) {
				e.printStackTrace();
			} 
			catch (Exception e) {
				e.printStackTrace();
			} 
			
			
		
	}
	
	
}
