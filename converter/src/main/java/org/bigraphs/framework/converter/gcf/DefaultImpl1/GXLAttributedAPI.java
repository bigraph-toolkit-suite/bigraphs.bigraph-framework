package org.bigraphs.framework.converter.gcf.DefaultImpl1;
/**
 * Abstract class to provide the create-and close-methods for any GXL-construct that is
 * attributed. For further information see the GXL-DTD, inherited and the
 * class-hierarchy of the GXL-Converter-Framework.
 */
public abstract class GXLAttributedAPI extends GXLStandardAPI {

    int depth=GXLOutputAPI.getCurrentDepth()+1;

    /*
    * inherited :  public abstract void setAttributeValue(String attributeName,String value);
    *              public abstract void close();
    */

    /** Empty constructor. */
    public GXLAttributedAPI() {
    }

    /**
     * Method to create a child-element of type attr (see GXL-DTD).
     */
    public Object createAttr() {
        GXLOutputAPI.writeln (">");
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write ("<attr");
        return (Object)new GXLAttrAPIImpl();
    }

    /**
     * Method to close a child-element of type attr (see GXL-DTD).
     */
    public void closeAttr() {}

}
