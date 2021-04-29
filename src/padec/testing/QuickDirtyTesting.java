package padec.testing;

import org.yaml.snakeyaml.Yaml;
import padec.attribute.Attribute;
import padec.attribute.Location;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

public class QuickDirtyTesting {
    public static void main(String[] args) throws Throwable {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("A", "B");
        System.out.println(map.get("Z"));
    }
}
