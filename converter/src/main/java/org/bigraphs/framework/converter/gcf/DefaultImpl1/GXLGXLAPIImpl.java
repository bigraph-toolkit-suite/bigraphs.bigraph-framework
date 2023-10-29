package org.bigraphs.framework.converter.gcf.DefaultImpl1;
/**
 * An abstract class to represent the gxl-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public class GXLGXLAPIImpl extends GXLStandardAPI {

    /*
    * inherited :  public abstract void setAttributeValue(String attributeName,String value);
    *              public abstract void close();
    */

    /** Empty constructor. */
    public GXLGXLAPIImpl() {
    }

    /**
     * Method to create the DOCTYPE declaration of the GXL-document..
     */
    public void createDoctypeDecl(String name, String pubid, String sysid) {
        GXLOutputAPI.writeln("<!DOCTYPE " + name +((pubid==""||pubid==null) ? " SYSTEM \""+sysid : " PUBLIC \""+
                             pubid+"\" \""+sysid)+"\">");
    }

    /**
     * Method to create a Processing Instruction in the GXL-document..
     */
    public void createProcessingInstruction(String target, String data) {
        GXLOutputAPI.writeln("<?"+target+" "+data+"?>");
    }

    /**
     * Method to create a the GXL-Node (see GXL-DTD).
     */
    public void createGXL() {
        GXLOutputAPI.write("<gxl");
    }

    /**
     * Method to create a child-element of type graph (see GXL-DTD).
     */
    public Object createGraph() {
        GXLOutputAPI.writeln (">");
        GXLOutputAPI.write("  <graph");
        return (Object)new GXLGraphAPIImpl();
    }

    /**
     * Method to close a child-element of type graph (see GXL-DTD).
     */
    public void closeGraph() {}

    public void close() {
        GXLOutputAPI.writeln(">");
        GXLOutputAPI.writeln("</gxl>");
    }
}
