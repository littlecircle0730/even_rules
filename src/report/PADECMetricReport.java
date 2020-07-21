package report;

import applications.PADECApp;
import core.Application;
import core.ApplicationListener;
import core.DTNHost;
import core.Message;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PADECMetricReport extends Report implements ApplicationListener {
    private Map<String, List<Long>> sizes;

    private static final String REQUEST_SIZES = "request";
    private static final String KEYHOLE_SIZES = "keyhole";
    private static final String KEY_SIZES = "key";
    private static final String DATA_SIZES = "data";

    public PADECMetricReport() {
        super();
        sizes = new LinkedHashMap<>();
        sizes.put(REQUEST_SIZES, new ArrayList<>());
        sizes.put(KEYHOLE_SIZES, new ArrayList<>());
        sizes.put(KEY_SIZES, new ArrayList<>());
        sizes.put(DATA_SIZES, new ArrayList<>());
    }

    public void gotEvent(String event, Object params, Application app,
                         DTNHost host) {
        // Check that the event is sent by correct application type
        if (!(app instanceof PADECApp)) return;

        // Increment the counters based on the event type
        if (event.equalsIgnoreCase("PADECRequest")) {
            Message m = (Message) params;
            sizes.get(REQUEST_SIZES).add(m.getPayloadSize());
        }
        if (event.equalsIgnoreCase("GotKeyholeRequest")) {
            Message m = (Message) params;
            sizes.get(KEYHOLE_SIZES).add(m.getPayloadSize());
        }
        if (event.equalsIgnoreCase("GotKeyhole")) {
            Message m = (Message) params;
            sizes.get(KEY_SIZES).add(m.getPayloadSize());
        }
        if (event.equalsIgnoreCase("GotKey")) {
            Message m = (Message) params;
            sizes.get(DATA_SIZES).add(m.getPayloadSize());
        }
    }


    @Override
    public void done() {
        write("PADEC metric statistics for scenario " + getScenarioName() +
                "\nsim_time: " + format(getSimTime()));
        write("Average request size: " +
                format(sizes.get(REQUEST_SIZES).stream().mapToLong(value -> value).average().getAsDouble()));
        write("Average keyhole size: " +
                format(sizes.get(KEYHOLE_SIZES).stream().mapToLong(value -> value).average().getAsDouble()));
        write("Average key size: " +
                format(sizes.get(KEY_SIZES).stream().mapToLong(value -> value).average().getAsDouble()));
        write("Average data size: " +
                format(sizes.get(DATA_SIZES).stream().mapToLong(value -> value).average().getAsDouble()));
        super.done();
    }
}