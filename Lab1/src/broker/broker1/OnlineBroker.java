package broker.broker1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

public class OnlineBroker {
	
	static HashMap<String,Long> stockQuotes = new HashMap<String,Long>();
	
	public static void main(String args[]) throws IOException
	{
		System.out.println("Enter...");
		ServerSocket server = null;
		loadStockFile();
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
				System.out.println("Starting server ...");
				server = new ServerSocket(Integer.parseInt(args[0]));
			}
		}
		catch(IOException e)
		{
			System.err.println("ERROR Could not listen on port ");
			System.exit(1);
		}
		while (listening)
		{
			new BrokerThread(server.accept()).start();
		}
		System.out.println("Exit...");
		server.close();
	}
	
public static void loadStockFile()
{
	File stockFile = new File("src\\broker\\broker1\\nasdaq");
	if (!stockFile.exists())
	{
		System.err.println("ERROR Stock mapping file not found "+stockFile.getAbsolutePath());
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
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
}

}
