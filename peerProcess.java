import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class peerProcess {
	private static final int port = 6008;
	private static String id;
	
	public void main(String[] args) 
	{
		try
		{
			if (!args[0].matches("\\d\\d\\d\\d")) 
			{
				throw new Exception("Error: Invalid peerId: " + args[0]);
			}
			this.id = args[0];
			
			
			System.out.println("Peer " + id + " is running!");
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		
	}
	
	public void initiateConnectionWithPeer(int peerId) 
	{
		
	}
	
	public void receiveConnectionFromPeer() 
	{
		
	}
	
	/**
	* A handler thread class.  Handlers are spawned from the listening
	* loop and are responsible for dealing with a single peer's requests.
	*/
	private static class Handler extends Thread {
		private Socket connection;
		private ObjectInputStream in;	//stream read from the socket
		private ObjectOutputStream out;    //stream write to the socket
		private String peerId;		//The index number of the client

		public Handler(Socket connection, String peerId) {
			this.connection = connection;
			this.no = no;
		}

        public void run() {
 		try{
			//initialize Input and Output streams
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());
			try{
				while(true)
				{
					//receive the message sent from the client
					message = (String)in.readObject();
					//show the message to the user
					System.out.println("Receive message: " + message + " from client " + no);
					//Capitalize all letters in the message
					MESSAGE = message.toUpperCase();
					//send MESSAGE back to the client
					sendMessage(MESSAGE);
				}
			}
			catch(ClassNotFoundException classnot){
					System.err.println("Data received in unknown format");
				}
		}
		catch(IOException ioException){
			System.out.println("Disconnect with Client " + no);
		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				connection.close();
			}
			catch(IOException ioException){
				System.out.println("Disconnect with Client " + no);
			}
		}
	}
	
}