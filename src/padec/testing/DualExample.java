package padec.testing;

import padec.application.Endpoint;
import padec.attribute.Location;
import padec.attribute.PADECContext;
import padec.attribute.Pair;
import padec.filtering.FilteredData;
import padec.filtering.techniques.BasicFuzzy;
import padec.key.Key;
import padec.lock.Lock;
import padec.rule.DualRule;
import padec.rule.Rule;
import padec.rule.operator.LessThanOperator;
import padec.rule.operator.RangeOperator;

public class DualExample {

    public static void main(String[] args) {
        PADECContext consumerContext = new PADECContext();
        PADECContext providerContext = new PADECContext();

        consumerContext.registerAttribute(Location.class);
        Location conLoc = (Location) consumerContext.getAttribute(Location.class);
        conLoc.setValue(new Pair<>(43.58898, 90.0));

        providerContext.registerAttribute(Location.class);
        Location provLoc = (Location) providerContext.getAttribute(Location.class);
        provLoc.setValue(new Pair<>(0.0, 0.0));

        Rule withinRange = new DualRule(Location.class, new Double[]{100.0}, new RangeOperator(), new LessThanOperator(), providerContext);

        BasicFuzzy filter = new BasicFuzzy();

        Endpoint mockEndpoint = (Endpoint<Double>) parameters -> 15.0;

        Lock lock = new Lock(mockEndpoint);
        lock.addAccessLevel(filter, 1.0, withinRange);

        Key conKey = new Key(lock.getMaxAccessLevel().getKeyhole(), consumerContext);
        FilteredData result = lock.getMaxAccessLevel().testAccess(new Object[]{}, conKey);

        System.out.println("Data: " + result.getData());
        System.out.println("Precision: " + result.getPrecision());
    }
}
