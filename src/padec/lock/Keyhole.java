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

    public Keyhole(List<Class<? extends Attribute>> attributes) {
        this.attributes = attributes;
    }

    public List<Class<? extends Attribute>> getAttributes() {
        return attributes;
    }
}