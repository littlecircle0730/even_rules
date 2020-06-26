package padec.attribute;

public class Location extends Attribute<Pair<Double, Double>> {

    Location(){
        super((Class<Pair<Double, Double>>) new Pair<Double, Double>().getClass());
    }
}