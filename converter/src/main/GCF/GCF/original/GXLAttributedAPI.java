package GCF.original;

/**
 * Abstract class to provide the create-and close-methods for any GXL-construct that is  
 * attributed. For further information see the GXL-DTD, inherited and the 
 * class-hierarchy of the GXL-Converter-Framework.
 */
public abstract class GXLAttributedAPI extends GXLStandardAPI {

    /* 
    * inherited :  public abstract void setAttributeValue(String attributeName,String value);
    *              public abstract void close();
    *              public abstract Object getChildElements();
    */   
    
    /** Empty constructor. */
    public GXLAttributedAPI() {
    }
    
    /**
     * Method to create a child-element of type attr (see GXL-DTD).
     */
    public abstract Object createAttr();
    
    /**
     * Method to close a child-element of type attr (see GXL-DTD).
     */
    public abstract void closeAttr();

}