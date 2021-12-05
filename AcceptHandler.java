import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

// loop to accept incoming connections
public class AcceptHandler implements Runnable {

  public int port;

  public void run() {
    serverListen();
  }

  public void serverListen() {
    try {
      ServerSocket serverSocket = new ServerSocket(port);

      ConnectionHandler.getInstance().setServerSocket(serverSocket);

      while (true) {
        Logger.Debug("Listening for client...");
        Socket client = serverSocket.accept();
        Logger.Debug("Accepting client " + port);
        Thread thread = ConnectionHandler.getInstance().createNewConnection(client, false);
        peerProcess.clientThreads.add(thread);
        // PeerConnection peer = new PeerConnection(client, port);
        // peer.initializeConnection();
      }
    } catch (IOException e) {
      System.out.println(e);
    }
  }
}
