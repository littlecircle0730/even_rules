package padec.rule.operator;

import padec.rule.ComparisonOperator;

public class SkipOperator implements ComparisonOperator {
    @Override
    public boolean operate(Object a, Object[] b) {
        return true;
    }
}