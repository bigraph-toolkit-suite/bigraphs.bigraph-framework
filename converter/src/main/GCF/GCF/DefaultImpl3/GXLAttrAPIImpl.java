package GCF.DefaultImpl3;

/**
 * An abstract class to represent the attr-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public class GXLAttrAPIImpl extends GXLTypedAndAttributedAPI implements GXLStandardValueMethods {

    
    /*
    * inherited : public abstract void setAttributeValue(String attributeName,String value);    
    *             public abstract void close();
    *             public abstract Object createAttr();
    *             public abstract void closeAttr();
    *             public abstract Object createType();
    *             public abstract void closeType();
    */
    
    /** Empty constructor */
    public GXLAttrAPIImpl() {
    }
    
    /**
     * Method to create a child-element of type attr (see GXL-DTD).
     */
    public Object createAttr() {
        return new GXLAttrAPIImpl();
    }
    
    /**
     * Method to create a child-element of type type (see GXL-DTD).
     */
    public Object createType() {
        return new GXLTypeAPIImpl();
    }
        
    /**
     * Method to create a child-element of type locator (see GXL-DTD).
     */
    public Object createLocator(){
        return new GXLLocatorAPIImpl();
    }
    
    /**
     * Method to create a child-element of type seq (see GXL-DTD).
     */
    public Object createSeq() {
        return new GXLSeqAPIImpl();
    }
    
    /**
     * Method to create a child-element of type set (see GXL-DTD).
     */
    public Object createSet() {
        return new GXLSetAPIImpl();
    }
    
    /**
     * Method to create a child-element of type bag (see GXL-DTD).
     */
    public Object createBag() {
        return new GXLBagAPIImpl();
    }
    
    /**
     * Method to create a child-element of type tup (see GXL-DTD).
     */
    public Object createTup() {
        return new GXLTupAPIImpl();
    }
    
    /**
     * Method to create a child-element of type bool (see GXL-DTD).
     */
    public Object createBool() {
        return null;
    }
    
    /**
     * Method to create a child-element of type int (see GXL-DTD).*/
    public Object createInt() {
        return null;
    }
    
    /**
     * Method to create a child-element of type float (see GXL-DTD).
     */
    public Object createFloat() {
        return null;
    }
    
    /**
     * Method to create a child-element of type string (see GXL-DTD).
     */
    public Object createString() {
        return null;
    }
    
    /**
     * Method to create a child-element of type enum (see GXL-DTD).
     */
    public Object createEnum() {
        return null;
    }
    
        /**
     * Method to create a child-element of type locator (see GXL-DTD).
     */
    public void closeLocator(){
    }
    
    /**
     * Method to create a child-element of type seq (see GXL-DTD).
     */
    public void closeSeq() {
    }
    
    /**
     * Method to create a child-element of type set (see GXL-DTD).
     */
    public void closeSet() {
    }
    
    /**
     * Method to create a child-element of type bag (see GXL-DTD).
     */
    public void closeBag() {
    }
    
    /**
     * Method to create a child-element of type tup (see GXL-DTD).
     */
    public void closeTup() {
    }
    
    /**
     * Method to create a child-element of type bool (see GXL-DTD).
     */
    public void closeBool() {
    }
    
    /**
     * Method to create a child-element of type int (see GXL-DTD).*/
    public void closeInt() {
    }
    
    /**
     * Method to create a child-element of type float (see GXL-DTD).
     */
    public void closeFloat() {
    }
    
    /**
     * Method to create a child-element of type string (see GXL-DTD).
     */
    public void closeString() {
    }
    
    /**
     * Method to create a child-element of type enum (see GXL-DTD).
     */
    public void closeEnum() {
    }
    
    /**
     * Method to print a CDATA-section.
     */
    public void printData(String data) {           
  
    }
    
    /**
     * Overwrites method toString() of class Object.
     */
    public String toString() {
        return null;
    }
}