import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Date;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
  public PeerInfo localPeer;
  private ServerSocket serverSocket;
  private Map<Integer, PeerConnection> peersSockets = new HashMap<>();
  private Integer currentOptimisticallyUnchokedNeighbor = 0;

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

  public Thread createNewConnection(Socket socket, boolean connectionCreatedFromLocalPeer) {
    ConnectionListener clientConnection = new ConnectionListener();
    clientConnection.connectionFromExistingSocket(socket, connectionCreatedFromLocalPeer);
    Thread t1 = new Thread(clientConnection);
    t1.start();
    return t1;
  }

  public void setServerSocket(ServerSocket ss) {
    this.serverSocket = ss;
  }

  public void savePeerSocket(int peerId, PeerConnection conn) {
    System.out.println("Saved " + peersSockets.keySet());
    peersSockets.put(peerId, conn);
  }

  public boolean peerConnectionExists(int port) {
    return peersSockets.containsKey(port);
  }

  public void determinePreferredNeighbors() {
    int numPreferredNeighbors = Configs.getPreferredNeighbors();
    TimerTask task = new TimerTask() {
      public void run() {
        Vector<Integer> currentlyUnchoked = getPeersFromChokedState(false);
        Map<Integer, Boolean> shouldBeUnchoked = new HashMap<>();
        
        if (ConnectionHandler.getInstance().localPeer.hasEntireFile) {
          // randomly choose from those interested
          Vector<Integer> interestedPeers = getInterestedPeers();
          System.out.println("Got int " + interestedPeers.size());
          if (interestedPeers.size() > 0) {
            ThreadLocalRandom.current().ints(0, interestedPeers.size()).distinct()
                .limit(Math.min(interestedPeers.size(), numPreferredNeighbors)).forEach((index) -> {
                  Integer randomId = interestedPeers.get(index);
                  PeerConnection randomPeer = peersSockets.get(randomId);
                  if (randomPeer.isChoked()) {
                    System.out.println("Unchoking peer " + randomPeer.otherPeerId);
                    randomPeer.unchokeConnection();
                  }
                  shouldBeUnchoked.put(randomId, true);
                });
          }
        } else {
          // calculate download rates
          Vector<Integer> interestedPeers = getInterestedPeers();

          Collections.sort(interestedPeers, new Comparator<Integer>() {

            @Override
            public int compare(Integer p1, Integer p2) {
              PeerConnection peer1 = peersSockets.get(p1);
              PeerConnection peer2 = peersSockets.get(p2);
              float peer1DownloadRate = peer1.previousIntervalDownloadRate();
              float peer2DownloadRate = peer2.previousIntervalDownloadRate();

              return Float.compare(peer1DownloadRate, peer2DownloadRate);
            }
          });

          // interestedPeers is now sorted in descending order of download rates, so limit the number of preferred neighbors
          // and choose the first N from the list
          int numNeighbors = Math.min(interestedPeers.size(), Configs.getPreferredNeighbors());
          for (int i = 0; i < numNeighbors; i++) {
            Integer peerId = interestedPeers.get(i);
            PeerConnection conn = peersSockets.get(peerId);
            // float downloadRate = conn.previousIntervalDownloadRate();

            // unchoke this neighbor
            if (conn.isChoked()) {
              conn.unchokeConnection();
            }
            shouldBeUnchoked.put(peerId, true);
          }
        }

        // For logging
        Set<Integer> keys = shouldBeUnchoked.keySet();
        int[] newPreferredNeighbors = new int[keys.size()];
        int index = 0;
        int localId = ConnectionHandler.getInstance().localPeer.peerId;
        for(Integer element : keys) array[index++] = element.intValue();
        Logger.LogPreferredNeighbors(localId, newPreferredNeighbors);


        // now choke the neighbors that are not in unchoked
        for (int i = 0; i < currentlyUnchoked.size(); i++) {
          Integer peerId = currentlyUnchoked.get(i);
          if (shouldBeUnchoked.containsKey(peerId) == false) {
            // this peer is unchoked but it shouldnt be, choke it
            PeerConnection conn = peersSockets.get(peerId);
            conn.chokeConnection();
          }
        }
        resetDownloadRates();
      }
    };

    float p = Configs.getUnchokingInterval();
    beginRepeatedTimer(task, p);
  }

  public void updateNotInterested() {
    // when a new piece is received, go through neighbors and see if interested pieces > 0
    for (Map.Entry<Integer, PeerConnection> entry : peersSockets.entrySet()) {
      PeerConnection conn = entry.getValue();

      if (conn.isChoked() == false && PieceHandler.getInstance().getRequestablePieces(conn.otherPeerBitfield).size() == 0) {
        conn.sendNotInterested();
      }
    }
  }

  public void optimisticallyUnchoke() {
    TimerTask task = new TimerTask() {
      public void run() {
        // TODO verify if this is correct, this was made with heavy Copilot use at 5am

        // get neighbors that are currently choked
        Vector<Integer> currentlyChoked = getPeersFromChokedState(true);
        // get neighbors that are currently interested in this peers pieces
        Vector<Integer> interestedNeighbors = getInterestedPeers();
        // get the elements that are in both vectors
        Vector<Integer> intersection = new Vector<>();
        for (int i = 0; i < currentlyChoked.size(); i++) {
          Integer neighborId = currentlyChoked.get(i);
          if (interestedNeighbors.contains(neighborId)) {
            intersection.add(neighborId);
          }
        }
        // randomly choose one of the elements from the intersection
        if (intersection.size() > 0) {
          int index = ThreadLocalRandom.current().nextInt(0, intersection.size());
          Integer neighborId = intersection.get(index);
          PeerConnection conn = peersSockets.get(neighborId);
          conn.unchokeConnection();
          currentOptimisticallyUnchokedNeighbor = neighborId;

          int localId = ConnectionHandler.getInstance().localPeer.peerId;
          Logger.LogOptimisticallyUnchokedNeighbor(localId, neighborId);
        }

      }
    };

    float p = Configs.getOptimisticUnchokingInterval();
    beginRepeatedTimer(task, p);
  }

  private void beginRepeatedTimer(TimerTask task, float seconds) {
    Timer timer = new Timer("Timer");
    long delay = (long) seconds * 1000L;
    timer.scheduleAtFixedRate(task, 0, delay);
  }

  public void resetDownloadRates() {
    for (Map.Entry<Integer, PeerConnection> entry : peersSockets.entrySet()) {
      PeerConnection conn = entry.getValue();
      conn.newUnchokingInterval();
    }
  }

  public Vector<Integer> getInterestedPeers() {
    Vector<Integer> interestedPeers = new Vector<>();
    System.out.println("Int: " + peersSockets.entrySet());
    for (Map.Entry<Integer, PeerConnection> entry : peersSockets.entrySet()) {
      Integer peerId = entry.getKey();
      PeerConnection conn = entry.getValue();

      if (conn.otherPeerInterested) {
        interestedPeers.add(peerId);
      }
    }
    return interestedPeers;
  }

  public void sendHavesToAllOtherPeers(int peerIdToNotSendTo, int pieceIndex) {
    for (Map.Entry<Integer, PeerConnection> entry : peersSockets.entrySet()) {
      // Integer peerId = entry.getKey();
      PeerConnection conn = entry.getValue();

      if (conn.otherPeerId != peerIdToNotSendTo) {
        conn.sendHave(pieceIndex);
      }
    }
  }

  // TODO can use hash for this
  public Vector<Integer> getPeersFromChokedState(boolean isChoked) {
    Vector<Integer> unchokedPeers = new Vector<>();
    for (Map.Entry<Integer, PeerConnection> entry : peersSockets.entrySet()) {
      Integer peerId = entry.getKey();
      PeerConnection conn = entry.getValue();

      if (conn.isChoked() == isChoked) {
        unchokedPeers.add(peerId);
      }
    }
    return unchokedPeers;
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

  public void setLocalPeer(PeerInfo p) {
    ConnectionHandler.getInstance().localPeer = p;
  }
}
