import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.io.*;

// The PeerConnection class handles the connection to another peer
// input stream to send to the client
// socket
public class PeerConnection {
  private Socket clientSocket;
  static private PeerInfo localPeer;
  private PeerInfo otherPeer;
  // output steam is the data to send to the other peer
  private DataOutputStream out;
  // input stream is the data coming in from the other peer
  private DataInputStream in;

  PeerConnection(Socket client, PeerInfo otherPeer) {
    this.clientSocket = client;
    this.otherPeer = otherPeer;
    initializeConnection();
  }

  // for when another peer connects and we dont have the entire peer info
  PeerConnection(Socket client, int port) {
    this.clientSocket = client;
    this.otherPeer = new PeerInfo(port);
    initializeConnection();
  }

  public void initializeConnection() {
    try {
      out = new DataOutputStream(clientSocket.getOutputStream());
      in = new DataInputStream(clientSocket.getInputStream());

      sendHandshake();
      receiveMessage();
    } catch (IOException e) {

    }
  }

  public void receiveMessage() {
    readHandshake();
    try {
      while (true) {
        int length = in.readInt();

      }
    } catch (IOException e) {

    }

  }

  public void readHandshake() {
    try {
      byte[] handshake = in.readNBytes(32);
      System.out.println("GOT HANDSHAKE FROM " + Arrays.toString(handshake));
    } catch (IOException e) {

    }

  }

  public void sendHandshake() {
    String header = "P2PFILESHARINGPROJ";
    String empty10Bytes = "\u0000".repeat(10);
    int peerId = Integer.parseInt(PeerConnection.localPeer.peerId);
    byte[] handshake = ByteBuffer.allocate(32).put(header.getBytes()).put(empty10Bytes.getBytes()).putInt(peerId)
        .array();
    System.out.println("Sending handshake " + Arrays.toString(handshake) + " " + handshake.length + " " + peerId);

    try {
      out.write(handshake);
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

  public static void setLocalPeer(PeerInfo p) {
    PeerConnection.localPeer = p;
  }
}
