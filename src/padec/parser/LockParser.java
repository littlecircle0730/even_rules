package padec.parser;

import org.yaml.snakeyaml.Yaml;
import padec.application.Endpoint;
import padec.attribute.PADECContext;
import padec.filtering.FilterTechnique;
import padec.lock.Lock;
import padec.rule.Rule;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class LockParser {
    private static final String LOCK_KEY = "lock";
    private static final String ACCESSLEVELS_KEY = "accesslevels";
    private static final String ENDPOINT_KEY = "endpoint";
    private static final String LEVEL1_KEY = "level1";
    private static final String LEVEL2_KEY = "level2";
    private static final String LEVEL3_KEY = "level3";
    private static final String POSITION_KEY = "position";
    private static final String FILTER_KEY = "filter";
    private static final String RULE_KEY = "rule";
    private static final String TECHNIQUE_KEY = "technique";
    private static final String FILTERPARAMS_KEY = "params";

    public static Lock parse(String file, PADECContext context) {
        Lock lock = null;
        try {
            InputStream inputStream =
                    new FileInputStream(file);
            Yaml yaml = new Yaml();

            Map<String, Object> yamlObj = yaml.load(inputStream);
            Map<String, Object> iLock = (Map<String, Object>) yamlObj.get(LOCK_KEY);
            Endpoint endpoint = (Endpoint)
                    ClassLoader.getSystemClassLoader().loadClass((String) iLock.get(ENDPOINT_KEY))
                            .newInstance();

            Object[] iAccesslevel = ((List<Map<String, Object>>) iLock.get(ACCESSLEVELS_KEY)).toArray();

            lock = new Lock(endpoint);
            int i = 1;
            for(Object level : iAccesslevel){
                Map<String, Object> iLevel = (Map<String, Object>) ((Map<String, Object>) level).get("level"+i);
                int position = (int) iLevel.get(POSITION_KEY);

                Map<String, Object> iFilter = (Map<String, Object>) iLevel.get(FILTER_KEY);
                Map<String, Object> filterParams = (Map<String, Object>) iFilter.get(FILTERPARAMS_KEY);
                FilterTechnique filter = (FilterTechnique)
                        ClassLoader.getSystemClassLoader().loadClass((String) iFilter.get(TECHNIQUE_KEY))
                                .newInstance();

                Map<String, Object> iRule = (Map<String, Object>) iLevel.get(RULE_KEY);
                Rule accessRule = RuleParser.ruleParse(iRule, context);

                lock.addAccessLevel(filter, filterParams, accessRule, position);
                i = i + 1;
            }

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
        return lock;
    }
}