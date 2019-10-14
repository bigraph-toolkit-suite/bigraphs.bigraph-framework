package GCF.DefaultImpl3;

/**
 * An abstract class to represent the relend-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public class GXLRelendAPIImpl extends GXLAttributedAPI {

    
    /*
    * inherited:public abstract void setAttributeValue(String attributeName,String value);    
    *           public abstract void close();
    *           public abstract Object getChildElements();
    *           public abstract Object createAttr();   
    *           public abstract void closeAttr();
    */
    
    /** Empty constructor. */
    public GXLRelendAPIImpl() {
    }               
    
    /**
     * Method to create a child-element of type attr (see GXL-DTD).
     */
    public Object createAttr() {
        return new GXLAttrAPIImpl();
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