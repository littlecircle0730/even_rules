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
    private Map<String, List<Double>> metrics;

    private static final String REQUEST_SIZES = "request";
    private static final String KEYHOLE_SIZES = "keyhole";
    private static final String KEY_SIZES = "key";
    private static final String DATA_SIZES = "data";
    private static final String SENT_DATA = "sent";
    private static final String KEY_CATEGORY = "category";
    private static final String ATTR_SENT = "attributes";
    private static final String PRECISION = "precision";

    public PADECMetricReport() {
        super();
        metrics = new LinkedHashMap<>();
        metrics.put(REQUEST_SIZES, new ArrayList<>());
        metrics.put(KEYHOLE_SIZES, new ArrayList<>());
        metrics.put(KEY_SIZES, new ArrayList<>());
        metrics.put(DATA_SIZES, new ArrayList<>());
        metrics.put(SENT_DATA, new ArrayList<>());
        metrics.put(KEY_CATEGORY, new ArrayList<>());
        metrics.put(ATTR_SENT, new ArrayList<>());
        metrics.put(PRECISION, new ArrayList<>());
    }

    public void gotEvent(String event, Object params, Application app,
                         DTNHost host) {
        // Check that the event is sent by correct application type
        if (!(app instanceof PADECApp)) return;

        // Increment the counters based on the event type
        if (event.equalsIgnoreCase("PADECRequest")) {
            Message m = (Message) params;
            metrics.get(REQUEST_SIZES).add((double) m.getPayloadSize());
        }
        if (event.equalsIgnoreCase("GotKeyholeRequest")) {
            Message m = (Message) params;
            metrics.get(KEYHOLE_SIZES).add((double) m.getPayloadSize());
        }
        if (event.equalsIgnoreCase("GotKeyhole")) {
            Message m = (Message) params;
            metrics.get(KEY_SIZES).add((double) m.getPayloadSize());
        }
        if (event.equalsIgnoreCase("GotKey")) {
            Message m = (Message) params;
            metrics.get(DATA_SIZES).add((double) m.getPayloadSize());
        }
        if (event.equalsIgnoreCase("InfSentMetric")) {
            metrics.get(SENT_DATA).add((double) params);
        }
        if (event.equalsIgnoreCase("KeyCategory")) {
            metrics.get(KEY_CATEGORY).add((double) params);
        }
        if (event.equalsIgnoreCase("AttributesSent")) {
            metrics.get(ATTR_SENT).add((double) params);
        }
        if (event.equalsIgnoreCase("PrecisionGot")) {
            metrics.get(PRECISION).add((double) params);
        }
    }


    @Override
    public void done() {
        write("PADEC metric statistics for scenario " + getScenarioName() +
                "\nsim_time: " + format(getSimTime()));
        write("Average request size: " +
                format(metrics.get(REQUEST_SIZES).stream().mapToDouble(value -> value).average().getAsDouble()));
        write("Average keyhole size: " +
                format(metrics.get(KEYHOLE_SIZES).stream().mapToDouble(value -> value).average().getAsDouble()));
        write("Average key size: " +
                format(metrics.get(KEY_SIZES).stream().mapToDouble(value -> value).average().getAsDouble()));
        write("Average data size: " +
                format(metrics.get(DATA_SIZES).stream().mapToDouble(value -> value).average().getAsDouble()));
        write("Average percentage of full history sent: " +
                format(metrics.get(SENT_DATA).stream().mapToDouble(value -> value).average().getAsDouble() * 100));
        write("Average category of data released as key: " +
                format(metrics.get(KEY_CATEGORY).stream().mapToDouble(value -> value).average().getAsDouble()));
        write("Minimum category of data released as key: " +
                format(metrics.get(KEY_CATEGORY).stream().mapToDouble(value -> value).min().getAsDouble()));
        write("Average number of attributes released as key: " +
                format(metrics.get(ATTR_SENT).stream().mapToDouble(value -> value).average().getAsDouble()));
        write("Average precision of data obtained: " +
                format(metrics.get(PRECISION).stream().mapToDouble(value -> value).average().getAsDouble()));
        super.done();
    }
}