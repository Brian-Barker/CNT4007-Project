import java.util.BitSet;

public class PeerInfo {
  public int peerId;
  public String peerAddress;
  public int peerPort;
  public boolean hasEntireFile;

  public PeerInfo(int pId, String pAddress, int pPort, boolean hasFile) {
    peerId = pId;
    peerAddress = pAddress;
    peerPort = pPort;
    hasEntireFile = hasFile;
  }

  public PeerInfo() {
    peerId = 0;
    peerAddress = "";
    peerPort = 0;
    hasEntireFile = false;
  }
}
