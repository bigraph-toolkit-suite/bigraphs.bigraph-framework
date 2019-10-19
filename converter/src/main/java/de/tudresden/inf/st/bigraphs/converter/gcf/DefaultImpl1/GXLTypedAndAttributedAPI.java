package de.tudresden.inf.st.bigraphs.converter.gcf.DefaultImpl1;
/**
 * Abstract class to provide the create-and close-methods for any GXL-construct that is typed 
 * and attributed. For further information see the GXL-DTD, inherited and the 
 * class-hierarchy of the GXL-Converter-Framework.
 */
public abstract class GXLTypedAndAttributedAPI extends GXLAttributedAPI {

    /* 
    * inherited :  public abstract void setAttributeValue(String attributeName,String value);
    *              public abstract void close();
    *              public abstract Object createAttr();
    *              public abstract void closeAttr();
    */    
    
    /** Empty constructor. */
    public GXLTypedAndAttributedAPI() {
    }
    
    int depth=GXLOutputAPI.getCurrentDepth()+1;
    
    /**
     * Method to create a child-element of type type (see GXL-DTD).
     */
    public Object createType() {
        GXLOutputAPI.writeln(">");        
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write("<type");
        return (Object)new GXLTypeAPIImpl();
    }
    
    /**
     * Method to close a child-element of type type (see GXL-DTD).
     */
    public void closeType() {}
}