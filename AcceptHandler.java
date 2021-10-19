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
        System.out.println("Listening for client...");
        Socket client = serverSocket.accept();
        System.out.println("Accepting client " + port);
        Thread thread = ConnectionHandler.getInstance().createNewConnection(client);
        PeerProcess.clientThreads.add(thread);
        // PeerConnection peer = new PeerConnection(client, port);
        // peer.initializeConnection();
      }
    } catch (IOException e) {
      System.out.println(e);
    }
  }
}
