import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.io.FileReader;

public class Configs {
  
  public static Map<String, String> commonConfig;
	public static Vector<PeerInfo> peerInfo;

  public static void loadConfigs(){
		commonConfig = Configs.getCommonConfig("Common.cfg");
    //common.forEach((key, value) -> System.out.println(key + ":" + value));
		peerInfo = Configs.getPeerList("PeerInfo.cfg");
  }
  private static Vector<PeerInfo> getPeerList(String filename) {
    Vector<PeerInfo> peerInfoVector = new Vector<PeerInfo>();

    try {
      String st;
      BufferedReader in = new BufferedReader(new FileReader(filename));
      while ((st = in.readLine()) != null) {
        String[] tokens = st.split("\\s+");

        int peerId = Integer.parseInt(tokens[0]);
        String peerAddress = tokens[1];
        int port = Integer.parseInt(tokens[2]);
        boolean hasEntireFile = tokens[3].equals("1");
        peerInfoVector.addElement(new PeerInfo(peerId, peerAddress, port, hasEntireFile));
      }

      in.close();
    } catch (Exception ex) {
      System.out.println(ex.toString());
    }
    return peerInfoVector;
  }

  private static Map<String, String> getCommonConfig(String filename) {
    Map<String, String> configMap = new HashMap<String, String>();

    try {
      String st;
      BufferedReader in = new BufferedReader(new FileReader(filename));
      while ((st = in.readLine()) != null) {
        String[] tokens = st.split("\\s+");
        configMap.put(tokens[0], tokens[1]);
      }

      in.close();
    } catch (Exception ex) {
      System.out.println(ex.toString());
    }
    return configMap;
  }
}
