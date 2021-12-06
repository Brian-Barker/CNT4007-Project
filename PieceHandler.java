import java.util.*;

import java.io.IOException;
import java.lang.Math;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.*;

/* 
  thread safe singleton instance to keep track of pieces that are downloaded
  and piece that are needed
*/
public class PieceHandler {
  private static volatile PieceHandler instance;
  private static Object mutex = new Object();

  // these are shared among all threads
  private byte[] fileData;
  public Bitfield bitfield;
  // the pieces that we have requested for in the above bitset
  public Bitfield requested;

  /*
    Each time a peer requests a piece to Other Peer, store
    Other Peer: list of pieces requested from other peer

    When other peer chokes before receiving the piece, reset the
    piece index in requested

    Clear requested buffer when:
    1. receiving a proper piece from other peer
    2. other peer has choked us
  */
  Map<Integer, ArrayList<Integer>> requestedBuffer;

  int pieceSize = 0;
  int fileSize = 0;
  String fileName = "";
  int pieces = 0;
  int piecesDownloaded = 0;
  public boolean hasWritten = false;

  private PieceHandler() {
  }

  public static PieceHandler getInstance() {
    PieceHandler result = instance;
    if (result == null) {
      synchronized (mutex) {
        result = instance;
        if (result == null)
          instance = result = new PieceHandler();
      }
    }
    return result;
  }

  public void initBitfield() {
    String fileName = Configs.getFileName();
    int fileSize = Configs.getFileSize();
    int pieceSize = Configs.getPieceSize();

    int pieces = (int) Math.ceil((float) fileSize / (float) pieceSize);

    this.fileName = fileName;
    this.fileSize = fileSize;
    this.pieceSize = pieceSize;
    this.pieces = pieces;

    int bitfieldLength = pieces;
    this.bitfield = new Bitfield(bitfieldLength);
    this.requested = new Bitfield(bitfieldLength);
  }

  public void loadFile() {
    Logger.Debug("Loading file");

    try {
      this.fileData = Files.readAllBytes(getFilePath());
      this.bitfield.bitfield.set(0, pieces, true);

      hasWritten = true;
    } catch (IOException e) {
      Logger.Debug("File not found");
      // I don't think we need to handle the case of missing file if there is a 1 in peerinfo
      // I think we can assume if there is a 1 in PeerInfo, the file will exist
      //handleMissingFile(peerId);
    }
  }

  public void initEmptyBytes() {
    this.fileData = new byte[fileSize];
  }

  public boolean handleNewPieceData(int pieceIndex, byte[] pieceData) {
    Logger.Debug("Got piece data. Index: " + pieceIndex + " of length " + pieceData.length);

    int byteIndex = pieceIndex * pieceSize;
    System.arraycopy(pieceData, 0, fileData, byteIndex, pieceData.length);
    // TODO this assumes setPieceData is only called with unique pieceIndex's
    piecesDownloaded += 1;
    bitfield.setBit(pieceIndex);

    if (piecesDownloaded == pieces) {
      hasWritten = true;
      // all done!
      writeFile(fileData);
      return true;
    } else {
      return false;
    }
  }

  public byte[] getPieceData(int pieceIndex) {
    int byteStart = pieceIndex * pieceSize;
    int byteEnd = ((pieceIndex + 1) * pieceSize) - 1;
    // check end of file
    if (byteEnd > fileSize) {
      byteEnd = fileSize - 1;
    }
    Logger.Debug("Get " + byteStart + " to " + byteEnd + " size " + fileData.length + " piece " + pieceIndex);
    return Arrays.copyOfRange(fileData, byteStart, byteEnd + 1);
  }

  public void writeFile(byte[] data) {
    try {
      Logger.Debug("WRITING FILE");
      Files.write(getFilePath(), data);
    } catch (IOException e) {
      Logger.Debug("Error writing file " + e);
    }
  }

  public Vector<Integer> getRequestablePieces(Bitfield otherPeer) {
    // select a random piece the other peer has and local peer does not and local has not requested yet
    Vector<Integer> unreqs = new Vector<>();
    for (int i = requested.bitfield.nextClearBit(0); i < pieces; i = requested.bitfield.nextClearBit(i + 1)) {
      if (bitfield.hasBit(i) == false && otherPeer.hasBit(i)) {
        unreqs.add(i);
      }
    }
    return unreqs;
  }

  public boolean didRequestFor(int index) {
    return requested.hasBit(index);
  }

  public boolean shouldBeInterested(Bitfield otherPeerBitfield) {
    return bitfield.shouldBeInterested(otherPeerBitfield);
  }

  public byte[] bitfieldToByteArray() {
    return bitfield.bitfieldToByteArray();
  }

  public int bitfieldCardinality() {
    return bitfield.bitfieldCardinality();
  }

  private Path getFilePath() {
    return Paths.get("./peer_" + ConnectionHandler.getInstance().localPeer.peerId + "/" + this.fileName);
  }

  private void handleMissingFile(int peerId) {
    try {
      List<String> lines = new ArrayList<String>();
      String line = null;
      File peerInfoFile = new File("./PeerInfo.cfg");
      FileReader fr = new FileReader(peerInfoFile);
      BufferedReader br = new BufferedReader(fr);
      while ((line = br.readLine()) != null) {
        line.trim();
        if (line.contains(String.valueOf(peerId))) {
          line = line.substring(0, line.length() - 1) + "0";
        }
        lines.add(line + "\n");
      }
      fr.close();
      br.close();

      FileWriter fw = new FileWriter(peerInfoFile);
      BufferedWriter out = new BufferedWriter(fw);
      for (String s : lines) {
        out.write(s);
      }
      out.flush();
      out.close();
      fw.close();
    } catch (Exception e) {
      System.out.println("Error while updating peerInfo: " + e);
    }

  }
}
