import java.util.*;
import java.io.File;

public class PeerProcess {
	// private static final int port = 6008;
	private int peerId;

	// handle other peers that want to connect to us
	ConnectionListener serverConnections;

	// unknown if we need to keep track of the threads
	Thread serverThread;
	Vector<Thread> clientThreads = new Vector<Thread>();

	public void start(int id) {
		this.peerId = id;
		Configs.loadConfigs();
		initializePeerDirectory();
		connectToPreviousPeers();
		joinThreads();
	}

	public void connectToPreviousPeers() {
		for (int i = 0; i < Configs.peerInfo.size(); i++) {
			PeerInfo peer = Configs.peerInfo.get(i);
			// System.out.println(this.peerId + "r " + peer.peerId);
			if (peer.peerId == this.peerId) {
				setupLocalPeer(peer);
				peer = Configs.peerInfo.get(i); // Check again in case the information was incorrect (and has been updated)
				return;
			}
			System.out.println("connecting to " + peer.peerId + " as " + this.peerId);
			connectToPeer(peer);
		}
	}

	public void setupLocalPeer(PeerInfo peer) {
		PeerConnection.setLocalPeer(peer);

		PieceHandler.getInstance().initBitfield();
		if (peer.hasEntireFile) {
			PieceHandler.getInstance().loadFile();
		} else {
			PieceHandler.getInstance().initEmptyBytes();
		}
		setupListening(peer);
		ConnectionHandler.getInstance().determinePreferredNeighbors();
		ConnectionHandler.getInstance().optimisticallyUnchoke();
	}

	public void joinThreads() {
		try {
			serverThread.join();
			for (int i = 0; i < clientThreads.size(); i++) {
				clientThreads.get(i).join();
			}
		} catch (InterruptedException e) {
			System.out.println(e);
		}
	}

	// done once
	public void setupListening(PeerInfo thisPeer) {
		serverConnections = new ConnectionListener();
		serverConnections.peerInfo = thisPeer;
		serverConnections.server = true;
		serverThread = new Thread(serverConnections);
		serverThread.start();
	}

	// done per client to connect to
	public void connectToPeer(PeerInfo peerInfo) {
		ConnectionListener clientConnection = new ConnectionListener();
		clientConnection.peerInfo = peerInfo;
		clientConnection.server = false;
		Thread t1 = new Thread(clientConnection);
		t1.start();
		clientThreads.addElement(t1);
	}

	public void initializePeerDirectory() {
		File directory = new File("./peer_" + peerId);
		if (!directory.exists()) {
			directory.mkdir();
		}
	}
}
