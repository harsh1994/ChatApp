import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

class Client {
	Socket requestSocket;           //socket connect to the server
	ObjectOutputStream out;         //stream write to the socket
 	ObjectInputStream in;          //stream read from the socket
	String message;                //message send to the server
	String MESSAGE;                //capitalized message read from the server
	int port;
	String name;
	public Client(String name,String port) {
		this.port = Integer.parseInt(port);
		this.name = name;
		String curr_dir = System.getProperty("user.dir");
		File f = new File(curr_dir+"\\"+name);
		try{
    		if(f.mkdir()) { 
        	// System.out.println("Directory Created");
    		} else {
        	// System.out.println("Directory is not created");
    		}
		} catch(Exception e){
    		e.printStackTrace();
			} 
	}

	void run()
	{
		boolean running = true;
		while(running){
		try{
			//create a socket to connect to the server
			requestSocket = new Socket("localhost", port);
			//initialize inputStream and outputStream
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			// out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			out.writeObject(name);
			out.flush();			
			//get Input from standard input
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			new getMessagesThread().start(); //listens messages from Server 
				
			while(true)
			{
				System.out.println("Please enter command as a string");
				//read a sentence from the standard input
				message = bufferedReader.readLine();
				String[] message_split = message.split(" ");
				//tell server if it is a message/file
				String indicator = message_split[1];
				sendMessage(indicator);
				// System.out.println(indicator);
				if(indicator.equals("message"))
				{
				//Send the sentence to the server
				sendMessage(message);
				System.out.println("Message sent");
				}
				else if(indicator.equals("file"))
				{
					sendMessage(message);
					sendFile(message_split[2]);
				}
				else
					System.out.println("Wrong Message format");

			}
		}
		catch (ConnectException e) {
    			System.err.println("Connection refused. You need to initiate a server first.");
		} 
//		catch ( ClassNotFoundException e ) {
//           		System.err.println("Class not found");
//        	} 
		catch(UnknownHostException unknownHost){
			System.err.println("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}
	}
	
	class getMessagesThread extends Thread
	{
		public void run(){
	//				System.out.println("I am listening");
			
			String line;
			try{
				while(true){
					line = (String)in.readObject();
					if(line.equals("xxx"))
					{
						String CURR_DIR = System.getProperty("user.dir").replace('\\','/');
						String cName = (String)in.readObject();
						String FILE_PATH = (String)in.readObject();
						String[] file_name_split = FILE_PATH.split("/");
						String file_name = file_name_split[file_name_split.length-1];
						byte[] file_in_bytes = (byte[])in.readObject();
						FileOutputStream fos = new FileOutputStream(CURR_DIR+"/"+name+"/"+file_name);
						BufferedOutputStream bos = new BufferedOutputStream(fos);
						bos.write(file_in_bytes);
						fos.close();
						bos.close();
						System.out.println("File Received from "+cName);
						System.out.println("Please enter command");
					}
					else
					{	
						System.out.println(line);
						System.out.println("Please enter command");
					}
				}
			}
				catch(Exception ex){}
				finally{
				}
		}
	}
	//send a message to the output stream
	void sendMessage(String msg)
	{
		try{
			//stream write the message
			out.writeObject(msg);
			out.flush();
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	//send a file to the output stream
	void sendFile(String file_path)
	{
		try
		{
		Path path = Paths.get(file_path);	
		// System.out.println(file_path);
		byte[] file_in_bytes = Files.readAllBytes(path);
		out.writeInt(file_in_bytes.length);
		out.flush();
		out.write(file_in_bytes);
		out.flush();
		out.writeObject(file_path);
		out.flush();
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	//main method
	public static void main(String args[])
	{
		Client client = new Client(args[0],args[1]);
		client.run();
	}

}
