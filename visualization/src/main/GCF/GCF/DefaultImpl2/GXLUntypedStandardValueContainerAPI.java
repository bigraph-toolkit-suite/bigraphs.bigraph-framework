package GCF.DefaultImpl2;

/**
 * Abstract class to provide the printData()-method and the create()-and close() methods for 
 * any GXL-construct that is an untyped standard-value-container (e.g. set, seq, ...).
 * For further information see the GXL-DTD, inherited and the class-hierarchy of the 
 * GXL-Converter-Framework.
 */
public abstract class GXLUntypedStandardValueContainerAPI extends GXLStandardAPI implements GXLStandardValueMethods {

    /*
    * inherited : public abstract void setAttributValue (String attributeName, String value);
    *             public abstract void close();
    *             public abstract Object getChildElements();  
    */
    
    /** Empty constructor. */
    public GXLUntypedStandardValueContainerAPI() {
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
     * Method to create a child-element of type string (see GXL-DTD).
     */
    public abstract Object createString();
    
    /**
     * Method to create a child-element of type int (see GXL-DTD).
     */
    public abstract Object createInt();
    
    /**
     * Method to create a child-element of type float (see GXL-DTD).
     */
    public abstract Object createFloat();
    
    /**
     * Method to create a child-element of type enum (see GXL-DTD).
     */
    public abstract Object createEnum();
    
    /**
     * Method to close a child-element of type locator (see GXL-DTD).
     */
    public void closeLocator() {}
    
    /**
     * Method to close a child-element of type seq (see GXL-DTD).
     */
    public void closeSeq() {}
    
    /**
     * Method to close a child-element of type set (see GXL-DTD).
     */
    public void closeSet() {}
    
    /**
     * Method to close a child-element of type bag (see GXL-DTD).
     */
    public void closeBag() {}
    
    /**
     * Method to close a child-element of type tup (see GXL-DTD).
     */
    public void closeTup() {}
        
    /**
     * Method to close child-element of type bool (see GXL-DTD).
     */
    public void closeBool() {}
    
    /**
     * Method to close a child-element of type int (see GXL-DTD).
     */
    public void closeInt() {}
    
    /**
     * Method to close a child-element of type float (see GXL-DTD).
     */
    public void closeFloat() {}
    
    /**
     * Method to close a child-element of type string (see GXL-DTD).
     */
    public void closeString() {}
    
    /**
     * Method to close a child-element of type enum (see GXL-DTD).
     */
    public void closeEnum() {}
    
    /**
     * Method to print a CDATA-section.
     */
    public abstract void printData(String data);
}