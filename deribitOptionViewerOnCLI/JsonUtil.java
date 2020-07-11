import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import javax.script.*;

import org.json.*;

public class JsonUtil {

    public static Object[] getAsArray(String json, String code) {
        Object obj = get(json, code);
        if (obj instanceof Object[]) {
            return (Object[]) obj;
        } else {
            return null;
        }
    }

    public static Object get(String json, String code) {
        // Get the JavaScript engine
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");

        String script = "var obj = " + json + ";";
        try {
            engine.eval(script);
            {
                Object obj = engine.eval("obj." + code);
                if (obj instanceof Map) {
                    java.util.Map map = (java.util.Map) obj;
                    Set entrySet = map.entrySet();
                    Object[] arr = new Object[entrySet.size()];
                    int n = 0;
                    for (Object objValue : map.values()) {
                        if (objValue instanceof String) {
                            String sValue = (String) objValue;
                            arr[n] = sValue;
                        } else {
                            arr[n] = map.get(obj);
                        }
                        n++;
                    }
                    return arr;
                }
                return obj;
            }
        } catch (ScriptException e) {
            e.printStackTrace();
            return null;
        }
    }
}
