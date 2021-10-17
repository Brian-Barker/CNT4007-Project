/*
 *                     CEN5501C Project2
 * This is the program starting remote processes.
 * This program was only tested on CISE SunOS environment.
 * If you use another environment, for example, linux environment in CISE 
 * or other environments not in CISE, it is not guaranteed to work properly.
 * It is your responsibility to adapt this program to your running environment.
 */

public class PeerInfo {
  public String peerId;
  public String peerAddress;
  public int peerPort;
  public boolean hasEntireFile;

  public PeerInfo(String pId, String pAddress, int pPort, boolean hasFile) {
    peerId = pId;
    peerAddress = pAddress;
    peerPort = pPort;
    hasEntireFile = hasFile;
  }

  public PeerInfo(int pPort) {
    peerId = "";
    peerAddress = "";
    peerPort = pPort;
    hasEntireFile = false;
  }
}
