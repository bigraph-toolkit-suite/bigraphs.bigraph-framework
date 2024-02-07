package GCF.original;

/**
 * An abstract class to represent the attr-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public abstract class GXLAttrAPIImpl extends GXLTypedAndAttributedAPI implements GXLStandardValueMethods {
    
    /*
    * inherited : public abstract void setAttributeValue(String attributeName,String value);    
    *             public abstract void close();
    *             public abstract Object createAttr();
    *             public abstract Object createType();
    *             public abstract void closeAttr();
    *             public abstract void closeType();
    */
    
    /** Empty constructor */
    public GXLAttrAPIImpl() {
    }
    
    /**
     * Method to create a child-element of type locator (see GXL-DTD).
     */
    public abstract Object createLocator();

    
    /**
     * Method to create a child-element of type seq (see GXL-DTD).
     */
    public abstract Object createSeq();
    
    /**
     * Method to create a child-element of type set (see GXL-DTD).
     */
    public abstract Object createSet();
    
    /**
     * Method to create a child-element of type bag (see GXL-DTD).
     */
    public abstract Object createBag();
    
    /**
     * Method to create a child-element of type tup (see GXL-DTD).
     */
    public abstract Object createTup();
    
    /**
     * Method to create a child-element of type bool (see GXL-DTD).
     */
    public abstract Object createBool();
    
    /**
     * Method to create a child-element of type int (see GXL-DTD).*/
    public abstract Object createInt();
    
    /**
     * Method to create a child-element of type float (see GXL-DTD).
     */
    public abstract Object createFloat();
    
    /**
     * Method to create a child-element of type string (see GXL-DTD).
     */
    public abstract Object createString();
    
    /**
     * Method to create a child-element of type enum (see GXL-DTD).
     */
    public abstract Object createEnum();
    
    /**
     * Method to close a child-element of type locator (see GXL-DTD).
     */
    public abstract void closeLocator();
    
    /**
     * Method to close a child-element of type seq (see GXL-DTD).
     */
    public abstract void closeSeq();
    
    /**
     * Method to close a child-element of type set (see GXL-DTD).
     */
    public abstract void closeSet();
    
    /**
     * Method to close a child-element of type bag (see GXL-DTD).
     */
    public abstract void closeBag();
    
    /**
     * Method to close a child-element of type tup (see GXL-DTD).
     */
    public abstract void closeTup();
        
    /**
     * Method to close child-element of type bool (see GXL-DTD).
     */
    public abstract void closeBool();
    
    /**
     * Method to close a child-element of type int (see GXL-DTD).
     */
    public abstract void closeInt();
    
    /**
     * Method to close a child-element of type float (see GXL-DTD).
     */
    public abstract void closeFloat();
    
    /**
     * Method to close a child-element of type string (see GXL-DTD).
     */
    public abstract void closeString();
    
    /**
     * Method to close a child-element of type enum (see GXL-DTD).
     */
    public abstract void closeEnum();
    
    /**
     * Method to print a CDATA-section.
     */
    public abstract void printData(String data);
    
    
}