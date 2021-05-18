package padec.rule.operator;

import padec.rule.ComparisonOperator;
import padec.rule.operator.EqualOperatorNative;

import java.util.Collection;
import java.util.Objects;

public class InOperatorNative implements ComparisonOperator {
    @Override
    public boolean operate(Object a, Object[] b) {
        boolean result = b == null || b.length == 0;
        if (!(a instanceof Collection) && !a.getClass().isArray()) {
            for (Object element : b) {
                if (result) {
                    break;
                }
                result = new EqualOperatorNative().operate(a, new Object[]{element});
            }
        } else {
            if (a.getClass().isArray()) {
                Object[] aArray = (Object[]) a;
                for (Object original : aArray) {
                    for (Object element : b) {
                        if (result) {
                            break;
                        }
                        result = new EqualOperatorNative().operate(original, new Object[]{element});
                    }
                }
            } else {
                Collection aCol = (Collection) a;
                for (Object original : aCol) {
                    for (Object element : b) {
                        if (result) {
                            break;
                        }
                        result = new EqualOperatorNative().operate(original, new Object[]{element});
                    }
                }
            }
        }
        return result;
    }
}
