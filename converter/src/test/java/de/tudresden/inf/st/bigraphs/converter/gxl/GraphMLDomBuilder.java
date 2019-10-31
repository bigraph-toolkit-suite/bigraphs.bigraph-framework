package de.tudresden.inf.st.bigraphs.converter.gxl;
/*
 * GraphMLConverter.java
 *
 */

import java.util.*;
import java.io.*;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.net.*;

/**
 * Util to convert nodes and edges into grapml document
 *
 * @author rbolze
 */
public class GraphMLDomBuilder {

    public static Element graphml;
    public static Document document;
    public static Element graph;
    // no constructor
    // only use static method of this class

    /**
     * method which write in the xml file the graphML structure of the list of nodes and edges
     */
    public static void buildtop() {
        // graphml document header
        graphml = new Element("graphml", "http://graphml.graphdrawing.org/xmlns");
        document = new Document(graphml);
        Namespace xsi = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        Namespace schemLocation = Namespace.getNamespace("schemLocation", "http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd");
//        Namespace y = Namespace.getNamespace("y", "http://www.yworks.com/xml/graphml");

        // add Namespace
        graphml.addNamespaceDeclaration(xsi);
        graphml.addNamespaceDeclaration(schemLocation);
//        graphml.addNamespaceDeclaration(y);

        // keys for graphic representation
        addKey(graphml, "d0", "type", "string");
        addKey(graphml, "d1", "control", "string");
        addKey(graphml, "parent", "parent", "string");
//        Element key_d1 = new Element("key");
//        key_d1.setAttribute("id", "d1");
//        key_d1.setAttribute("for", "node");
//        key_d1.setAttribute("attr.name", "url");
//        key_d1.setAttribute("attr.type", "string");
//        graphml.addContent(key_d1);

        graph = new Element("graph");
        graph.setAttribute("id", "G");
        graph.setAttribute("edgedefault", "undirected");
        graphml.addContent(graph);


//        for (Iterator itNode = nodes.iterator();itNode.hasNext();){
//            URL url = (URL)itNode.next();
//            addNode(nodes.indexOf(url),url,graph,graphml);
//        }
//        for (Iterator itEdge = edges.iterator();itEdge.hasNext();){
//            String edge = (String)itEdge.next();
//            int id = edges.indexOf(edge);
//            try{
//                URL source = new URL(edge.substring(0,edge.indexOf("->")));
//                URL target = new URL(edge.substring(edge.indexOf("->")+2));
//                int idSource = nodes.indexOf(source);
//                int idTarget = nodes.indexOf(target);
//                if (idSource<0 || idTarget <0){
//                    System.err.println("bad edge: "+edge);
//                }
//                if(idSource<0){
//                    System.err.println("bad source :"+source);
//                }
//                if(idTarget<0){
//                    System.err.println("bad target :"+target);
//                }
//                addEdge(id,idSource,idTarget,source,target,graph,graphml);
//            }catch(java.net.MalformedURLException e){
//                e.printStackTrace();
//            }
//
//        }
//        printAll(document);
//        save(fileName,document);
    }

    private static void addKey(Element graphml, String id, String name, String type) {
        Element key_d0 = new Element("key");
        key_d0.setAttribute("id", id);
        key_d0.setAttribute("for", "node");
        key_d0.setAttribute("attr.name", name);
        key_d0.setAttribute("attr.type", type);
        graphml.addContent(key_d0);
    }

    /**
     * add a edge to the graphML document
     *
     * @param id    the id of the edge
     */
    public static void addEdge(String id, String sourceId, String targetId) {
        Element edge = new Element("edge");
        edge.setAttribute("id", id);
        edge.setAttribute("source", sourceId);
        edge.setAttribute("target", targetId);
        graph.addContent(edge);
    }

    /**
     * add a node to the graphML document
     *
     * @param id      the id of the node
     */
    public static Element addNode(String id, String type, String control) {
        Element node = new Element("node");
        node.setAttribute("id", id);

        Element data0 = new Element("data");
        data0.setAttribute("key", "d0");
        data0.setText(type);
        node.addContent(data0);

        Optional.ofNullable(control).ifPresent(x -> {
            Element data1 = new Element("data");
            data1.setAttribute("key", "d1");
            data1.setText(x);
            node.addContent(data1);
        });

        graph.addContent(node);
        return node;
    }

    public static void addAttributeToNode(Element node, String keyId, String value) {
        Optional.ofNullable(value).ifPresent(x -> {
            Element data1 = new Element("data");
            data1.setAttribute("key", keyId);
            data1.setText(x);
            node.addContent(data1);
        });
    }


    //TODO: below belongs to ConverterClass
//    /**
//     * print the content of the document
//     * @param doc xml document
//     */
//    public static void printAll(Document doc) {
//        try{
//            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
//            outputter.output(doc, System.out);
//        } catch (java.io.IOException e){
//            e.printStackTrace();
//        }
//    }

//    /**
//     * write the xml document into file
//     * @param file the file name
//     * @param doc xml document
//     */
//    public static void save(String file,Document doc) {
//        System.out.println("### document saved in : "+file);
//        try {
//            XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
//            sortie.output(doc, new java.io.FileOutputStream(file));
//        } catch (java.io.IOException e){}
//    }

}
