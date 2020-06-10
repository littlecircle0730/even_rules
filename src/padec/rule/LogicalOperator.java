package padec.rule;

import java.util.Map;

/**
 * Logical operator.
 */
public interface LogicalOperator {
    /**
     * Logically operates with booleans.
     * @param a Left-hand-side boolean.
     * @param b Right-hand-side boolean.
     * @return Result of the operation.
     */
    boolean operate(Boolean a, Boolean b);
}
