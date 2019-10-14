package GCF.DefaultImpl3;

/**
 * Abstract class to provide the create-and close-methods for any GXL-construct that is typed 
 * and attributed. For further information see the GXL-DTD, inherited and the 
 * class-hierarchy of the GXL-Converter-Framework.
 */
public abstract class GXLTypedAndAttributedAPI extends GXLAttributedAPI {

    /* 
    * inherited :  public abstract void setAttributeValue(String attributeName,String value);
    *              public abstract void close();
    *              public abstract Object getChildElements();
    *              public abstract Object createAttr();
    *              public abstract void closeAttr();
    */    
    
    /** Empty constructor. */
    public GXLTypedAndAttributedAPI() {
    }
    
    /**
     * Method to create a child-element of type type (see GXL-DTD).
     */
    public abstract Object createType();
    
    /**
     * Method to close a child-element of type type (see GXL-DTD).
     */
    public void closeType(){}
}