
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;

public class BrokerExchange {
	public static void main(String args[])
	{
		Socket sock = null;
		ObjectInputStream input = null;
		ObjectOutputStream out = null;
		
		HashMap<Integer,String> errorExchangeCodes = new HashMap<Integer,String>();
		errorExchangeCodes.put(BrokerPacket.ERROR_INVALID_EXCHANGE,"Invalid Exchange");
		errorExchangeCodes.put(BrokerPacket.ERROR_INVALID_SYMBOL,"Invalid symbol");
		errorExchangeCodes.put(BrokerPacket.ERROR_OUT_OF_RANGE,"Quote out of range");
		errorExchangeCodes.put(BrokerPacket.ERROR_SYMBOL_EXISTS,"Symbol exists");
		
		try {
			
		String hostname = "";
		int port = 0;
		if (args.length==2)
		{
			hostname = args[0];
			port = Integer.parseInt(args[1]);
			
		}
		else
		{
			System.err.println("ERROR INVALID ARGS");
			System.exit(1);
		}
		
		sock = new Socket(hostname,port);
		System.out.println("Connection made to server ...");
		out = new ObjectOutputStream(sock.getOutputStream());
		input = new ObjectInputStream( sock.getInputStream());
		System.out.print(">");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String userInput="";			
		
		while((userInput = br.readLine()) != null && userInput.indexOf("x")==-1)
		{
			String[] commandWords = userInput.split(" "); //commandWords
			BrokerPacket newExchangePacket = new BrokerPacket();
			if (commandWords[0].equalsIgnoreCase("add"))
			{
				newExchangePacket.type = BrokerPacket.EXCHANGE_ADD;
				newExchangePacket.symbol = commandWords[1].trim();
				newExchangePacket.quote = Long.parseLong(commandWords[2].trim());
			}
			else if(commandWords[0].equalsIgnoreCase("remove"))
			{
				newExchangePacket.type = BrokerPacket.EXCHANGE_REMOVE;
				newExchangePacket.symbol = commandWords[1].trim();
			}
			else if (commandWords[0].equalsIgnoreCase("update"))
			{
				newExchangePacket.type = BrokerPacket.EXCHANGE_UPDATE;
				newExchangePacket.symbol = commandWords[1].trim();
				newExchangePacket.quote = Long.parseLong(commandWords[2].trim());
			}
			else if (commandWords[0].equalsIgnoreCase("x"))
			{
				newExchangePacket.type = BrokerPacket.EXCHANGE_BYE;
				out.writeObject(newExchangePacket);
				break;
			}
			
			System.out.println("Write packet to server ");
			out.writeObject(newExchangePacket);
			
			BrokerPacket replyPacket = (BrokerPacket) input.readObject();
			if(replyPacket.type==BrokerPacket.EXCHANGE_REPLY)
			{
				if(errorExchangeCodes.containsKey(replyPacket.error_code))
				{
					System.err.println(errorExchangeCodes.get(replyPacket.error_code));
					
				}
				else {
					System.out.println("Received reply from server ");
					
				}
				
			}
			System.out.print(">");
			
			
		}
		BrokerPacket pack = new BrokerPacket();
		pack.type = BrokerPacket.BROKER_BYE;
		out.writeObject(pack);
		
		input.close();
		out.close();
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
