package applications;

import core.DTNHost;
import core.Message;
import core.Settings;
import padec.rbac.RoleMapping;
import padec.rbac.SimRoleMap;

public class RBACPADECApp extends PADECApp {

    public RBACPADECApp(Settings s) {
        super(s);
    }

    public RBACPADECApp(PADECApp a) {
        super(a);
    }

    @Override
    public Message handle(Message msg, DTNHost host) {
        if (SimRoleMap.getMapping(host.getAddress()) == null) {
            RoleMapping mapping = new RoleMapping();
            mapping.loadFromYamlFile("rbac_roles/Roles.yaml");
            SimRoleMap.updateMapping(host.getAddress(), mapping);
        }
        return super.handle(msg, host);
    }
}
