package GCF.original;

/**
 * An abstract class to represent the rel-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public abstract class GXLRelAPIImpl extends GXLGraphContainerAPI {
   
    /*
    * inherited:public abstract void setAttributeValue(String attributeName,String value);    
    *           public abstract void close();
    *           public abstract Object createType();
    *           public abstract Object createAttr();
    *           public abstract Object createGraph(); 
    */
    
    /** Empty constructor. */
    public GXLRelAPIImpl() {
    } 
    
    /**
     * Method to create a child-element of type relend (see GXL-DTD).
     */
    public abstract Object createRelend();
    
    /**
     * Method to close a child-element of type relend (see GXL-DTD).
     */
    public abstract void closeRelend();   
}