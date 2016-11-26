CNT5106c (Computer Networks) Project 

The Single server multiple client application enables the clients to communicate and share files with each other. Users send messages and files using the Server which decodes the user input. Users can broadcast, blockcast or unicast messages and broadcast or unicast files.
To run the project, start multiple command prompts, one for server at the beginning and rest for clients. Enter "java Server *portNo*" to start the server. Then enter "java Client clientName *portNo*" for each client.

Following command formats can be used by the clients-
Broadcast message - broadcast message "Your Message"
Blockcast message - blockcast message "Your Message" except clientName
Unicast message - unicast message "Your Message" clientName
Broadcast file - broadcast file FILE_PATH
Unicast file - unicast file FILE_PATH clientName
Blockcast file - blockcast file FILE_PATH clientName

Built With
Text Editors- Sublime, Notepad++
Java Socket programming concepts
