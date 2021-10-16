import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.HashMap;
/*
  Where the server socket lives to listen to new connections
  on a new thread


  using implements Runnable instead of extend Thread for
  for flexibility and sharing resources

*/

public class ConnectionListener implements Runnable {
  private ServerSocket serverSocket;
  private Map<Integer, PeerConnection> peersSockets = new HashMap<>();
  public RemotePeerInfo peerInfo;
  public boolean server = false;

  public void run() {
    try {
      int port = Integer.parseInt(peerInfo.peerPort);
      if (port > 0 && server) {
        // server code
        serverSocket = new ServerSocket(port);

        while (true) {
          Socket client = serverSocket.accept();
          PeerConnection peer = new PeerConnection(client);
          peersSockets.put(port, peer);
        }
      } else if (port > 0 && !server) {
        // client code
        // TODO properly deal with if the socket already exists
        if (!peersSockets.containsKey(port)) {
          Socket clientSocket = new Socket(peerInfo.peerAddress, port);
          PeerConnection peer = new PeerConnection(clientSocket);
          peersSockets.put(port, peer);
        }
      }

    } catch (IOException e) {

    }
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
