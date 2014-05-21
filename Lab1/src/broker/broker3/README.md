The 3rd part introduces a LookupService and another Broker. Now, each broker will register their ip and port with the lookup service on initialization. After that, a client can choose which broker to connect to by enterin g "local <broker name>" at the prompt. This will trigger a fresh connection to the lookup server which will then return the details of the said Broker and the client will then be able to
connect to that Broker and query from it. Assuming both brokers are active, request forwarding is also handled. So if something is not present in nasdaq but in tse then the nasdaq broker will forward the request to the tse server and then get the response and push it to the client. The exchange client will similarly connect to the specific broker and do the necessary modifications.

To run:

- lookup.sh <port> - Need to run this first to start the look up server
- server.sh <lookup host> <lookup port> <port> <brokername> - the host and port for the lookup server, the port the broker will be running on and the name of the broker - either tse or nasdaq
- client.sh <lookup host> <lookup port> - to connect to said lookup server
- exchange.sh <lookup host> <lookup  port> <brokername> - to query broker from lookup server and then modify corress. entries for that broker

The system is not entirely robust for example, if a broker dies midway, there is no way to remove it from the lookup and that will trigger an exception on the client side since it won't be able to connect to the broker. This can be taken care of using periodic heartbeats from the lookup. If there is a timeout, the entry will be removed from the lookup.The client can then be given an appropriate response message. 
Another possible issue is multiple nasdaq and tse servers. This can be handled by storing them with signature names e.g nasdaq1,nasdaq2 etc on the lookup. Then based on load at a particular nasdaq server,incoming requests can be "balanced out" by spreading them across different brokers.