package padec.rule.operator;

import padec.rule.ComparisonOperator;

import java.util.Objects;

public class EqualOperator implements ComparisonOperator {
    @Override
    public boolean operate(Object a, Object[] b) {
        return b.length == 1 && Objects.equals(a, b[0]);
    }
}
