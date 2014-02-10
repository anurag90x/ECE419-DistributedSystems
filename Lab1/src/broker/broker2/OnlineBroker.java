package broker.broker2;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

public class OnlineBroker {
	
	static HashMap<String,Long> stockQuotes = new HashMap<String,Long>();
	//static HashMap<Integer,String> exchangeCodes = new HashMap<Integer,String>();
	
	public static void main(String args[]) throws IOException
	{
		init();
		
		ServerSocket server = null;
		
		
		
		boolean listening = true;
		try
		{
			if(args.length!=1)
			{
				System.err.println("INVALID ARGUMENTS");
				System.exit(1);
				}
			else
			{
				server = new ServerSocket(Integer.parseInt(args[0]));
			}
		}
		catch(IOException e)
		{
			System.err.println("ERROR Could not listen on port ");
			System.exit(1);
		}
		
		System.out.println("Listen for connections ...");
		while (listening)
		{
			new Thread(new BrokerThread(server.accept())).start();
		}
		
		
		// flush contents to disk
		//flushToDisk();

		System.out.println("Close server..");
		server.close();

	}
	

	
public static void flushToDisk()
{
	File stockFile = new File("src\\broker\\broker2\\nasdaq");
	if (!stockFile.exists())
	{
		System.err.println("ERROR Stock mapping file not found");
		System.exit(1);
	}
	try {
		FileWriter writer = new FileWriter(stockFile);
		BufferedWriter bw = new BufferedWriter(writer);
		for(String key:stockQuotes.keySet())
		{
			bw.write(key+" "+stockQuotes.get(key)+"\n");
		}
		
		writer.close();
		bw.close();
		
		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

}

public static void init()
{

	
	File stockFile = new File("src\\broker\\broker2\\nasdaq");
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
	
}

}
