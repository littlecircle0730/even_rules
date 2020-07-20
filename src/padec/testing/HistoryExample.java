package padec.testing;

import padec.application.Endpoint;
import padec.application.HistoryEndpoint;
import padec.attribute.Location;
import padec.attribute.PADECContext;
import padec.util.Pair;
import padec.filtering.FilteredData;
import padec.filtering.techniques.HistoryFuzzy;
import padec.key.Key;
import padec.lock.Lock;
import padec.rule.ComposedRule;
import padec.rule.ConsumerRule;
import padec.rule.Rule;
import padec.rule.operator.AndOperator;
import padec.rule.operator.GreaterThanOperator;
import padec.rule.operator.LessThanOperator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class HistoryExample {

    public static void main(String[] args) throws ParseException {
        PADECContext consumerContext = new PADECContext();

        consumerContext.registerAttribute(Location.class);
        Location conLoc = (Location) consumerContext.getAttribute(Location.class);
        conLoc.setValue(new Pair<>(15.5, 0.0));

        Rule withinAreaMax = new ConsumerRule(Location.class, new Pair[]{new Pair<>(20., 15.)}, new LessThanOperator());
        Rule withinAreaMin = new ConsumerRule(Location.class, new Pair[]{new Pair<>(-1., -1.)}, new GreaterThanOperator());
        Rule alRule = new ComposedRule(withinAreaMax, withinAreaMin, new AndOperator());

        HistoryFuzzy filter = new HistoryFuzzy();

        Endpoint myEndpoint = new HistoryEndpoint("padec_history/Histo1.json");

        Lock lock = new Lock(myEndpoint);
        Map<String, Object> params = new HashMap<>();
        params.put(HistoryFuzzy.AT_LEAST_TIMES_KEY, 3);
        params.put(HistoryFuzzy.AFTER_DATE_KEY, new SimpleDateFormat("yyyy-MM-dd").parse("2020-03-01"));
        lock.addAccessLevel(filter, params, alRule);

        Key conKey = new Key(lock.getMaxAccessLevel().getKeyhole(), consumerContext);
        FilteredData result = lock.getMaxAccessLevel().testAccess(new HashMap<>(), conKey);

        System.out.println("Data: " + result.getData());
        System.out.println("Precision: " + result.getPrecision());
    }

}
