package padec.rule;

public interface CombineOperator {
    /**
     * Combines two values in some way (e.g. a+b, a-b, etc.)
     * @param a Value to be combined with b.
     * @param b Value to be combined with a.
     * @return Combined value.
     */
    Object combine(Object a, Object b);
}
