package padec.lock;

import applications.PADECApp;
import padec.application.Endpoint;
import padec.filtering.FilterTechnique;
import padec.rule.Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Lock with a multitude of access levels and keyholes.
 */
public class Lock {
    private List<AccessLevel> levels;
    private Endpoint endpoint;

    public Lock(Endpoint endpoint){
        levels = new ArrayList<>(); //ArrayList looks the best for this task to me.
        // Levels will be quite dynamic, but will not be deleted as often.
        this.endpoint = endpoint;
    }

    public void addAccessLevel(FilterTechnique filter, Map<String, Object> filterParams, Rule accessRule) {
        AccessLevel al = new AccessLevel(filter, endpoint, filterParams, accessRule);
        levels.add(al);
    }

    public void addAccessLevel(FilterTechnique filter, Map<String, Object> filterParams, Rule accessRule, int position) {
        AccessLevel al = new AccessLevel(filter, endpoint, filterParams, accessRule);
        levels.add(position, al);
    }

    public void addAcessLevel(AccessLevel al) {
        levels.add(al);
    }

    public void addAccessLevel(AccessLevel al, int position) {
        levels.add(position, al);
    }

    public int getTotalAccessLevels(){
        return levels.size();
    }

    public AccessLevel getAccessLevel(int index){
        AccessLevel ret = null;
        if (index < levels.size() && index >= 0){
            ret = levels.get(index);
        }
        return ret;
    }

    public AccessLevel getMaxAccessLevel(){
        AccessLevel ret = null;
        if (levels.size() > 0) {
            ret = levels.get(levels.size() - 1);
        }
        return ret;
    }

    public boolean removeAccessLevel(int index){
        boolean success = false;
        if(index < levels.size()){
            levels.remove(index);
            success = true;
        }
        return success;
    }

    public List<Keyhole> getKeyholes(){
        List<Keyhole> keyholes = new ArrayList<>();
        for (AccessLevel l : levels){
            keyholes.add(l.getKeyhole());
        }
        return keyholes;
    }

    /**
     * Do not use this method outside of TheONE simulator. I'm making sure it breaks if you try to do so.
     * This is only used for updating the endpoint in TheONE.
     */
    @Deprecated
    public Endpoint getEndpointOnlyForTheONE() {
        try {
            Class callerClass = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
            if (callerClass == PADECApp.class) {
                return endpoint;
            }
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String getRegistryName() {
        return endpoint.getClass().getName();
    }

}
