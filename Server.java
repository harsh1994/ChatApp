import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

class Server {

	private static int sPort;   //The server will be listening on this port number
	private static HashSet<String> clientList = new HashSet<String>();
	private static Hashtable<String,ObjectOutputStream> outStreams = new Hashtable<String,ObjectOutputStream>();
	public static void main(String[] args) throws Exception {
		System.out.println("The server is running."); 
		sPort = Integer.parseInt(args[0]);
        	ServerSocket listener = new ServerSocket(sPort);
//		int clientNum = 1;
        	try {
            		while(true) {
                		new Handler(listener.accept()).start();
				
//				System.out.println("Client "  + clientNum + " is connected!");
//				clientList.add(""+clientNum);
//				clientNum++;
            			}
        	} finally {
            		listener.close();
        	} 
 
    	}

	/**
     	* A handler thread class.  Handlers are spawned from the listening
     	* loop and are responsible for dealing with a single client's requests.
     	*/
    	private static class Handler extends Thread {
        private String message;    //message received from the client
		private String MESSAGE;    //uppercase message send to the client
		private Socket connection;
        private ObjectInputStream in;	//stream read from the socket
        private ObjectOutputStream out;    //stream write to the socket
		private String cName;		//The name of the client

        	public Handler(Socket connection) {
            		this.connection = connection;
        	}

        public void run() {
 		try{
			//initialize Input and Output streams
			in = new ObjectInputStream(connection.getInputStream());
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			try{	
			cName = (String)in.readObject();
			clientList.add(cName);
			outStreams.put(cName,out);
			System.out.println("Client "  + cName + " is connected!");

				while(true)
				{
					//receive the message/file indicator from the client
					String indicator = (String)in.readObject();
					if(indicator.equalsIgnoreCase("message"))
					{
					//receive the message sent from the client
					message = (String)in.readObject();
					if(message==null) return;
					
					StringBuilder sb = new StringBuilder();
					String[] stringArray = message.split(" ");

					//broadcast message
					if(stringArray[0].equalsIgnoreCase("broadcast"))
					{
						
					for(int i=2;i<stringArray.length;i++)
					{
						sb.append(stringArray[i]);
						sb.append(" ");
					}
					String outputmessage=sb.toString();
						
					System.out.println(cName+" broadcasted");
					Enumeration e = outStreams.elements();
					while(e.hasMoreElements())
					{
						ObjectOutputStream os = (ObjectOutputStream)e.nextElement();
						if(!os.equals(out)) 
							os.writeObject("@"+cName+": "+outputmessage);						
						
						os.flush();
					}	
					}
					
					//unicast message
					if(stringArray[0].equalsIgnoreCase("unicast"))
					{
					
					for(int i=2;i<stringArray.length-1;i++)
					{
						sb.append(stringArray[i]);
						sb.append(" ");
					}
					String outputmessage=sb.toString();
					String receiver = stringArray[stringArray.length-1];
					System.out.println(cName+" unicast message to ["+receiver+"]");
					if(outStreams.get(receiver)!=null && !receiver.equals(cName))
					{
						outStreams.get(receiver).writeObject("@"+cName+": "+outputmessage);	
			}			outStreams.get(receiver).flush();
					}
					
					//blockcast message
					if(stringArray[0].equalsIgnoreCase("blockcast"))
					{
					
					for(int i=2;i<stringArray.length-1;i++)
					{
						sb.append(stringArray[i]);
						sb.append(" ");
					}
					String outputmessage=sb.toString();

					String excluded = stringArray[stringArray.length-1];						

					System.out.println(cName+" blockcast message excluding ["+excluded+"]");
					for(String client:clientList)
					{
						if(!client.equals(cName) && !client.equals(excluded))
						{
							ObjectOutputStream os = outStreams.get(client);
							os.writeObject("@"+cName+":"+outputmessage);	
							os.flush();
						}
					}
					}
				}
				else if(indicator.equalsIgnoreCase("file"))
				{
					// System.out.println(indicator);
					// System.out.println("File procedure started");
					message = (String)in.readObject();
					String[] message_split = message.split(" ");
					// System.out.println(message);
					// out.flush();
					int size = in.readInt();
					// System.out.println("File size " + size);
					byte[] file_in_bytes = new byte[size];
					in.readFully(file_in_bytes,0,size);
					String file_path = (String)in.readObject();
					// System.out.println((file_path));
					//file broadcast
					if(message_split[0].equalsIgnoreCase("broadcast"))
					{
						for(String key: outStreams.keySet())
						{
							// System.out.println(key);
							if(!key.equals(cName))
							{
							outStreams.get(key).writeObject("xxx");
							outStreams.get(key).flush();
							outStreams.get(key).writeObject(cName);
							outStreams.get(key).flush();
							outStreams.get(key).writeObject(file_path);
							outStreams.get(key).flush();
							outStreams.get(key).writeObject(file_in_bytes);
							outStreams.get(key).flush();
							}
						}
						System.out.println(cName+" broadcasted files ");
					}
					//file unicast
					else if(message_split[0].equalsIgnoreCase("unicast"))
					{
						String client = message_split[3];
						// System.out.println(client);
						out = outStreams.get(client);
						out.writeObject("xxx");
						out.flush();
						out.writeObject(cName);
						out.flush();
						out.writeObject(file_path);
						out.flush();
						out.writeObject(file_in_bytes);
						out.flush();
						System.out.println(cName + " unicasted file to "+ client);
					}
					else if(message_split[0].equalsIgnoreCase("blockcast"))			//blockcast files
					{
						String client_to_block = message_split[3];
						out = outStreams.get(client_to_block);

						for(String key:outStreams.keySet())
						{
							if(!(key.equals(client_to_block)||key.equals(cName)))
							{
							outStreams.get(key).writeObject("xxx");
							outStreams.get(key).flush();
							outStreams.get(key).writeObject(cName);
							outStreams.get(key).flush();
							outStreams.get(key).writeObject(file_path);
							outStreams.get(key).flush();
							outStreams.get(key).writeObject(file_in_bytes);
							outStreams.get(key).flush();
							}
						}
						System.out.println(cName +" blockcasted "+ client_to_block);
					}																
					//Invalid file transfer command
					else
					{
						System.out.println("Invalid command!");
					}
				}
				else
					System.out.println("Incorrect command!");
				}
			}
			catch(ClassNotFoundException classnot){
					System.err.println("Data received in unknown format");
				}
		}
		catch(IOException ioException){
			System.out.println("Disconnect with Client " + cName);
		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				connection.close();
				System.out.println("Client "  + cName + " is disconnected!");

			}
			catch(IOException ioException)
			{
				System.out.println("Disconnect with Client " + cName);
			}
		}
	}

	//send a message to the output stream
	public void sendMessage(String msg)
	{
		try{
			out.writeObject(msg);
			out.flush();
			System.out.println("Send message: " + msg + " to Client " + cName);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}

    }
}
