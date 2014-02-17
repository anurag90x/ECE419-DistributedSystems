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

import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * An implementation of {@link LocalClient} that is controlled by the keyboard
 * of the computer on which the game is being run.  
 * @author Geoffrey Washburn &lt;<a href="mailto:geoffw@cis.upenn.edu">geoffw@cis.upenn.edu</a>&gt;
 * @version $Id: GUIClient.java 343 2004-01-24 03:43:45Z geoffw $
 */

public class GUIClient extends LocalClient implements KeyListener {

        /**
         * Create a GUI controlled {@link LocalClient}.  
         */
		
		
		Socket clientSocket = null;
		DirectedPoint startingPoint = null;
		Mazewar mazeWar;
		
        public GUIClient(String name) {
                super(name);
               
                
        }
        
        public void setStartPoint(DirectedPoint startingPoint,Mazewar mazewar) {
			this.startingPoint = startingPoint;
			mazeWar = mazewar;
			
			//clientSocket = clientSock;
			//transmitPacket(pack);
			
		}

        
        /**
         * Handle a key press.
         * @param e The {@link KeyEvent} that occurred.
         */
        public void keyPressed(KeyEvent e) {
                // If the user pressed Q, invoke the cleanup code and quit. 
        		MazewarPacket pack = new MazewarPacket();
        		pack.player = getName();
                if((e.getKeyChar() == 'q') || (e.getKeyChar() == 'Q')) {
                       // Mazewar.quit();
                        if(clientSocket!=null)
                        {
                        	
                        }
                        
                // Up-arrow moves forward.
                } else if(e.getKeyCode() == KeyEvent.VK_UP) {
                       // forward();
                        if(mazeWar.clientSocket!=null)
                        {
                        	pack.type = ClientEvent.MOVE_FORWARD;
                        	transmitPacket(pack);
                        }
                // Down-arrow moves backward.
                } else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
                        //backup();
                        if(mazeWar.clientSocket!=null)
                        {
                        	pack.type = ClientEvent.MOVE_BACKWARD;
                        	transmitPacket(pack);
                        }
                // Left-arrow turns left.
                } else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
                        //turnLeft();
                        if(mazeWar.clientSocket!=null)
                        {
                        	pack.type = ClientEvent.TURN_LEFT;
                        	transmitPacket(pack);
                        }
                // Right-arrow turns right.
                } else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
                       // turnRight();
                        if(mazeWar.clientSocket!=null)
                        {
                        	pack.type = ClientEvent.TURN_RIGHT;
                        	transmitPacket(pack);
                        }
                // Spacebar fires.
                } else if(e.getKeyCode() == KeyEvent.VK_SPACE) {
                       // fire();
                        if(mazeWar.clientSocket!=null)
                        {
                        	pack.type = ClientEvent.FIRE;
                        	transmitPacket(pack);
                        }
                }
                
        }
        
        
       
        
        public void transmitPacket(MazewarPacket packetToServer)
        {
        	ObjectOutputStream out;
			try {
				
				mazeWar.outputToServer.writeObject(packetToServer);
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        }
        
        
        
        /**
         * Handle a key release. Not needed by {@link GUIClient}.
         * @param e The {@link KeyEvent} that occurred.
         */
        public void keyReleased(KeyEvent e) {
        }
        
        /**
         * Handle a key being typed. Not needed by {@link GUIClient}.
         * @param e The {@link KeyEvent} that occurred.
         */
        public void keyTyped(KeyEvent e) {
        }

		
}
