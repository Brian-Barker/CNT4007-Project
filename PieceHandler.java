import java.util.*;

import java.io.IOException;
import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;
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
  private BitSet bitfield;
  int pieceSize = 0;
  int fileSize = 0;
  String fileName = "";
  int pieces = 0;

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

  public void initBitfield(Map<String, String> config) {
    String fileName = config.get("FileName");
    int fileSize = Integer.parseInt(config.get("FileSize"));
    int pieceSize = Integer.parseInt(config.get("PieceSize"));

    int pieces = (int) Math.ceil((float) fileSize / (float) pieceSize);

    this.fileName = fileName;
    this.fileSize = fileSize;
    this.pieceSize = pieceSize;
    this.pieces = pieces;

    int spareBits = 0;// 8 - (pieces % 8);

    int bitfieldLength = pieces + spareBits;
    this.bitfield = new BitSet(bitfieldLength);
  }

  public void loadFile(int peerId) {
    System.out.println("Loading file");

    try {
      this.fileData = Files.readAllBytes(Paths.get("./peer_" + peerId + "/" + this.fileName));
      this.bitfield.set(0, pieces, true);
    } catch (IOException e) {
      System.out.println("File not found, updating peerInfo");
	  handleMissingFile(peerId);
    }
  }

  public byte[] getPieceData(int pieceIndex) {
    int byteStart = pieceIndex * pieceSize;
    int byteEnd = ((pieceIndex + 1) * pieceSize) - 1;
    // check end of file
    if (byteEnd > fileSize) {
      byteEnd = fileSize - 1;
    }

    return Arrays.copyOfRange(fileData, byteStart, byteEnd + 1);
  }
  
  private void handleMissingFile(int peerId) {
	try {
		List<String> lines = new ArrayList<String>();
		String line = null;
		File peerInfoFile = new File("./PeerInfo.cfg");
		FileReader fr = new FileReader(peerInfoFile);
		BufferedReader br = new BufferedReader(fr);
		while((line = br.readLine()) != null) {
			line.trim();
			if (line.contains(String.valueOf(peerId))) {
				line = line.substring(0, line.length()-1) + "0";
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

  // compares other peer bitset and sees if current peer should be interested
  public boolean shouldBeInterested(BitSet otherPeerBitset) {
    // bitset length retuns the position of the highest set bit
    for (int i = 0; i < Math.max(bitfield.length(), otherPeerBitset.length()); i++) {
      if (bitfield.get(i) == false && otherPeerBitset.get(i) == true) {
        // other peer has this piece and we dont
        return true;
      }
    }
    return false;
  }

  public byte[] bitfieldToByteArray() {
    byte[] bitfieldBytes = bitfield.toByteArray();
    return bitfieldBytes;
  }

  public int bitfieldCardinality() {
    return bitfield.cardinality();
  }
}
