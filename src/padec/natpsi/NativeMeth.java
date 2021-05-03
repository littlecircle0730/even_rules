package padec.natpsi;

public class NativeMeth {

    static {
        System.loadLibrary("JNIMeth");
    }

    public enum SingleCircuitType {
        CIRC_LESS_THAN,
        CIRC_GREATER_THAN,
        CIRC_EQUAL,
        CIRC_AND,
        CIRC_OR
    };

    public enum MultiCircuitType {
        CIRC_RANGE,
        CIRC_WITHIN
    };

    public native int singleInputServer(int input, int type);
    public native void singleInputClient(int input);

    public native int multiInputServer(int[] input, int type);
    public native void multiInputClient(int[] input);
}
