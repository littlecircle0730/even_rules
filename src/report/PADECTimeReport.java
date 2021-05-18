package report;

import applications.PADECApp;
import core.Application;
import core.ApplicationListener;
import core.DTNHost;

import java.util.Map;
import java.util.LinkedHashMap;

public class PADECTimeReport extends Report implements ApplicationListener {

    private Map<String, Long> khCalcInitial;
    private Map<String, Long> khCalcFinal;
    private Map<String, Long> khChoiceInitial;
    private Map<String, Long> khChoiceFinal;
    private Map<String, Long> ruleEvalInitial;
    private Map<String, Long> ruleEvalFinal;

    public PADECTimeReport(){
        super();
        khCalcInitial = new LinkedHashMap<>();
        khCalcFinal = new LinkedHashMap<>();
        khChoiceInitial = new LinkedHashMap<>();
        khChoiceFinal = new LinkedHashMap<>();
        ruleEvalInitial = new LinkedHashMap<>();
        ruleEvalFinal = new LinkedHashMap<>();
    }

    public void gotEvent(String event, Object params, Application app, DTNHost host) {
        // Check that the event is sent by correct application type
        if (!(app instanceof PADECApp))
            return;

        if (event.equalsIgnoreCase("CalcKHStart")){
            Map<String, Object> m = (Map<String, Object>) params;
            khCalcInitial.put((String) m.get("transID"), (Long) m.get("time"));
        }
        if (event.equalsIgnoreCase("CalcKHEnd")) {
            Map<String, Object> m = (Map<String, Object>) params;
            khCalcFinal.put((String) m.get("transID"), (Long) m.get("time"));
        }
        if (event.equalsIgnoreCase("ChooseKHStart")) {
            Map<String, Object> m = (Map<String, Object>) params;
            khChoiceInitial.put((String) m.get("transID"), (Long) m.get("time"));
        }
        if (event.equalsIgnoreCase("ChooseKHEnd")) {
            Map<String, Object> m = (Map<String, Object>) params;
            khChoiceFinal.put((String) m.get("transID"), (Long) m.get("time"));
        }
        if (event.equalsIgnoreCase("RuleEvalStart")) {
            Map<String, Object> m = (Map<String, Object>) params;
            ruleEvalInitial.put((String) m.get("transID"), (Long) m.get("time"));
        }
        if (event.equalsIgnoreCase("RuleEvalEnd")) {
            Map<String, Object> m = (Map<String, Object>) params;
            ruleEvalFinal.put((String) m.get("transID"), (Long) m.get("time"));
        }
    }

    @Override
    public void done() {
        write("Kind;Transaction ID;Initial time (ms);Final time (ms)\n");
        for (String transId : khCalcFinal.keySet()){
            write("Keyhole calculation;"+transId+";"+khCalcInitial.get(transId)+";"+khCalcFinal.get(transId)
                    + "\n");
        }
        for (String transId : khChoiceFinal.keySet()) {
            write("Keyhole choice;" + transId + ";" + khChoiceInitial.get(transId) + ";" + khChoiceFinal.get(transId)
                    + "\n");
        }
        for (String transId : ruleEvalFinal.keySet()) {
            write("Rule evaluation;" + transId + ";" + ruleEvalInitial.get(transId) + ";" + ruleEvalFinal.get(transId)
                    + "\n");
        }
        write("PADEC party information statistics for scenario " + getScenarioName() + "\nsim_time: "
                + format(getSimTime()));
        super.done();
    }
    
}
