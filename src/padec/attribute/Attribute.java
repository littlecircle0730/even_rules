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
     * Base constructor for attribute providing a value.
     * @param innerClass Type of the attribute.
     * @param value Current value for the attribute.
     */
    Attribute(Class<T> innerClass, T value) {
        this.value = value;
        this.innerClass = innerClass;
    }

    /**
     * Base constructor for attribute not providing a value.
     * @param innerClass Type of the attribute.
     */
    Attribute(Class<T> innerClass) {
        this.innerClass = innerClass;
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

    /**
     * Overridden toString method for convenience.
     * @return Stringified version of an attribute.
     */
    @Override
    public String toString() {
        return "<"+this.getClass().getName()+": "+value.toString()+">";
    }
}
