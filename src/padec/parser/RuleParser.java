package padec.parser;

import org.yaml.snakeyaml.Yaml;
import padec.attribute.PADECContext;
import padec.rule.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RuleParser {

    private static final String RULE_KEY = "rule";
    private static final String TYPE_KEY = "type";
    private static final String ATTRIBUTE_KEY = "attribute";
    private static final String VALUES_KEY = "values";
    private static final String COMBINATOR_KEY = "combinator";
    private static final String OPERATOR_KEY = "operator";
    private static final String RULEA_KEY = "ruleA";
    private static final String RULEB_KEY = "ruleB";

    public static Rule parse(String file, PADECContext context) {
        Rule rule = null;
        try {
            InputStream inputStream =
                    new FileInputStream(file);
            Yaml yaml = new Yaml();
            Map<String, Object> yamlObj = yaml.load(inputStream);
            Map<String, Object> iRule = (Map<String, Object>) yamlObj.get(RULE_KEY);

            rule = parse(iRule, context);

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassCastException ex) {
            System.err.println("Could not cast object properly. " + ex);
            ex.printStackTrace();
        }
        return rule;
    }

    public static Rule parse(Map<String, Object> iRule, PADECContext context) {
        Rule rule = null;
        try {
            switch (((String) iRule.get(TYPE_KEY)).toUpperCase(Locale.getDefault())) {
                case "PRODUCER":
                    Class attr = ClassLoader.getSystemClassLoader().loadClass((String) iRule.get(ATTRIBUTE_KEY));
                    Object[] values = ((List<Object>) iRule.get(VALUES_KEY)).toArray();
                    ComparisonOperator op = (ComparisonOperator)
                            ClassLoader.getSystemClassLoader().loadClass((String) iRule.get(OPERATOR_KEY))
                                    .newInstance();
                    rule = new ProducerRule(attr, context, values, op);
                    break;
                case "CONSUMER":
                    attr = ClassLoader.getSystemClassLoader().loadClass((String) iRule.get(ATTRIBUTE_KEY));
                    values = ((List<Object>) iRule.get(VALUES_KEY)).toArray();
                    op = (ComparisonOperator)
                            ClassLoader.getSystemClassLoader().loadClass((String) iRule.get(OPERATOR_KEY))
                                    .newInstance();
                    rule = new ConsumerRule(attr, values, op);
                    break;
                case "DUAL":
                    attr = ClassLoader.getSystemClassLoader().loadClass((String) iRule.get(ATTRIBUTE_KEY));
                    values = ((List<Object>) iRule.get(VALUES_KEY)).toArray();
                    CombineOperator comb = (CombineOperator)
                            ClassLoader.getSystemClassLoader().loadClass((String) iRule.get(COMBINATOR_KEY))
                                    .newInstance();
                    op = (ComparisonOperator)
                            ClassLoader.getSystemClassLoader().loadClass((String) iRule.get(OPERATOR_KEY))
                                    .newInstance();
                    rule = new DualRule(attr, values, comb, op, context);
                    break;
                case "COMPOSED":
                    //Rule A
                    Map<String, Object> iRuleA = (Map<String, Object>) iRule.get(RULEA_KEY);
                    Rule ruleA = parse(iRuleA, context);

                    //Rule B
                    Map<String, Object> iRuleB = (Map<String, Object>) iRule.get(RULEB_KEY);
                    Class attrB = ClassLoader.getSystemClassLoader().loadClass((String) iRuleB.get(ATTRIBUTE_KEY));
                    Rule ruleB = parse(iRuleB, context);

                    //A & B
                    LogicalOperator opComb = (LogicalOperator)
                            ClassLoader.getSystemClassLoader().loadClass((String) iRule.get(OPERATOR_KEY))
                                    .newInstance();
                    rule = new ComposedRule(ruleA, ruleB, opComb);
                    break;
            }
        } catch (ClassCastException ex) {
            System.err.println("Could not cast object properly. " + ex);
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            System.err.println("Class does not exist. " + ex);
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            System.err.println("Illegal access to method. " + ex);
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            System.err.println("Could not instantiate class. " + ex);
            ex.printStackTrace();
        }
        return rule;
    }
}