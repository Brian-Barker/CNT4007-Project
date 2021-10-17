import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

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
    peersSockets.put(port, conn);
  }

  public boolean peerConnectionExists(int port) {
    return peersSockets.containsKey(port);
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
