
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class BrokerLookupServer {
	
	static HashMap<String,BrokerLocation> lookupTable = new HashMap<String,BrokerLocation>();

	
	public static void main(String args[])
	{
		ServerSocket lookupServer = null;
		if(args.length==1)
		{
			try {
				lookupServer = new ServerSocket(Integer.parseInt(args[0]));
				while(true)
				{
					new Thread(new LookupClass(lookupServer.accept())).start();
				}
				
				
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//lookupServer.close();

		}
		else
		{
			System.err.println("ERROR - Invalid number of arguments");
			System.exit(1);
		}
	
		
	
	}
	
	

}

class LookupClass implements Runnable
{

	Socket incomingConn = null;
	LookupClass(Socket sock)
	{
		incomingConn = sock;	
		}
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		System.out.println("Got connection from client ");
		
		try {
			
			ObjectOutputStream outputToClient = new ObjectOutputStream(incomingConn.getOutputStream());

			ObjectInputStream inputFromClient = new ObjectInputStream(incomingConn.getInputStream());
			
			BrokerPacket pack;
			while((pack = (BrokerPacket) inputFromClient.readObject())!=null)
			{
				if(pack.type == BrokerPacket.LOOKUP_REGISTER)
				{
					System.out.println("Registered broker "+pack.symbol+" "+pack.locations[0].broker_host+" "+pack.locations[0].broker_port);
					BrokerLocation loc= new BrokerLocation(pack.locations[0].broker_host,pack.locations[0].broker_port);
					// should check for multiple registrations?? as in multiple nasdaq brokers and tse brokers?
					BrokerLookupServer.lookupTable.put(pack.symbol,loc); // register name and address
					break;
					/*pack.type = BrokerPacket.LOOKUP_REPLY;
					outputToClient.writeObject(pack);*/
				}
				else if(pack.type == BrokerPacket.LOOKUP_REQUEST)
				{
					System.out.println("Lookup request for "+pack.symbol);

					pack.type = BrokerPacket.LOOKUP_REPLY;
					pack.locations = new BrokerLocation[1];
					if(BrokerLookupServer.lookupTable.containsKey(pack.symbol))
						pack.locations[0] = BrokerLookupServer.lookupTable.get(pack.symbol);
					else
						pack.locations[0]=null;
					outputToClient.writeObject(pack); //write back location
					break;
				}
			}
			
			inputFromClient.close();
			outputToClient.close();
			incomingConn.close();
			System.out.println("Close connection....");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	
}