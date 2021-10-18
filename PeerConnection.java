import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Vector;

// The PeerConnection class handles the connection to another peer
// input stream to send to the client
// socket
public class PeerConnection {
  public static PeerInfo localPeer;

  private Socket clientSocket;

  // if this is true, no more uploading
  private boolean connectionChoked = false;
  private boolean interestedInOtherPeer = false;

  // other peer info
  public PeerInfo otherPeer;
  // TODO dont forget, this properly is not always be set
  public Bitfield otherPeerBitfield;
  public boolean otherPeerInterested = false;
  // output steam is the data to send to the other peer
  private DataOutputStream out;
  // input stream is the data coming in from the other peer
  private DataInputStream in;

  private final byte TYPE_CHOKE = 0;
  private final byte TYPE_UNCHOKE = 1;
  private final byte TYPE_INTERESTED = 2;
  private final byte TYPE_NOT_INTERESTED = 3;
  private final byte TYPE_BITFIELD = 5;
  private final byte TYPE_REQUEST = 6;
  private final byte TYPE_PIECE = 7;
  private final byte[] EMPTY_BYTES = new byte[] {};

  PeerConnection(Socket client, PeerInfo otherPeer) {
    this.clientSocket = client;
    this.otherPeer = otherPeer;
  }

  // for when another peer connects and we dont have the entire peer info
  PeerConnection(Socket client, int port) {
    this.clientSocket = client;
    this.otherPeer = new PeerInfo(port);
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
        int rawLength = in.readInt();
        byte type = in.readByte();
        int payloadLength = rawLength - 1;
        if (type == TYPE_UNCHOKE) {
          sendRequestMessage();
        }
        if (type == TYPE_INTERESTED) {
          System.out.println("Got interested from " + otherPeer.peerId);
          otherPeerInterested = true;
        }
        if (type == TYPE_NOT_INTERESTED) {
          System.out.println("Got not interested from " + otherPeer.peerId);
          otherPeerInterested = false;
        }
        if (type == TYPE_BITFIELD) {
          byte[] payload = in.readNBytes(payloadLength);
          readBitfield(payload);
        }
        if (type == TYPE_REQUEST) {
          if (connectionChoked == false) {
            // valid request message
            // send piece
            int pieceIndex = in.readInt();
            readRequest(pieceIndex);
          }
        }
        if (type == TYPE_PIECE) {
          int pieceIndex = in.readInt();
          byte[] payload = in.readNBytes(payloadLength - 4);
          readPiece(pieceIndex, payload);
        }
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

  public void sendRequestMessage() {
    // select a random piece the other peer has and local peer does not and local has not requested yet
    Vector<Integer> interestingPieces = PieceHandler.getInstance().getRequestablePieces(otherPeerBitfield);
    System.out.println("UNREQ " + interestingPieces.size());
    if (interestingPieces.size() > 0) {
      Integer randomPiece = interestingPieces.get((int) (Math.random() * interestingPieces.size()));
      PieceHandler.getInstance().requested.setBit(randomPiece);
      sendMessage(TYPE_REQUEST, intToByteArray(randomPiece));
    } else {
      // no more interesting pieces from this peer
      // maybe this is handled elsewhere (uninterested)
    }

  }

  public void sendPiece(int pieceIndex) {
    byte[] pieceData = PieceHandler.getInstance().getPieceData(pieceIndex);
    byte[] message = prefixBytesWithInt(pieceIndex, pieceData);
    sendMessage(TYPE_PIECE, message);
  }

  public void sendNotInterested() {
    sendMessage(TYPE_NOT_INTERESTED, EMPTY_BYTES);
  }

  public void chokeConnection() {
    sendMessage(TYPE_CHOKE, EMPTY_BYTES);
    connectionChoked = true;
  }

  public void unchokeConnection() {
    sendMessage(TYPE_UNCHOKE, EMPTY_BYTES);
    connectionChoked = false;
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

  public void readBitfield(byte[] payload) {
    System.out.println("Got bitfield " + Arrays.toString(payload));
    otherPeerBitfield = new Bitfield(payload);
    if (PieceHandler.getInstance().shouldBeInterested(otherPeerBitfield)) {
      interestedInOtherPeer = true;
      sendMessage(TYPE_INTERESTED, EMPTY_BYTES);
    }
  }

  public void readRequest(int pieceIndex) {
    // send piece
    sendPiece(pieceIndex);
  }

  public void readPiece(int pieceIndex, byte[] payload) {
    if (PieceHandler.getInstance().setPieceData(pieceIndex, payload) == false) {
      // still more pieces needed
      sendRequestMessage();
    }

  }

  private void sendMessage(byte messageType, byte[] payload) {

    // 5 = length (4 bytes) + type (1 byte)
    byte[] message = prefixBytesWithNull(5, payload);
    // subtract 4 since we are not including the length int itself
    byte[] lengthBytes = intToByteArray(message.length - 4);
    System.arraycopy(lengthBytes, 0, message, 0, lengthBytes.length);

    message[4] = messageType;

    try {
      out.write(message);
    } catch (IOException e) {
      System.out.println("Error sending " + e);
    }
  }

  private byte[] prefixBytesWithInt(int num, byte[] data) {
    byte[] out = new byte[4 + data.length];
    byte[] numBytes = intToByteArray(num);
    System.arraycopy(data, 0, out, 4, data.length);
    // set the int bytes
    System.arraycopy(numBytes, 0, out, 0, 4);
    return out;
  }

  private byte[] prefixBytesWithNull(int numEmptyBytes, byte[] data) {
    byte[] out = new byte[numEmptyBytes + data.length];
    System.arraycopy(data, 0, out, numEmptyBytes, data.length);
    return out;
  }

  public byte[] intToByteArray(int num) {
    byte[] bytes = ByteBuffer.allocate(4).putInt(num).array();
    return bytes;
  }

  public boolean isChoked() {
    return connectionChoked;
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
