package GCF;

import java.net.*;
import java.lang.reflect.*;
import java.util.Objects;

/**
 * The GCF.GXLConnector's tasks is simply to provide a handler-method for each event
 * generated by a GXL parser. This provided handler method is one of the in the
 * abstract class hierarchy of the GXL Converter Framework defined methods.
 * So the GCF.GXLConnector does nothing more than to hold an abstract syntax tree in
 * memory and call a appropriate handler method for an incoming parser event.
 * <p>
 * example (typical parser event stream):
 * 1) the parser generates the event "startElement ("gxl")"
 * --> the GCF.GXLDocumentHandler calls the create()-method of the
 * GCF.GXLConnector with the parameter "gxl" and so the
 * GCF.GXLConnector calls the createGXL()-method of the GXLGXLAPIImpl
 * 2) the parser generates the event "startElement ("graph")"
 * --> the GCF.GXLDocumentHandler calls the create()-method of the
 * GCF.GXLConnector with the parameter "graph" and so the
 * GCF.GXLConnector calls the createGraph()-method of the GXLGXLAPIImpl
 * 3) the parser generates the event "startElement ("node")"
 * --> the GCF.GXLDocumentHandler calls the create()-method of the
 * GCF.GXLConnector with the parameter "node" and so the
 * GCF.GXLConnector calls the createNode()-method of the GXLGraphAPIImpl
 * 4) the parser generates the event "attribute ("id", "Node1")"
 * --> the GCF.GXLDocumentHandler calls the setAttribute()-method of the
 * GCF.GXLConnector with the parameters "id" and "Node1" and so the
 * GCF.GXLConnector calls the setAttribute()-method of the GXLGraphAPIImpl
 * with the same parameters
 * 5) the parser generates the event "endElement ("node")"
 * --> the GCF.GXLDocumentHandler calls the close()-method of the
 * GCF.GXLConnector with the parameter "node" and so the
 * GCF.GXLConnector calls the closeNode()-method of the GXLGraphAPIImpl
 * and the close()-method of the GXLNodeAPIImpl
 * 6) the parser generates the event "endElement ("graph")"
 * --> the GCF.GXLDocumentHandler calls the close()-method of the
 * GCF.GXLConnector with the parameter "graph" and so the
 * GCF.GXLConnector calls the closeGraph()-method of the GXLGXLAPIImpl
 * and the close()-method of the GXLGraphAPIImpl
 * 7) the parser generates the event "endElement ("gxl")"
 * --> the GCF.GXLDocumentHandler calls the close()-method of the
 * GXLConnectorwith the parameter "gxl" and so the
 * GCF.GXLConnector calls the close()-method of the GXLGXLAPIImpl
 */

public class GXLConnector extends Object {

    // some protected variables that can be used in the GXLConnector2 as well

    protected java.util.Stack parentNodeStack = new java.util.Stack();   // holds the abstract syntax tree
    protected Object parentNode;                                         // holds the parent node of the
    // current object
    protected int currentDepth = 0;                                        // holds the current depth of the
    // abstract syntac tree


    protected Object GXLObject;                     // holds the GXL construct, which is the root of the
    // document tree

    protected final String attrStr = "attr";         // constant String so that no unnecessary to objects
    // are created during the conversion

    protected String last_created, before_last_created; // to save which gxl construct has been created 
    // last and before last


    // some private variables

    private URL[] url = new URL[1];      // a URL, where the GCF.GXLConnector shall search for the
    // user implementation of the abstract classes
    private ClassLoader cl;                         // needed to load the right GXLOutputAPI (which has to
    // be located in the same directory as the user implementation


    // In the following some often used private variables (classes, methods and arrays) are defined, so 
    // that the GCF.GXLConnector works more efficiently

    private Class GXLUntypedValueContainerClass;  // Class of the GXLUntapedValueContainerAPI
    private Class GXLAttrAPIImplClass;            // Class of the GXLAttrAPIImpl
    private Class GXLRelClass;                    // Class of the GXLRelAPIImpl
    private Class GXLGraphClass;                  // Class of the GXLGraphAPIImpl
    private Class GXLGraphContClass;              // Class of the GXLGraphContainerAPI
    private Class GXLAttributedClass;             // Class of the GXLAttributedAPI
    private Class GXLTypedClass;                  // Class of the GXLTypedAndAttributedAPI
    private Class GXLGXLClass;                    // Class of the GXLGXLAPIImpl
    private Class GXLStandardClass;               // Class of the GXLStandardClass

    private Method setCurrDepthMethod;            // Method setCurrentDepth() of the GXLOutputAPI
    private Method closeMethod;                   // Method close() of the GXLStandardAPI
    private Method setAttrValMethod;              // Method setAttributeValue() of the GXLStandardAPI
    private Method printDataMethod;               // Method printData() of the GXLAttrAPIImpl
    private Method printDataMethod2;              // Method printData() of the GXLUntypedStandardValueContainerAPI

    private Class[] SingleStringClassArray = {(new String()).getClass()};
    // needed to invoke the method printData()
    private Class[] DoubleStringClassArray = {(new String()).getClass(), (new String()).getClass()};
    // defines a class array with 2 strings (needed to set Attributes)
    private Class[] TripleStringClassArray = {(new String()).getClass(), (new String()).getClass(), (new String()).getClass()};
    // defines a class array with 3 strings
    private Class[] SingleIntClassArray = {(new Integer(0)).getClass()};
    // needed to invoke the method setCurrentDepth()

    private java.util.Hashtable createMethods = new java.util.Hashtable(); // a hashtable for all create<nodeName>() methods
    private java.util.Hashtable closeMethods = new java.util.Hashtable(); // and all close<nodeName>() methods

    /**
     * The constructor has to get the following parameters :
     * GXLObject   : the GXL construct which is the root of the document tree
     * location    : the path, where the implementation of the abstract classes you want to
     * use is located
     * packageName : the package name of the implementation
     */
    public GXLConnector(Object GXLObject, URL location, String packageName) {

        // save the root object
        this.GXLObject = GXLObject;

        if ((packageName != null)) {

            // load the defined classes and methods
            url[0] = location;
            if (Objects.nonNull(url[0])) {
                cl = new URLClassLoader(url);
            } else {
                cl = getClass().getClassLoader();
            }

            /**********************************************************************************************/
            /* Initialization of all needed methods (including all create() and close() methods of the    */
            /* abstract class hierarchy and some further methods)                                         */
            /**********************************************************************************************/

            try {
                setCurrDepthMethod = cl.loadClass(packageName + ".GXLOutputAPI").getMethod("setCurrentDepth", SingleIntClassArray);

                GXLStandardClass = cl.loadClass(packageName + ".GXLStandardAPI");

                setAttrValMethod = GXLStandardClass.getMethod("setAttributeValue", DoubleStringClassArray);
                closeMethod = GXLStandardClass.getMethod("close", null);

                GXLGXLClass = cl.loadClass(packageName + ".GXLGXLAPIImpl");

                createMethods.put("graph2", GXLGXLClass.getMethod("createGraph", null));
                closeMethods.put("graph2", GXLGXLClass.getMethod("closeGraph", null));
                createMethods.put("cPI", GXLGXLClass.getMethod("createProcessingInstruction", DoubleStringClassArray));
                createMethods.put("cDocDec", GXLGXLClass.getMethod("createDoctypeDecl", TripleStringClassArray));
                createMethods.put("gxl", GXLGXLClass.getMethod("createGXL", null));


                GXLTypedClass = cl.loadClass(packageName + ".GXLTypedAndAttributedAPI");

                createMethods.put("type", GXLTypedClass.getMethod("createType", null));
                closeMethods.put("type", GXLTypedClass.getMethod("closeType", null));

                GXLAttributedClass = cl.loadClass(packageName + ".GXLAttributedAPI");


                createMethods.put("attr", GXLAttributedClass.getMethod("createAttr", null));
                closeMethods.put("attr", GXLAttributedClass.getMethod("closeAttr", null));

                GXLGraphContClass = cl.loadClass(packageName + ".GXLGraphContainerAPI");

                createMethods.put("graph", GXLGraphContClass.getMethod("createGraph", null));
                closeMethods.put("graph", GXLGraphContClass.getMethod("closeGraph", null));

                GXLGraphClass = cl.loadClass(packageName + ".GXLGraphAPIImpl");

                createMethods.put("node", GXLGraphClass.getMethod("createNode", null));
                closeMethods.put("node", GXLGraphClass.getMethod("closeNode", null));
                createMethods.put("edge", GXLGraphClass.getMethod("createEdge", null));
                closeMethods.put("edge", GXLGraphClass.getMethod("closeEdge", null));
                createMethods.put("rel", GXLGraphClass.getMethod("createRel", null));
                closeMethods.put("rel", GXLGraphClass.getMethod("closeRel", null));


                GXLRelClass = cl.loadClass(packageName + ".GXLRelAPIImpl");

                createMethods.put("relend", GXLRelClass.getMethod("createRelend", null));
                closeMethods.put("relend", GXLRelClass.getMethod("closeRelend", null));

                GXLAttrAPIImplClass = cl.loadClass(packageName + ".GXLAttrAPIImpl");

                createMethods.put("locator", GXLAttrAPIImplClass.getMethod("createLocator", null));
                createMethods.put("enum", GXLAttrAPIImplClass.getMethod("createEnum", null));
                createMethods.put("seq", GXLAttrAPIImplClass.getMethod("createSeq", null));
                createMethods.put("set", GXLAttrAPIImplClass.getMethod("createSet", null));
                createMethods.put("bag", GXLAttrAPIImplClass.getMethod("createBag", null));
                createMethods.put("tup", GXLAttrAPIImplClass.getMethod("createTup", null));
                createMethods.put("int", GXLAttrAPIImplClass.getMethod("createInt", null));
                createMethods.put("bool", GXLAttrAPIImplClass.getMethod("createBool", null));
                createMethods.put("string", GXLAttrAPIImplClass.getMethod("createString", null));
                createMethods.put("float", GXLAttrAPIImplClass.getMethod("createFloat", null));
                closeMethods.put("locator", GXLAttrAPIImplClass.getMethod("closeLocator", null));
                closeMethods.put("enum", GXLAttrAPIImplClass.getMethod("closeEnum", null));
                closeMethods.put("seq", GXLAttrAPIImplClass.getMethod("closeSeq", null));
                closeMethods.put("set", GXLAttrAPIImplClass.getMethod("closeSet", null));
                closeMethods.put("bag", GXLAttrAPIImplClass.getMethod("closeBag", null));
                closeMethods.put("tup", GXLAttrAPIImplClass.getMethod("closeTup", null));
                closeMethods.put("int", GXLAttrAPIImplClass.getMethod("closeInt", null));
                closeMethods.put("bool", GXLAttrAPIImplClass.getMethod("closeBool", null));
                closeMethods.put("string", GXLAttrAPIImplClass.getMethod("closeString", null));
                closeMethods.put("float", GXLAttrAPIImplClass.getMethod("closeFloat", null));

                printDataMethod = GXLAttrAPIImplClass.getMethod("printData", SingleStringClassArray);

                GXLUntypedValueContainerClass = cl.loadClass(packageName + ".GXLUntypedStandardValueContainerAPI");

                createMethods.put("locator2", GXLUntypedValueContainerClass.getMethod("createLocator", null));
                createMethods.put("enum2", GXLUntypedValueContainerClass.getMethod("createEnum", null));
                createMethods.put("seq2", GXLUntypedValueContainerClass.getMethod("createSeq", null));
                createMethods.put("set2", GXLUntypedValueContainerClass.getMethod("createSet", null));
                createMethods.put("bag2", GXLUntypedValueContainerClass.getMethod("createBag", null));
                createMethods.put("tup2", GXLUntypedValueContainerClass.getMethod("createTup", null));
                createMethods.put("int2", GXLUntypedValueContainerClass.getMethod("createInt", null));
                createMethods.put("bool2", GXLUntypedValueContainerClass.getMethod("createBool", null));
                createMethods.put("string2", GXLUntypedValueContainerClass.getMethod("createString", null));
                createMethods.put("float2", GXLUntypedValueContainerClass.getMethod("createFloat", null));
                closeMethods.put("locator2", GXLUntypedValueContainerClass.getMethod("closeLocator", null));
                closeMethods.put("enum2", GXLUntypedValueContainerClass.getMethod("closeEnum", null));
                closeMethods.put("seq2", GXLUntypedValueContainerClass.getMethod("closeSeq", null));
                closeMethods.put("set2", GXLUntypedValueContainerClass.getMethod("closeSet", null));
                closeMethods.put("bag2", GXLUntypedValueContainerClass.getMethod("closeBag", null));
                closeMethods.put("tup2", GXLUntypedValueContainerClass.getMethod("closeTup", null));
                closeMethods.put("int2", GXLUntypedValueContainerClass.getMethod("closeInt", null));
                closeMethods.put("bool2", GXLUntypedValueContainerClass.getMethod("closeBool", null));
                closeMethods.put("string2", GXLUntypedValueContainerClass.getMethod("closeString", null));
                closeMethods.put("float2", GXLUntypedValueContainerClass.getMethod("closeFloat", null));
                printDataMethod2 = GXLUntypedValueContainerClass.getMethod("printData", SingleStringClassArray);
            } // try
            catch (ClassNotFoundException cnfe) {
                printError(0, "Constructor");
            } catch (NoSuchMethodException nsme) {
                printError(1, "Constructor");
            } catch (Exception e) {
                printError(2, "Constructor");
            }
        } //  if ((location != null) && (packageName != null))
    }

    /**
     * Calls the {@literal create"<nodeName>"()}-method of the currently opened GXL construct.
     */
    public void create(String nodeName) {

        // if the depth of the stack is 0, the current construct is the root object...
        if (currentDepth <= 0) parentNode = GXLObject;
            // otherwise load it from the stack...
        else parentNode = parentNodeStack.peek();

        // save the last 2 constructs which have been cerated
        before_last_created = last_created;
        last_created = nodeName;

        // invoke the required create<nodeName>() method
        if (currentDepth > 0) {
            if (currentDepth == 1) parentNode = invokeMethod("graph2", parentNode, null, 0);
            else parentNode = invokeMethod(nodeName.toLowerCase(), parentNode, null, 0);
        } else invokeMethod(nodeName.toLowerCase(), parentNode, null, 0);

        // add the created GXL construct to the parentNodeStack and increase the
        // depth of the syntax tree
        parentNodeStack.add(currentDepth, parentNode);
        currentDepth += 1;
        // set the currentDepth in the GXLOutputAPI
        Object[] args = {new Integer(currentDepth)};
        try {
            setCurrDepthMethod.invoke(null, new Object[]{new Integer(currentDepth)});
        } catch (Exception e) {
            printError(3, "create");
        }
    }

    /**
     * Calls the printData() method of the currently opened GXL construct to handle
     * CDATA-events.
     */
    public void printData(String data) {
        // invoke the required printData() method...
        try {
            // get the before last object from the stack
            Object dummy = parentNodeStack.pop();
            if (parentNodeStack.size() != 0)
                parentNode = parentNodeStack.peek();
            parentNodeStack.push(dummy);
            Object[] args = {data};
            try {
                // if the parent object is of type attr...
                if (before_last_created.equals(attrStr))
                    //...call the printData() method of the GXLAttrAPIImpl
                    printDataMethod.invoke(parentNode, args);
                    // otherwise...
                else
                    //...call the printData() method of the GXLUntypedStandardValueContainerAPI
                    printDataMethod2.invoke(parentNode, args);
                last_created = before_last_created;
            } catch (Exception e) {
                printError(4, "printData");
            }

        } catch (NullPointerException npe) {
            printError(5, "printData");
        }
    }

    /**
     * Calls the "setAttributeValue"-method of the currently opened GXL-construct to handle
     * attribute-events.
     */
    public void setAttributeValue(String attributeName, String value) {
        // invoke the setAttributeValue() method
        Object[] args = {attributeName, value};
        try {
            setAttrValMethod.invoke(parentNode, args);
        } catch (Exception e) {
            printError(6, "setAttributeValue");
        }
    }


    /**
     * Calls the close() method of the currently opened GXL construct and
     * the {@literal close"<nodeName>"()} method of the parent construct.
     */
    public void close(String nodeName) {
        // call the close() method of the current GXL construct
        try {
            parentNode = parentNodeStack.pop();
            closeMethod.invoke(parentNode, null);
        } catch (java.lang.reflect.InvocationTargetException ite) {
            printError(7, "close");
        } catch (java.lang.IllegalAccessException iae) {
            printError(8, "close");
        } catch (NullPointerException npe) {
            // do nothing here, because a NullPointerException can occur when
            // the current construct is one of the value types (see GXL Graph Model)
            // that has no representing class in the abstract class hierarchy
        } catch (Exception e) {
        }

        // if the current GXL construct is not "gxl" call the close<nodeName>() method of
        // the parent construct
        if (currentDepth > 1) {
            parentNode = parentNodeStack.peek();
            if (currentDepth == 2) parentNode = invokeMethod("graph2", parentNode, null, 1);
            else parentNode = invokeMethod(nodeName.toLowerCase(), parentNode, null, 1);
        } // if (currentDepth>1)
        if (currentDepth > 0) {
            // decrease the current depth and invoke the setCurrentDepth() method of the GXLOutputAPI
            currentDepth -= 1;
            try {
                setCurrDepthMethod.invoke(null, new Object[]{new Integer(currentDepth)});
            } catch (Exception e) {
                printError(8, "close");
            }
        } // if (currentDepth>0)
    }

    /**
     * Method to create the DOCTYPE declaration of the GXL document.
     */
    public void createDoctypeDecl(String name, String pubid, String sysid) {
        // invoke the createDoctypeDecl() method, which has to be implemented 
        // in the GXLGXLAPIImpl
        Object[] args = {name, pubid, sysid};
        invokeMethod("cDocDec", GXLObject, args, 0);
    }

    /**
     * Method to create a processing instruction in the GXL document.
     */
    public void createProcessingInstruction(String target, String data) {
        // invoke the createProcessingInstruction() method, which has to be 
        // implemented in the GXLGXLAPIImpl
        Object[] args = {target, data};
        invokeMethod("cPI", GXLObject, args, 0);
    }


    /**
     * Outputs an error message and stops the program execution giving an exit code.
     *
     * @param errorNumber An internal error number
     * @param methodName  The method where the error occured
     */
    protected void printError(int errorNumber, String methodName) {
        System.out.println("An internal error occured.");
        System.out.println("error number : " + errorNumber);
        System.out.println("method       : " + methodName);
//        throw new RuntimeException(methodName);
//        System.exit (errorNumber);
    }

    /**
     * Method to resolve and invoke the method of the GCF given by the following parameters
     *
     * @param keyName   The name of the method
     * @param toResolve The object declaring the method that ahall be called
     * @param args      The arguments for the method
     *                  //     * @param hastable  0 -> createMethods is used, 1 -> closeMethods is used
     */

    private Object invokeMethod(String keyName, Object toResolve, Object[] args, int hashtable) {
        Class a_class = null;
        try {

            // get the method from the hastable...
            Method m = (hashtable == 0) ? (Method) createMethods.get(keyName) : (Method) closeMethods.get(keyName);
            // ..., invoke it and return the result
            return (Object) m.invoke(toResolve, args);
        }
        // catch the exceptions that can occur
        catch (java.lang.NullPointerException npe) {
            if (!keyName.equals("close")) printError(9, "invoke" + "at " + keyName);
        } catch (InvocationTargetException ivte) {
            printError(10, "invoke" + "at " + keyName);
        } catch (IllegalAccessException iae) {
            printError(11, "invoke" + "at " + keyName);
        } catch (Exception e) {
            if (keyName.substring(keyName.length() - 2, keyName.length() - 1).equals("2"))
                printError(12, "invoke" + "at " + keyName);
            else
                invokeMethod(keyName + "2", toResolve, args, hashtable);
        }
        return new Object();
    }


}