package GCF.DefaultImpl2;

import java.util.Vector;

/**
 * An abstract class to represent the relend-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public class GXLRelendAPIImpl extends GXLAttributedAPI {
    
    private Vector childElements = new Vector(); // to save the child objects
    
    private Vector attributes = new Vector(); // to return the attributes;
    
    // attributes of the relend construct according to the gxl-DTD
    public String target;
    public String role;
    public String direction;
    public String startorder;
    public String endorder;
    
    /*
    * inherited:public abstract void setAttributeValue(String attributeName,String value);    
    *           public abstract void close();
    *           public abstract Object getChildElements();
    *           public abstract Object createAttr();   
    *           public abstract void closeAttr();
    */
    
    /** Empty constructor. */
    public GXLRelendAPIImpl() {
    }               
    
    /**
     * Method to create a child-element of type attr (see GXL-DTD).
     */
    public Object createAttr() {
        GXLAttrAPIImpl attr = new GXLAttrAPIImpl();
        childElements.add(attr);
        return attr;
    }
    
    /**
     * Method to set an attribute-value (see GXL-DTD).
     */
    public void setAttributeValue(String attributeName, String value) {
        String name=attributeName.toLowerCase();
        if (name.equals("target")) target=value;
        else if (name.equals("role")) role=value;
             else if (name.equals("direction")) direction=value.toLowerCase();
                  else if (name.equals ("startorder")) startorder=value;
                       else if (name.equals ("endorder")) endorder=value;    
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
        // attributes are saven in a array of length 2, where the name of the attribute is located 
        // at pos 0 and its value at pos 1
        if (target!=null) attributes.add (new String[] {"target", target});
        if (role!=null) attributes.add (new String[] {"role", role});
        if (direction!=null) attributes.add (new String[] {"direction", direction});        
        if (startorder!=null) attributes.add (new String[] {"startorder", startorder});
        if (endorder!=null) attributes.add (new String[] {"endorder", endorder});
        
        return attributes;
    }
    
    /**
     * Overwrites method toString() of class Object.
     */
    public String toString() {
        return new String ("relend");
    }
}