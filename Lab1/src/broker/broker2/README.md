This is very similar to the first part except now there is an exchange client which connects to the broker service and updates/adds/removes entries to the nasdaq file. To run is as follows:

- server.sh <port number>  - starts the broker service
- client.sh <broker host name> <broker port number> - starts the client interface for querying quotes
- exchange.sh <broker host name> <broker port number> - starts the exchange interface for modifying the nasdaq file
