package padec.filtering;

/**
 * Class to represent data that has been filtered.
 */
public class FilteredData<T> {

    /**
     * Filtered data.
     */
    protected T data;

    /**
     * Precision of the data after filtering (e.g. 100 implies "in a circle of radius 100m")
     */
    protected double precision;

    public FilteredData(T data, double precision) {
        this.data = data;
        this.precision = precision;
    }

    public T getData() {
        return data;
    }

    public double getPrecision() {
        return precision;
    }

    // No setters. You're supposed to generate and consume this data, not to edit it.
}
