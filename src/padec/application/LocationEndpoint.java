package padec.application;

import padec.util.Pair;

import java.util.Map;

public class LocationEndpoint implements Endpoint<Pair<Double, Double>> {

    private Pair<Double, Double> currLocation;

    public void updateLocation(Pair<Double, Double> currLocation){
        this.currLocation = currLocation;
    }

    @Override
    public Pair<Double, Double> execute(Map<String, Object> parameters) {
        return currLocation;
    }
}
