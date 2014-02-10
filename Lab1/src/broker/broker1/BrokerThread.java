package broker.broker1;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class BrokerThread extends Thread {
	
	Socket sock = null;
	
	BrokerThread(Socket sock)
	{
		super();
		System.out.println("Start new thread for client ...");

		this.sock = sock;
		
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		//boolean gotByePacket = false;
		try {
			ObjectInputStream inputFromClient =new ObjectInputStream(sock.getInputStream());
			ObjectOutputStream outputToClient = new ObjectOutputStream( sock.getOutputStream());

			BrokerPacket pack;
			Long quoteValue = (long) 0;
			while((pack = (BrokerPacket) inputFromClient.readObject())!=null)
			{
				System.out.println("Get input from client..");
				if(pack.type == BrokerPacket.BROKER_REQUEST)
				{
					String brokerKey = pack.symbol; // request quote for the given key
					if (OnlineBroker.stockQuotes.containsKey(brokerKey))
					{
						quoteValue = OnlineBroker.stockQuotes.get(brokerKey);
						pack.type = BrokerPacket.BROKER_QUOTE;
						pack.quote = quoteValue;
						System.out.println("Write output back...");
					}
					else
					{
						pack.quote = (long) 0;
					}
					
				}
				else if (pack.type == BrokerPacket.BROKER_BYE || pack.type == BrokerPacket.BROKER_NULL)
				{
					/*gotByePacket = true;
					pack = new BrokerPacket();
					pack.type = BrokerPacket.BROKER_BYE;
					System.out.println("Write output back for exit...");
					outputToClient.writeObject(pack);*/
					break;
				}
				outputToClient.writeObject(pack);

			}
			

			// book-keeping
			inputFromClient.close();
			outputToClient.close();
			sock.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	


}
