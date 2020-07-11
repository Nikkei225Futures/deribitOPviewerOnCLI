import java.io.*;
import java.util.*;

public class ExeCommand {
    public String[] exeCmd(String command){
        String rst = "";
        try {
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec(command);
            InputStream ism = p.getInputStream();
            InputStreamReader reader = new InputStreamReader(ism,"Shift_JIS");
            BufferedReader br = new BufferedReader(reader);
            ArrayList <String> arr = new ArrayList<>();
            while ((rst = br.readLine()) != null) {
                arr.add(rst);
            }
            String[] ret = arr.toArray(new String[arr.size()]);
            return ret;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public String connectStrArr(String[] result){
        String connectedStr = "";
        for(int i = 0; i < result.length; i++){
            connectedStr += result[i];
        }
        return connectedStr;
    }

}

