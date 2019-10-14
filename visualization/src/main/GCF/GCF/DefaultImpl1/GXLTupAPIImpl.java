package GCF.DefaultImpl1;
/**
 * An abstract class to represent the tup-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public class GXLTupAPIImpl extends GXLUntypedStandardValueContainerAPI {

    /*
     * inherited: public abstract void setAttributeValue(String attributeName,String value);
     *            public abstract void close();
     *            public abstract Object createLocator();
     *            public abstract Object createSeq();
     *            public abstract Object createSet();
     *            public abstract Object createBag();
     *            public abstract Object createTup();
     *            public abstract Object createBool();
     *            public abstract Object createString();
     *            public abstract Object createInt();
     *            public abstract Object createFloat();
     *            public abstract Object createEnum();   
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
    
    /** Empty constructor. */
    public GXLTupAPIImpl() {
    }
    
    public void close() {
        GXLOutputAPI.writeln(">");
        int depth=GXLOutputAPI.getCurrentDepth()-1;
        for (int i=1; i<=depth; i++) GXLOutputAPI.write("  ");
        GXLOutputAPI.write("</tup");
    }

}