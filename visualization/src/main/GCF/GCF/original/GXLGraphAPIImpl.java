package GCF.original;

/**
 * An abstract class to represent the graph-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public abstract class GXLGraphAPIImpl extends GXLStandardAPI {
  
    /*
    * inherited:public abstract void setAttributeValue(String attributeName,String value);    
    *           public abstract void close();
    */
    
    /** Empty constructor. */
    public GXLGraphAPIImpl() {
    }
    
    /**
     * Method to create a child-element of type node (see GXL-DTD).
     */
    public abstract Object createNode(); 
    
    /**
     * Method to close a child-element of type node (see GXL-DTD).
     */
    public abstract void closeNode();
    
    /**
     * Method to create a child-element of type edge (see GXL-DTD).
     */
    public abstract Object createEdge();
    
    /**
     * Method to close a child-element of type edge (see GXL-DTD).
     */
    public abstract void closeEdge();
    
    /**
     * Method to create a child-element of type rel (see GXL-DTD).
     */
    public abstract Object createRel();
    
    /**
     * Method to close a child-element of type rel (see GXL-DTD).
     */
    public abstract void closeRel();
    
}