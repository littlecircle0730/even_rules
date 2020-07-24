package padec.registry;

import padec.lock.Lock;

import java.util.LinkedHashMap;
import java.util.Map;

public class ServiceRegistry {
    private Map<String, Lock> registry;

    public ServiceRegistry() {
        registry = new LinkedHashMap<>();
    }

    public void addService(Lock l) {
        registry.put(l.getRegistryName(), l);
    }

    public boolean exposesService(String name) {
        return registry.containsKey(name);
    }

    public Lock getService(String name) {
        return registry.get(name);
    }
}
