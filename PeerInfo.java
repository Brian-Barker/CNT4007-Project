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

  public PeerInfo(int pPort) {
    peerId = 0;
    peerAddress = "";
    peerPort = pPort;
    hasEntireFile = false;
  }
}
