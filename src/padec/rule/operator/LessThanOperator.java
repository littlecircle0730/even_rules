package padec.rule.operator;

import padec.rule.ComparisonOperator;

public class LessThanOperator implements ComparisonOperator {
    @Override
    public boolean operate(Object a, Object[] b) {
        boolean result = b.length == 1;
        result = result && a instanceof Comparable;
        result = result && b[0] instanceof Comparable;
        if (result) {
            result = ((Comparable) a).compareTo(b[0]) < 0;
        }
        return result;
    }
}
