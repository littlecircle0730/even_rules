package padec.lock;

import padec.attribute.Attribute;
import padec.key.Key;
import padec.perception.PrivacyPerception;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * Allows a user to get the maximum information category using their own subjective perceptions.
     *
     * @param perception User's privacy perceptions.
     * @return Maximum category of information required by this keyhole.
     */
    public int getCategory(PrivacyPerception perception) {
        return attributes.stream().mapToInt(perception::getCategoryFromAttribute).max().getAsInt();
    }

    public boolean fits(Key k) {
        boolean fits = true;
        for (Class<? extends Attribute> c : attributes) {
            fits = fits && k.getData().containsKey(c.getName());
        }
        return fits;
    }

    /**
     * Adds all the attributes from one keyhole to another, to create a joint keyhole.
     *
     * @param kh Keyhole, only for key-creation purposes.
     */
    public void join(Keyhole kh) {
        Set<Class<? extends Attribute>> allAttrs = new HashSet<>(attributes);
        allAttrs.addAll(kh.getAttributes());
        attributes.clear();
        attributes.addAll(allAttrs);
    }

}