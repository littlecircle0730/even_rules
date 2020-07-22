package report;

import applications.PADECApp;
import core.Application;
import core.ApplicationListener;
import core.DTNHost;
import core.Message;

import java.util.ArrayList;
import java.util.List;

public class PADECDetailReporter extends Report implements ApplicationListener {
    private List<String> events = new ArrayList<>();

    public void gotEvent(String event, Object params, Application app,
                         DTNHost host) {
        String parsedEvent = "";
        // Check that the event is sent by correct application type
        if (!(app instanceof PADECApp)) return;

        // Increment the counters based on the event type
        if (event.equalsIgnoreCase("PADECRequest")) {
            Message m = (Message) params;
            parsedEvent = "Request " + m.getId() + ". From: " + m.getFrom() + ", to: " + m.getTo() + ".";
        }
        if (event.equalsIgnoreCase("GotKeyholeRequest")) {
            Message m = (Message) params;
            parsedEvent = "Keyhole " + m.getId() + ". From: " + m.getFrom() + ", to: " + m.getTo() + ".";
        }
        if (event.equalsIgnoreCase("GotKeyhole")) {
            Message m = (Message) params;
            parsedEvent = "Key " + m.getId() + ". From: " + m.getFrom() + ", to: " + m.getTo() + ". Minimum precision requested: " + m.getProperty(PADECApp.KEY_MIN_PRECISION) + ", endpoint params: " + m.getProperty(PADECApp.KEY_ENDPOINT_PARAMS) + ".";
        }
        if (event.equalsIgnoreCase("GotKey")) {
            Message m = (Message) params;
            parsedEvent = ((Integer) m.getProperty(PADECApp.MSG_TYPE) == PADECApp.MSG_TYPE_INFO ? "Access granted. From: " : "Access rejected. From: ") + m.getFrom() + ", to: " + m.getTo() + ".";
        }
        if (event.equalsIgnoreCase("AccessGranted")) {
            return;
        }
        if (event.equalsIgnoreCase("AccessDenied")) {
            return;
        }
        if (event.equalsIgnoreCase("AttackedKeyhole")) {
            Message m = (Message) params;
            parsedEvent = "Keyhole attacked. From: " + m.getFrom() + ", to: " + m.getTo() + ".";
        }
        if (event.equalsIgnoreCase("AttackSuccessful")) {
            parsedEvent = "Keyhole attack from " + host + " successful";
        }
        if (event.equalsIgnoreCase("TPartyInfoReveal")) {
            parsedEvent = "Reveal attack from " + host + " started";
        }
        if (event.equalsIgnoreCase("TPartyRevealSuccessful")) {
            parsedEvent = "Reveal attack from " + host + " successful";
        }
        if (event.equalsIgnoreCase("AttackRejected")) {
            parsedEvent = "Attack from " + host + " rejected";
        }
        if (event.equalsIgnoreCase("NoFittingLevel")) {
            parsedEvent = "No fitting level found in " + host;
        }
        if (event.equalsIgnoreCase("FittingLevel")) {
            parsedEvent = "Keyhole in level " + ((int[]) params)[0] + " in " + host + " fits the key sent by " + ((int[]) params)[1];
        }
        events.add(parsedEvent);

    }


    @Override
    public void done() {
        write("PADEC log for scenario " + getScenarioName() +
                "\nsim_time: " + format(getSimTime()));

        StringBuilder logText = new StringBuilder();
        for (String s : events) {
            logText.append(s).append("\n");
        }

        write(logText.toString());
        super.done();
    }
}