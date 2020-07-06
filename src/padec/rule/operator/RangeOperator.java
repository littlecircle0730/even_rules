package padec.rule.operator;

import padec.attribute.Pair;
import padec.rule.CombineOperator;

/**
 * WARNING!!! To be used in SIMULATIONS!
 * Real GPS coordinates need to have their range calculated with the Haversine formula.
 */
public class RangeOperator implements CombineOperator {
    @Override
    public Object combine(Object a, Object b) {
        Double combined = null;
        if(a instanceof Pair && b instanceof Pair){
            if(((Pair) a).getA() instanceof Double &&
                    ((Pair) a).getB() instanceof Double &&
                    ((Pair) b).getA() instanceof Double &&
                    ((Pair) b).getB() instanceof Double){
                Pair<Double, Double> pairA = (Pair<Double, Double>) a;
                Pair<Double, Double> pairB = (Pair<Double, Double>) b;
                combined = Math.sqrt(Math.pow(pairA.getA()-pairB.getA(), 2)+Math.pow(pairA.getB()-pairB.getB(), 2));
            }
        }
        return combined;
    }
}
