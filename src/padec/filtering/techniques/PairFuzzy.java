package padec.filtering.techniques;

import padec.attribute.Pair;
import padec.filtering.FilterTechnique;
import padec.filtering.FilteredData;

import java.security.SecureRandom;
import java.util.Map;

public class PairFuzzy implements FilterTechnique<Pair<Double, Double>> {

    public static final String PRECISION_KEY = "precision";

    private boolean checkCorrectParams(Map<String, Object> parameters) {
        return parameters.get(PRECISION_KEY) instanceof Double;
    }

    /**
     * Fuzzies a double value with a given precision.
     * @param data Data to be fuzzied.
     * @param parameters Must contain at least a Double with Precision
     * @return Filtered pair
     */
    @Override
    public FilteredData<Pair<Double, Double>> filter(Pair<Double, Double> data, Map<String, Object> parameters) {
        FilteredData<Pair<Double, Double>> postFilter = null;
        if(checkCorrectParams(parameters)){
            SecureRandom rng = new SecureRandom();
            double precisionToAddA = rng.nextDouble();
            double precisionToAddB = rng.nextDouble();
            if (rng.nextBoolean()){ //Randomly have it be positive or negative
                precisionToAddA = precisionToAddA * -1;
            }
            if (rng.nextBoolean()){ //Randomly have it be positive or negative
                precisionToAddB = precisionToAddB * -1;
            }
            precisionToAddA = precisionToAddA * (Double) parameters.get(PRECISION_KEY);
            precisionToAddB = precisionToAddB * (Double) parameters.get(PRECISION_KEY);
            postFilter = new FilteredData<>(new Pair<>(data.getA() + precisionToAddA, data.getB() + precisionToAddB), (Double) parameters.get(PRECISION_KEY));
        }
        return postFilter;
    }

    @Override
    public Double getPrecision(Map<String, Object> parameters) {
        if (!checkCorrectParams(parameters)) {
            return Double.MAX_VALUE;
        }
        return (Double) parameters.get(PRECISION_KEY);
    }
}
