package GCF.DefaultImpl2;

import java.util.Vector;

/**
 * An abstract class to represent the graph-Construct of the GXL-DTD.
 * See "inherited" for further information on the provided methods.
 */
public class GXLGraphAPIImpl extends GXLStandardAPI {
    
    private Vector childElements = new Vector(); // to save the child objects
    
    private Vector attributes = new Vector(); // to return the attributes
    
    // attributes for the Graph construct according to the gxl-DTD
    public String id ;                         
    public String role;
    public boolean edgeids = false;
    public boolean hypergraph = false;
    public String edgemode = "directed";
    
    
    /*
    * inherited:public abstract void setAttributeValue(String attributeName,String value);    
    *           public abstract void close();
    *           public abstract Object getChildElements();
    */
    
    /** Empty constructor. */
    public GXLGraphAPIImpl() {
    }
    
    /**
     * Method to create a child-element of type node (see GXL-DTD).
     */
    public Object createNode() {
        GXLNodeAPIImpl node = new GXLNodeAPIImpl();
        childElements.add(node);
        return node;
    }
    
    /**
     * Method to create a child-element of type edge (see GXL-DTD).
     */
    public Object createEdge() {
        GXLEdgeAPIImpl edge = new GXLEdgeAPIImpl();
        childElements.add(edge);
        return edge;
    }
    
    /**
     * Method to create a child-element of type rel (see GXL-DTD).
     */
    public Object createRel() {
        GXLRelAPIImpl rel = new GXLRelAPIImpl();
        childElements.add(rel);
        return rel;
    }
    
    /**
     * Method to close a child-element of type node (see GXL-DTD).
     */
    public void closeNode() {}
    
    /**
     * Method to close a child-element of type edge (see GXL-DTD).
     */
    public void closeEdge() {}
    
    /**
     * Method to close a child-element of type rel (see GXL-DTD).
     */
    public void closeRel() {}
    
    /**
     * Method to set an attribute-value (see GXL-DTD).
     */
    public void setAttributeValue(String attributeName, String value) {
        String name=attributeName.toLowerCase();
        if (name.equals("id")) id=value;
        else if (name.equals("role")) role=value;
             else if (name.equals("edgeids")) edgeids=value.toLowerCase().equals("true");
                  else if (name.equals ("hypergraph")) hypergraph=value.toLowerCase().equals("true");
                       else if (name.equals ("edgemode")) edgemode=value.toLowerCase();        
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
        if (role!=null) attributes.add (new String[] {"role", role});
        attributes.add (new String[] {"edgeids", String.valueOf(edgeids)});
        attributes.add (new String[] {"hypergraph", String.valueOf(hypergraph)});
        if (edgemode!=null) attributes.add (new String[] {"edgemode", edgemode});
       
        return attributes;
    }
    
    /**
     * Overwrites method toString() of class Object.
     */
    public String toString() {
        return new String ("graph");
    }
    
}