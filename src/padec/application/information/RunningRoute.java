package padec.application.information;

import padec.util.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class RunningRoute {
    public static final int ROUTE_TYPE_TRAINING = 0;
    public static final int ROUTE_TYPE_SOCIAL = 1;

    private int routeType;
    private List<Pair<Double, Double>> routeCoords;
    private Date routeDate;
    private Double bestTime;

    public static final long RNG_SEED = 0;

    public RunningRoute(int routeType, List<Pair<Double, Double>> routeCoords, Date routeDate, Double bestTime) {
        this.routeType = routeType;
        this.routeCoords = routeCoords;
        this.routeDate = routeDate;
        this.bestTime = bestTime;
    }

    public RunningRoute(int routeType, int maxRouteLen, Date minDate, Date maxDate) {
        this.routeType = routeType;
        Random rng = new Random(RNG_SEED);
        this.routeCoords = new ArrayList<>();
        int routeLen = Math.abs(rng.nextInt(maxRouteLen));
        for (int i = 0; i < routeLen; i++) {
            Pair<Double, Double> pointCoords = new Pair<>();
            pointCoords.setA(rng.nextDouble());
            pointCoords.setB(rng.nextDouble());
            this.routeCoords.add(pointCoords);
        }
        long minTS = minDate.getTime();
        long maxTS = maxDate.getTime();
        this.routeDate = new Date((Math.abs(rng.nextLong()) + minTS) % maxTS);
        this.bestTime = Math.abs(rng.nextDouble());
    }

    public int getRouteType() {
        return routeType;
    }

    public void setRouteType(int routeType) {
        this.routeType = routeType;
    }

    public List<Pair<Double, Double>> getRouteCoords() {
        return routeCoords;
    }

    public void setRouteCoords(List<Pair<Double, Double>> routeCoords) {
        this.routeCoords = routeCoords;
    }

    public Date getRouteDate() {
        return routeDate;
    }

    public void setRouteDate(Date routeDate) {
        this.routeDate = routeDate;
    }
}
