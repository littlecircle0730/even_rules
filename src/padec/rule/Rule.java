package padec.rule;

import padec.attribute.Attribute;

import java.util.List;
import java.util.Map;

/**
 * Rule to control access
 */
public abstract class Rule {

    /**
     * Method to get access to the attributes of the rule (to generate a keyhole).
     * @return List of attributes that the rule uses.
     */
    public abstract List<Class<? extends Attribute>> getAttributes();

    /**
     * Method to check if a set of values pass the rule or not.
     * @param values Set of values, indexed by their attribute name.
     * @return True if the rule is passed.
     */
    public abstract boolean check(Map<String, Object> values); // Maybe this becomes a key in the future.
}
