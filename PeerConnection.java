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
  public BitSet otherPeerBitfield;

  // output steam is the data to send to the other peer
  private DataOutputStream out;
  // input stream is the data coming in from the other peer
  private DataInputStream in;

  private final byte TYPE_INTERESTED = 2;
  private final byte TYPE_BITFIELD = 5;
  private final byte[] EMPTY_BYTES = new byte[] {};

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
        byte type = in.readByte();
        byte[] payload = in.readNBytes(length - 1);
        if (type == 2) {
          System.out.println("AYO PEER " + otherPeer.peerId + " IS INTERESTED IN MY PIECES");
        }
        if (type == 5) {
          System.out.println("Got bitfield " + Arrays.toString(payload));
          BitSet set = BitSet.valueOf(payload);
          // TODO dont forget, this properly is not always be set
          otherPeerBitfield = set;

          if (PieceHandler.getInstance().shouldBeInterested(otherPeerBitfield)) {
            sendMessage(TYPE_INTERESTED, EMPTY_BYTES);
          }
        }
      }
    } catch (IOException e) {

    }

  }

  public void readHandshake() {
    try {
      String header = new String(in.readNBytes(18));
      if (header.equals("P2PFILESHARINGPROJ")) {
        in.readNBytes(10); // the empty 10 bytes of 0
        int otherId = in.readInt();
        otherPeer.peerId = otherId;
        System.out.println("Got valid handshake from " + otherId);
        sendBitfield();
      }

    } catch (IOException e) {

    }
  }

  public void sendBitfield() {
    // only send bitfield if we have at least one pieces
    if (PieceHandler.getInstance().bitfieldCardinality() > 0) {
      byte[] bitfieldBytes = PieceHandler.getInstance().bitfieldToByteArray();
      System.out.println("Sending bitfield");
      sendMessage(TYPE_BITFIELD, bitfieldBytes);
    }
  }

  public void sendMessage(byte messageType, byte[] payload) {
    byte[] message = new byte[5 + payload.length];
    // subtract 4 since we are not including the length int itself
    byte[] lengthBytes = lengthToByteArray(message.length - 4);

    System.arraycopy(payload, 0, message, 5, payload.length);
    System.arraycopy(lengthBytes, 0, message, 0, 4);
    message[4] = messageType;
    try {
      out.write(message);
    } catch (IOException e) {
      System.out.println("Error sending " + e);
    }
  }

  public void sendHandshake() {
    String header = "P2PFILESHARINGPROJ";
    String empty10Bytes = "\u0000".repeat(10);
    int peerId = PeerConnection.localPeer.peerId;
    byte[] handshake = ByteBuffer.allocate(32).put(header.getBytes()).put(empty10Bytes.getBytes()).putInt(peerId)
        .array();
    System.out.println("Sending handshake as " + peerId);

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
