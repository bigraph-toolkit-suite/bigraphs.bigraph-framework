package GCF.DefaultImpl2;

import java.util.Vector;

/**
 * An abstract class to represent the node-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public class GXLNodeAPIImpl extends GXLGraphContainerAPI {
    
    private Vector childElements = new Vector(); // to save the child objects
    
    private Vector attributes = new Vector(); // to return the attributes
    
    public String id; // id-attribute for the node construct
    
    /*
    * inherited:public abstract void setAttributeValue(String attributeName,String value);    
    *           public abstract void close();
    *           public abstract Object getChildElements();
    *           public abstract Object createType();
    *           public abstract void closeType();
    *           public abstract Object createAttr();
    *           public abstract void closeAttr();
    *           public abstract Object createGraph(); 
    *           public abstract void closeGraph();
    */
    
    /** Empty constructor. */
    public GXLNodeAPIImpl(){
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
     * Method to create a child-element of type type (see GXL-DTD).
     */
    public Object createType() {
        GXLTypeAPIImpl type = new GXLTypeAPIImpl();
        childElements.add(type);
        return type;
    }
    
    /**
     * Method to create a child-element of type graph (see GXL-DTD).
     */
    public Object createGraph() {
        GXLGraphAPIImpl graph = new GXLGraphAPIImpl();
        childElements.add(graph);
        return graph;
    }
    
    /**
     * Method to set an attribute-value (see GXL-DTD).
     */
    public void setAttributeValue(String attributeName, String value) {
        if (attributeName.toLowerCase().equals ("id")) this.id=value;
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
        if (id!=null) attributes.add (new String[] {"id", id});
        return attributes;
    }
    
    /**
     * Overwrites method toString() of class Object.
     */
    public String toString() {
        return new String ("node");
    }
    
    
}