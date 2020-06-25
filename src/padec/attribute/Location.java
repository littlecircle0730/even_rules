package padec.attribute;

public class Location extends Attribute<Pair<Double, Double>> {

    public Location(){
        super((Class<Pair<Double, Double>>) new Pair<Double, Double>().getClass());
    }
}