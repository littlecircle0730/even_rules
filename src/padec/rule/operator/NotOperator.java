package padec.rule.operator;

import padec.rule.LogicalOperator;

public class NotOperator implements LogicalOperator {
    @Override
    public boolean operate(Boolean a, Boolean b) {
        return !a;
    }
}
