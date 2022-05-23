package report;

import applications.PADECApp;
import core.Application;
import core.ApplicationListener;
import core.DTNHost;
import padec.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class PADECKeyIdentifiabilityReport extends Report implements ApplicationListener {

    private List<Pair<Integer, String>> identifiabilityData = new ArrayList<>();

    public void gotEvent(String event, Object params, Application app,
                         DTNHost host) {
        // Check that the event is sent by correct application type
        if (!(app instanceof PADECApp)) return;

        // Increment the counters based on the event type
        if (!event.equalsIgnoreCase("KeyIdentifiabilityReport")) return;

        Pair<Integer, String> res = (Pair<Integer, String>) params;
        identifiabilityData.add(res);
    }


    @Override
    public void done() {
        write("Nodes with same context;Key");
        for (Pair<Integer, String> row : identifiabilityData) {
            write(row.getA() + ";" + row.getB());
        }
        super.done();
    }
}
