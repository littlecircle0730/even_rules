package padec.rule.operator;

import padec.rule.LogicalOperator;

public class AndOperator implements LogicalOperator {
    @Override
    public boolean operate(Boolean a, Boolean b) {
        return b!=null && a && b;
    }
}
