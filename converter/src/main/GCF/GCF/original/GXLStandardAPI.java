package GCF.original;

/**
 * Abstract class to provide the setAttributeValue() and close()-method for any 
 * GXL-construct except the atomic Values. It is the superior class of the GXL-
 * Converter-Framework. For further information see the GXL-DTD and the class-hierarchy
 * of the GXL-Converter-Framework.
 */
public abstract class GXLStandardAPI extends Object {

    /** Empty constructor. */
    public GXLStandardAPI() {
    }
    
    /**
     * Method to set an attribute-value (see GXL-DTD).
     */
    public abstract void setAttributeValue(String attributeName,String value);
    
    /**
     * Method to close the current GXL-construct.
     */
    public abstract void close();

    /**
     * Method to get the list of child elements.
     */
    public abstract Object getChildElements();
    
    /**
     * Method to return the list of attributs.
     */
    public abstract Object getAttributes ();

}