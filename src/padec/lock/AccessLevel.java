package padec.lock;

import padec.application.Endpoint;
import padec.filtering.FilterTechnique;
import padec.filtering.FilteredData;
import padec.key.Key;
import padec.rule.Rule;

import java.util.Collections;
import java.util.Map;

/**
 * Access level of a lock.
 */
public class AccessLevel {

    private static final String PRECISION_KEY = "precision";

    /**
     * Filtering technique to apply at this access level.
     */
    private FilterTechnique filter;
    /**
     * Endpoint this access level protects (should always be the one from the lock).
     */
    private Endpoint endpoint;
    /**
     * Filtering parameters.
     */
    private Map<String, Object> filterParams;
    /**
     * Precision
     */
    private Double precision;
    /**
     * Rule that must be passed to be granted access.
     */
    private Rule accessRule;

    <T> AccessLevel(FilterTechnique<T> filter, Endpoint<T> endpoint, Double precision, Rule accessRule) {
        this.filter = filter;
        this.endpoint = endpoint;
        this.precision = precision;
        this.filterParams = Collections.singletonMap(PRECISION_KEY, precision);
        this.accessRule = accessRule;
    }

    <T> AccessLevel(FilterTechnique<T> filter, Endpoint<T> endpoint, Double precision, Map<String, Object> filterParams, Rule accessRule) {
        this.filter = filter;
        this.endpoint = endpoint;
        this.precision = precision;
        this.filterParams = filterParams;
        this.accessRule = accessRule;
    }

    /**
     * Method to call to check for access.
     * @param endpointParams Parameters for the endpoint.
     * @param key Key to test access.
     * @return Filtered response, or null if access was denied.
     */
    public FilteredData testAccess(Map<String, Object> endpointParams, Key key) {
        return accessRule.check(key.getData()) ?
                filter.filter(endpoint.execute(endpointParams), filterParams) : null;
    }

    /**
     * Retrieve keyhole of the access level.
     * @return Keyhole for this access level.
     */
    public Keyhole getKeyhole(){
        return new Keyhole(accessRule.getAttributes(), precision);
    }

}
