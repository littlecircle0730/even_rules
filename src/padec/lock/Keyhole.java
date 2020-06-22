package padec.lock;

import padec.attribute.Attribute;

import java.util.List;

/**
 * Keyhole of a lock
 */
public class Keyhole {

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
