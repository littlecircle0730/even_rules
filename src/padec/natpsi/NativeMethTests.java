package padec.natpsi;

public class NativeMethTests {
    public static void main(String[] args) {
        NativeMeth nm = new NativeMeth();
        System.out.println(nm.singleInputServer(5, NativeMeth.SingleCircuitType.CIRC_EQUAL.ordinal()));
    }
}
