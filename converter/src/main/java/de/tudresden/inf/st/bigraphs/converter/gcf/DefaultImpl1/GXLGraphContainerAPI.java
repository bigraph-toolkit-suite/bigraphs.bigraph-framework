package de.tudresden.inf.st.bigraphs.converter.gcf.DefaultImpl1;
/**
 * Abstract class to provide the create-and close-methods for any GXL-construct that is a 
 * graph-container. For further information see the GXL-DTD, inherited and the 
 * class-hierarchy of the GXL-Converter-Framework.
 */
public abstract class GXLGraphContainerAPI extends GXLTypedAndAttributedAPI {

    int depth=GXLOutputAPI.getCurrentDepth()+1;
    
    /*
    * inherited : public abstract void setAttributeValue(String attributeName,String value);    
    *             public abstract void close();
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
    public Object createGraph() {
        GXLOutputAPI.writeln(">");
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write("<graph");
        return (Object)new GXLGraphAPIImpl();
    }
        
    /**
     * Method to close a child-element of type graph (see GXL-DTD).
     */
    public void closeGraph() {}
}