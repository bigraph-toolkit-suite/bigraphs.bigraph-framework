package GCF.DefaultImpl2;

/**
 * An abstract class to represent the seq-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public class GXLSeqAPIImpl extends GXLUntypedStandardValueContainerAPI {

    private java.util.Vector childElements = new java.util.Vector(); // to save the child - objects
    private int lastSimpleType;                              // to save, which simple type (string, int,...
                                                             // has been created last, to be able to set the
                                                             // value of the created Object in the printData
                                                             // method
    
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
    public GXLSeqAPIImpl() {
    }
    
    /**
     * Method to create a child-element of type attr (see GXL-DTD).
     */
    public Object createAttr() {
        lastSimpleType=-1;
        GXLAttrAPIImpl attr = new GXLAttrAPIImpl();
        childElements.add(attr);
        return attr;
    }
    
    /**
     * Method to create a child-element of type type (see GXL-DTD).
     */
    public Object createType() {
        lastSimpleType=-1;
        GXLTypeAPIImpl type = new GXLTypeAPIImpl();
        childElements.add(type);
        return type;
    }
        
    /**
     * Method to create a child-element of type locator (see GXL-DTD).
     */
    public Object createLocator(){
        lastSimpleType=-1;
        GXLLocatorAPIImpl locator = new GXLLocatorAPIImpl();
        childElements.add(locator);
        return locator;
    }
    
    /**
     * Method to create a child-element of type seq (see GXL-DTD).
     */
    public Object createSeq() {
        lastSimpleType=-1;
        GXLSeqAPIImpl seq = new GXLSeqAPIImpl();
        childElements.add(seq);
        return seq;
    }
    
    /**
     * Method to create a child-element of type set (see GXL-DTD).
     */
    public Object createSet() {
        lastSimpleType=-1;
        GXLSetAPIImpl set = new GXLSetAPIImpl();
        childElements.add(set);
        return set;
    }
    
    /**
     * Method to create a child-element of type bag (see GXL-DTD).
     */
    public Object createBag() {
        lastSimpleType=-1;
        GXLBagAPIImpl bag = new GXLBagAPIImpl();
        childElements.add(bag);
        return bag;
    }
    
    /**
     * Method to create a child-element of type tup (see GXL-DTD).
     */
    public Object createTup() {
        lastSimpleType=-1;
        GXLTupAPIImpl tup = new GXLTupAPIImpl();
        childElements.add(tup);
        return tup;
    }
    
    /**
     * Method to create a child-element of type bool (see GXL-DTD).
     */
    public Object createBool() {
        lastSimpleType=0;
        return null;
    }
    
    /**
     * Method to create a child-element of type int (see GXL-DTD).*/
    public Object createInt() {
        lastSimpleType=1;
        return null;
    }
    
    /**
     * Method to create a child-element of type float (see GXL-DTD).
     */
    public Object createFloat() {
        lastSimpleType=2;
        return null;
    }
    
    /**
     * Method to create a child-element of type string (see GXL-DTD).
     */
    public Object createString() {
        lastSimpleType=3;
        return null;
    }
    
    /**
     * Method to create a child-element of type enum (see GXL-DTD).
     */
    public Object createEnum() {
        lastSimpleType=4;
        return null;
    }
    
    /**
     * Method to print a CDATA-section.
     */
    public void printData(String data) {           
        switch (lastSimpleType) {
            case 0 : childElements.add(new String[] { "bool", data });
                     break;
            case 1 : childElements.add(new String[] { "int", data });
                     break;
            case 2 : childElements.add(new String[] { "float", data });
                     break;
            case 3 : childElements.add(new String[] { "string", data });
                     break;
            case 4 : childElements.add(new String[] { "enum", data });
                     break;
        }
    }
    
    /**
     * Method to get the List of child elements.
     */
    public Object getChildElements() {
        return childElements;
    } 
    
    /**
     * Overwrites method toString() of class Object.
     */
    public String toString() {
        return new String ("seq");
    }
}