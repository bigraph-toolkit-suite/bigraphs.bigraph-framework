package GCF.DefaultImpl3;

/**
 * An abstract class to represent the edge-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public class GXLEdgeAPIImpl extends GXLGraphContainerAPI {

     
    /*
    * inherited:public abstract void setAttributeValue(String attributeName,String value);    
    *           public abstract void close();
    *           public abstract Object getChildElements();
    *           public abstract Object createType();
    *           public abstract void closeType();
    *           public abstract Object createAttr();
    *           public abstract void closeAttr();
    *           public abstract Object createGraph(); 
    *           public abstract void closeGraph();
    */
    
    /** Empty constructor. */
    public GXLEdgeAPIImpl(){
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
     * Method to create a child-element of type graph (see GXL-DTD).
     */
    public Object createGraph() {
        return new GXLGraphAPIImpl();
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