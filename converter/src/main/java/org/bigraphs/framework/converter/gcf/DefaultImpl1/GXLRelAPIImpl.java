package org.bigraphs.framework.converter.gcf.DefaultImpl1;
/**
 * An abstract class to represent the rel-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public class GXLRelAPIImpl extends GXLGraphContainerAPI {

    int depth=GXLOutputAPI.getCurrentDepth()+1;

    /*
    * inherited:public abstract void setAttributeValue(String attributeName,String value);
    *           public abstract void close();
    *           public abstract Object createType();
    *           public abstract void closeType();
    *           public abstract Object createAttr();
    *           public abstract void closeAttr();
    *           public abstract Object createGraph();
    *           public abstract void closeGraph();
    */

    /** Empty constructor. */
    public GXLRelAPIImpl() {
    }

    /**
     * Method to create a child-element of type relend (see GXL-DTD).
     */
    public Object createRelend() {
        GXLOutputAPI.writeln (">");
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write("<relend");
        return (Object)new GXLRelendAPIImpl();
    }

    /**
     * Method to close a child-element of type relend (see GXL-DTD).
     */
    public void closeRelend() {}

    public void close() {
        GXLOutputAPI.writeln (">");
        for (int i=2; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write ("</rel");
    }
}
