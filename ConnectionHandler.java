import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

// thread safe singleton to handle connections
public class ConnectionHandler {

  private static volatile ConnectionHandler instance;
  private static Object mutex = new Object();

  // these are shared among all threads
  private ServerSocket serverSocket;
  private Map<Integer, PeerConnection> peersSockets = new HashMap<>();

  private ConnectionHandler() {
  }

  public static ConnectionHandler getInstance() {
    ConnectionHandler result = instance;
    if (result == null) {
      synchronized (mutex) {
        result = instance;
        if (result == null)
          instance = result = new ConnectionHandler();
      }
    }
    return result;
  }

  public void setServerSocket(ServerSocket ss) {
    this.serverSocket = ss;
  }

  public void savePeerSocket(int port, PeerConnection conn) {
    System.out.println("Saved " + peersSockets.keySet());
    peersSockets.put(port, conn);
  }

  public boolean peerConnectionExists(int port) {
    return peersSockets.containsKey(port);
  }

  public void determinePreferredNeighbors() {
    int numPreferredNeighbors = Integer.parseInt(Configs.commonConfig.get("NumberOfPreferredNeighbors"));
    TimerTask task = new TimerTask() {
      public void run() {
        if (PeerConnection.localPeer.hasEntireFile) {
          // randomly choose from those interested
          Vector<Integer> interestedPeers = getInterestedPeers();
          System.out.println("Got int " + interestedPeers.size());
          if (interestedPeers.size() > 0) {
            System.out.println("Int " + interestedPeers.size() + " ");
            ThreadLocalRandom.current().ints(0, interestedPeers.size()).distinct()
                .limit(Math.min(interestedPeers.size(), numPreferredNeighbors)).forEach((index) -> {
                  Integer randomPort = interestedPeers.get(index);
                  PeerConnection randomPeer = peersSockets.get(randomPort);
                  System.out.println("Unchoking peer " + randomPeer.otherPeer.peerId);
                  randomPeer.unchokeConnection();
                });
          }
        }

      }
    };

    int p = Integer.parseInt(Configs.commonConfig.get("UnchokingInterval"));
    beginRepeatedTimer(task, p);
  }

  public void updateNotInterested() {
    // when a new piece is received, go through neighbors and see if interested pieces > 0
    for (Map.Entry<Integer, PeerConnection> entry : peersSockets.entrySet()) {
      // Integer port = entry.getKey();
      PeerConnection conn = entry.getValue();

      if (conn.isChoked() == false
          && PieceHandler.getInstance().getRequestablePieces(conn.otherPeerBitfield).size() == 0) {
        conn.sendNotInterested();
      }
    }
  }

  public void optimisticallyUnchoke() {
    TimerTask task = new TimerTask() {
      public void run() {
      }
    };

    int p = Integer.parseInt(Configs.commonConfig.get("OptimisticUnchokingInterval"));
    beginRepeatedTimer(task, p);
  }

  private void beginRepeatedTimer(TimerTask task, int seconds) {
    Timer timer = new Timer("Timer");
    long delay = seconds * 1000L;
    timer.scheduleAtFixedRate(task, 0, delay);
  }

  // return a sorted list of socket numbers of the peers that have the highest
  // upload rate
  // public Vector<Integer> getDownloadRates() {
  // }

  public Vector<Integer> getInterestedPeers() {
    Vector<Integer> interestedPeers = new Vector<>();
    System.out.println("Int: " + peersSockets.entrySet());
    for (Map.Entry<Integer, PeerConnection> entry : peersSockets.entrySet()) {
      Integer port = entry.getKey();
      PeerConnection conn = entry.getValue();

      if (conn.otherPeerInterested) {
        interestedPeers.add(port);
      }
    }
    return interestedPeers;
  }

  public void close() {
    try {
      for (int i = 0; i < peersSockets.size(); i++) {
        peersSockets.get(i).close();
      }
      serverSocket.close();
    } catch (IOException e) {
    }
  }
}
