package padec.lock;

import java.util.ArrayList;
import java.util.List;

/**
 * Lock with a multitude of access levels and keyholes.
 */
public class Lock {
    private List<AccessLevel> levels;

    public Lock(){
        levels = new ArrayList<>(); //ArrayList looks the best for this task to me.
        // Levels will be quite dynamic, but will not be deleted as often.
    }

    public void addAccessLevel(AccessLevel level){
        levels.add(level);
    }

    public void addAccessLevel(AccessLevel level, int position){
        levels.add(position, level);
    }

    public int getTotalAccessLevels(){
        return levels.size();
    }

    public AccessLevel getAccessLevel(int index){
        AccessLevel ret = null;
        if (index < levels.size()){
            ret = levels.get(index);
        }
        return ret;
    }

    public void replaceAccessLevel(int index, AccessLevel level){
        if (index < levels.size()){
            levels.remove(index);
            levels.add(index, level);
        }
    }

    public AccessLevel getMaxAccessLevel(){
        AccessLevel ret = null;
        if (levels.size() > 0) {
            ret = levels.get(levels.size() - 1);
        }
        return ret;
    }

    public AccessLevel getMinAccessLevel(){
        AccessLevel ret = null;
        if (levels.size() > 0){
            ret = levels.get(0);
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
