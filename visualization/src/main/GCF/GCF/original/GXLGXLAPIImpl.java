package GCF.original;

/**
 * An abstract class to represent the gxl-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public abstract class GXLGXLAPIImpl extends GXLStandardAPI {    
    
    /*
    * inherited :  public abstract void setAttributeValue(String attributeName,String value);    
    *              public abstract void close();
    */
    
    /** Empty constructor. */
    public GXLGXLAPIImpl() {
    }
    
    /**
     * Method to create a the GXL-Node (see GXL-DTD).
     */  
    public abstract void createGXL();
    
    /**
     * Method to create the DOCTYPE declaration of the GXL-document..
     */  
    public abstract void createDoctypeDecl(String name, String pubid, String sysid);
    
    /**
     * Method to create a Processing Instruction in the GXL-document..
     */ 
    public abstract void createProcessingInstruction(String target, String data);
    
    /**
     * Method to create a child-element of type graph (see GXL-DTD).
     */
    public abstract Object createGraph();    
    
    /**
     * Method to close a child-element of type graph (see GXL-DTD).
     */
    public abstract void closeGraph();  
}