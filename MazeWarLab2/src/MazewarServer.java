import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
//import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;


public class MazewarServer {

		static int sequenceTag = 0;
		static PriorityBlockingQueue<MazewarPacket> packQueue = new PriorityBlockingQueue<MazewarPacket>();
		static Thread queueThread;	
		static HashMap<Socket,ArrayList<Object>> socketMap = new HashMap<Socket,ArrayList<Object>>();
		static ArrayList<MazewarPacket> entryPacketList = new ArrayList<MazewarPacket>();
		static final int CLIENTS_SUPPORTED = 4;
		
		public static void main(String args[])
		{
			int port = 4445;
			ServerSocket server = null;
			boolean listening;
			
			
			try {
				server = new ServerSocket(port);
				listening = true;
				
				queueThread = new Thread(new Runnable() {
					
					
					@Override
					public void run() {
						boolean rendered = false;

						MazewarPacket render = new MazewarPacket();
						render.renderReady = 1;
						int counter = 0;
						while(true){
							
							
							
							while(MazewarServer.packQueue.size()>0)
							{
								System.out.println("Writing to sockets");
								MazewarPacket packet = MazewarServer.packQueue.remove();
								if(packet.type == ClientEvent.ENTER)
								{	
									entryPacketList.add(packet);
									counter++;
								}
								if(counter==CLIENTS_SUPPORTED && !rendered)
								{
									for(Socket s: socketMap.keySet())
									{
										System.out.println("Transmit packet render");
										
										for(MazewarPacket p: entryPacketList)
										{
											transmitPacket(s,p);
										}
										transmitPacket(s,render);
										
									}
									rendered = true;
									
								}
								
								else if(packet.type!=ClientEvent.ENTER)
								{
									for(Socket s: socketMap.keySet())
									{
										System.out.println("Transmit packet ");
										transmitPacket(s,packet);
										
									}
								}
							}
							
						}
					
					}
					
					public void transmitPacket(Socket s, MazewarPacket packet)
					{
						try {
							System.out.println("Write packet with tag "+packet.sequenceTag+" and action "+packet.type);	
							ObjectOutputStream reference = (ObjectOutputStream) socketMap.get(s).get(0);
							reference.writeObject(packet);  
							//oStream.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
					}
					
				});
				queueThread.start();
				
				while(listening)
				{
					Socket s = server.accept();
					
					
					ObjectOutputStream outputToClient =  new ObjectOutputStream(s.getOutputStream());

					ObjectInputStream inputFromClient =  new ObjectInputStream(s.getInputStream());
					ArrayList<Object> temp = new ArrayList<Object>();
					temp.add(outputToClient);
					temp.add(inputFromClient);
					socketMap.put(s, temp);
					
					new Thread(new MazeWarThread(s)).start();
					
				}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		}
		
		
		
}
