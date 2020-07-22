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
        RoleMapping mapping = SimRoleMap.getMapping(provId);
        if (mapping != null) {
            roles = mapping.getRoles(consId).toArray(roles);
        }
        return roles;
    }
}
