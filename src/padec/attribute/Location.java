package padec.attribute;

public class Location extends Attribute<Pair<Double, Double>> {

    private static final int ATTR_CATEGORY = 6;

    Location(){
        super((Class<Pair<Double, Double>>) new Pair<Double, Double>().getClass());
    }

    public static int getCategory() {
        return ATTR_CATEGORY;
    }
}