package padec.lock;

import padec.attribute.Attribute;
import padec.key.Key;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
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
    /**
     * Maximum category of the information required by the keyhole
     */
    protected int category;

    public Keyhole(List<Class<? extends Attribute>> attributes, Double precision) {
        this.attributes = attributes;
        this.precision = precision;
        category = attributes.stream().mapToInt(c -> {
            try {
                return (int) c.getMethod("getCategory").invoke(null);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                System.err.println("getCategory not correctly re-implemented in attribute class");
                ex.printStackTrace();
                return -1;
            }
        }).max().getAsInt();
    }

    public List<Class<? extends Attribute>> getAttributes() {
        return attributes;
    }

    public Double getPrecision() {
        return precision;
    }

    public int getCategory() {
        return category;
    }

    public boolean fits(Key k) {
        boolean fits = true;
        for (Class<? extends Attribute> c : attributes) {
            fits = fits && k.getData().containsKey(c.getName());
        }
        return fits;
    }
}