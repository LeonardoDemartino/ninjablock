# ninjablock
A blockchain disk parser and utilities to work with bitcoin web services.

I have made this to learn how the blockchain is stored, after implementing the ninja_readBlocksFromDisk
I started to add support for querying transactions, addresses and other stuffs from web services (blockchain.info, blockcypher.com)

TODO:
1) Add the webservices in a separate class to make the query more abstract.
2) Make the process of adding more webservices easy inside that class.
3) Create an RPC connection to a full node as a "webservice" too and add it to the webservices class (point 1).
4) ...
