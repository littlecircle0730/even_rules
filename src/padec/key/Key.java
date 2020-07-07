package padec.key;

import padec.attribute.Attribute;
import padec.attribute.PADECContext;
import padec.lock.Keyhole;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Consumer key.
 */
public class Key implements Serializable {

    private static final long serialVersionUID = -2257607054470962832L;
    /**
     * Values of the key. We cannot use static attributes since keys are fully dynamic.
     */
    private Map<String, Object> data;

    /**
     * Purpose of the key.
     * It's a string so you can use custom purposes as well as agreed-upon purposes.
     */
    private String purpose;

    public Key(Keyhole keyhole, PADECContext context){
        //TODO Is LinkedHashMap the most appropriate?
        data = new LinkedHashMap<>();

        for (Class<? extends Attribute> attr: keyhole.getAttributes()) {
            Attribute provAttr = context.getAttribute(attr);

            //TODO Here, I think that we should filter the data. But, to do that we need information about the precision in the keyhole, right?
            //and a method to select the right filtering technique depending on the precision, the data to process, etc.
            //I think that we agreed that this will be done in the second version :-)

            data.put(attr.getName(), provAttr.getValue());
        }
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

    //TODO The purpose should be provided by the application consuming the microservice. We have to implement an interface to provide this information
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
