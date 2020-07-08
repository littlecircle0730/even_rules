package padec.lock;

import padec.application.Endpoint;
import padec.filtering.FilterTechnique;
import padec.filtering.FilteredData;
import padec.key.Key;
import padec.rule.Rule;

/**
 * Access level of a lock.
 */
public class AccessLevel {

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
    private Object[] filterParams;
    /**
     * Precision
     */
    private Double precision;
    /**
     * Rule that must be passed to be granted access.
     */
    private Rule accessRule;

    AccessLevel(FilterTechnique filter, Endpoint endpoint, Double precision, Rule accessRule){
        this.filter = filter;
        this.endpoint = endpoint;
        this.precision = precision;
        this.filterParams = new Double[]{precision};
        this.accessRule = accessRule;
    }

    AccessLevel(FilterTechnique filter, Endpoint endpoint, Double precision, Object[] filterParams, Rule accessRule) {
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
    public FilteredData testAccess(Object[] endpointParams, Key key){
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
