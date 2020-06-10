package padec.application;

import java.util.Map;

/**
 * API endpoint specification. Exposes something to the outside through PADEC.
 */
public interface Endpoint<T> {

    /**
     * Method to implement at concrete endpoints to contain business logic.
     * @param parameters Parameters required by the endpoint to do its logic.
     * @return Result of its logic (i.e. contextual data, possibly processed).
     */
    T execute(Object[] parameters);
}
