package padec.filtering.techniques;

import padec.application.information.RunningRoute;
import padec.filtering.FilterTechnique;
import padec.filtering.FilteredData;
import padec.util.Pair;

import java.util.Map;

public class PresenceFilter implements FilterTechnique<Pair<RunningRoute, Double>> {

    public static final String ROUTE_KIND_KEY = "kind";
    public static final String SINCE_SECONDS_KEY = "since";

    @Override
    public FilteredData<Pair<RunningRoute, Double>> filter(Pair<RunningRoute, Double> data, Map<String, Object> parameters) {
        boolean ok = true;
        if (parameters.containsKey(ROUTE_KIND_KEY)) {
            Integer allowedRouteKind = (Integer) parameters.get(ROUTE_KIND_KEY);
            ok = allowedRouteKind == data.getA().getRouteType();
        }
        if (parameters.containsKey(SINCE_SECONDS_KEY) && ok) {
            Double maxAllowedTime = (Double) parameters.get(SINCE_SECONDS_KEY);
            ok = data.getB() <= maxAllowedTime;
        }
        return ok ? new FilteredData<>(data, -Double.MAX_VALUE) : new FilteredData<>(null, Double.MAX_VALUE);
    }

    @Override
    public Double getPrecision(Map<String, Object> parameters) {
        return -Double.MAX_VALUE;
    }
}
