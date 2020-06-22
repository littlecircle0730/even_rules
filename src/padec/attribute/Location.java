package padec.attribute;

public class Location extends Attribute<Pair<Double, Double>> {

    private static final String LOCATION_NAME = "location";

    public Location(){
        super((Class<Pair<Double, Double>>) new Pair<Double, Double>().getClass(), LOCATION_NAME);
    }
}