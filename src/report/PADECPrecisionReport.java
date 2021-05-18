package report;

import applications.PADECApp;
import core.Application;
import core.ApplicationListener;
import core.DTNHost;

import java.util.Map;
import java.util.UUID;
import java.util.LinkedHashMap;
import padec.util.Pair;

public class PADECPrecisionReport extends Report implements ApplicationListener {

    private Map<String, Pair<Double, Double>> data;
    
    public PADECPrecisionReport(){
        super();
        data = new LinkedHashMap<>();
    }

    public void gotEvent(String event, Object params, Application app, DTNHost host){
        if (!(app instanceof PADECApp))
            return;
        
        if (event.equalsIgnoreCase("ReqProvPrec")){
            Pair<Double, Double> dPair = (Pair<Double, Double>) params;
            UUID key = UUID.randomUUID();
            data.put(key.toString(), dPair);
        }
    }

    @Override
    public void done(){
        write("Transaction ID;Required precision;Provided precision\n");
        for (String key: data.keySet()){
            write(key+";"+data.get(key).getA()+";"+data.get(key).getB()+"\n");
        }
        super.done();
    }
    
}
