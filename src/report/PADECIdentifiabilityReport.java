package report;

import applications.PADECApp;
import core.Application;
import core.ApplicationListener;
import core.DTNHost;
import padec.util.Pair;

import java.util.ArrayList;
import java.util.List;


public class PADECIdentifiabilityReport extends Report implements ApplicationListener {
    private List<Pair<Integer, Double>> identifiabilityData = new ArrayList<>();

    public void gotEvent(String event, Object params, Application app,
                         DTNHost host) {
        // Check that the event is sent by correct application type
        if (!(app instanceof PADECApp)) return;

        // Increment the counters based on the event type
        if (!event.equalsIgnoreCase("IdentifiabilityReport")) return;

        Pair<Integer, Double> res = (Pair<Integer, Double>) params;
        identifiabilityData.add(res);
    }


    @Override
    public void done() {
        write("Nodes with same context;Nodes with same context (%)");
        for (Pair<Integer, Double> row : identifiabilityData) {
            write(row.getA() + ";" + row.getB());
        }
        super.done();
    }
}
