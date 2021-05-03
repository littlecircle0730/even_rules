package padec.rule.operator;

import padec.rule.ComparisonOperator;
import padec.natpsi.NativeMeth;
import padec.crypto.SimpleCrypto;

public class LessThanOperatorNative implements ComparisonOperator {
    @Override
    public boolean operate(Object a, Object[] b) {
        if (b == null || b.length != 1 || a == null) {
            return false;
        }
        final boolean[] result = new boolean[1];
        Thread clientThread = new Thread(new Runnable() {
            public void run() {
                int aInt = a instanceof Integer ? ((Integer) a).intValue() : a instanceof Double ? (int)Math.round((Double) a) : SimpleCrypto.integerify(a);
                NativeMeth nm = new NativeMeth();
                nm.singleInputClient(aInt);
            }
        });
        Thread serverThread = new Thread(new Runnable() {
            public void run() {
                int bInt = b[0] instanceof Integer ? ((Integer) b[0]).intValue() : b[0] instanceof Double ? (int)Math.round((Double) b[0]) : SimpleCrypto.integerify(b[0]);
                NativeMeth nm = new NativeMeth();
                result[0] = nm.singleInputServer(
                        bInt, NativeMeth.SingleCircuitType.CIRC_LESS_THAN.ordinal()) == 1;
            }
        });
        serverThread.start();
        clientThread.start();
        try {
            serverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            result[0] = false;
        }
        return result[0];
    }
}
