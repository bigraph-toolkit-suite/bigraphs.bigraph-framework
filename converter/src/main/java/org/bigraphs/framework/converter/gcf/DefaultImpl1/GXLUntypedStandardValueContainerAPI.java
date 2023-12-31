package org.bigraphs.framework.converter.gcf.DefaultImpl1;
/**
 * Abstract class to provide the printData()-method and the create()-and close() methods for
 * any GXL-construct that is an untyped standard-value-container (e.g. set, seq, ...).
 * For further information see the GXL-DTD, inherited and the class-hierarchy of the
 * GXL-Converter-Framework.
 */
public abstract class GXLUntypedStandardValueContainerAPI extends GXLStandardAPI implements GXLStandardValueMethods {

    int depth=GXLOutputAPI.getCurrentDepth()+1;

    /*
    * inherited : public abstract void setAttributValue (String attributeName, String value);
    *             public abstract void close();
    */

    /** Empty constructor. */
    public GXLUntypedStandardValueContainerAPI() {
    }


    /**
     * Method to create a child-element of type locator (see GXL-DTD).
     */
    public Object createLocator() {
        GXLOutputAPI.writeln (">");
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write ("<locator");
        return (Object)new GXLLocatorAPIImpl();
    }

    /**
     * Method to create a child-element of type seq (see GXL-DTD).
     */
    public Object createSeq() {
        GXLOutputAPI.writeln (">");
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write ("<seq");
        return (Object)new GXLSeqAPIImpl();
    }

    /**
     * Method to create a child-element of type set (see GXL-DTD).
     */
    public Object createSet() {
        GXLOutputAPI.writeln (">");
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write ("<set");
        return (Object)new GXLSetAPIImpl();
    }

    /**
     * Method to create a child-element of type bag (see GXL-DTD).
     */
    public Object createBag() {
        GXLOutputAPI.writeln (">");
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write ("<bag");
        return (Object)new GXLBagAPIImpl();
    }

    /**
     * Method to create a child-element of type tup (see GXL-DTD).
     */
    public Object createTup() {
        GXLOutputAPI.writeln (">");
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write ("<tup");
        return (Object)new GXLTupAPIImpl();
    }

    /**
     * Method to create a child-element of type bool (see GXL-DTD).
     */
    public Object createBool() {
        GXLOutputAPI.writeln (">");
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write ("<bool>");
        return null;
    }

    /**
     * Method to create a child-element of type int (see GXL-DTD).*/
    public Object createInt(){
        GXLOutputAPI.writeln (">");
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write ("<int>");
        return null;
    }

    /**
     * Method to create a child-element of type float (see GXL-DTD).
     */
    public Object createFloat(){
        GXLOutputAPI.writeln (">");
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write ("<float>");
        return null;
    }

    /**
     * Method to create a child-element of type string (see GXL-DTD).
     */
    public Object createString(){
        GXLOutputAPI.writeln (">");
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write ("<string>");
        return null;
    }

    /**
     * Method to create a child-element of type enum (see GXL-DTD).
     */
    public Object createEnum(){
        GXLOutputAPI.writeln (">");
        for (int i=1; i<=depth;i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write ("<enum>");
        return null;
    }

    /**
     * Method to close a child-element of type locator (see GXL-DTD).
     */
    public void closeLocator() {}

    /**
     * Method to close a child-element of type seq (see GXL-DTD).
     */
    public void closeSeq() {}

    /**
     * Method to close a child-element of type set (see GXL-DTD).
     */
    public void closeSet() {}

    /**
     * Method to close a child-element of type bag (see GXL-DTD).
     */
    public void closeBag() {}

    /**
     * Method to close a child-element of type tup (see GXL-DTD).
     */
    public void closeTup() {}

    /**
     * Method to close child-element of type bool (see GXL-DTD).
     */
    public void closeBool(){
        GXLOutputAPI.write ("</bool");
    }

    /**
     * Method to close a child-element of type int (see GXL-DTD).
     */
    public void closeInt(){
        GXLOutputAPI.write ("</int");
    }

    /**
     * Method to close a child-element of type float (see GXL-DTD).
     */
    public void closeFloat(){
        GXLOutputAPI.write ("</float");
    }

    /**
     * Method to close a child-element of type string (see GXL-DTD).
     */
    public void closeString(){
        GXLOutputAPI.write ("</string");
    }

    /**
     * Method to close a child-element of type enum (see GXL-DTD).
     */
    public void closeEnum(){
        GXLOutputAPI.write ("</enum");
    }

    /**
     * Method to print a CDATA-section.
     */
    public void printData(String data){
        GXLOutputAPI.write (data);
    }
}
