package padec.rbac;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class RoleMapping {

    private Map<Integer, Set<String>> roleMap;

    public RoleMapping() {
        roleMap = new HashMap<>();
    }

    public Set<String> getRoles(Integer identity) {
        return roleMap.getOrDefault(identity, new LinkedHashSet<>());
    }

    public void addRole(Integer identity, String role) {
        if (!roleMap.containsKey(identity)) {
            roleMap.put(identity, new LinkedHashSet<>());
        }
        roleMap.get(identity).add(role);
    }

    public boolean hasRole(Integer identity, String role) {
        return roleMap.getOrDefault(identity, new LinkedHashSet<>()).contains(role);
    }

    public boolean loadFromYamlFile(String file) {
        boolean success = false;
        try {
            InputStream iStream = new FileInputStream(file);
            Yaml yaml = new Yaml();
            Map<Integer, Object> mappings = yaml.load(iStream);
            roleMap.clear();
            for (Integer key : mappings.keySet()) {
                roleMap.put(key, new LinkedHashSet<>((List<String>) mappings.get(key)));
            }
            iStream.close();
            success = true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return success;
    }
}
