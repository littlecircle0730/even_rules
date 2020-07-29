package padec.rule.operator.rbac;

import padec.rbac.RoleMapping;
import padec.rbac.SimRoleMap;
import padec.rule.CombineOperator;

public class RoleOperator implements CombineOperator {

    @Override
    public Object combine(Object a, Object b) {
        String[] roles = new String[]{};
        Integer consId = (Integer) b;
        Integer provId = (Integer) a;
        if (SimRoleMap.getMapping(provId) == null) {
            RoleMapping mapping = new RoleMapping();
            mapping.loadFromYamlFile("rbac_roles/Roles.yaml");
            SimRoleMap.updateMapping(provId, mapping);
        }
        RoleMapping mapping = SimRoleMap.getMapping(provId);
        if (mapping != null) {
            roles = mapping.getRoles(consId).toArray(roles);
        }
        return roles;
    }
}
