public class StartPeer {
  public static void main(String args[]) {
    try {
      String peerId = args[0];

      if (!peerId.matches("\\d\\d\\d\\d")) {
        throw new Exception("Error: Invalid peerId: " + peerId);
      }

      // start the peer with the valid peer id
      PeerProcess peer = new PeerProcess();
      peer.start(Integer.parseInt(peerId));
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
}
