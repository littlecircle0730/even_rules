package padec.parser;

import org.yaml.snakeyaml.Yaml;
import padec.application.Endpoint;
import padec.attribute.PADECContext;
import padec.filtering.FilterTechnique;
import padec.lock.AccessLevel;
import padec.rule.Rule;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class AccessLevelParser {

    private static final String ACCESSLEVEL_KEY = "accesslevel";
    private static final String FILTER_KEY = "filter";
    private static final String ENDPOINT_KEY = "endpoint";
    private static final String FILTERPARAMS_KEY = "params";
    private static final String ACCESSRULE_KEY = "rule";
    private static final String TECHNIQUE_KEY = "technique";

    public static AccessLevel parse(String file, PADECContext context) {
        AccessLevel accesslevel = null;
        try {
            InputStream inputStream =
                    new FileInputStream(file);
            Yaml yaml = new Yaml();

            Map<String, Object> yamlObj = yaml.load(inputStream);
            Map<String, Object> iAccesslevel = (Map<String, Object>) yamlObj.get(ACCESSLEVEL_KEY);
            Endpoint endpoint = (Endpoint)
                    ClassLoader.getSystemClassLoader().loadClass((String) iAccesslevel.get(ENDPOINT_KEY))
                            .newInstance();

            Map<String, Object> iFilter = (Map<String, Object>) iAccesslevel.get(FILTER_KEY);
            Map<String, Object> filterParams = (Map<String, Object>) iFilter.get(FILTERPARAMS_KEY);
            FilterTechnique filter = (FilterTechnique)
                ClassLoader.getSystemClassLoader().loadClass((String) iFilter.get(TECHNIQUE_KEY))
                        .newInstance();

            Map<String, Object> iRule = (Map<String, Object>) iAccesslevel.get(ACCESSRULE_KEY);
            Rule accessRule = RuleParser.ruleParse(iRule, context);

            accesslevel = new AccessLevel(filter, endpoint, filterParams, accessRule);

        } catch (
        IOException ex) {
            ex.printStackTrace();
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
        return accesslevel;
    }
}