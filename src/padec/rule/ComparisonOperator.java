package padec.rule;

/**
 * Simple operator to perform comparisons.
 */
public interface ComparisonOperator {
    /**
     * Operates with values to get a comparison result.
     * @param a Value to be compared (i.e. the value for the attribute).
     * @param b Values to compare a with (i.e. values set in the rule).
     * @return Result of the comparison.
     */
    boolean operate(Object a, Object[] b);
}
