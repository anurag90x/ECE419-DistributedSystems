/*
Copyright (C) 2004 Geoffrey Alan Washburn

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
USA.

MODIFIED by:

Anurag Chaudhury
Sachin Siby

 */

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;

/**
 * The entry point and glue code for the game.  It also contains some helpful
 * global utility methods.
 * @author Geoffrey Washburn &lt;<a href="mailto:geoffw@cis.upenn.edu">geoffw@cis.upenn.edu</a>&gt;
 * @version $Id: Mazewar.java 371 2004-02-10 21:55:32Z geoffw $
 */

/**
 * @author Anurag,Sachin
 *
 */
public class Mazewar extends JFrame {


	// for Naming Service
	static Socket conn = null;
	static ObjectOutputStream output ;
	static 	ObjectInputStream in;


	static  ArrayList<MazewarPacket> messageQueue = new ArrayList<MazewarPacket>();
	static boolean hasToken =false;

	// for outgoing neighbour connection in ring.
	static Socket outgoingSocket = null;
	static ObjectOutputStream outSock = null;
	static ObjectInputStream inSock = null;

	/**
	 * The default width of the {@link Maze}.
	 */
	private final int mazeWidth = 20;

	/**
	 * The default height of the {@link Maze}.
	 */
	private final int mazeHeight = 10;

	/**
	 * The default random seed for the {@link Maze}.
	 * All implementations of the same protocol must use 
	 * the same seed value, or your mazes will be different.
	 */
	private final int mazeSeed = 42;

	/**
	 * The {@link Maze} that the game uses.
	 */
	public static MazeImpl maze = null;

	/**
	 * The {@link GUIClient} for the game.
	 */
	public static GUIClient guiClient = null;

	public static HashMap<String, RemoteClient> remoteClientMap = new HashMap<String, RemoteClient>();

	/**
	 * The panel that displays the {@link Maze}.
	 */
	OverheadMazePanel overheadPanel = null;

	/**
	 * The table the displays the scores.
	 */
	private JTable scoreTable = null;


	public ScoreTableModel scoreModel=null;

	/** 
	 * Create the textpane statically so that we can 
	 * write to it globally using
	 * the static consolePrint methods  
	 */
	private static final JTextPane console = new JTextPane();

	/** 
	 * Write a message to the console followed by a newline.
	 * @param msg The {@link String} to print.
	 */ 
	public static synchronized void consolePrintLn(String msg) {
		console.setText(console.getText()+msg+"\n");
	}

	/** 
	 * Write a message to the console.
	 * @param msg The {@link String} to print.
	 */ 
	public static synchronized void consolePrint(String msg) {
		console.setText(console.getText()+msg);
	}

	/** 
	 * Clear the console. 
	 */
	public static synchronized void clearConsole() {
		console.setText("");
	}

	/**
	 * Static method for performing cleanup before exiting the game.
	 */
	/**
	 * 
	 */
	public static void quit() {
		// Put any network clean-up code you might have here.
		// (inform other implementations on the network that you have 
		//  left, etc.)

		System.out.println("Leaving game");

		

		try {
			//Sending enter packet
			//output = new ObjectOutputStream(conn.getOutputStream());
			MazewarPacket packet = new MazewarPacket(MazewarPacket.BYE);
			output.writeObject(packet);
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


		System.exit(0);
		
	}

	/** 
	 * The place where all the pieces are put together. 
	 */
	public Mazewar() {
		super("ECE419 Mazewar");
		consolePrintLn("ECE419 Mazewar started!");

		// Create the maze
		maze = new MazeImpl(new Point(mazeWidth, mazeHeight), mazeSeed);
		assert(maze != null);

		// Have the ScoreTableModel listen to the maze to find
		// out how to adjust scores.
		scoreModel = new ScoreTableModel();
		assert(scoreModel != null);
		maze.addMazeListener(scoreModel);

		// Throw up a dialog to get the GUIClient name.
		String name = JOptionPane.showInputDialog("Enter your name");
		if((name == null) || (name.length() == 0)) {
			Mazewar.quit();
		}

		// You may want to put your network initialization code somewhere in
		// here.

		// Create the GUIClient and connect it to the KeyListener queue
		guiClient = new GUIClient(name);
		guiClient.messageQueue = Mazewar.messageQueue;
		maze.addClient(guiClient);
		this.addKeyListener(guiClient);



	}

	public void renderLocalGUI(MazewarPacket pack) {

		//System.out.println("Remote client details ...");
		for(String pName : pack.clMap.keySet())
		{

			if(!pName.equals(guiClient.getName()))
			{
				RemoteClient cl = new RemoteClient(pName,pack.clMap.get(pName));

				remoteClientMap.put(pName,cl);
				System.out.println(pName+" "+pack.clMap.get(pName).direction);
				maze.addRemoteClient(cl, pack.clMap.get(pName));

			}

		}

		//System.out.println("Seen by the local client : ");

		for(Object client : maze.clientMap.keySet())
		{
			Client o = (Client) client;
			System.out.print(o.getName()+" ");
			((DirectedPoint)maze.clientMap.get(client)).toStringMethod();
		}


		overheadPanel = new OverheadMazePanel(maze, guiClient);
		assert (overheadPanel != null);
		maze.addMazeListener(overheadPanel);

		// Don't allow editing the console from the GUI
		console.setEditable(false);
		console.setFocusable(false);
		console.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder()));

		// Allow the console to scroll by putting it in a scrollpane
		JScrollPane consoleScrollPane = new JScrollPane(console);
		assert (consoleScrollPane != null);
		consoleScrollPane.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Console"));

		// Create the score table
		scoreTable = new JTable(scoreModel);
		assert (scoreTable != null);
		scoreTable.setFocusable(false);
		scoreTable.setRowSelectionAllowed(false);

		// Allow the score table to scroll too.
		JScrollPane scoreScrollPane = new JScrollPane(scoreTable);
		assert (scoreScrollPane != null);
		scoreScrollPane.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Scores"));

		// Create the layout manager
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		getContentPane().setLayout(layout);

		// Define the constraints on the components.
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 3.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(overheadPanel, c);
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 2.0;
		c.weighty = 1.0;
		layout.setConstraints(consoleScrollPane, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		layout.setConstraints(scoreScrollPane, c);

		// Add the components
		getContentPane().add(overheadPanel);
		getContentPane().add(consoleScrollPane);
		getContentPane().add(scoreScrollPane);

		// Pack everything neatly.
		pack();

		// Let the magic begin.
		setVisible(true);
		overheadPanel.repaint();
		this.requestFocusInWindow();
	}


	/**
	 * Entry point for the game.  
	 * @param args Command-line arguments.
	 */
	public static void main(String args[]) {

		/* Create the GUI */
		Mazewar mw = new Mazewar();
		String hostName = args[0];
		int nmServerPort = Integer.parseInt(args[1]);
		int port = 4445;
		try {
			conn = new Socket(hostName,port);
			try {
				//Sending enter packet
				output = new ObjectOutputStream(conn.getOutputStream());
				in =  new ObjectInputStream(conn.getInputStream());

				MazewarPacket packet = new MazewarPacket(MazewarPacket.ENTER);
				packet.clMap.put(mw.guiClient.getName(), mw.guiClient.getDirectedPoint());
				packet.mPort = nmServerPort;
				output.writeObject(packet);


			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


		MazewarPacket pack = null;
		try {
			while((pack=(MazewarPacket) in.readObject())!=null)
			{
				if (pack.mType == MazewarPacket.TOKEN)
				{
					System.out.println("Got token 1ST TIME");
					Mazewar.hasToken = true;
				}
				else if(pack.mType == MazewarPacket.REPLY_ENTER)
				{
					mw.renderLocalGUI(pack);
					break;
				}
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		Thread rnm = new Thread(new ReceiveNeighbourManager(nmServerPort,mw));
		rnm.start();


		//Request neighbour and  loop until connection is made
		while(Mazewar.outgoingSocket == null)
		{
			String neighbour = requestNeighbour(nmServerPort);	
			String hostname2 = neighbour.split(":")[0];
			hostname2= hostname2.split("/")[1];

			int port2 = Integer.parseInt(neighbour.split(":")[1]);
			System.out.println("Neighbour info is "+hostname2+" "+port2);		

			try
			{
				Mazewar.outgoingSocket = new Socket(hostname2, port2);	
			}	
			catch(Exception ex)
			{
				continue;
			}							
		}
		try
		{
			outSock = new ObjectOutputStream(Mazewar.outgoingSocket.getOutputStream());
			inSock = new ObjectInputStream(Mazewar.outgoingSocket.getInputStream());
			MazewarPacket pack3 = new MazewarPacket(MazewarPacket.NULL);
			outSock.writeObject(pack3);
			System.out.println("Connected to neighbour server "+Mazewar.outgoingSocket);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}



	}
	/**
	 * @param nmServerPort
	 * @return String - neighbour information i.e ip and port
	 */
	public static String requestNeighbour(int nmServerPort)
	{
		//System.out.println("ENTER request neighbour info ");
		MazewarPacket packet = new MazewarPacket(MazewarPacket.REQUEST_NEIGHBOUR);
		String neighbourInfo = "";
		try {
			packet.mPort = nmServerPort;
			//System.out.println("Server port is "+nmServerPort);
			Mazewar.output.writeObject(packet);
			neighbourInfo = (String) Mazewar.in.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		//System.out.println("EXIT with neighbour info as "+neighbourInfo);
		return neighbourInfo;
	}


}





class ReceiveNeighbourManager implements Runnable
{
	int nmServerPort;
	RemoteClient incomingClient = null;
	Mazewar mw;	
	ReceiveNeighbourManager(int nm,Mazewar m)
	{
		nmServerPort = nm;
		mw = m;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("Start receiver thread thing ");
		
		Socket clientSock;
		try {		
			ServerSocket server = new ServerSocket(nmServerPort);

			
			try {
				while(true)
				{
					//System.out.println("Waiting for connections....");
					clientSock = server.accept();
					ObjectOutputStream out = new ObjectOutputStream(clientSock.getOutputStream());
					ObjectInputStream in = new ObjectInputStream(clientSock.getInputStream());
					MazewarPacket packet;
					//System.out.println("Received connection from "+clientSock.getInetAddress().getHostAddress());
					if(Mazewar.hasToken)
					{
						// Now, the client has the token and should be allowed to send


						if( Mazewar.messageQueue.size()==0)
						{
							MazewarPacket tokenPacket = new MazewarPacket(MazewarPacket.TOKEN);
							if(Mazewar.outSock ==null && Mazewar.inSock == null)
							{
								Mazewar.outSock = new ObjectOutputStream(Mazewar.outgoingSocket .getOutputStream());
								Mazewar.inSock = new ObjectInputStream(Mazewar.outgoingSocket .getInputStream());
							}
							Mazewar.outSock.writeObject(tokenPacket);
							Mazewar.hasToken = false;
						}
						else
						{
							MazewarPacket pack = Mazewar.messageQueue.get(0);
							Mazewar.messageQueue.remove(0);

							Mazewar.outSock.writeObject(pack);					
							Mazewar.hasToken = false;
						}

					}
					while((packet = (MazewarPacket)in.readObject())!=null)
					{

						if(packet.mType == MazewarPacket.ACTION )
						{
							//System.out.println("ACTION RECEIVED from "+packet.mPlayer);

							if(packet.mPlayer.equals(Mazewar.guiClient.getName()))
							{
								// TODO : Pass token along
								if(packet.mType == MazewarPacket.ACTION)
								{
									System.out.println("Handle action for local client ");
									handleAction(Mazewar.guiClient, packet.mActionType);
								}


								MazewarPacket tokenPacket = new MazewarPacket(MazewarPacket.TOKEN);
								if(Mazewar.outSock ==null && Mazewar.inSock == null)
								{
									Mazewar.outSock = new ObjectOutputStream(Mazewar.outgoingSocket .getOutputStream());
									Mazewar.inSock = new ObjectInputStream(Mazewar.outgoingSocket .getInputStream());
								}
								Mazewar.outSock.writeObject(tokenPacket);
							}


							else
							{
								incomingClient = Mazewar.remoteClientMap.get(packet.mPlayer);


								handleAction(incomingClient,packet.mActionType);
								//forward packet
								if(Mazewar.outgoingSocket == null)
								{
									String neighbour = requestNeighbour();

									String hostname = neighbour.split(":")[0];
									int port = Integer.parseInt(neighbour.split(":")[1]);	
									Mazewar.outgoingSocket = new Socket(hostname, port);							
								}
								forwardPacket(Mazewar.outgoingSocket,packet);
							}
						}


						else if (packet.mType == MazewarPacket.TOKEN )
						{
							// Now, the client has the token and should be allowed to send

							//Mazewar.hasToken = true;
							if( Mazewar.messageQueue.size()==0)
							{
								//System.out.println("PASS TOKEN ");
								//Mazewar.hasToken = false;
								MazewarPacket tokenPacket = new MazewarPacket(MazewarPacket.TOKEN);
								if(Mazewar.outSock ==null && Mazewar.inSock == null)
								{
									Mazewar.outSock = new ObjectOutputStream(Mazewar.outgoingSocket .getOutputStream());
									Mazewar.inSock = new ObjectInputStream(Mazewar.outgoingSocket .getInputStream());
								}
								Mazewar.outSock.writeObject(tokenPacket);
								Mazewar.hasToken = false;
							}
							else
							{
								MazewarPacket pack = Mazewar.messageQueue.get(0);
								Mazewar.messageQueue.remove(0);

								//Send the first packet to neighbour
								Mazewar.outSock.writeObject(pack);					
								Mazewar.hasToken = true;


							}

						}
						else if (packet.mType == MazewarPacket.BYE)
						{
							break;
						}


					}
					//System.out.println("Recycle conn..");
					out.close();
					in.close();
					clientSock.close();

				}

			} catch (Exception e) {
				
				e.printStackTrace();
			}


		} catch (IOException e) {
			
			e.printStackTrace();
		}


	}

	/**
	 * @param client
	 * @param type
	 * Function to handle action i.e incoming action packet
	 */
	/**
	 * @param client
	 * @param type
	 */
	public void handleAction(Client client,int type)
	{
		switch (type) {
		case ClientEvent.MOVE_FORWARD: {
			System.out.println("forward");
			client.forward();
			break;
		}
		case ClientEvent.MOVE_BACKWARD: {
			System.out.println("backup");

			client.backup();
			break;
		}
		case ClientEvent.TURN_LEFT: {
			System.out.println("Left");
			client.turnLeft();
			break;
		}
		case ClientEvent.TURN_RIGHT: {
			System.out.println("Rightt");
			client.turnRight();
			break;
		}
		case ClientEvent.FIRE: {
			client.fire();
			break;
		}
		}
	}



	public void forwardPacket(Socket outgoingSocket,MazewarPacket packet)
	{

		try {
			if(Mazewar.outSock==null && Mazewar.inSock== null)
			{
				Mazewar.outSock = new ObjectOutputStream(outgoingSocket.getOutputStream());
				Mazewar.inSock = new ObjectInputStream(outgoingSocket.getInputStream());
			}	
			Mazewar.outSock.writeObject(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String requestNeighbour()
	{
		System.out.println("ENTER request neighbour info ");
		MazewarPacket packet = new MazewarPacket(MazewarPacket.REQUEST_NEIGHBOUR);
		String neighbourInfo = "";
		try {
			packet.mPort = nmServerPort;
			System.out.println("Server port is "+nmServerPort);
			Mazewar.output.writeObject(packet);
			neighbourInfo = (String) Mazewar.in.readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("EXIT with neighbour info as "+neighbourInfo);
		return neighbourInfo;
	}


	public String requestPrevious()
	{
		System.out.println("ENTER request previous neighbour info ");
		MazewarPacket packet = new MazewarPacket(MazewarPacket.REQUEST_PREVIOUS);
		String neighbourInfo = "";
		try {
			packet.mPort = nmServerPort;
			System.out.println("Server port is "+nmServerPort);
			Mazewar.output.writeObject(packet);
			neighbourInfo = (String) Mazewar.in.readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("EXIT with PREVIOUS neighbour info as "+neighbourInfo);
		return neighbourInfo;
	}


}
