package padec.key;

import padec.attribute.Attribute;
import padec.attribute.PADECContext;
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
        //TODO PADECContext should be initialized outside this class (probably by TheONE). Here we should obtain just an instance
        PADECContext context = new PADECContext();

        for (Attribute attr: keyhole.getAttributes()) {
            Attribute provAttr = context.getAttribute(attr.getInnerClass()); //TODO I am not sure if the InnerClass should be used to obtain an attribute

            //TODO Here, I think that we should filter the data. But, to do that we need information about the precision in the keyhole, right?
            //and a method to select the right filtering technique depending on the precision, the data to process, etc.
            //I think that we agreed that this will be done in the second version :-)
            attr.setValue(provAttr.getValue());

            //TODO I think that the string in the map should be provided in the Keyhole or should be a standard value. For now, I used the attribute name
            data.put(attr.getAttrName(), attr);
        }

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

    //TODO The purpose should be provided by the application consuming the microservice. We have to implement an interface to provide this information
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
