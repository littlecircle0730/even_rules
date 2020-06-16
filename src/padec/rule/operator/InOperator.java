package padec.rule.operator;

import padec.rule.ComparisonOperator;

import java.util.Objects;

public class InOperator implements ComparisonOperator {
    @Override
    public boolean operate(Object a, Object[] b) {
        boolean result = false;
        for(Object element : b){
            if(result){
                break;
            }
            result = Objects.equals(a, b);
        }
        return result;
    }
}
