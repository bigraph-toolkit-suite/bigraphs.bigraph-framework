package GCF.DefaultImpl2;

import java.util.Vector;

/**
 * An abstract class to represent the locator-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public class GXLLocatorAPIImpl extends GXLStandardAPI {

    private Vector attributes = new Vector(); // to return the attributes
    
    // attributes according to the gxl-DTD
    public String xlink_type = "simple";
    public String xlink_href;
    
    /*
     * inherited: public abstract void setAttributeValue(String attributeName,String value);
     *            public abstract void close();
     *            public abstract Object getChildElements();
     */
    
    /** Empty constructor. */
    public GXLLocatorAPIImpl() {
    }
    
    /**
     * Method to set an attribute-value (see GXL-DTD).
     */
    public void setAttributeValue(String attributeName, String value) {
        if (attributeName.toLowerCase().equals("xlink:href")) xlink_href=value;
    }
    
    /**
     * Method to get the List of child elements.
     */
    public Object getChildElements() {
        return new Vector();
    }
    
    /**
     * Method to return a specific attribute.
     */
    public Object getAttributes () {
        // write each attribute to the attributeVector
        // attributes are saven in a array of length 2, where the name of the attribute is located 
        // at pos 0 and its value at pos 1
        if (xlink_href!=null) {
            attributes=new Vector();
            attributes.add (new String[] {"xlink:href", xlink_href});
        }
     
        return attributes;
    }
    
    /**
     * Overwrites method toString() of class Object.
     */
    public String toString() {
        return new String ("locator");
    }

}