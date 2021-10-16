import java.net.*;
import java.io.*;

// The PeerConnection class handles the connection to another peer
// input stream to send to the client
// socket
public class PeerConnection {
  private Socket clientSocket;
  private PrintWriter out;
  private BufferedReader in;

  PeerConnection(Socket client) {
    this.clientSocket = client;
    try {
      out = new PrintWriter(clientSocket.getOutputStream(), true);
      in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

    } catch (IOException e) {

    }
  }

  public void close() {
    try {
      in.close();
      out.close();
      clientSocket.close();
    } catch (IOException e) {

    }
  }
}
