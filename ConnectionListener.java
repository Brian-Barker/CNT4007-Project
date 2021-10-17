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

  public PeerInfo peerInfo;
  public boolean server = false;

  public void run() {

    int port = peerInfo.peerPort;
    if (port > 0 && server) {
      // server code
      serverListen(port);
    } else if (port > 0 && !server) {
      // client code
      clientConnect(port);
    }

  }

  public void serverListen(int port) {
    try {
      ServerSocket serverSocket = new ServerSocket(port);

      ConnectionHandler.getInstance().setServerSocket(serverSocket);

      while (true) {
        Socket client = serverSocket.accept();
        System.out.println("Accepting client " + port);
        PeerConnection peer = new PeerConnection(client, port);
        ConnectionHandler.getInstance().savePeerSocket(port, peer);
      }
    } catch (IOException e) {

    }
  }

  public void clientConnect(int port) {
    try {
      // TODO properly deal with if the socket already exists
      if (!ConnectionHandler.getInstance().peerConnectionExists(port)) {
        // the peerInfo is the peer to connect to
        Socket clientSocket = new Socket(peerInfo.peerAddress, port);
        System.out.println("Connecting to " + port + " from " + peerInfo.peerId + " " + peerInfo.peerPort);
        PeerConnection peer = new PeerConnection(clientSocket, peerInfo);
        ConnectionHandler.getInstance().savePeerSocket(port, peer);
      } else {
        System.out.println("Already exists " + port);
      }
    } catch (IOException e) {

    }
  }
}
