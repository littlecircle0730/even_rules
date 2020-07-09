package padec.lock;

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
}
