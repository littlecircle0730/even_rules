package padec.rule;

import padec.attribute.Attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Base rule, based on a comparison of the value of an attribute with other values.
 */
public class BaseRule extends Rule {

    /**
     * Attribute whose value is to be compared.
     */
    private Class<? extends Attribute> attribute;
    /**
     * Values to compare the attribute with.
     */
    private Object[] values;
    /**
     * Comparison operator to be used.
     */
    private ComparisonOperator operator;

    public BaseRule(Class<? extends Attribute> attribute, Object[] values, ComparisonOperator operator) {
        this.attribute = attribute;
        this.values = values;
        this.operator = operator;
    }

    @Override
    public List<Class<? extends Attribute>> getAttributes(){
        // Extremely simple: only the attribute being used.
        List<Class<? extends Attribute>> res = new ArrayList<>();
        res.add(attribute);
        return res;
    }

    @Override
    public boolean check(Map<String, Object> values) {
        boolean result;
        Object value = values.get(attribute.getName());
        result =  operator.operate(value, this.values);
        return result;
    }
}
