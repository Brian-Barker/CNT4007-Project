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

  public PeerConnection peer;

  public void run() {
    if (peer != null) {
      peer.initializeConnection();
    }
  }

  public void connectionFromExistingSocket(Socket socket) {
    // for when the server accepts an incoming request

    // TODO properly deal with if the socket already exists
    peer = new PeerConnection(socket);

  }
}
