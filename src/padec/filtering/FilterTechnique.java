package padec.filtering;

import java.util.Map;

/**
 * Technique to perform filtering (obfuscation, abstraction, etc.)
 */
public interface FilterTechnique<T> {
    /**
     * Method to implement in concrete techniques. Filters data as required.
     * @param data Data to be filtered.
     * @param parameters Filtering parameters (e.g. precision).
     * @return Filtered data of same type.
     */
    FilteredData<T> filter(T data, Map<String, Object> parameters);

    Double getPrecision(Map<String, Object> parameters);
}
