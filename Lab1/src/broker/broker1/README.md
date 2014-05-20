This is a simple Client-Broker Service. It can be run as follows:

- For the Broker service, the file server.sh needs to be executed along with 1 command line parameter specifying the port the server will be listening in on.
- For the Client interface, the file client.sh needs to be executed along with 2 command line parameters specifying the hostname and port number on which the Broker service is running.

Thereafter, you will be greeted with a prompt ">" at which you have to specify the company for whom you want a quote to be retrieved and then the server will return the value accordingly.
For a company name not listed in the "nasdaq" file the quote value returned will be 0. The nasdaq file can easily be edited to contain more companies and respective quotes.

Additions of companies through client interface is done in the next part of the assignment i.e broker2.
