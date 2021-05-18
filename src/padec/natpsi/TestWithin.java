package padec.natpsi;

import padec.rule.operator.*;
import padec.util.Pair;

public class TestWithin {
    public static void main(String[] args) {
        RangeOperatorNative ron = new RangeOperatorNative();
        System.out.println(ron.combine(new Pair<>(0.0, 0.0), new Pair<>(1.0, 1.5)));
    }
}
