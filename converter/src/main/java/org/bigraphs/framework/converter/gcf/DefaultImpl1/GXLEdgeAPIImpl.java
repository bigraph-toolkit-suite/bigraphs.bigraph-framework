package org.bigraphs.framework.converter.gcf.DefaultImpl1;
/**
 * An abstract class to represent the edge-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public class GXLEdgeAPIImpl extends GXLGraphContainerAPI {

    /*
    * inherited :  public abstract void setAttributeValue(String attributeName,String value);
    *              public abstract void close();
    *              public abstract Object createType();
    *              public abstract Object createAttr();
    *              public abstract Object createGraph();
    */

    /** Empty constructor */
    public GXLEdgeAPIImpl() {
    }

    public void close() {
        GXLOutputAPI.writeln (">");
        int depth=GXLOutputAPI.getCurrentDepth()-1;
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write("</edge");
    }
}
