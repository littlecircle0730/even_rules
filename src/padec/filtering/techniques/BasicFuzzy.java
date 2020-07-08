package padec.filtering.techniques;

import padec.filtering.FilterTechnique;
import padec.filtering.FilteredData;

import java.security.SecureRandom;
import java.util.Map;

/**
 * Basic fuzzying for Double values.
 * Parameters should be a 1-item-long array with a Double value for precision.
 */
public class BasicFuzzy implements FilterTechnique<Double> {

    private static final String PRECISION_KEY = "precision";

    private boolean checkCorrectParams(Map<String, Object> parameters) {
        return parameters.get(PRECISION_KEY) instanceof Double;
    }

    /**
     * Fuzzies a double value with a given precision.
     * @param data Data to be fuzzied.
     * @param parameters Must contain at least a Double with Precision
     * @return Filtered double
     */
    @Override
    public FilteredData<Double> filter(Double data, Map<String, Object> parameters) {
        FilteredData<Double> postFilter = null;
        if(checkCorrectParams(parameters)){
            SecureRandom rng = new SecureRandom();
            double precisionToAdd = rng.nextDouble();
            if (rng.nextBoolean()){ //Randomly have it be positive or negative
                precisionToAdd = precisionToAdd * -1;
            }
            precisionToAdd = precisionToAdd * (Double) parameters.get(PRECISION_KEY);
            postFilter = new FilteredData<>(data + precisionToAdd, (Double) parameters.get(PRECISION_KEY));
        }
        return postFilter;
    }
}
