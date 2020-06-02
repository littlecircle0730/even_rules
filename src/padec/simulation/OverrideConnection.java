package padec.simulation;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.LinkedHashMap;

public class OverrideConnection{
    private Map<String, List> innerConnections;
    private static OverrideConnection instance = null;

    private OverrideConnection(){
        this.innerConnections = new LinkedHashMap<>();
    }

    public static synchronized OverrideConnection getInstance(){
        if (instance == null){
            instance = new OverrideConnection();
        }
        return instance;
    }

    public void registerSelf(String address){
        if (!innerConnections.containsKey(address)){
            innerConnections.put(address, new LinkedList<>());
        }
    }

    public boolean isRegistered(String address){
        return innerConnections.containsKey(address);
    }

    public void unregisterSelf(String address){
        innerConnections.remove(address);
    }

    public boolean sendTo(Object message, String address){
        boolean success = false;
        if (innerConnections.containsKey(address)){
            success = innerConnections.get(address).add(message);
        }
        return success;
    }

    public List<Object> receive(String address){
        List<Object> ret = null;
        if (innerConnections.containsKey(address)){
            ret = new LinkedList<>(innerConnections.get(address));
            innerConnections.get(address).clear();
        }
        return ret;
    }
}