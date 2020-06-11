package GCF;

import com.microstar.xml.XmlHandler;
import com.microstar.xml.XmlParser;
import de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants;
//import lombok.AllArgsConstructor;
//import lombok.Data;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


/**
 * Base class used to handle the events raised by the XML-parser.
 * It implements the XMLHandler-Interface which defines specific
 * methods for each possible parser-event.
 * The events (except the startDocument and endDocument-events) are handeled in
 * the way  that the according method in the GXLConnecotr is being called.
 * The GCF.GXLDocumentHandler has to be created over the createDocumentHandler()-method
 * of the GCF.GXLConverterAPI.
 */
public class GXLDocumentHandler implements XmlHandler {


    public boolean isApplet = false;

    public XmlParser parser;

    private GXLConnector con;

//    @Deprecated
//    Stack<String> elementStack = new Stack<>();

    private Stack<ChldPrntRel> elementStack2 = new Stack<>();
    Stack<BEdgeType> edgesStack = new Stack<>();
    private Stack<BLinkRel> elementStackLinks = new Stack<>();
    private List<ChldPrntRel> edges = new LinkedList<>();
    //    Stack<String> attributeIndexStack = new Stack<>();
    private String xmlnsIdentifier = "";
    private Map<String, List<BLinkRel>> linkMap = new HashMap<>();
    private Map<String, BLinkRel> edgesMap = new HashMap<>();
    private int edgeCounter = 0;
    private int outerCounter = 0;
    private int innerCounter = 0;

    //    @Data
//    @AllArgsConstructor
    public static class ChldPrntRel {
        public String type;
        public String idTargetLink;
        public String id;
        public String name = "";

        public ChldPrntRel(String type, String idTargetLink, String id, String name) {
            this.type = type;
            this.idTargetLink = idTargetLink;
            this.id = id;
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public String getIdTargetLink() {
            return idTargetLink;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    //    @Data
//    @AllArgsConstructor
    public static class BLinkRel {
        public String type = "";
        public String fromId = "";
        public String bLink = "";
        public String name = "";
        public String bPoints = "";
        public int count = 0;

        public BLinkRel(String type, String fromId, String bLink, String name, String bPoints, int count) {
            this.type = type;
            this.fromId = fromId;
            this.bLink = bLink;
            this.name = name;
            this.bPoints = bPoints;
            this.count = count;
        }

        public String getType() {
            return type;
        }

        public String getFromId() {
            return fromId;
        }

        public String getbLink() {
            return bLink;
        }

        public String getName() {
            return name;
        }

        public String getbPoints() {
            return bPoints;
        }

        public int getCount() {
            return count;
        }
    }

//    @Data
//    @AllArgsConstructor
    public static class BEdgeType {

    }

    private static String createGxlId() {
        return "";
    }

    /**
     * The constructor needs a referrence to the current GCF.GXLConnector-instance to
     * call the methods according to the parser-events.
     */
    public GXLDocumentHandler(GXLConnector con) {
        this.con = con;
    }


    //////////////////////////////////////////////////////////////////////
    // Implementation of XmlParser interface.
    //
    // The following methods provide a full skeleton implementation of the
    // XmlHandler interface.
    //////////////////////////////////////////////////////////////////////


    /**
     * Resolve an external entity.
     * <p>This method could generate a new URL by looking up the
     * public identifier in a hash table, or it could replace the
     * URL supplied with a different, local one; for now, however,
     * just return the URL supplied.
     * <p>
     * //     * @see com.Microstar.xml.XmlHandler#resolveEntity
     */
    public Object resolveEntity(String publicId, String systemId) {
        return null;
    }


    public void startExternalEntity(String systemId) {
    }


    public void endExternalEntity(String systemId) {
    }


    /**
     * Handle the start of the document.
     * This method will always be called first.
     * <p>
     * //     * @see com.Microstar.xml.XmlHandler#startDocument
     */
    public void startDocument() {
        displayText("Start document");
//        <?xml version="1.0" encoding="ISO-10646-UCS-2"?>
        con.createProcessingInstruction("xml", "version=\"1.0\" encoding=\"UTF-8\"");
        con.createDoctypeDecl("gxl", "http://www.gupro.de/GLX/gxl.dtd", "gxl.dtd");
        con.create("gxl");
    }


    /**
     * Handle the end the document.
     * This method will always be called last.
     * <p>
     * //     * @see com.Microstar.xml.XmlHandler#endDocument
     */
    public void endDocument() {
        displayText("End document");
        con.close("gxl");
    }


    /**
     * Handle a DOCTYPE declaration.
     * Well-formed XML documents might not have one of these.
     * <p>The query methods in XmlParser will return useful
     * values only after this callback.
     * <p>
     * //     * @see com.Microstar.xml.XmlHandler#doctypeDecl
     */
    public void doctypeDecl(String name,
                            String pubid, String sysid) {
        con.createDoctypeDecl(name, pubid, sysid);
    }


    /**
     * Handle an attribute value specification.
     * <p>@see com.Microstar.xml.XmlHandler#attribute
     */
    public void attribute(String name, String value, boolean isSpecified) {
        if (name.contains("xmlns:") && !name.contains("xmlns:xmi") && !name.contains("xmlns:xsi") && xmlnsIdentifier.equals("")) {
            xmlnsIdentifier = name.replace("xmlns:", "");
        }

        if (!isSpecified) {
            return;
        }

        // for link graph
        // name attribute for link graph elements
        if (!elementStackLinks.isEmpty()) {
            if (name.contains("name")) {
                BLinkRel peek = elementStackLinks.peek();
                peek.name = value;
                return;
            }
            if (name.contains(BigraphMetaModelConstants.REFERENCE_LINK)) {
                BLinkRel peek = elementStackLinks.peek();
                if (peek.type.equals(BigraphMetaModelConstants.REFERENCE_PORT)) { // is port
                    peek.bLink = value;
                    linkMap.putIfAbsent(value, new LinkedList<>());
                    linkMap.get(value).add(peek);
                    elementStackLinks.pop(); // a port is not outputted to gxl, so remove from stack
                    return;
                } else if (peek.type.equals(BigraphMetaModelConstants.REFERENCE_BINNERNAMES)) { // is inner name
                    peek.bLink = value;
                    linkMap.putIfAbsent(value, new LinkedList<>());
                    linkMap.get(value).add(peek);
                    return;
                }
            }
            if (name.contains(BigraphMetaModelConstants.REFERENCE_POINT)) {
                BLinkRel peek = elementStackLinks.peek();
                if (peek.type.equals(BigraphMetaModelConstants.REFERENCE_BEDGES)) { // is edge
                    peek.bPoints = value;
                    return;
                } else if (peek.type.equals(BigraphMetaModelConstants.REFERENCE_BOUTERNAMES)) { // is outer name
                    peek.bPoints = value;
                    return;
                }
            }
        }


        // for the place graph
        if (!elementStack2.isEmpty()) {
            ChldPrntRel peek = elementStack2.peek();
            if (peek.type.contains(BigraphMetaModelConstants.CLASS_NODE) || peek.type.contains(BigraphMetaModelConstants.CLASS_ROOT) || peek.type.contains(BigraphMetaModelConstants.CLASS_SITE)) {
                if (name.contains("name")) {
                    peek.id = value;
                }
                if (name.contains("xsi:type")) {
                    peek.name = value;
                    if (value.contains(BigraphMetaModelConstants.CLASS_SITE)) { //rectify element type
                        peek.type = BigraphMetaModelConstants.CLASS_SITE;
                    }
                }
                if (peek.type.contains(BigraphMetaModelConstants.CLASS_ROOT) || peek.type.contains(BigraphMetaModelConstants.CLASS_SITE)) {
                    if (name.contains("index")) {
//                    if (!elementStack2.isEmpty()) {
//                        ChldPrntRel pop = elementStack2.peek();
                        String prefix = peek.type.contains(BigraphMetaModelConstants.CLASS_SITE) ? "s_" : "r_";
                        peek.idTargetLink = prefix + value;
                        peek.id = prefix + value;
//                    }
                    }
                }
            }
        }
    }


    /**
     * Handle the start of an element.
     * <p>@see com.Microstar.xml.XmlHandler#startElement
     */
    public void startElement(String name) {
        System.out.println("name=" + name);
        if (name.toLowerCase().contains("bbigraph")) {
            con.create("graph");
            con.setAttributeValue("id", "sample");
            con.setAttributeValue("hypergraph", "true");
            con.setAttributeValue("edgemode", "undirected");
        } else if (name.contains(BigraphMetaModelConstants.REFERENCE_BROOTS)) {
            elementStack2.push(new ChldPrntRel(BigraphMetaModelConstants.CLASS_ROOT, "", "r_0", "r_0"));
        } else if (name.contains(BigraphMetaModelConstants.REFERENCE_CHILD)) {
            elementStack2.push(new ChldPrntRel(BigraphMetaModelConstants.CLASS_NODE, "", "s_0", "s_0")); //if not a site, the values will be overridden
        } else if (name.contains(BigraphMetaModelConstants.REFERENCE_PORT)) {
            ChldPrntRel peek = elementStack2.peek();
            elementStackLinks.push(new BLinkRel(BigraphMetaModelConstants.REFERENCE_PORT, peek.id, "", "", "", 0));
        } else if (name.contains(BigraphMetaModelConstants.REFERENCE_BEDGES)) {
            elementStackLinks.push(new BLinkRel(BigraphMetaModelConstants.REFERENCE_BEDGES, "", "", "", "", edgeCounter++));
        } else if (name.contains(BigraphMetaModelConstants.REFERENCE_BINNERNAMES)) {
            elementStackLinks.push(new BLinkRel(BigraphMetaModelConstants.REFERENCE_BINNERNAMES, "", "", "", "", innerCounter++));
        } else if (name.contains(BigraphMetaModelConstants.REFERENCE_BOUTERNAMES)) {
            elementStackLinks.push(new BLinkRel(BigraphMetaModelConstants.REFERENCE_BOUTERNAMES, "", "", "", "", outerCounter++));
        }
    }


    /**
     * Handle the end of an element.
     * <p>@see com.Microstar.xml.XmlHandler#endElement
     */
    public void endElement(String name) {
        if (name.toLowerCase().contains("bbigraph")) {
            // rebuild the hicreate place graph links between nodes
            for (ChldPrntRel each : edges) {
                con.create("edge");
                con.setAttributeValue("from", each.id);
                con.setAttributeValue("to", each.idTargetLink);
                createIntAttribute("Line", "92");
                con.close("edge");
            }

            edgesMap.forEach((k, v) -> {
                if (linkMap.get(k) != null) {
                    List<BLinkRel> bLinkRels = linkMap.get(k);

                    if (v.type.equals(BigraphMetaModelConstants.REFERENCE_BEDGES)) {

                        con.create("rel");
                        con.setAttributeValue("id", v.name);
                        con.setAttributeValue("isdirected", "false");
                        createStringAttribute("type", BigraphMetaModelConstants.CLASS_EDGE);
                        for (BLinkRel each : bLinkRels) {
                            con.create("relend");

                            String targetId = each.fromId;
                            if (each.type.equals(BigraphMetaModelConstants.REFERENCE_BINNERNAMES)) {
                                targetId = each.name;
                            }
                            con.setAttributeValue("target", targetId);
                            createStringAttribute("type", BigraphMetaModelConstants.REFERENCE_POINT);
                            con.close("relend");
                        }
                        con.close("rel");
                    } else if (v.type.equals(BigraphMetaModelConstants.REFERENCE_BOUTERNAMES)) {
                        for (BLinkRel each : bLinkRels) {
                            con.create("edge");
                            String fromId = each.fromId;
                            if (each.type.equals(BigraphMetaModelConstants.REFERENCE_BINNERNAMES)) {
                                fromId = each.name;
                            }
                            con.setAttributeValue("from", fromId);
                            con.setAttributeValue("to", v.name);
                            createStringAttribute("type", BigraphMetaModelConstants.REFERENCE_POINT);
                            createColorAttribute("green");
                            con.close("edge");
                        }
                    }
                }
            });
            con.close("graph");
            return;
        }

        ChldPrntRel currentChild;

        if (name.contains(BigraphMetaModelConstants.REFERENCE_BROOTS)) {
            if (elementStack2.isEmpty()) {
                return;
            }
            currentChild = elementStack2.pop();
            con.create("node");
            // has index attribute?
            if (!currentChild.id.isEmpty()) {
                String id = currentChild.id; //attributeIndexStack.pop();
                con.setAttributeValue("id", id);
                createStringAttribute("type", BigraphMetaModelConstants.CLASS_ROOT);
            } else {
                con.setAttributeValue("id", "r_0");
            }
            con.create("attr");
            con.setAttributeValue("name", "shape");
            con.create("string");
            con.printData("rectangle");
            con.close("string");
            con.close("attr");

            con.close("node");
        } else if (name.contains(BigraphMetaModelConstants.REFERENCE_CHILD)) {
            if (elementStack2.isEmpty()) {
                return;
            }
            currentChild = elementStack2.pop();
            con.create("node");
            con.setAttributeValue("id", currentChild.id);
            createStringAttribute("type", BigraphMetaModelConstants.CLASS_NODE);
            String label = currentChild.id;
            if (currentChild.type.equals(BigraphMetaModelConstants.CLASS_SITE)) {
                createStringAttribute("shape", "oval");
                createStringAttribute("style", "filled");
            } else {
                label = currentChild.id + currentChild.name.replace(xmlnsIdentifier, "");
            }
            createStringAttribute("label", label);

            con.close("node");

            if (!elementStack2.isEmpty()) {
                ChldPrntRel prnt = elementStack2.peek();
                currentChild.idTargetLink = prnt.id;
                edges.add(currentChild);
            }
        }

        // for link graph
        if (name.contains(BigraphMetaModelConstants.REFERENCE_BEDGES)) {
            BLinkRel peek = elementStackLinks.peek();
            //todo: edge in separater liste ablegen
            if (peek.type.equals(BigraphMetaModelConstants.REFERENCE_BEDGES)) {
//                linkMap.
                edgesMap.putIfAbsent("//@" + peek.type + "." + peek.count, peek);
                elementStackLinks.pop();
            }
        } else if (name.contains(BigraphMetaModelConstants.REFERENCE_BINNERNAMES)) {
            if (elementStackLinks.isEmpty()) {
                return;
            }
            BLinkRel pop = elementStackLinks.pop();

            con.create("node");
            con.setAttributeValue("id", pop.name);
            createStringAttribute("type", BigraphMetaModelConstants.CLASS_INNERNAME);
            String label = pop.type + ":" + pop.name;
            createStringAttribute("label", label);
            createColorAttribute("green");
            con.close("node");

        } else if (name.contains(BigraphMetaModelConstants.REFERENCE_BOUTERNAMES)) {
            if (elementStackLinks.isEmpty()) {
                return;
            }
            BLinkRel pop = elementStackLinks.pop();
            con.create("node");
            con.setAttributeValue("id", pop.name);
            createStringAttribute("type", BigraphMetaModelConstants.CLASS_OUTERNAME);
            String label = pop.type + ":" + "outer:" + pop.name;
            createStringAttribute("label", label);
            createColorAttribute("green");
            con.close("node");
            edgesMap.putIfAbsent("//@" + pop.type + "." + pop.count, pop);
        }
    }

    private void createStringAttribute(String name, String value) {
        con.create("attr");
        con.setAttributeValue("name", name);
        con.create("string");
        con.printData(value);
        con.close("string");
        con.close("attr");
    }

    private void createIntAttribute(String name, String value) {
        con.create("attr");
        con.setAttributeValue("name", name);
        con.create("int");
        con.printData(value);
        con.close("int");
        con.close("attr");
    }

    private void createColorAttribute(String color) {
        createStringAttribute("color", color);
    }


    /**
     * Handle character data.
     * <p>@see com.Microstar.xml.XmlHandler#charData
     */
    public void charData(char ch[], int start, int length) {
        String a = new String(ch);
        con.printData(a.substring(0, length));
    }


    /**
     * Handle ignorable whitespace.
     * <p>Do nothing for now.  Subclasses can override this method
     * if they want to take a specific action.
     * <p>
     * //     * @see com.Microstar.xml.XmlHandler#ignorableWhitespace
     */
    public void ignorableWhitespace(char ch[],
                                    int start, int length) {
    }


    /**
     * Handle a processing instruction.
     * <p>@see com.Microstar.xml.XmlHandler#processingInstruction
     */
    public void processingInstruction(String target,
                                      String data) {
        con.createProcessingInstruction(target, data);
    }


    /**
     * Handle a parsing error.
     * <p>By default, print a message and throw an Error.
     * <p>Subclasses can override this method if they want to do something
     * different.
     * <p>
     * //     * @see com.Microstar.xml.XmlHandler#error
     */
    public void error(String message,
                      String url, int line, int column) {
        displayText("FATAL ERROR: " + message);
        displayText("  at " + url.toString() + ": line " + line
                + " column " + column);
        throw new Error(message);
    }


    //////////////////////////////////////////////////////////////////////
    // General utility methods.
    //////////////////////////////////////////////////////////////////////


    /**
     * Start a parse in application mode.
     * <p>Output will go to STDOUT.
     *
     * @see #displayText
     * //     * @see com.microstar.xml.XmlParser#run
     */
    void doParse(String url)
            throws java.lang.Exception {
        String docURL = makeAbsoluteURL(url);

        // create the parser
        parser = new XmlParser();
        parser.setHandler(this);
        parser.parse(docURL, null, (String) null);
    }

    static String makeAbsoluteURL(String url)
            throws MalformedURLException {
        URL baseURL;

        String currentDirectory = System.getProperty("user.dir");

        String fileSep = System.getProperty("file.separator");
        String file = currentDirectory.replace(fileSep.charAt(0), '/') + '/';
        if (file.charAt(0) != '/') {
            file = "/" + file;
        }
        baseURL = new URL("file", null, file);
        return new URL(baseURL, url).toString();
    }


    /**
     * Display text on STDOUT or in an applet TextArea.
     */
    void displayText(String text) {
        System.out.println(text);
    }


    /**
     * Escape a string for printing.
     */
    String escape(char ch[], int length) {
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < length; i++) {
            switch (ch[i]) {
                case '\\':
                    out.append("\\\\");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                case '\t':
                    out.append("\\t");
                    break;
                case '\r':
                    out.append("\\r");
                    break;
                case '\f':
                    out.append("\\f");
                    break;
                default:
                    out.append(ch[i]);
                    break;
            }
        }
        return out.toString();
    }

}

// end of GCF.GXLDocumentHandler.java
