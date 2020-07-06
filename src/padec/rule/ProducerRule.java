package padec.rule;

import padec.attribute.Attribute;
import padec.attribute.PADECContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Base producer rule, based on a comparison of the value of a producer attribute with other values.
 */
public class ProducerRule extends Rule {

    /**
     * Attribute whose value is to be compared.
     */
    private Class<? extends Attribute> attribute;
    /**
     * PADECContext to get the attribute value from.
     */
    private PADECContext context;
    /**
     * Values to compare the attribute with.
     */
    private Object[] values;
    /**
     * Comparison operator to be used.
     */
    private ComparisonOperator operator;

    public ProducerRule(Class<? extends Attribute> attribute, PADECContext context, Object[] values, ComparisonOperator operator) {
        this.attribute = attribute;
        this.context = context;
        this.values = values;
        this.operator = operator;
    }

    @Override
    public List<Class<? extends Attribute>> getAttributes() {
        // This only counts for the key/keyhole, so no attributes are being used.
        return new ArrayList<>();
    }

    @Override
    public boolean check(Map<String, Object> values) {
        boolean result = false;
        Attribute attr = context.getAttribute(attribute);
        if (attr!=null){
            Object value = attr.getValue();
            result = operator.operate(value, this.values);
        }
        return result;
    }
}
