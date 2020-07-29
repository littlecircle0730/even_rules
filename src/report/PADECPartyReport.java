package report;

import applications.PADECApp;
import core.Application;
import core.ApplicationListener;
import core.DTNHost;
import core.Message;
import padec.crypto.SimpleCrypto;
import padec.filtering.FilteredData;
import padec.key.Key;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class PADECPartyReport extends Report implements ApplicationListener {

    private Map<String, Set<String>> infoPerParty;

    public PADECPartyReport() {
        super();
        infoPerParty = new LinkedHashMap<>();
    }

    private void createSetIfNotExist(DTNHost host) {
        if (!infoPerParty.containsKey(host.toString())) {
            infoPerParty.put(host.toString(), new HashSet<>());
        }
    }

    public void gotEvent(String event, Object params, Application app,
                         DTNHost host) {
        // Check that the event is sent by correct application type
        if (!(app instanceof PADECApp)) return;

        if (event.equalsIgnoreCase("GotKeyhole")) {
            Message m = (Message) params;
            createSetIfNotExist(m.getTo());
            byte[] encK = (byte[]) m.getProperty(PADECApp.KEY_KEY);
            Key k = (Key) SimpleCrypto.getInstance().decrypt(encK, PADECApp.getCryptoKeys().get(m.getTo().getAddress()).getPrivate());
            for (String attr : k.getData().keySet()) {
                String formattedAttr = attr + " from " + m.getFrom().toString();
                infoPerParty.get(m.getTo().toString()).add(formattedAttr);
            }
        }
        if (event.equalsIgnoreCase("GotKey")) {
            Message m = (Message) params;
            createSetIfNotExist(m.getTo());
            byte[] dataEnc = (byte[]) m.getProperty(PADECApp.INFO_DATA);
            if (dataEnc != null) {
                FilteredData data = (FilteredData) SimpleCrypto.getInstance().decrypt(dataEnc, PADECApp.getCryptoKeys().get(m.getTo().getAddress()).getPrivate());
                infoPerParty.get(m.getTo().toString()).add("History (precision " + data.getPrecision() + ") from " + m.getFrom().toString());
            }
        }
    }


    @Override
    public void done() {
        write("PADEC party information statistics for scenario " + getScenarioName() +
                "\nsim_time: " + format(getSimTime()));
        for (String host : infoPerParty.keySet()) {
            write(host + ": " + infoPerParty.get(host) + "\n");
        }
        super.done();
    }
}
