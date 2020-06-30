package padec.filtering.techniques;

import padec.attribute.Pair;
import padec.filtering.FilterTechnique;
import padec.filtering.FilteredData;

import java.security.SecureRandom;

public class PairFuzzy implements FilterTechnique<Pair<Double, Double>> {
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
    public FilteredData<Pair<Double, Double>> filter(Pair<Double, Double> data, Object[] parameters) {
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
            precisionToAddA = precisionToAddA * (Double) parameters[0];
            precisionToAddB = precisionToAddB * (Double) parameters[0];
            postFilter = new FilteredData<>(new Pair<>(data.getA()+precisionToAddA, data.getB()+precisionToAddB), (Double) parameters[0]);
        }
        return postFilter;
    }
}
