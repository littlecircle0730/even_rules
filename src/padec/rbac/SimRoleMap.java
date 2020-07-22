package padec.rbac;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class SimRoleMap {
    private static Map<Integer, RoleMapping> simRoleMap = new LinkedHashMap<>();

    public static void updateMapping(Integer identity, RoleMapping mapping) {
        simRoleMap.put(identity, mapping);
    }

    public static RoleMapping getMapping(Integer identity) {
        return simRoleMap.getOrDefault(identity, null);
    }
}
