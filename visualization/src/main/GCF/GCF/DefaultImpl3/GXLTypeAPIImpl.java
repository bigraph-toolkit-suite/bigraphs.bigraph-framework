package GCF.DefaultImpl3;

/**
 * An abstract class to represent the type-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public class GXLTypeAPIImpl extends GXLStandardAPI {    
    
   
    /*
    * inherited :  public abstract void setAttributeValue(String attributeName,String value);    
    *              public abstract void close();
    *              public abstract Object getChildElements();
    */
    
    /** Empty constructor. */
    public GXLTypeAPIImpl() {
    }        
    
    /**
     * Method to set an attribute-value (see GXL-DTD).
     */
    public void setAttributeValue(String attributeName, String value) {
    }
    
    /**
     * Method to get the List of child elements.
     */
    public Object getChildElements() {
        return null;
    }
    
    /**
     * Method to return a specific attribute.
     */
    public Object getAttributes () {        
        return null;
    }
    
    /**
     * Overwrites method toString() of class Object.
     */
    public String toString() {
        return null;
    }
    
}