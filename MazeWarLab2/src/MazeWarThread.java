import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class MazeWarThread implements Runnable{
	
	Socket incomingClient = null; 
	ObjectOutputStream out;
	ObjectInputStream in;
	
	MazeWarThread(Socket sock)
	{
		incomingClient = sock;
		out = (ObjectOutputStream) MazewarServer.socketMap.get(sock).get(0);
		in = (ObjectInputStream) MazewarServer.socketMap.get(sock).get(1);
	}
	
	
	
	@Override
	public void run() {
		
		try {
			System.out.println("ENTER SPAWNED THREAD ");
		
			//outputToClient.close();
			MazewarPacket packet;
			
			while((packet = (MazewarPacket) in.readObject())!=null)
			{
				MazewarPacket copyPack = new MazewarPacket();
				copyPack.sequenceTag = MazewarServer.sequenceTag++;
				copyPack.type = packet.type;
				copyPack.player = packet.player;
				copyPack.startingPoint = packet.startingPoint;
				MazewarServer.packQueue.add(copyPack);
				System.out.println(packet.player+" "+copyPack.type);//+copyPack.sequenceTag);
				if(packet.startingPoint!=null)
				{
					System.out.println("yo "+packet.startingPoint.getX()+" "+packet.startingPoint.getY());
				}
			}
			
			in.close();
			//outputToClient.close();
			/*while((packet = (MazewarPacket) inputFromClient.readObject())!=null)// as long as there are inputs
			{
				packet.sequenceTag = MazewarServer.sequenceTag++;
				MazewarServer.packQueue.add(packet);
			}*/

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
