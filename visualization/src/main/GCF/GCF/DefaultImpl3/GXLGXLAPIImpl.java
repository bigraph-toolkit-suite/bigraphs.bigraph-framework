package GCF.DefaultImpl3;

/**
 * An abstract class to represent the gxl-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public class GXLGXLAPIImpl extends GXLStandardAPI {    

    
    /*
    * inherited :  public abstract void setAttributeValue(String attributeName,String value);    
    *              public abstract void close();
    *              public abstract Object getChildElements();
    */
    
    /** Empty constructor. */
    public GXLGXLAPIImpl() {
    }

    /**
     * Method to create a the GXL-Node (see GXL-DTD).
     */  
    public void createGXL() {}
    
    /**
     * Method to create the DOCTYPE declaration of the GXL-document..
     */  
    public void createDoctypeDecl(String name, String pubid, String sysid) {}    
    
    /**
     * Method to create a Processing Instruction in the GXL-document..
     */ 
    public void createProcessingInstruction(String target, String data) {}
    
    /**
     * Method to create a child-element of type graph (see GXL-DTD).
     */
    public Object createGraph() {
        return new GXLGraphAPIImpl();
    }
    
    /**
     * Method to close a child-element of type graph (see GXL-DTD).
     */
    public void closeGraph() {}
    
    /**
     * Method to set an attribute-value (see GXL-DTD).
     */
    public void setAttributeValue(String attributeName, String value) {}
    
    /**
     * Method to get the List of child elements.
     */
    public Object getChildElements() {
        return null;
    }
    
    /**
     * Overwrites method toString() of class Object.
     */
    public String toString() {
        return null;
    }
}