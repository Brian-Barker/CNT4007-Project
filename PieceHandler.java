import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;

import java.io.IOException;
import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Paths;

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
  // these dont really need to exist
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

  public void loadFile(Map<String, String> config) {
    String fileName = config.get("FileName");
    int fileSize = Integer.parseInt(config.get("FileSize"));
    int pieceSize = Integer.parseInt(config.get("PieceSize"));

    int pieces = (int) Math.ceil((float) fileSize / (float) pieceSize);

    this.fileSize = fileSize;
    this.pieceSize = pieceSize;
    this.pieces = pieces;

    try {
      this.fileData = Files.readAllBytes(Paths.get(fileName));

      int spareBits = 8 - (pieces % 8);

      int bitfieldLength = pieces + spareBits;
      this.bitfield = new BitSet(bitfieldLength);
      // this.bitfield.set(0, bitfieldLength + 1, false);
      this.bitfield.set(0, pieces, true);
      // if (spareBits > 0) {
      // bitfield.set(bitfieldLength-1);
      // }

      // System.out.println("READING " + fileName + " " + fileData.length + " " +
      // pieces + " " + spareBits + " "
      // + bitfieldLength + " " + bitfield.length());

      StringBuilder s = new StringBuilder();
      for (int i = 0; i < bitfield.length(); i++) {
        s.append(bitfield.get(i) == true ? "1" : "0");
        // System.out.println(i + ":" + bitfield.get(i));
      }

      System.out.println("S:" + s);
      // Arrays.fill(this.bitfield, true);
    } catch (IOException e) {
      System.out.println("E " + e);
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

  public byte[] bitfieldToByteArray() {
    byte[] bitfieldBytes = bitfield.toByteArray();
    byte[] flipped = ByteBuffer.wrap(bitfieldBytes).order(ByteOrder.BIG_ENDIAN).array();
    System.out.println("Conv " + Arrays.toString(bitfieldBytes));
    System.out.println("Conv2 " + Arrays.toString(flipped));

    return bitfieldBytes;
  }
}