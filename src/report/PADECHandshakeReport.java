package report;

import applications.PADECApp;
import core.Application;
import core.ApplicationListener;
import core.DTNHost;

public class PADECHandshakeReport extends Report implements ApplicationListener {
    private int requestsSent = 0, requestsReceived = 0;
    private int keyholesReceived = 0, keysReceived = 0;
    private int granted = 0, denied = 0;
    private int attacks = 0, correct = 0;
    private int tparty = 0, successful = 0;
    private int rejected = 0;

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
        if (event.equalsIgnoreCase("AttackedKeyhole")) {
            attacks++;
        }
        if (event.equalsIgnoreCase("AttackSuccessful")) {
            correct++;
        }
        if (event.equalsIgnoreCase("TPartyInfoReveal")) {
            tparty++;
        }
        if (event.equalsIgnoreCase("TPartyRevealSuccessful")) {
            successful++;
        }
        if (event.equalsIgnoreCase("AttackRejected")) {
            rejected++;
        }

    }


    @Override
    public void done() {
        write("Request status;Number");
        write("Successful handshakes;" + this.granted + this.denied);
        write("Granted or denied message lost;" + (this.keysReceived - (this.granted + this.denied)));
        write("Key lost;" + (this.keyholesReceived - this.keysReceived));
        write("Keyhole lost;" + (this.requestsReceived - this.keyholesReceived));
        write("Request lost;" + (this.requestsSent - this.requestsReceived));
        super.done();
    }
}