package de.tudresden.inf.st.bigraphs.converter.graphml;
/*
 * GraphMLConverter.java
 *
 */

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility class for converting bigraph objects into a GraphML document.
 *
 * @author Dominik Grzelak
 */
public class GraphMLDomBuilder {

    public Element graphMLRoot;
    public Document document;
    public Element graph;

    public GraphMLDomBuilder() {
    }

    public void addHeader() {
        // graphml document header
        graphMLRoot = new Element("graphml", "http://graphml.graphdrawing.org/xmlns");
        document = new Document(graphMLRoot);
        Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        Namespace schemLocation = Namespace.getNamespace("schemaLocation", "http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd");
//        Namespace y = Namespace.getNamespace("y", "http://www.yworks.com/xml/graphml");

        // add Namespace
        graphMLRoot.addNamespaceDeclaration(xsi);
        graphMLRoot.addNamespaceDeclaration(schemLocation);
//        graphMLRoot.addNamespaceDeclaration(y);

        graph = new Element("graph");
        graph.setAttribute("id", "bigraph");
        graph.setAttribute("edgedefault", "undirected");
        graphMLRoot.addContent(graph);
    }

    public void addKey(String id, String name, String type) {
        Element key_d0 = new Element("key");
        key_d0.setAttribute("id", id);
        key_d0.setAttribute("for", "node");
        key_d0.setAttribute("attr.name", name);
        key_d0.setAttribute("attr.type", type);
        graphMLRoot.addContent(key_d0);
    }

    /**
     * Add an edge to the graphML document.
     *
     * @param id the id of the edge
     */
    public void addEdge(String id, String sourceId, String targetId, boolean directed) {
        Element edge = new Element("edge");
        edge.setAttribute("id", id);
        edge.setAttribute("source", sourceId);
        edge.setAttribute("target", targetId);
        edge.setAttribute("directed", String.valueOf(directed));
        graph.addContent(edge);
    }

    public void addEdge(String id, String sourceId, String targetId) {
        addEdge(id, sourceId, targetId, true);
    }

    public Element addNode(String id, String nodeType) {
        return addNode(id, nodeType, null);
    }

    /**
     * Add a node to the graphML document.
     *
     * @param id the id of the node
     */
    public Element addNode(String id, String nodeType, String control) {
        Element node = new Element("node");
        node.setAttribute("id", id);

        addDataAttributeToElement(node, "d0", nodeType);
        Optional.ofNullable(control).ifPresent(x -> {
            addDataAttributeToElement(node, "d1", x);
        });

        graph.addContent(node);
        return node;
    }

    public Element addHyperedge() {
        Element node = new Element("hyperedge");
        graph.addContent(node);
        return node;
    }

    public Element addAttributeToElement(Element element, String key, String value) {
        element.setAttribute(key, value);
        return element;
    }

    public Element addDataAttributeToElement(Element element, String keyId, String content) {
        Element data0 = new Element("data");
        data0.setAttribute("key", keyId);
        data0.setText(content);
        element.addContent(data0);
        return element;
    }

    public Element addEndpointToHyperedge(Element hyperedge, String nodeId, String portIndex) {
        assert hyperedge.getName().equalsIgnoreCase("hyperedge");
        assert Objects.nonNull(nodeId);
        Element endpoint = new Element("endpoint");
        endpoint.setAttribute("node", nodeId);
        Optional.ofNullable(portIndex).ifPresent(x -> {
            endpoint.setAttribute("port", portIndex);
        });
        hyperedge.addContent(endpoint);
        return hyperedge;
    }

    public void addPortToNode(Element node, String portName) {
        if (Objects.isNull(node)) return;
        Optional.ofNullable(portName).ifPresent(x -> {
            Element portElem = new Element("port");
            portElem.setAttribute("name", portName);
            node.addContent(portElem);
        });
    }

    public void addAttributeToNode(Element node, String attributeKeyId, String attributeValue) {
        if (Objects.isNull(node)) return;
        Optional.ofNullable(attributeValue).ifPresent(x -> {
            Element data1 = new Element("data");
            data1.setAttribute("key", attributeKeyId);
            data1.setText(x);
            node.addContent(data1);
        });
    }

    public String printToString(Document doc) {
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        return outputter.outputString(doc);
    }

    public String printToString() {
        return printToString(document);
    }

    public void toStandardOutput() throws IOException {
        toStandardOutput(document);
    }

    public void toOutputStream(OutputStream outputStream) throws IOException {
        toOutputStream(document, outputStream);
    }

    public void toOutputStream(Document doc, OutputStream outputStream) throws IOException {
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        outputter.output(doc, outputStream);
    }

    public void toStandardOutput(Document doc) throws IOException {
        toOutputStream(doc, System.out);
    }
}
