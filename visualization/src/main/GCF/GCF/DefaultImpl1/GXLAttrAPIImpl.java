package GCF.DefaultImpl1;
/**
 * An abstract class to represent the attr-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public class GXLAttrAPIImpl extends GXLTypedAndAttributedAPI implements GXLStandardValueMethods {
    
    /*
    * inherited : public abstract void setAttributeValue(String attributeName,String value);    
    *             public abstract void close();
    *             public abstract Object createAttr();
    *             public abstract Object createType();
    *             public abstract Object createLocator();
    *            public abstract Object createSeq();
     *            public abstract Object createSet();
     *            public abstract Object createBag();
     *            public abstract Object createTup();
    *             public abstract Object createBool();
    *             public abstract Object createInt();
    *             public abstract Object createFloat();
    *             public abstract Object createString();
    *             public abstract Object createEnum();
     *            public abstract void closeLocator();
     *            public abstract void closeSeq();
     *            public abstract void closeSet();
     *            public abstract void closeBag();
     *            public abstract void closeTup();
     *            public abstract void closeBool();
     *            public abstract void closeString();
     *            public abstract void closeInt();
     *            public abstract void closeFloat();
     *            public abstract void closeEnum();  
    */
    
    /** Empty constructor */
    public GXLAttrAPIImpl() {
    }
    
    public void close() {
        GXLOutputAPI.writeln(">");
        int depth=GXLOutputAPI.getCurrentDepth()-1;
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write ("</attr");
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