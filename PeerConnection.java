import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;
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
        System.out.println("Starting to listen");
        int length = in.readInt();
        System.out.println("DLKFJ:S " + length);
        byte type = in.readByte();
        byte[] payload = in.readNBytes(length - 1);
        System.out.println("Got message " + length + " " + type);
        if (type == 5) {
          System.out.println("Got bitfield " + Arrays.toString(payload));
          for (byte b : payload) {
            System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
          }
          BitSet set = BitSet.valueOf(payload);
          StringBuilder s = new StringBuilder();
          for (int i = 0; i < set.length(); i++) {
            s.append(set.get(i) == true ? "1" : "0");
            // System.out.println(i + ":" + bitfield.get(i));
          }

          System.out.println("2S:" + s);
        }
      }
    } catch (IOException e) {

    }

  }

  public void readHandshake() {
    try {
      byte[] handshake = in.readNBytes(32);
      System.out.println("GOT HANDSHAKE FROM " + Arrays.toString(handshake));

      sendBitfield();
    } catch (IOException e) {

    }
  }

  public void sendBitfield() {
    if (localPeer.hasEntireFile) {

      byte[] bitfieldBytes = PieceHandler.getInstance().bitfieldToByteArray();
      byte[] message = new byte[5 + bitfieldBytes.length];
      // subtract 4 since we are not including the length int itself
      byte[] lengthBytes = lengthToByteArray(message.length - 4);
      byte type = 5;

      System.arraycopy(bitfieldBytes, 0, message, 5, bitfieldBytes.length);
      System.arraycopy(lengthBytes, 0, message, 0, 4);
      message[4] = type;
      try {
        System.out.println("Sending bitfield " + Arrays.toString(message) + " " + message.length + " "
            + bitfieldBytes.length + " " + Arrays.toString(bitfieldBytes));
        out.write(message);
      } catch (IOException e) {
        System.out.println("Error sending " + e);
      }
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

  public byte[] lengthToByteArray(int length) {
    byte[] bytes = ByteBuffer.allocate(4).putInt(length).array();
    return bytes;
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
