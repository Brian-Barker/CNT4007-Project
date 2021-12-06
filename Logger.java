import java.io.FileWriter;
import java.io.IOException;

public class Logger {

  // file writer variable
  private static FileWriter fileWriter;

  public static void SetupLogger(int peerId) {
    // create log file
    String logFileName = "log_peer_" + peerId + ".log";
    try {
      fileWriter = new FileWriter(logFileName);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // log TCP connection message to file
  public static void LogTCPConnection(int peerId, int otherPeer) {
    Write(p(peerId)+" makes a connection to "+p(otherPeer));
  }

  // log other peer connection to this peer
  public static void LogOtherPeerConnection(int peerId, int otherPeer) {
    Write(p(peerId)+" is connected from "+p(otherPeer));
  }

  // log the change of preferred neighbors
  public static void LogPreferredNeighbors(int peerId, int[] preferredNeighbors) {
    String preferredNeighborsString = " has preferred neighbors ";
    for (int i = 0; i < preferredNeighbors.length; i++) {
      preferredNeighborsString += preferredNeighbors[i];
      if (i != preferredNeighbors.length - 1) {
        preferredNeighborsString += ",";
      }
    }
    Write(p(peerId)+preferredNeighborsString);
  }

  // log change of optimistically unchoked neighbor
  public static void LogOptimisticallyUnchokedNeighbor(int peerId, int optimisticallyUnchokedNeighbor) {
    Write(p(peerId) + " has the optimistically unchoked neighbor " + p(optimisticallyUnchokedNeighbor));
  }

  // log unchoking of a neighbor
  public static void LogUnchoking(int peerId, int neighbor) {
    Write(p(peerId) + " is unchoked by " + p(neighbor));
  }

  // log choking of a neighbor
  public static void LogChoking(int peerId, int neighbor) {
    Write(p(peerId) + " is choked by " + p(neighbor));
  }

  // log receiving have message
  public static void LogReceiveHave(int peerId, int otherPeer, int pieceIndex) {
    Write(p(peerId) + " received a 'have' message from " + p(otherPeer) + " for piece " + pieceIndex);
  }

  // receive interested message
  public static void LogReceiveInterested(int peerId, int otherPeer) {
    Write(p(peerId) + " received the 'interested' message from " + p(otherPeer));
  }

  // receive not interested message
  public static void LogReceiveNotInterested(int peerId, int otherPeer) {
    Write(p(peerId) + " received the 'not interested' message from " + p(otherPeer));
  }

  // log finishing downloading a piece
  public static void LogFinishDownloadingPiece(int peerId, int otherPeer, int pieceIndex, int totalPieces) {
    Write(p(peerId) + "has downloaded the piece " + pieceIndex + " from " + p(otherPeer) + ". Now the number of pieces it has is " + totalPieces);
  }

  // log completion of download
  public static void LogDownloadComplete(int peerId) {
    Write(p(peerId) + " has downloaded the complete file.");
  }

  // not in spec
  public static void LogPeerDisconnected(int peerId, int otherPeer) {
    Write(p(peerId)+" disconnected from "+p(otherPeer));
  }

  public static void Write(String rawMessage) {
    try {
      String message = getDateTime() + ": " + rawMessage + "\n";
      System.out.print(message);
      fileWriter.write(message);
      fileWriter.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void Debug(String rawMessage) {
    if (true) {
      System.out.println(rawMessage);
    }
  }

  // helper to format peer id string
  public static String p(int peerId) {
    return "[" + peerId + "]";
  }

  // get date time string
  public static String getDateTime() {
    // returns in format [yyyy-MM-dd HH:mm:ss]
    String s = new java.util.Date().toString();
    return "[" + s + "]";
  }
}
