package org.bigraphs.framework.converter.bigred;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.BigraphMetaModelConstants;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.emf.ecore.EStructuralFeature;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;

/**
 * Support class for BigRed XML loader implementations.
 * Provides some basic XML parsing stubs for the BigRed file format.
 *
 * @author Dominik Grzelak
 */
public abstract class BigraphXmlLoaderSupport implements BigRedXmlLoader {

    protected Stack<BigraphEntity> parentStack = new Stack<>();
    protected String xmlFile;
    private XmlProcessorCallback callback = event -> {
    };

    public interface XmlProcessorCallback {
        void processAfterHook(XMLEvent event);
    }

    public abstract Bigraph<?> importObject();

    public void readXml(String file) {
        try {
            this.xmlFile = file;
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            InputStream in = new FileInputStream(file);
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    if (startElement.getName().getLocalPart().equals("signature")) {
                        processStartSignature(startElement);
                    }
                    if (startElement.getName().getLocalPart().equals("outername")) {
                        processStartOutername(startElement);
                    }
                    if (startElement.getName().getLocalPart().equals("innername")) {
                        processStartInnername(startElement);
                    }
                    if (startElement.getName().getLocalPart().equals("edge")) {
                        processStartEdge(startElement);
                    }
                    if (startElement.getName().getLocalPart().equals("root")) {
                        processStartRoot(startElement);
                    }
                    if (startElement.getName().getLocalPart().equals("node")) {
                        processStartNode(startElement);
                    }
                    if (startElement.getName().getLocalPart().equals("site")) {
                        processStartSite(startElement);
                    }
                    if (startElement.getName().getLocalPart().equals("port")) {
                        processStartPort(startElement);
                    }
                }
//
                if (event.isEndElement()) {
                    EndElement endElement = event.asEndElement();
                    if (endElement.getName().getLocalPart().equals("signature")) {
                        processEndSignature(endElement);
                    }
                    if (endElement.getName().getLocalPart().equals("outername")) {
                        processEndOutername(endElement);
                    }
                    if (endElement.getName().getLocalPart().equals("innername")) {
                        processEndInnername(endElement);
                    }
                    if (endElement.getName().getLocalPart().equals("edge")) {
                        processEndEdge(endElement);
                    }
                    if (endElement.getName().getLocalPart().equals("node")) {
                        processEndNode(endElement);
                    }
                    if (endElement.getName().getLocalPart().equals("site")) {
                        processEndSite(endElement);
                    }
                    if (endElement.getName().getLocalPart().equals("port")) {
                        processEndPort(endElement);
                    }
                    if (endElement.getName().getLocalPart().equals("root")) {
                        processEndRoot(endElement);
                    }
                }

                callback.processAfterHook(event);
            }
        } catch (FileNotFoundException | XMLStreamException e) {
            e.printStackTrace();
        }
    }

    public XmlProcessorCallback getCallback() {
        return callback;
    }

    public void setCallback(@NonNull XmlProcessorCallback callback) {
        if (Objects.nonNull(callback))
            this.callback = callback;
    }

    protected abstract void processStartSignature(StartElement startElement);

    protected abstract void processEndSignature(EndElement endElement);

    protected abstract void processStartOutername(StartElement startElement);

    protected abstract void processEndOutername(EndElement endElement);

    protected abstract void processStartInnername(StartElement startElement);

    protected abstract void processEndInnername(EndElement endElement);

    protected abstract void processStartEdge(StartElement startElement);

    protected abstract void processEndEdge(EndElement endElement);

    protected abstract void processStartRoot(StartElement startElement);

    protected abstract void processEndRoot(EndElement endElement);

    protected abstract void processStartNode(StartElement startElement);

    protected abstract void processEndNode(EndElement endElement);

    protected abstract void processStartSite(StartElement startElement);

    protected abstract void processEndSite(EndElement endElement);

    protected abstract void processStartPort(StartElement startElement);

    protected abstract void processEndPort(EndElement endElement);

    public String getXmlFile() {
        return xmlFile;
    }

    // TODO: put in a Util class
    protected void setParentOfNode(final BigraphEntity node, final BigraphEntity parent) {
        EStructuralFeature prntRef = node.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        node.getInstance().eSet(prntRef, parent.getInstance()); // child is automatically added to the parent according to the ecore model
    }

    Optional<BigraphEntity.Link> findLinkWithName(String name, Map<String, BigraphEntity.OuterName> outers,
                                                  Map<String, BigraphEntity.Edge> edges) {
        if (outers.containsKey(name)) return Optional.ofNullable(outers.get(name));
        if (edges.containsKey(name)) return Optional.ofNullable(edges.get(name));
        return Optional.empty();
    }
}
