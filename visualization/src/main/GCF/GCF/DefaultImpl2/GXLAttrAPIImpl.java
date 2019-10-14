package GCF.DefaultImpl2;

import java.util.Vector;

/**
 * An abstract class to represent the attr-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public class GXLAttrAPIImpl extends GXLTypedAndAttributedAPI implements GXLStandardValueMethods {
    
    private Vector childElements = new Vector(); // to save the child - objects
    private int lastSimpleType;                              // to save, which simple type (string, int,...
                                                             // has been created last, to be able to set the
                                                             // value of the created Object in the printData
                                                             // method
    
    private Vector attributes = new Vector();   // to return all attributes
    
    // attributes of the attr construct according to the gxl-DTD
    public String id;
    public String name;
    public String kind;
    
    /*
    * inherited : public abstract void setAttributeValue(String attributeName,String value);    
    *             public abstract void close();
    *             public abstract Object createAttr();
    *             public abstract void closeAttr();
    *             public abstract Object createType();
    *             public abstract void closeType();
    */
    
    /** Empty constructor */
    public GXLAttrAPIImpl() {
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
     * Method to close a child-element of type locator (see GXL-DTD).
     */
    public void closeLocator() {}
        
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
     * Method to close a child-element of type seq (see GXL-DTD).
     */
    public void closeSeq() {}
    
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
     * Method to close a child-element of type set (see GXL-DTD).
     */
    public void closeSet() {}        
    
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
     * Method to close a child-element of type bag (see GXL-DTD).
     */
    public void closeBag() {}        
    
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
     * Method to close a child-element of type tup (see GXL-DTD).
     */
    public void closeTup() {}            
    
    /**
     * Method to create a child-element of type bool (see GXL-DTD).
     */
    public Object createBool() {
        lastSimpleType=0;
        return null;
    }
    
    /**
     * Method to close child-element of type bool (see GXL-DTD).
     */
    public void closeBool() {}        
    
    /**
     * Method to create a child-element of type int (see GXL-DTD).*/
    public Object createInt() {
        lastSimpleType=1;
        return null;
    }
    
    /**
     * Method to close a child-element of type int (see GXL-DTD).
     */
    public void closeInt() {}        
    
    /**
     * Method to create a child-element of type float (see GXL-DTD).
     */
    public Object createFloat() {
        lastSimpleType=2;
        return null;
    }
    
    /**
     * Method to close a child-element of type float (see GXL-DTD).
     */
    public void closeFloat() {}        
    
    /**
     * Method to create a child-element of type string (see GXL-DTD).
     */
    public Object createString() {
        lastSimpleType=3;
        return null;
    }
    
    /**
     * Method to close a child-element of type string (see GXL-DTD).
     */
    public void closeString() {}        
    
    /**
     * Method to create a child-element of type enum (see GXL-DTD).
     */
    public Object createEnum() {
        lastSimpleType=4;
        return null;
    }
    
    /**
     * Method to close a child-element of type enum (see GXL-DTD).
     */
    public void closeEnum() {} 
    
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
     * Method to set an attribute-value (see GXL-DTD).
     */
    public void setAttributeValue(String attributeName, String value) {
        String nameToLower=attributeName.toLowerCase();
        if (nameToLower.equals("id")) id=value;
        else if (nameToLower.equals("name")) name=value;
             else if (nameToLower.equals("kind")) kind=value;  
    }
    
    /**
     * Method to get the List of child elements.
     */
    public Object getChildElements() {
        return childElements;
    }        
    
    /**
     * Method to return a specific attribute.
     */
    public Object getAttributes () {
        attributes=new Vector();
        
        // write each attribute to the attributeVector
        // attributes are saved in an array of length 2, where the name of the attribute is located 
        // at pos 0 and its value at pos 1
        if (id!=null) attributes.add (new String[] {"id", id});
        if (name!=null) attributes.add (new String[] {"name", name});
        if (kind!=null) attributes.add (new String[] {"kind", kind});
        return attributes;
    }
    
    /**
     * Overwrites method toString() of class Object.
     */
    public String toString() {
        return new String ("attr");
    }
}