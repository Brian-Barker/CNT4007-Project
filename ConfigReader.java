import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.io.FileReader;

public class ConfigReader {

  public static Vector<RemotePeerInfo> getPeerList(String filename) {
    Vector<RemotePeerInfo> peerInfoVector = new Vector<RemotePeerInfo>();

    try {
      String st;
      BufferedReader in = new BufferedReader(new FileReader(filename));
      while ((st = in.readLine()) != null) {
        String[] tokens = st.split("\\s+");
        peerInfoVector.addElement(new RemotePeerInfo(tokens[0], tokens[1], tokens[2], tokens[3].equals("1")));
      }

      in.close();
    } catch (Exception ex) {
      System.out.println(ex.toString());
    }
    return peerInfoVector;
  }

  public static Map<String, String> getCommonConfig(String filename) {
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
