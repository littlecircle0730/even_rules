package padec.filtering.techniques;

import padec.filtering.FilterTechnique;
import padec.filtering.FilteredData;

import java.util.Map;

public class NoFilter implements FilterTechnique<Object> {

    @Override
    public FilteredData<Object> filter(Object data, Map<String, Object> parameters) {
        return new FilteredData<>(data, -1 * Double.MAX_VALUE);
    }

    @Override
    public Double getPrecision(Map<String, Object> parameters) {
        return -1 * Double.MAX_VALUE;
    }
}
