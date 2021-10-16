public class StartPeer {
  public static void main(String args[]) {
    String peerId = "1001";// args[0];
    try {
      if (!peerId.matches("\\d\\d\\d\\d")) {
        throw new Exception("Error: Invalid peerId: " + peerId);
      }

      // start the peer with the valid peer id
      PeerProcess peer = new PeerProcess();
      peer.start(peerId);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}
