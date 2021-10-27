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
  public static void LogTCPConnection(int peerId, int otherPeer, String message) {
    try {
      fileWriter.write(getDateTime() + ": Peer " + peerId + ": makes a connection to Peer " + otherPeer + "\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // log other peer connection to this peer
  public static void LogOtherPeerConnection(int peerId, int otherPeer, String message) {
    try {
      fileWriter.write(getDateTime() + ": Peer " + peerId + " is connected from Peer " + otherPeer + "\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // log the change of preferred neighbors
  public static void LogPreferredNeighbors(int peerId, int[] preferredNeighbors) {
    try {
      fileWriter.write(getDateTime() + ": Peer " + peerId + " has preferred neighbors ");
      for (int i = 0; i < preferredNeighbors.length; i++) {
        fileWriter.write(preferredNeighbors[i] + ",");
      }
      fileWriter.write("\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // log change of optimistically unchoked neighbor
  public static void LogOptimisticallyUnchokedNeighbor(int peerId, int optimisticallyUnchokedNeighbor) {
    try {
      fileWriter.write(getDateTime() + ": Peer " + peerId + " has the optimistically unchoked neighbor "
          + optimisticallyUnchokedNeighbor + "\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // log unchoking of a neighbor
  public static void LogUnchoking(int peerId, int neighbor) {
    try {
      fileWriter.write(getDateTime() + ": Peer " + peerId + " is unchoked by " + neighbor + "\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // log choking of a neighbor
  public static void LogChoking(int peerId, int neighbor) {
    try {
      fileWriter.write(getDateTime() + ": Peer " + peerId + " is choked by " + neighbor + "\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // log receiving have message
  public static void LogReceiveHave(int peerId, int otherPeer, int pieceIndex) {
    try {
      fileWriter.write(getDateTime() + ": Peer " + peerId + " received the 'have' message from " + otherPeer
          + " for piece " + pieceIndex + "\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // receive interested message
  public static void LogReceiveInterested(int peerId, int otherPeer) {
    try {
      fileWriter
          .write(getDateTime() + ": Peer " + peerId + " received the 'interested' message from " + otherPeer + "\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // receive not interested message
  public static void LogReceiveNotInterested(int peerId, int otherPeer) {
    try {
      fileWriter.write(
          getDateTime() + ": Peer " + peerId + " received the 'not interested' message from " + otherPeer + "\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // log finishing downloading a piece
  public static void LogFinishDownloadingPiece(int peerId, int otherPeer, int pieceIndex, int totalPieces) {
    try {
      fileWriter.write(getDateTime() + ": Peer " + peerId + " has downloaded the piece " + pieceIndex + " from "
          + otherPeer + ". Now the number of pieces it has is " + totalPieces + "\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // log completion of download
  public static void LogDownloadComplete(int peerId) {
    try {
      fileWriter.write(getDateTime() + ": Peer " + peerId + " has downloaded the complete file\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void log(String message) {
    // get the current date and time
    String dateTime = new java.util.Date().toString();
    // log the message to file
    try {
      fileWriter.write(dateTime + ": " + message + "\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void log(String message, Object... args) {
    System.out.println(String.format(message, args));
  }

  public static void log(Throwable t) {
    t.printStackTrace();
  }

  public static void log(Throwable t, String message) {
    System.out.println(message);
    t.printStackTrace();
  }

  public static void log(Throwable t, String message, Object... args) {
    System.out.println(String.format(message, args));
    t.printStackTrace();
  }

  // get date time string
  public static String getDateTime() {
    // returns in format [yyyy-MM-dd HH:mm:ss]
    String s = new java.util.Date().toString();
    return "[" + s + "]";
  }
}
