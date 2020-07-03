package padec.rule.operator;

import padec.rule.CombineOperator;
import padec.rule.ComparisonOperator;

import java.util.Objects;

public class EqualOperator implements ComparisonOperator, CombineOperator {
    @Override
    public boolean operate(Object a, Object[] b) {
        return b.length == 1 && Objects.equals(a, b[0]);
    }

    @Override
    public Object combine(Object a, Object b) {
        Object result = null;
        if(a != null && b != null){
            result = a.equals(b);
        }
        return result;
    }
}
