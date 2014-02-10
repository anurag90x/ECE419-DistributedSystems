package broker.broker3;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import broker.broker3.OnlineBroker;

public class BrokerThread implements Runnable {
	
	Socket sock = null;
	static String name="";
	static String serverName="";
	static int port;
	
	
	BrokerThread(Socket sock,String name,String serverName,int port)
	{
		super();
		this.sock = sock;
		BrokerThread.name = name;
		BrokerThread.serverName = serverName;
		BrokerThread.port = port;
	}
	
	
	@Override
	public void run() {
		
		System.out.println("New client thread running ...");
		try {
			ObjectInputStream inputFromClient = new ObjectInputStream(sock.getInputStream());

			ObjectOutputStream outputToClient = new ObjectOutputStream(sock.getOutputStream());


			BrokerPacket pack;
			Long quoteValue = (long) 0;
			

			while((pack = (BrokerPacket) inputFromClient.readObject())!=null)
			{
				System.out.println("Get pack from client");
				BrokerPacket replyPack = new BrokerPacket();

				if(pack.type == BrokerPacket.BROKER_REQUEST || pack.type == BrokerPacket.BROKER_FORWARD)
				{
					String brokerKey = pack.symbol; // request quote for the given key
					replyPack.type = BrokerPacket.BROKER_QUOTE;

					replyPack.symbol = pack.symbol;
					
					if (OnlineBroker.stockQuotes.containsKey(brokerKey))
					{
						System.out.println("Broker key is "+brokerKey);
						quoteValue = OnlineBroker.stockQuotes.get(brokerKey);
						replyPack.quote = quoteValue;
						System.out.println("Return "+replyPack.quote);


					}
					
					else if (pack.type == BrokerPacket.BROKER_FORWARD)
					{
						replyPack.quote = (long) 0;
						
					}
					else
					{
						// if this had already been tried in the other broker server then dont bother looking up
							System.out.println("Lookup in another server");
							long quoteReturned = lookupOtherServer(name,pack);
							replyPack.quote = quoteReturned;
							System.out.println("Got quote back as "+quoteReturned);
						
					}

					
				}
				else if (pack.type == BrokerPacket.BROKER_BYE || pack.type == BrokerPacket.BROKER_NULL)
				{
					System.out.println("Bye from client. Close stuff ");
					break;
				}
				
				else
				{
					System.out.println("Exchange client case ");
					replyPack = exchangeServerManager(pack);
				}
			
				outputToClient.writeObject(replyPack);

				
			}
			
			
			// book-keeping
			inputFromClient.close();
			outputToClient.close();
			sock.close();
			flushToDisk();

			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public static long lookupOtherServer(String name,BrokerPacket pack)
	{
		
		String otherBroker = name.equals("nasdaq")?"tse":"nasdaq";
		String symbol = pack.symbol;
		
		Socket lookupConn;
		try {
			lookupConn = new Socket(serverName,port);
			ObjectOutputStream out = new ObjectOutputStream(lookupConn.getOutputStream());
			ObjectInputStream in = new ObjectInputStream(lookupConn.getInputStream());
			
			BrokerPacket otherBrokerQuery = new BrokerPacket();
			otherBrokerQuery.symbol = otherBroker;
			otherBrokerQuery.type = BrokerPacket.LOOKUP_REQUEST;
			out.writeObject(otherBrokerQuery);
			
			BrokerPacket locationInformationPack = (BrokerPacket) in.readObject();
			if (locationInformationPack.locations[0]==null)
				return 0; //the other broker server is not running in this case
			
			
			String otherBrokerHost = locationInformationPack.locations[0].broker_host;
			int otherBrokerPort = locationInformationPack.locations[0].broker_port;
			
			in.close();
			out.close();
			lookupConn.close();
			
			Socket brokerSocket = new Socket(otherBrokerHost,otherBrokerPort); // connect to other Broker
			out = new ObjectOutputStream(brokerSocket.getOutputStream());
			in = new ObjectInputStream(brokerSocket.getInputStream());
			BrokerPacket packet = new BrokerPacket();
			packet.symbol = pack.symbol;
			packet.type = BrokerPacket.BROKER_FORWARD; //forward request
			out.writeObject(packet);
			
			BrokerPacket reply = (BrokerPacket) in.readObject();
			if(reply.type == BrokerPacket.BROKER_QUOTE)
			{
				BrokerPacket byePack = new BrokerPacket();
				byePack.type = BrokerPacket.BROKER_BYE;
				out.writeObject(byePack);
				out.close();
				in.close();
				brokerSocket.close();
				System.out.println("Return quote as "+reply.quote);
				return reply.quote;
				
			}
			
		
			
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// should never reach this block
		return 0;			
		
		
	}
	
	
	
	public static BrokerPacket exchangeServerManager(BrokerPacket pack)
	{
		 if (pack.type == BrokerPacket.EXCHANGE_ADD)
		{
			// check if symbol already exists and the out of range condition
			pack.type = BrokerPacket.EXCHANGE_REPLY;

			if(OnlineBroker.stockQuotes.containsKey(pack.symbol))
			{
				pack.error_code = BrokerPacket.ERROR_SYMBOL_EXISTS;
			}
			else if (pack.quote <1 || pack.quote>300)
			{
				
				pack.error_code = BrokerPacket.ERROR_OUT_OF_RANGE;
			}
			else
			{
				// good case
				pack.type = BrokerPacket.EXCHANGE_REPLY;

				OnlineBroker.stockQuotes.put(pack.symbol,pack.quote);
			}
		}
		
		else if (pack.type == BrokerPacket.EXCHANGE_REMOVE)
		{
			pack.type = BrokerPacket.EXCHANGE_REPLY;

			if(OnlineBroker.stockQuotes.containsKey(pack.symbol))
			{

				OnlineBroker.stockQuotes.remove(pack.symbol);
			}
			else
			{

				pack.error_code = BrokerPacket.ERROR_INVALID_SYMBOL; // symbol not present
			}
		}
		
		else if (pack.type == BrokerPacket.EXCHANGE_UPDATE)
		{
			pack.type = BrokerPacket.EXCHANGE_REPLY;

			if (!OnlineBroker.stockQuotes.containsKey(pack.symbol))
			{

				pack.error_code = BrokerPacket.ERROR_INVALID_SYMBOL;
			}
			else if (pack.quote <1 || pack.quote>300)
				{

					pack.error_code = BrokerPacket.ERROR_OUT_OF_RANGE;
				}
			else
			{

				OnlineBroker.stockQuotes.put(pack.symbol, pack.quote);
			}
			 
		}
		 
		 return pack;
		
		
	}
	
	
	public static void flushToDisk()
	{
		System.out.println("Flushing to file "+name);
		File stockFile = new File("src\\broker\\broker3\\"+name);
		if (!stockFile.exists())
		{
			System.err.println("ERROR Stock mapping file not found");
			System.exit(1);
		}
		try {
			FileWriter writer = new FileWriter(stockFile);
			BufferedWriter bw = new BufferedWriter(writer);
			for(String key:OnlineBroker.stockQuotes.keySet())
			{
				bw.write(key+" "+OnlineBroker.stockQuotes.get(key)+"\n");
			}
			bw.flush();
			writer.close();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
