import java.util.*;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class PeerProcess {
	private int peerId;

	// handle other peers that want to connect to us
	AcceptHandler serverConnections;

	// unknown if we need to keep track of the threads
	static Thread serverThread;
	static Vector<Thread> clientThreads = new Vector<Thread>();

	public void start(int id) {
		this.peerId = id;
		Logger.SetupLogger(id);
		Configs.loadConfigs();
		initializePeerDirectory();
		setupLocalPeer();
		connectToPreviousPeers();
		joinThreads();
	}

	public void connectToPreviousPeers() {
		for (int i = 0; i < Configs.peerInfo.size(); i++) {
			PeerInfo peer = Configs.peerInfo.get(i);
			// System.out.println(this.peerId + "r " + peer.peerId);
			if (peer.peerId == this.peerId) {
				// peer = Configs.peerInfo.get(i); // Check again in case the information was incorrect (and has been updated)
				return;
			}
			Logger.Debug("connecting to " + peer.peerId + " as " + this.peerId);
			connectToPeer(peer);
		}
	}

	public void setupLocalPeer() {
		for (int i = 0; i < Configs.peerInfo.size(); i++) {
			PeerInfo peer = Configs.peerInfo.get(i);
			if (peer.peerId == this.peerId) {
				ConnectionHandler.getInstance().setLocalPeer(peer);
				break;
			}
		}

		PieceHandler.getInstance().initBitfield();
		if (ConnectionHandler.getInstance().localPeer.hasEntireFile) {
			PieceHandler.getInstance().loadFile();
		} else {
			PieceHandler.getInstance().initEmptyBytes();
		}
		setupListening();
		ConnectionHandler.getInstance().determinePreferredNeighbors();
		ConnectionHandler.getInstance().optimisticallyUnchoke();
	}

	public void joinThreads() {
		try {
			PeerProcess.serverThread.join();
			for (int i = 0; i < clientThreads.size(); i++) {
				PeerProcess.clientThreads.get(i).join();
			}
		} catch (InterruptedException e) {
			System.out.println(e);
		}
	}

	// done once
	public void setupListening() {
		serverConnections = new AcceptHandler();
		serverConnections.port = ConnectionHandler.getInstance().localPeer.peerPort;
		PeerProcess.serverThread = new Thread(serverConnections);
		PeerProcess.serverThread.start();
	}

	// done per client to connect to
	public void connectToPeer(PeerInfo peerInfo) {
		try {
			String address = peerInfo.peerAddress;
			int port = peerInfo.peerPort;
			// TODO properly deal with if the socket already exists
			// the peerInfo is the peer to connect to
			Socket clientSocket = new Socket(address, port);
			Thread clientThread = ConnectionHandler.getInstance().createNewConnection(clientSocket, true);
			PeerProcess.clientThreads.add(clientThread);
		} catch (IOException e) {

		}

	}

	public void initializePeerDirectory() {
		File directory = new File("./peer_" + peerId);
		if (!directory.exists()) {
			directory.mkdir();
		}
	}
}
