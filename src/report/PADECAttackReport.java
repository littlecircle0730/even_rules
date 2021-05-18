package report;

import applications.PADECApp;
import core.Application;
import core.ApplicationListener;
import core.DTNHost;

import java.util.Map;
import java.util.LinkedHashMap;

public class PADECAttackReport extends Report implements ApplicationListener {

    private Map<String, Integer> attacksSuccessful;

    public PADECAttackReport() {
        super();
        attacksSuccessful = new LinkedHashMap<>();
    }

    public void gotEvent(String event, Object params, Application app, DTNHost host) {
        // Check that the event is sent by correct application type
        if (!(app instanceof PADECApp))
            return;

        if (event.equalsIgnoreCase("CircumventingConstraintsAttack")) {
            if (!attacksSuccessful.containsKey("Circumventing context constraints")){
                attacksSuccessful.put("Circumventing context constraints", 1);
            }
            else{
                attacksSuccessful.put("Circumventing context constraints", attacksSuccessful.get("Circumventing context constraints")+1);
            }
        }
        if (event.equalsIgnoreCase("InsiderAttack")){
            if (!attacksSuccessful.containsKey("Insider attack")){
                attacksSuccessful.put("Insider attack", 1);
            }
            else{
                attacksSuccessful.put("Insider attack", attacksSuccessful.get("Insider attack")+1);
            }
        }
    }

    @Override
    public void done() {
        write("Attack;Successful executions\n");
        for (String attack: attacksSuccessful.keySet()){
            write(attack+";"+attacksSuccessful.get(attack)+"\n");
        }
        super.done();
    }

}
