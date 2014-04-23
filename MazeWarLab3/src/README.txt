To run:

1. Run the NamingService.java file using :

	java NamingService  <portNumber>
	
2. Run 4 different Mazewar clients using:

	java Mazewar <Ip address of Naming service> <port of Naming service>
	
	
	
How does it work?

1. The 4 clients register on the naming service. The NS also contains knowledge of which player is who's neighbour in a token ring formation.
2. A token is sent out by the naming service to the first player to register. 
3. Whenever a player makes a move, one of 2 events happen:
	- if the player has the token then the move is sent out as an action packet which is forwarded to each client. So A passes to B which passes to C and then back to A. When a player receives the packet
	  it displays the action, when the player that sent out the packet gets it back, it too displays the action. 
	- if the player doesn't have the token, their move is enqueued into their action queue.
4. If a player receives a token , then the head of the move (action) queue is dequeued and passed along as above.
5. If there are no moves in the queue at present then the token is passed to the next player.

Pros:
1. Low latency in case of 4 players.
2. Distributed system not having single point of failure + performance bottleneck of central server removed

Cons:
1. Doesn't scale well to a large number of players. With hundreds of players with different bandwidths the token movement might not be that clear cut and their would likely be sudden starts and jumps in the
game screens for different players.
