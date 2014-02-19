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
 */

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.PriorityBlockingQueue;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;

/**
 * The entry point and glue code for the game. It also contains some helpful
 * global utility methods.
 * 
 * @author Geoffrey Washburn &lt;<a
 *         href="mailto:geoffw@cis.upenn.edu">geoffw@cis.upenn.edu</a>&gt;
 * @version $Id: Mazewar.java 371 2004-02-10 21:55:32Z geoffw $
 */

public class Mazewar extends JFrame {

	/**
	 * The default width of the {@link Maze}.
	 */
	private final int mazeWidth = 20;

	/**
	 * The default height of the {@link Maze}.
	 */
	private final int mazeHeight = 10;

	/**
	 * The default random seed for the {@link Maze}. All implementations of the
	 * same protocol must use the same seed value, or your mazes will be
	 * different.
	 */
	private final int mazeSeed = 1882;
	//private final int mazeSeed = (int) System.currentTimeMillis()%1000;

	/**
	 * The {@link Maze} that the game uses.
	 */
	public Maze maze = null;

	/**
	 * The {@link GUIClient} for the game.
	 */
	public GUIClient guiClient = null;

	/**
	 * The panel that displays the {@link Maze}.
	 */
	private OverheadMazePanel overheadPanel = null;

	/**
	 * The table the displays the scores.
	 */
	private JTable scoreTable = null;

	public Socket clientSocket = null;

	private ArrayList<ClientEvent> messageQueue = new ArrayList<ClientEvent>();
	/**
	 * Create the textpane statically so that we can write to it globally using
	 * the static consolePrint methods
	 */
	private static final JTextPane console = new JTextPane();

	public PriorityBlockingQueue<MazewarPacket> packQueue = new PriorityBlockingQueue<MazewarPacket>();

	public ScoreTableModel scoreModel;
	public ObjectOutputStream outputToServer;
	public ObjectInputStream inputFromServer;

	/**
	 * Write a message to the console followed by a newline.
	 * 
	 * @param msg
	 *            The {@link String} to print.
	 */
	public static synchronized void consolePrintLn(String msg) {
		console.setText(console.getText() + msg + "\n");
	}

	/**
	 * Write a message to the console.
	 * 
	 * @param msg
	 *            The {@link String} to print.
	 */
	public static synchronized void consolePrint(String msg) {
		console.setText(console.getText() + msg);
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
	public void quit() {
		// Put any network clean-up code you might have here.
		// (inform other implementations on the network that you have
		// left, etc.)
		ObjectOutputStream out;
		try {
			MazewarPacket byePacket = new MazewarPacket();
			byePacket.type = ClientEvent.QUIT;
			outputToServer.writeObject(byePacket);
			outputToServer.close();
			inputFromServer.close();
			clientSocket.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.exit(0);
	}

	

	Mazewar(String hostname, int port) {
		super("ECE419 Mazewar");
		consolePrintLn("ECE419 Mazewar started!");
		try {
			clientSocket = new Socket(hostname, port);
			outputToServer =  new ObjectOutputStream(clientSocket.getOutputStream());
			inputFromServer = new ObjectInputStream(clientSocket.getInputStream());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// connect to the mazewar server
		
		

		// Create the maze
		maze = new MazeImpl(new Point(mazeWidth, mazeHeight), mazeSeed);
		assert (maze != null);

		// Have the ScoreTableModel listen to the maze to find
		// out how to adjust scores.
		scoreModel = new ScoreTableModel();
		assert (scoreModel != null);
		maze.addMazeListener(scoreModel);

		// Throw up a dialog to get the GUIClient name.
		String name = JOptionPane.showInputDialog("Enter your name");
		if ((name == null) || (name.length() == 0)) {
			quit();
		}

		// Create the GUIClient and connect it to the KeyListener queue
		guiClient = new GUIClient(name);
		DirectedPoint startingPoint = maze.addClient(guiClient);
		guiClient.setStartPoint(startingPoint,this);
		this.addKeyListener(guiClient);
		/*
		 * 
		 * guiClient = new GUIClient(name,server);
		 */
		

	}

	public void renderLocalGUI(ScoreTableModel model) {
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
		scoreTable = new JTable(model);
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
	
	
	   public void transmitPacket(MazewarPacket packetToServer)
       {
			try {
				
				outputToServer.writeObject(packetToServer);
				
				System.out.println("Transmitted start point info to server");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
       	
       }

	// make a method that does reading continuously. Join the the main method
	// with this method. Now if a key interrupt happens GUIClient handles and
	// transmits

	/**
	 * Entry point for the game.
	 * 
	 * @param args
	 *            Command-line arguments.
	 */
	public static void main(String args[]) {

		/* Create the GUI */
		String hostname = args[0];
		System.out.println(hostname);
		int port = 4445;
		Mazewar mw = new Mazewar(hostname, port);
		Thread queueThread;

		queueThread = new Thread(new  DequeueRunnable(mw));
		queueThread.start();

		// get input and output for the client socket to server connection
		//System.out.println("Connection established  ??? "+mw.clientSocket.isConnected()+mw.clientSocket.isInputShutdown());
		
		try {
			
			//outputToServer.writeObject("Hello");
			MazewarPacket pack = new MazewarPacket();
			pack.type = ClientEvent.ENTER;
			pack.player = mw.guiClient.getName();
			pack.startingPoint = mw.guiClient.startingPoint;
			mw.outputToServer.writeObject(pack);
			
			MazewarPacket inputPacket;
			//mw.renderLocalGUI(mw.scoreModel);
			while (true) {
				if ((inputPacket = (MazewarPacket)mw.inputFromServer.readObject())!= null) {
					System.out.println("Enqueue object");
					mw.packQueue.add(inputPacket);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Connection established  ??? "+mw.clientSocket.isClosed());

			e.printStackTrace();
		} /*catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

	}

}

class DequeueRunnable implements Runnable {

	Mazewar mw;
	HashMap<String, RemoteClient> remoteClientMap = new HashMap<String, RemoteClient>();
	int counter;

	DequeueRunnable(Mazewar mw) {
		this.mw = mw;
	}

	@Override
	public void run() {
		
		while (true) {
			while (mw.packQueue.size() > 0) {
				try {
					MazewarPacket packet = mw.packQueue.take();
					System.out.println("Removed "+packet.sequenceTag);
					String player = packet.player;
					System.out.println(player+" "+counter+" "+mw.guiClient.getName());
					/*if (player==null)
						continue;*/
					if (player!=null && !player.equals(mw.guiClient.getName())
							&& packet.type == ClientEvent.ENTER ) {
						parseRemote(packet);
						System.out.println("New player joined "+player);

						
						// No of players supported is 4
					

					}
					if(packet.renderReady==1)
					{
							mw.renderLocalGUI(mw.scoreModel);
					}
					
					
					
					else
						
					{
						if (player.equals(mw.guiClient.getName())) {
							clientHandler(mw.guiClient, packet);
						} else {
							clientHandler(remoteClientMap.get(packet.player),
									packet);
						}
					}

			}catch(InterruptedException e)
			{
				e.printStackTrace();
			}

			}
		}

		}

	//}

	public void clientHandler(Client client, MazewarPacket packet) {
		int type = packet.type;
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
		case ClientEvent.QUIT: {
			if (client instanceof GUIClient)
				mw.quit();
			else
				remoteClientMap.get(packet.player).quit();
			break;
		}
		}
	}

	public void parseRemote(MazewarPacket packet) {
		RemoteClient rc = new RemoteClient(packet.player, packet.startingPoint);
		System.out.println("Register new client here "+packet.startingPoint.getX()+" "+packet.startingPoint.getY());
		mw.maze.addRemoteClient(rc, packet.startingPoint);
		remoteClientMap.put(rc.getName(), rc);
	}
}
