package padec.application;

import padec.application.information.RunningRoute;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class RunningRoutesEndpoint implements Endpoint<List<RunningRoute>> {

    private abstract static class MetaParserToSkipExceptions {
        public static Date parseDate(String date) {
            try {
                return new SimpleDateFormat("dd/MM/yyyy").parse(date);
            } catch (ParseException ex) {
                return new Date();
            }
        }
    }

    public static int MAX_WAYPOINTS = 20;
    public static int MAX_ROUTES = 10;
    public static final long RNG_SEED = 0;
    public static final Date MIN_DATE = MetaParserToSkipExceptions.parseDate("01/01/2012");
    public static final Date MAX_DATE = MetaParserToSkipExceptions.parseDate("31/12/2013");

    private List<RunningRoute> routes;

    public RunningRoutesEndpoint(List<RunningRoute> routes) {
        this.routes = routes;
    }

    public RunningRoutesEndpoint() {
        Random rng = new Random(RNG_SEED);
        int numRoutes = Math.abs(rng.nextInt(MAX_ROUTES));
        routes = new ArrayList<>();
        for (int i = 0; i < numRoutes; i++) {
            int routeType = rng.nextBoolean() ? RunningRoute.ROUTE_TYPE_TRAINING : RunningRoute.ROUTE_TYPE_SOCIAL;
            routes.add(new RunningRoute(routeType, MAX_WAYPOINTS, MIN_DATE, MAX_DATE));
        }
    }

    @Override
    public List<RunningRoute> execute(Map<String, Object> parameters) {
        return routes;
    }
}
