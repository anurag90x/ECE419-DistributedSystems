
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;



public class BrokerClient {

	public static void main(String[] args) throws IOException,
		ClassNotFoundException {
		Socket regSocket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		String hostname = "localhost";
		int port = 4444;
		
			/* variables for hostname/port */
			
			
		if(args.length == 2 ) {
				hostname = args[0];
				port = Integer.parseInt(args[1]);
			} else {
				System.err.println("ERROR: Invalid arguments!");
				System.exit(-1);
			}
			//regSocket = new Socket(hostname, port);

			

		

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String userInput;
		System.out.println("Please set the broker with nasdaq or tse using 'local <broker name> ' and then query");
		System.out.println("Enter queries or x for exit:");
		System.out.print(">");
		String brokerName = "";
		BrokerLocation brokerLoc;
		Socket brokerSock = null;
		ObjectOutputStream brokerOut = null;
		ObjectInputStream brokerIn = null;
		
		//out = new ObjectOutputStream(regSocket.getOutputStream());
		//in = new ObjectInputStream(regSocket.getInputStream());

		while ((userInput = stdIn.readLine()) != null
				&& userInput.toLowerCase().indexOf("x") == -1) {
			/* make a new request packet */
			
			
			/**
			 * First do the lookup on the broker lookup server and then connect to chosen broker
			 */
			
			if(userInput.split(" ")[0].equalsIgnoreCase("local"))
			{
				try
				{
					regSocket = new Socket(hostname, port);
					out = new ObjectOutputStream(regSocket.getOutputStream());
					in = new ObjectInputStream(regSocket.getInputStream());

				}
				catch (UnknownHostException e) {
					System.err.println("ERROR: Don't know where to connect!!");
					System.exit(1);
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("ERROR: Couldn't get I/O for the connection.");
					System.exit(1);
				}
				
				if(brokerSock!=null)//first close connection
				{
					BrokerPacket byePack = new BrokerPacket();
					byePack.type = BrokerPacket.BROKER_BYE;
					brokerOut.writeObject(byePack);
					brokerOut.close();
					brokerIn.close();
					brokerSock.close();

				}
				
				
				brokerName = userInput.split(" ")[1];
				BrokerPacket lookupPacket = new BrokerPacket();
				lookupPacket.type = BrokerPacket.LOOKUP_REQUEST;
				lookupPacket.symbol = brokerName;
				out.writeObject(lookupPacket);
				
				BrokerPacket brokerLocationPacket = (BrokerPacket) in.readObject(); // get location info packet
				if(brokerLocationPacket.type == BrokerPacket.LOOKUP_REPLY)
				{
					brokerLoc = brokerLocationPacket.locations[0];
					if(brokerLoc==null)
					{
						System.out.println("Please try another broker as the one attempted has not registered with lookup service");
					}
					else
					{
						brokerSock = new Socket(brokerLoc.broker_host,brokerLoc.broker_port); // connect to chosen broker
						System.out.println(brokerLoc.broker_host+" "+brokerLoc.broker_port);
						brokerOut = new ObjectOutputStream(brokerSock.getOutputStream());
						brokerIn = new ObjectInputStream(brokerSock.getInputStream());

					}
					
				}
				else
				{
					System.err.println("ERROR Lookup failed to satisfy protocol"); // lookup server did not send approp. response
				}
				out.close();
				in.close();
				regSocket.close();
				
			}
			else
			{
				// if a user is trying to query a broker without setting the broker then print error message and continue
				if(brokerSock==null)
				{
					System.out.println(">Please set the broker first - nasdaq or tse ");
					
				}
				else
				{
					/* Normal protocol to the broker */
			
					BrokerPacket packetToServer = new BrokerPacket();
					
					packetToServer.type = BrokerPacket.BROKER_REQUEST;
					System.out.println("Query symbol - "+userInput);
					packetToServer.symbol = userInput;
					System.out.println("Write packet to broker server...");
					brokerOut.writeObject(packetToServer);

					/* print server reply */
					BrokerPacket packetFromServer = new BrokerPacket();
					packetFromServer = (BrokerPacket) brokerIn.readObject();
					//packetFromServer = (BrokerPacket) brokerIn.readObject();
					//System.out.println("packet "+packetFromServer.type+ " "+packetFromServer.symbol);
					if (packetFromServer.type == BrokerPacket.BROKER_QUOTE)
						System.out.println("Quote from broker: " + packetFromServer.quote);

					else if(packetFromServer.type == BrokerPacket.BROKER_NULL)
						System.out.println("Quote from broker: " + 0);

					}
				
			}
			/* re-print console prompt */
			System.out.print(">");
		}

		/* tell server that i'm quitting */
		BrokerPacket packetToServer = new BrokerPacket();
		packetToServer.type = BrokerPacket.BROKER_BYE;
		brokerOut.writeObject(packetToServer);

		
		brokerOut.close();
		brokerIn.close();
		//out.close();
		//in.close();
		stdIn.close();
		//regSocket.close();
		brokerSock.close();
	
	}
}
