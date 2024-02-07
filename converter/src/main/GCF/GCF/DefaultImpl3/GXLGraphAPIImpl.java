package GCF.DefaultImpl3;

/**
 * An abstract class to represent the graph-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public class GXLGraphAPIImpl extends GXLStandardAPI {

    
    
    /*
    * inherited:public abstract void setAttributeValue(String attributeName,String value);    
    *           public abstract void close();
    *           public abstract Object getChildElements();
    */
    
    /** Empty constructor. */
    public GXLGraphAPIImpl() {
    }
    
    /**
     * Method to create a child-element of type node (see GXL-DTD).
     */
    public Object createNode() {
        return new GXLNodeAPIImpl();
    }
    
    /**
     * Method to create a child-element of type edge (see GXL-DTD).
     */
    public Object createEdge() {
        return new GXLEdgeAPIImpl();
    }
    
    /**
     * Method to create a child-element of type rel (see GXL-DTD).
     */
    public Object createRel() {
        return new GXLRelAPIImpl();
    }
    
    /**
     * Method to close a child-element of type node (see GXL-DTD).
     */
    public void closeNode() {}
    
    /**
     * Method to close a child-element of type edge (see GXL-DTD).
     */
    public void closeEdge() {}
    
    /**
     * Method to close a child-element of type rel (see GXL-DTD).
     */
    public void closeRel() {}
    
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