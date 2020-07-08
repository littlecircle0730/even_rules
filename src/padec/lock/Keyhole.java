package padec.lock;

import padec.attribute.Attribute;

import java.io.Serializable;
import java.util.List;

/**
 * Keyhole of a lock
 */
public class Keyhole implements Serializable {

    private static final long serialVersionUID = -6273050345801942345L;
    /**
     * Attributes required by the keyhole
     */
    protected List<Class<? extends Attribute>> attributes;
    /**
     * Precision of the keyhole
     */
    protected Double precision;

    public Keyhole(List<Class<? extends Attribute>> attributes, Double precision) {
        this.attributes = attributes;
        this.precision = precision;
    }

    public List<Class<? extends Attribute>> getAttributes() {
        return attributes;
    }

    public Double getPrecision() {
        return precision;
    }
}