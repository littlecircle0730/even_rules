package report;

import applications.PADECApp;
import core.Application;
import core.ApplicationListener;
import core.DTNHost;

public class PADECAppReporter extends Report implements ApplicationListener {
    private int requestsSent=0, requestsReceived=0;
    private int keyholesReceived=0, keysReceived=0;
    private int granted=0, denied=0;

    public void gotEvent(String event, Object params, Application app,
                         DTNHost host) {
        // Check that the event is sent by correct application type
        if (!(app instanceof PADECApp)) return;

        // Increment the counters based on the event type
        if (event.equalsIgnoreCase("PADECRequest")) {
            requestsSent++;
        }
        if (event.equalsIgnoreCase("GotKeyholeRequest")) {
            requestsReceived++;
        }
        if (event.equalsIgnoreCase("GotKeyhole")) {
            keyholesReceived++;
        }
        if (event.equalsIgnoreCase("GotKey")) {
            keysReceived++;
        }
        if (event.equalsIgnoreCase("AccessGranted")) {
            granted++;
        }
        if (event.equalsIgnoreCase("AccessDenied")) {
            denied++;
        }

    }


    @Override
    public void done() {
        write("PADEC stats for scenario " + getScenarioName() +
                "\nsim_time: " + format(getSimTime()));

        String statsText = "Requests sent: " + this.requestsSent +
                "\nRequests received: " + this.requestsReceived +
                "\nKeyholes received: " + this.keyholesReceived +
                "\nKeys received: " + this.keysReceived +
                "\nGranted accesses: " + this.granted +
                "\nDenied accesses: " + this.denied
                ;

        write(statsText);
        super.done();
    }
}
