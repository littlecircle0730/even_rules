package padec.attribute;

import java.util.Map;

/**
 * User's context. All attributes should be accessed through this class.
 */
public class PADECContext{ // It's not called "Context" so we don't clash with Android's Context class.

    private Map<String, Attribute> attributeMap;//

    public PADECContext(){
        //Initialize inner attribute data structure
    }

    public PADECContext(Map<String, Attribute> attributeMap){
        //Initialize inner attribute data structure
        this.attributeMap = attributeMap;//
    }

    /**
     * Registers an attribute in the context.
     * @param type Attribute type.
     */
    public void registerAttribute(Class<? extends Attribute> type){
        String attributeName = type.getName();
        //Create an attribute with the specified name in the structure.
        attributeMap.put(attributeName, new Attribute(type, attributeName));
    }

    /**
     * Gets an attribute registered in the context.
     * @param type Attribute type.
     * @return Attribute, if registered.
     */
    public Attribute getAttribute(Class<? extends Attribute> type){
        String attributeName = type.getName();
        //Retrieve created attribute with the specified name from the structure.
        return attributeMap.get(attributeName);
    }
}
