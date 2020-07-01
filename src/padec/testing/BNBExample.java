package padec.testing;

import padec.application.Endpoint;
import padec.attribute.Location;
import padec.attribute.PADECContext;
import padec.attribute.Pair;
import padec.filtering.FilteredData;
import padec.filtering.techniques.BasicFuzzy;
import padec.key.Key;
import padec.lock.AccessLevel;
import padec.lock.Lock;
import padec.rule.ConsumerRule;
import padec.rule.ComposedRule;
import padec.rule.Rule;
import padec.rule.operator.AndOperator;
import padec.rule.operator.GreaterThanOperator;
import padec.rule.operator.LessThanOperator;

/**
 * Simple, Bread-and-Butter example to test access control
 */
public class BNBExample {

    public static void main(String[] args) {
        PADECContext consumerContext = new PADECContext();

        consumerContext.registerAttribute(Location.class);
        Location conLoc = (Location) consumerContext.getAttribute(Location.class);
        conLoc.setValue(new Pair<>(15.5, 0.0));

        Rule withinAreaMax = new ConsumerRule(Location.class, new Pair[]{new Pair<>(20., 15.)}, new LessThanOperator());
        Rule withinAreaMin = new ConsumerRule(Location.class, new Pair[]{new Pair<>(-1., -1.)}, new GreaterThanOperator());
        Rule alRule = new ComposedRule(withinAreaMax, withinAreaMin, new AndOperator());

        BasicFuzzy filter = new BasicFuzzy();

        Endpoint mockEndpoint = (Endpoint<Double>) parameters -> 15.0;

        AccessLevel al = new AccessLevel(filter, mockEndpoint, new Double[]{1.0}, alRule);

        Lock lock = new Lock();
        lock.addAccessLevel(al);

        Key conKey = new Key(lock.getMaxAccessLevel().getKeyhole(), consumerContext);
        FilteredData result = lock.getMaxAccessLevel().testAccess(new Object[]{}, conKey);

        System.out.println("Data: " + result.getData());
        System.out.println("Precision: " + result.getPrecision());
    }
}
