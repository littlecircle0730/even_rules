package padec.rule.operator;

import padec.rule.LogicalOperator;
import padec.natpsi.NativeMeth;

public class OrOperatorNative implements LogicalOperator {
    @Override
    public boolean operate(Boolean a, Boolean b) {
        if (b == null || a == null) {
            return false;
        }
        final boolean[] result = new boolean[1];
        Thread clientThread = new Thread(new Runnable() {
            public void run() {
                int aInt = a ? 1 : 0;
                NativeMeth nm = new NativeMeth();
                nm.singleInputClient(aInt);
            }
        });
        Thread serverThread = new Thread(new Runnable() {
            public void run() {
                int bInt = b ? 1 : 0;
                NativeMeth nm = new NativeMeth();
                result[0] = nm.singleInputServer(bInt, NativeMeth.SingleCircuitType.CIRC_OR.ordinal()) == 1;
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
