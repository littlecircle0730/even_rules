package padec.attribute;

// This is what I had on the Attribute class.
// Feel free to reuse it, to modify it or to delete it and make your own.

/**
 * Attribute from an user's context
 * @param <T> Attribute type
 */
public abstract class Attribute<T> {

    /**
     * Value of the attribute, to be updated over time.
     */
    protected T value;

    /**
     * Type of the attribute, required to perform some checks.
     */
    protected Class<T> innerClass;

    /**
     * Name of the attribute (e.g. location, sound level...)
     */
    protected String attrName;

    /**
     * Base constructor for attribute providing a value.
     * @param innerClass Type of the attribute.
     * @param value Current value for the attribute.
     * @param attrName Name of the attribute.
     */
    public Attribute(Class<T> innerClass, T value, String attrName) {
        this.value = value;
        this.innerClass = innerClass;
        this.attrName = attrName;
    }

    /**
     * Base constructor for attribute not providing a value.
     * @param innerClass Type of the attribute.
     * @param attrName Name of the attribute.
     */
    public Attribute(Class<T> innerClass, String attrName) {
        this.innerClass = innerClass;
        this.attrName = attrName;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public Class getInnerClass(){
        return innerClass;
    }

    public String getAttrName(){
        return attrName;
    }

    /**
     * Overridden toString method for convenience.
     * @return Stringified version of an attribute.
     */
    @Override
    public String toString() {
        return "<"+attrName+": "+value.toString()+">";
    }
}
