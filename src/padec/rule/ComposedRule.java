package padec.rule;

import padec.attribute.Attribute;

import java.util.*;

/**
 * Rule composed of two rules, joined by a logical operator.
 */
public class ComposedRule extends Rule {

    /**
     * Left-hand-side rule.
     */
    private Rule a;
    /**
     * Right-hand-side rule.
     */
    private Rule b;
    /**
     * Operator to join both rules.
     */
    private LogicalOperator operator;

    public ComposedRule(Rule a, Rule b, LogicalOperator operator) {
        assert a != null;
        this.a = a;
        this.b = b;
        this.operator = operator;
    }

    @Override
    public List<? extends Attribute> getAttributes() {
        // Attributes of a plus attributes of b.
        // Esentially, the query is propagated downwards.
        Set<Attribute> dupRemover = new HashSet<>(); // No duplicates.
        dupRemover.addAll(a.getAttributes());
        if(b != null) {
            dupRemover.addAll(b.getAttributes());
        }
        // Remove duplicates
        List<Attribute> attributes = new ArrayList<>(); // A list is returned.
        attributes.addAll(dupRemover);
        return attributes;
    }

    @Override
    public boolean check(Map<String, Object> values) {
        // Checks are propagated downwards, then the operator is applied.
        if(b != null) {
            return operator.operate(a.check(values), b.check(values));
        }
        else{
            return operator.operate(a.check(values), null);
        }
    }
}
