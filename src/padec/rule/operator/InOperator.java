package padec.rule.operator;

import padec.rule.ComparisonOperator;

import java.util.Collection;
import java.util.Objects;

public class InOperator implements ComparisonOperator {
    @Override
    public boolean operate(Object a, Object[] b) {
        boolean result = b == null || b.length == 0;
        if (!(a instanceof Collection) && !a.getClass().isArray()) {
            for (Object element : b) {
                if (result) {
                    break;
                }
                result = Objects.equals(a, element);
            }
        } else {
            if (a.getClass().isArray()) {
                Object[] aArray = (Object[]) a;
                for (Object original : aArray) {
                    for (Object element : b) {
                        if (result) {
                            break;
                        }
                        result = Objects.equals(original, element);
                    }
                }
            } else {
                Collection aCol = (Collection) a;
                for (Object original : aCol) {
                    for (Object element : b) {
                        if (result) {
                            break;
                        }
                        result = Objects.equals(original, element);
                    }
                }
            }
        }
        return result;
    }
}
