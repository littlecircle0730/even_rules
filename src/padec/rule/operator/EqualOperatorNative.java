package padec.rule.operator;

import padec.rule.CombineOperator;
import padec.rule.ComparisonOperator;
import padec.natpsi.NativeMeth;
import padec.crypto.SimpleCrypto;

public class EqualOperatorNative implements ComparisonOperator, CombineOperator {
    @Override
    public boolean operate(Object a, Object[] b) {
        if (b == null || b.length != 1 || a == null){
            return false;
        }
        final boolean[] result = new boolean[1];
        Thread clientThread = new Thread(new Runnable(){
            public void run(){
                int aInt = a instanceof Integer ? ((Integer) a).intValue() : SimpleCrypto.integerify(a);
                NativeMeth nm = new NativeMeth();
                nm.singleInputClient(aInt >= 0 ? aInt : -aInt);
            }
        });
        Thread serverThread = new Thread(new Runnable(){
            public void run(){
                int bInt = b[0] instanceof Integer ? ((Integer) b[0]).intValue() : SimpleCrypto.integerify(b[0]);
                NativeMeth nm = new NativeMeth();
                result[0] = nm.singleInputServer(bInt >= 0 ? bInt : -bInt, NativeMeth.SingleCircuitType.CIRC_EQUAL.ordinal()) == 1;
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

    @Override
    public Object combine(Object a, Object b) {
        final Object[] result = new Object[1];
        result[0] = null;
        if(a != null && b != null){
            Thread clientThread = new Thread(new Runnable() {
                public void run() {
                    int aInt = a instanceof Integer ? ((Integer) a).intValue() : SimpleCrypto.integerify(a);
                    NativeMeth nm = new NativeMeth();
                    nm.singleInputClient(aInt);
                }
            });
            Thread serverThread = new Thread(new Runnable() {
                public void run() {
                    int bInt = b instanceof Integer ? ((Integer) b).intValue() : SimpleCrypto.integerify(b);
                    NativeMeth nm = new NativeMeth();
                    result[0] = nm.singleInputServer(
                            bInt, NativeMeth.SingleCircuitType.CIRC_EQUAL.ordinal()) == 1;
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
        }
        return result;
    }
}
