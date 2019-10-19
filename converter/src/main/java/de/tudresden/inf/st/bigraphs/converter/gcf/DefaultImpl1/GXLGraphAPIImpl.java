package de.tudresden.inf.st.bigraphs.converter.gcf.DefaultImpl1;
/**
 * An abstract class to represent the graph-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public class GXLGraphAPIImpl extends GXLStandardAPI {
    
    int depth=GXLOutputAPI.getCurrentDepth()+1;
    
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
    public Object createNode() {
        GXLOutputAPI.writeln (">");
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write ("<node");
        return (Object)new GXLNodeAPIImpl();
    }
    
    /**
     * Method to close a child-element of type node (see GXL-DTD).
     */
    public void closeNode() {}
    
    /**
     * Method to create a child-element of type edge (see GXL-DTD).
     */
    public Object createEdge() {
        GXLOutputAPI.writeln (">");
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write ("<edge");
        return (Object)new GXLEdgeAPIImpl();
    }
    
    /**
     * Method to close a child-element of type edge (see GXL-DTD).
     */
    public void closeEdge() {}
    
    /**
     * Method to create a child-element of type rel (see GXL-DTD).
     */
    public Object createRel(){
        GXLOutputAPI.writeln (">");
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write ("<rel");
        return (Object)new GXLRelAPIImpl();
    }
    
    /**
     * Method to close a child-element of type rel (see GXL-DTD).
     */
    public void closeRel() {}
    
    public void close() {
        GXLOutputAPI.writeln(">");
        int depth=GXLOutputAPI.getCurrentDepth()-1;
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write ("</graph");
    }
    
}