package padec.key;

import padec.lock.Keyhole;

import java.util.Map;

/**
 * Consumer key.
 */
public class Key {

    /**
     * Values of the key. We cannot use static attributes since keys are fully dynamic.
     */
    private Map<String, Object> data;

    /**
     * Purpose of the key.
     * It's a string so you can use custom purposes as well as agreed-upon purposes.
     */
    private String purpose;

    public Key(Keyhole keyhole){
        // For each attribute contained in the keyhole:
            // Retrieve attribute value from PADECContext.
            // Filter value accordingly.
            // Put value in the data attribute, with its key being the attribute name.
        // Should we filter this data? We agreed on not letting the provider get it anyways.
    }

    /**
     * Simple method to get the value of an attribute.
     * @param attributeName Attribute to get the value of.
     * @return Value of the attribute, or null if it is not found.
     */
    public Object getValue(String attributeName){
        return data.get(attributeName);
    }

    public Map<String, Object> getData() {
        return data;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
