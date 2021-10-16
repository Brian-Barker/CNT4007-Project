import java.util.*;

public class PeerProcess {
	// private static final int port = 6008;
	private String peerId;
	Map<String, String> commonConfig;
	Vector<RemotePeerInfo> peerInfo;
	// handle other peers that want to connect to us
	ConnectionListener serverConnections;

	// unknown if we need to keep track of the threads
	Thread serverThread;
	Vector<Thread> clientThreads = new Vector<Thread>();

	public void start(String id) {
		this.peerId = id;
		commonConfig = ConfigReader.getCommonConfig("Common.cfg");
		// common.forEach((key, value) -> System.out.println(key + ":" + value));
		peerInfo = ConfigReader.getPeerList("PeerInfo.cfg");
		connectToPreviousPeers();
		joinThreads();
	}

	public void connectToPreviousPeers() {
		for (int i = 0; i < peerInfo.size(); i++) {
			RemotePeerInfo peer = peerInfo.get(i);
			// System.out.println(this.peerId + "r " + peer.peerId);
			if (peer.peerId.equals(this.peerId)) {
				setupListening(peer);
				return;
			}
			System.out.println("connecting to " + peer.peerId + " as " + this.peerId);
			connectToPeer(peer);
		}
	}

	public void joinThreads() {
		try {
			serverThread.join();
			for (int i = 0; i < clientThreads.size(); i++) {
				clientThreads.get(i).join();
			}
		} catch (InterruptedException e) {

		}
	}

	public void setupListening(RemotePeerInfo thisPeer) {
		serverConnections = new ConnectionListener();
		serverConnections.peerInfo = thisPeer;
		serverConnections.server = true;
		serverThread = new Thread(serverConnections);
		serverThread.start();
	}

	public void connectToPeer(RemotePeerInfo peerInfo) {
		ConnectionListener clientConnection = new ConnectionListener();
		clientConnection.peerInfo = peerInfo;
		clientConnection.server = false;
		Thread t1 = new Thread(clientConnection);
		t1.start();
		clientThreads.addElement(t1);
	}

	/*
	 * A handler thread class.
	 */

	// private static class Handler extends Thread {
	// private Socket connection;
	// private ObjectInputStream in; //stream read from the socket
	// private ObjectOutputStream out; //stream write to the socket
	// private String peerId; //The index number of the client

	// public Handler(Socket connection, String peerId) {
	// this.connection = connection;
	// this.peerId = peerId;
	// }

	// public void run() {
	// try{
	// //initialize Input and Output streams
	// out = new ObjectOutputStream(connection.getOutputStream());
	// out.flush();
	// in = new ObjectInputStream(connection.getInputStream());
	// try{
	// while(true)
	// {
	// //receive the message sent from the client
	// message = (String)in.readObject();
	// //show the message to the user
	// System.out.println("Receive message: " + message + " from client " + no);
	// //Capitalize all letters in the message
	// MESSAGE = message.toUpperCase();
	// //send MESSAGE back to the client
	// sendMessage(MESSAGE);
	// }
	// }
	// catch(ClassNotFoundException classnot){
	// System.err.println("Data received in unknown format");
	// }
	// }
	// catch(IOException ioException){
	// System.out.println("Disconnect with Client " + no);
	// }
	// finally{
	// //Close connections
	// try{
	// in.close();
	// out.close();
	// connection.close();
	// }
	// catch(IOException ioException){
	// System.out.println("Disconnect with Client " + no);
	// }
	// }
	// }
	// }

}