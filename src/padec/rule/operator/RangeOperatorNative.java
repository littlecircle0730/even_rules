package padec.rule.operator;

import padec.util.Pair;
import padec.rule.CombineOperator;
import padec.natpsi.NativeMeth;

/**
 * WARNING!!! To be used in SIMULATIONS!
 * Real GPS coordinates need to have their range calculated with the Haversine formula.
 */
public class RangeOperatorNative implements CombineOperator {
    @Override
    public Object combine(Object a, Object b) {
        final Double[] result = new Double[1];
        result[0] = null;
        if(a instanceof Pair && b instanceof Pair){
            if(((Pair) a).getA() instanceof Double &&
                    ((Pair) a).getB() instanceof Double &&
                    ((Pair) b).getA() instanceof Double &&
                    ((Pair) b).getB() instanceof Double){
                Pair<Double, Double> pairA = (Pair<Double, Double>) a;
                Pair<Double, Double> pairB = (Pair<Double, Double>) b;
                
                Thread clientThread = new Thread(new Runnable() {
                    public void run() {
                        NativeMeth nm = new NativeMeth();
                        nm.multiInputClient(new int[]{Math.abs((int) Math.round(pairA.getA())), Math.abs((int) Math.round(pairA.getB()))});
                    }
                });
                Thread serverThread = new Thread(new Runnable() {
                    public void run() {
                        NativeMeth nm = new NativeMeth();
                        result[0] = Math.sqrt(Double.valueOf(nm.multiInputServer(new int[]{Math.abs((int) Math.round(pairB.getA())), Math.abs((int) Math.round(pairB.getB()))}, NativeMeth.MultiCircuitType.CIRC_RANGE.ordinal())));
                    }
                });
                serverThread.start();
                clientThread.start();
                try {
                    serverThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return result[0];
    }
}
