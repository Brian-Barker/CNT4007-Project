import java.util.BitSet;
import java.util.Vector;

public class Bitfield {
  // the pieces that exist
  public BitSet bitfield;

  Bitfield(int length) {
    bitfield = new BitSet(length);
  }

  Bitfield(byte[] payload) {
    bitfield = BitSet.valueOf(payload);
  }

  public void setBit(int index) {
    bitfield.set(index);
  }

  public boolean hasBit(int index) {
    return bitfield.get(index);
  }

  // compares other peer bitset and sees if current peer should be interested
  public boolean shouldBeInterested(Bitfield otherPeerBitfield) {
    BitSet otherPeerBitset = otherPeerBitfield.bitfield;
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
