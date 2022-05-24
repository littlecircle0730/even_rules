package padec.application;

import padec.application.information.RunningRoute;
import padec.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.stream.DoubleStream;

public class PresenceEndpoint implements Endpoint<Pair<RunningRoute, Double>> {

    public static final String TIME_KEY = "time";

    private Pair<Double, Double> currLocation = null;
    private List<RunningRoute> routeList = null;
    private RunningRoute currentRoute = null;
    private Double enteredTime = 0.0;

    public void updateRoutes(List<RunningRoute> routeList, Double updateTime) {
        this.routeList = routeList;
        updateCurrentRoute(updateTime);
    }

    private void updateCurrentRoute(Double time) {
        RunningRoute olderRoute = currentRoute;
        currentRoute = null;
        if (currLocation != null && routeList != null) {
            for (RunningRoute route : routeList) {
                DoubleStream coordsX = route.getRouteCoords().stream().mapToDouble(Pair::getA);
                DoubleStream coordsY = route.getRouteCoords().stream().mapToDouble(Pair::getB);
                Double minX = coordsX.min().getAsDouble();
                Double minY = coordsY.min().getAsDouble();
                Double maxX = coordsX.max().getAsDouble();
                Double maxY = coordsY.max().getAsDouble();
                if (currLocation.getA() >= minX && currLocation.getA() <= maxX &&
                        currLocation.getB() >= minY && currLocation.getB() <= maxY) {
                    currentRoute = route;
                    if (currentRoute != olderRoute) {
                        enteredTime = time;
                    }
                    break;
                }
            }
        }
    }

    public void updateLocation(Pair<Double, Double> currLocation, Double updateTime) {
        this.currLocation = currLocation;
        updateCurrentRoute(updateTime);
    }

    @Override
    public Pair<RunningRoute, Double> execute(Map<String, Object> parameters) {
        Double time = (Double) parameters.getOrDefault(TIME_KEY, Double.MAX_VALUE);
        return new Pair<>(currentRoute, time - enteredTime);
    }
}
