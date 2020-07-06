package padec.rule;

import padec.attribute.Attribute;
import padec.attribute.PADECContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Base dual rule, based on a comparison of the value of a combination between an attribute of the consumer and producer
 * with other values.
 */
public class DualRule extends Rule {

    /**
     * Attribute whose value is to be compared.
     */
    private Class<? extends Attribute> attribute;
    /**
     * Values to compare the attribute with.
     */
    private Object[] values;
    /**
     * Combination operator to be used.
     */
    private CombineOperator combinator;
    /**
     * Comparison operator to be used.
     */
    private ComparisonOperator operator;
    /**
     * PADECContext to get the attribute value from.
     */
    private PADECContext context;

    public DualRule(Class<? extends Attribute> attribute, Object[] values, CombineOperator combinator, ComparisonOperator operator, PADECContext context) {
        this.attribute = attribute;
        this.values = values;
        this.combinator = combinator;
        this.operator = operator;
        this.context = context;
    }

    @Override
    public List<Class<? extends Attribute>> getAttributes() {
        // Extremely simple: only the attribute being used.
        List<Class<? extends Attribute>> res = new ArrayList<>();
        res.add(attribute);
        return res;
    }

    @Override
    public boolean check(Map<String, Object> values) {
        boolean result = false;
        Attribute attr = context.getAttribute(attribute);
        if (attr!=null){
            Object prodValue = attr.getValue();
            Object consValue = values.get(attribute.getName());
            Object value = combinator.combine(prodValue, consValue);
            result = operator.operate(value, this.values);
        }
        return result;
    }
}
