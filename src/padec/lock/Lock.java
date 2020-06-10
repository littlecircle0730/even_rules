package padec.lock;

import java.util.ArrayList;
import java.util.List;

/**
 * Lock with a multitude of access levels and keyholes.
 */
public class Lock {
    private List<AccessLevel> levels;

    public Lock(){
        //Initialize data structures, etc.
    }

    public void addAccessLevel(AccessLevel level){
        levels.add(level);
    }

    public List<Keyhole> getKeyholes(){
        List<Keyhole> keyholes = new ArrayList<>();
        for (AccessLevel l : levels){
            keyholes.add(l.getKeyhole());
        }
        return keyholes;
    }
}
