package GCF.original;

/**
 * Abstract class to provide the create-and close-methods for any GXL-construct that is a 
 * graph-container. For further information see the GXL-DTD, inherited and the 
 * class-hierarchy of the GXL-Converter-Framework.
 */
public abstract class GXLGraphContainerAPI extends GXLTypedAndAttributedAPI {

    /*
    * inherited : public abstract void setAttributeValue(String attributeName,String value);    
    *             public abstract void close();
    *             public abstract Object getChildElements();
    *             public abstract Object createAttr();
    *             public abstract void closeAttr();
    *             public abstract Object createType();
    *             public abstract void closeType();
    */
    
    /** Empty constructor. */
    public GXLGraphContainerAPI() {
    }
    
    /**
     * Method to create a child-element of type graph (see GXL-DTD).
     */
    public abstract Object createGraph();
        
    /**
     * Method to close a child-element of type graph (see GXL-DTD).
     */
    public abstract void closeGraph();
}