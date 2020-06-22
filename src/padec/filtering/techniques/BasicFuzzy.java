package padec.filtering.techniques;

import padec.filtering.FilterTechnique;
import padec.filtering.FilteredData;

import java.security.SecureRandom;

/**
 * Basic fuzzying for Double values.
 * Parameters should be a 1-item-long array with a Double value for precision.
 */
public class BasicFuzzy implements FilterTechnique<Double> {

    private boolean checkCorrectParams(Object[] parameters){
        return parameters.length == 1 && parameters[0] instanceof Double;
    }

    /**
     * Fuzzies a double value with a given precision.
     * @param data Data to be fuzzied.
     * @param parameters 1-item-long array with a Double value for precision
     * @return
     */
    @Override
    public FilteredData<Double> filter(Double data, Object[] parameters) {
        FilteredData<Double> postFilter = null;
        if(checkCorrectParams(parameters)){
            SecureRandom rng = new SecureRandom();
            double precisionToAdd = rng.nextDouble();
            if (rng.nextBoolean()){ //Randomly have it be positive or negative
                precisionToAdd = precisionToAdd * -1;
            }
            precisionToAdd = precisionToAdd * (Double) parameters[0];
            postFilter = new FilteredData<>(data+precisionToAdd, (Double) parameters[0]);
        }
        return postFilter;
    }
}
