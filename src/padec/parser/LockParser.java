package padec.parser;

import org.yaml.snakeyaml.Yaml;
import padec.application.Endpoint;
import padec.attribute.PADECContext;
import padec.lock.AccessLevel;
import padec.lock.Lock;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public class LockParser {
    private static final String LOCK_KEY = "lock";
    private static final String ACCESSLEVELS_KEY = "accesslevels";
    private static final String ACCESSLEVEL_KEY = "accesslevel";
    private static final String ENDPOINT_KEY = "endpoint";
    private static final String CONSTRUCTOR_PARAMS_KEY = "conparams";
    private static final String CON_PARAM_TYPE_KEY = "type";
    private static final String CON_PARAM_VAL_KEY = "value";

    public static Lock parse(Map<String, Object> iLock, PADECContext context) {
        Lock lock = null;
        try {
            Class endpointClass = ClassLoader.getSystemClassLoader().loadClass((String) iLock.get(ENDPOINT_KEY));
            Endpoint endpoint;
            List<Map<String, Object>> conParams = (List<Map<String, Object>>) iLock.getOrDefault(CONSTRUCTOR_PARAMS_KEY, null);
            if (conParams != null) {
                Class[] paramTypes = new Class[conParams.size()];
                Object[] values = new Object[conParams.size()];
                for (int i = 0; i < conParams.size(); i++) {
                    Map<String, Object> m = conParams.get(i);
                    Class cls = ClassLoader.getSystemClassLoader().loadClass((String) m.get(CON_PARAM_TYPE_KEY));
                    paramTypes[i] = cls;
                    values[i] = cls.cast(m.get(CON_PARAM_VAL_KEY));
                }
                endpoint = (Endpoint) endpointClass
                        .getDeclaredConstructor(paramTypes)
                        .newInstance(values);
            } else {
                endpoint = (Endpoint) endpointClass.newInstance();
            }
            lock = new Lock(endpoint);

            List<Map<String, Object>> iAccesslevel = ((List<Map<String, Object>>) iLock.get(ACCESSLEVELS_KEY));
            for (Map<String, Object> level : iAccesslevel) {
                Map<String, Object> iLevel = (Map<String, Object>) level.get(ACCESSLEVEL_KEY);
                AccessLevel al = AccessLevelParser.parse(iLevel, context, endpoint);
                lock.addAcessLevel(al);
            }
        } catch (NoSuchMethodException ex) {
            System.err.println("Could not find fitting constructor. " + ex);
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            System.err.println("Wrong arguments for constructor. " + ex);
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            System.err.println("Illegal access to method. " + ex);
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            System.err.println("Could not instantiate class. " + ex);
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            System.err.println("Class does not exist. " + ex);
            ex.printStackTrace();
        }
        return lock;
    }

    public static Lock parse(String file, PADECContext context) {
        Lock lock = null;
        try {
            InputStream inputStream =
                    new FileInputStream(file);
            Yaml yaml = new Yaml();

            Map<String, Object> yamlObj = yaml.load(inputStream);
            Map<String, Object> iLock = (Map<String, Object>) yamlObj.get(LOCK_KEY);

            lock = parse(iLock, context);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassCastException ex) {
            System.err.println("Could not cast object properly. " + ex);
            ex.printStackTrace();
        }
        return lock;
    }
}