package report;

import applications.PADECApp;
import core.Application;
import core.ApplicationListener;
import core.DTNHost;


public class PADECErrorReport extends Report implements ApplicationListener {
    private Integer correct = 0;
    private Integer type1Err = 0;
    private Integer type2Err = 0;

    public void gotEvent(String event, Object params, Application app,
                         DTNHost host) {
        // Check that the event is sent by correct application type
        if (!(app instanceof PADECApp)) return;

        // Increment the counters based on the event type
        if (!event.equalsIgnoreCase("ErrorReportMessage")) return;

        Integer res = (Integer) params;
        switch (res) {
            case 0:
                correct++;
                break;
            case 1:
                type1Err++;
                break;
            case 2:
                type2Err++;
                break;
        }
    }


    @Override
    public void done() {
        write("Error type;Error number");
        write("No error;" + correct);
        write("Type I;" + type1Err);
        write("Type II;" + type2Err);
        super.done();
    }
}
