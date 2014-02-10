package broker.broker2;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import broker.broker2.OnlineBroker;

public class BrokerThread implements Runnable {
	
	Socket sock = null;
	Socket exchangeSock = null;
	
	BrokerThread(Socket sock)
	{
		super();
		this.sock = sock;
		
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("New client thread running ...");
		try {
			ObjectInputStream inputFromClient = new ObjectInputStream(sock.getInputStream());
			ObjectOutputStream outputToClient = new ObjectOutputStream(sock.getOutputStream());

			BrokerPacket pack;
			Long quoteValue = (long) 0;
			while((pack = (BrokerPacket) inputFromClient.readObject())!=null)
			{
				if(pack.type == BrokerPacket.BROKER_REQUEST)
				{
					String brokerKey = pack.symbol; // request quote for the given key
					if (OnlineBroker.stockQuotes.containsKey(brokerKey))
					{
						quoteValue = OnlineBroker.stockQuotes.get(brokerKey);
						pack.type = BrokerPacket.BROKER_QUOTE;
						pack.quote = quoteValue;
						outputToClient.writeObject(pack);

					}
					
					
					
				}
				else if (pack.type == BrokerPacket.BROKER_BYE || pack.type == BrokerPacket.BROKER_NULL)
				{
					/*gotByePacket = true;
					pack = new BrokerPacket();
					pack.type = BrokerPacket.BROKER_BYE;
					pack.symbol = "Bye";
					outputToClient.writeObject(pack);*/

					break;
				}
				
				else if (pack.type == BrokerPacket.EXCHANGE_ADD)
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
				
				outputToClient.writeObject(pack);

				
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
	
	public static void flushToDisk()
	{
		System.out.println("Flushing");
		File stockFile = new File("src\\broker\\broker2\\nasdaq");
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
