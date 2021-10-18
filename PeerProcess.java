import java.util.*;
import java.io.File;

public class PeerProcess {
	// private static final int port = 6008;
	private int peerId;
	Map<String, String> commonConfig;
	Vector<PeerInfo> peerInfo;
	// handle other peers that want to connect to us
	ConnectionListener serverConnections;

	// unknown if we need to keep track of the threads
	Thread serverThread;
	Vector<Thread> clientThreads = new Vector<Thread>();

	public void start(int id) {
		this.peerId = id;
		commonConfig = ConfigReader.getCommonConfig("Common.cfg");
		initializePeerDirectory();
		// common.forEach((key, value) -> System.out.println(key + ":" + value));
		peerInfo = ConfigReader.getPeerList("PeerInfo.cfg");

		connectToPreviousPeers();
		joinThreads();
	}

	public void connectToPreviousPeers() {
		for (int i = 0; i < peerInfo.size(); i++) {
			PeerInfo peer = peerInfo.get(i);
			// System.out.println(this.peerId + "r " + peer.peerId);
			if (peer.peerId == this.peerId) {
				setupLocalPeer(peer);
				return;
			}
			System.out.println("connecting to " + peer.peerId + " as " + this.peerId);
			connectToPeer(peer);
		}
	}

	public void setupLocalPeer(PeerInfo peer) {
		PieceHandler.getInstance().initBitfield(commonConfig);
		if (peer.hasEntireFile) {
			PieceHandler.getInstance().loadFile(peerId);
		}
		PeerConnection.setLocalPeer(peer);
		setupListening(peer);
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
		peerDirectory = directory;
	}
}
