package de.tudresden.inf.st.bigraphs.converter.gcf.DefaultImpl1;
/**
 * An abstract class to represent the relend-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public class GXLRelendAPIImpl extends GXLAttributedAPI {
    
    /*
    * inherited:public abstract void setAttributeValue(String attributeName,String value);    
    *           public abstract void close();
    *           public abstract Object createAttr();   
    *           public abstract void closeAttr();
    */
    
    /** Empty constructor. */
    public GXLRelendAPIImpl() {
    }               
    
    public void close(){
        GXLOutputAPI.writeln(">");
        int depth=GXLOutputAPI.getCurrentDepth()-1;
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write ("</relend");
    }
}