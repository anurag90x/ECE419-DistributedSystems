
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;


public class OnlineBroker {
	static HashMap<String,Long> stockQuotes = new HashMap<String,Long>();
	//static HashMap<Integer,String> exchangeCodes = new HashMap<Integer,String>();
	
	public static void main(String args[]) throws IOException
	{
		
		/*
		 * # arguments to OnlineBroker
			# $1 = hostname of BrokerLookupServer
			# $2 = port where BrokerLookupServer is listening
			# $3 = port where I will be listening
			# $4 = my name ("nasdaq" or "tse")

		  */
		
		
		ServerSocket server = null;
		String myName="";
		String listeningServerHostName = "";
		int listeningServerPort = 0;
		
		boolean listening = true;
		try
		{
			if(args.length!=4)
			{
				System.err.println("INVALID ARGUMENTS");
				System.exit(1);
				}
			else
			{
				System.out.println("Initialize broker server");
				listeningServerHostName = args[0];
				listeningServerPort = Integer.parseInt(args[1]);
				myName = args[3];
				server = new ServerSocket(Integer.parseInt(args[2]));
			}
		}
		catch(IOException e)
		{
			System.err.println("ERROR Could not listen on port ");
			System.exit(1);
		}
		
		
		init(myName);
		registerService(listeningServerHostName,listeningServerPort,myName,server.getLocalPort()); //register broker server
		
		System.out.println("Listen for connections ...");
		while (listening)
		{
			new Thread(new BrokerThread(server.accept(),myName,listeningServerHostName,listeningServerPort)).start();
		}
		
		
		// flush contents to disk
		//flushToDisk();

		System.out.println("Close server..");
		server.close();

	}
	

public static void registerService(String lookupHost,int lookupPort,String serverName,int serverPort)
{
	
	System.out.println("Register broker ");
	try {
		Socket registerSocket = new Socket(lookupHost,lookupPort);
		ObjectOutputStream output = new ObjectOutputStream(registerSocket.getOutputStream());
		ObjectInputStream input = new ObjectInputStream(registerSocket.getInputStream());	
		BrokerPacket registerPacket = new BrokerPacket();
		registerPacket.type = BrokerPacket.LOOKUP_REGISTER;
		registerPacket.symbol = serverName;
		registerPacket.locations = new BrokerLocation[1];
		registerPacket.locations[0]=new BrokerLocation(registerSocket.getLocalAddress().getHostAddress(),serverPort);
		output.writeObject(registerPacket);		
		output.close();
		input.close();
		registerSocket.close();
		System.out.println("Registration completed by broker");
		
	} catch (UnknownHostException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
	
	

public static void init(String myName)
{

	System.out.println("Broker name is  "+myName);
	File stockFile = new File(myName);
	if (!stockFile.exists())
	{
		System.err.println("ERROR Stock mapping file not found");
		System.exit(1);
	}
	FileReader reader;
	try {
		reader = new FileReader(stockFile);
		BufferedReader br = new BufferedReader(reader);
		String firstLine = br.readLine();
		while(firstLine != null)
		{
			String[] mapping = firstLine.split(" ");
			System.out.println(mapping[0].trim()+" "+mapping[1].trim());
			stockQuotes.put(mapping[0].trim(),Long.parseLong(mapping[1].trim()));
			firstLine = br.readLine();

		}
		reader.close();
		br.close();
		
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	System.out.println("Initial hashmap populated ");
	
}

}
