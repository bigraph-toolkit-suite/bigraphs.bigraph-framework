package de.tudresden.inf.st.bigraphs.converter.bigred;

import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.MutableBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import org.eclipse.emf.ecore.EStructuralFeature;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Dominik Grzelak
 */
public class PureBigraphXMLLoader {

    private DefaultSignatureXMLLoader sxl = new DefaultSignatureXMLLoader();
    private DefaultDynamicSignature signature = null;
    private MutableBuilder<DefaultDynamicSignature> builder;
    private final Map<Integer, BigraphEntity.RootEntity> newRoots = org.eclipse.collections.impl.factory.Maps.mutable.empty();
    private final Map<Integer, BigraphEntity.SiteEntity> newSites = org.eclipse.collections.impl.factory.Maps.mutable.empty();
    private final Map<String, BigraphEntity.NodeEntity> newNodes = org.eclipse.collections.impl.factory.Maps.mutable.empty();
    private final Map<String, BigraphEntity.OuterName> newOuterNames = org.eclipse.collections.impl.factory.Maps.mutable.empty();
    private final Map<String, BigraphEntity.Edge> newEdges = org.eclipse.collections.impl.factory.Maps.mutable.empty();
    private final Map<String, BigraphEntity.InnerName> newInnerNames = org.eclipse.collections.impl.factory.Maps.mutable.empty();
    private Stack<BigraphEntity> parentStack = new Stack<>();

    public PureBigraphXMLLoader() {
        super();
    }

    public PureBigraph importObject() {
        PureBigraph context = new PureBigraph(builder.new InstanceParameter(
                builder.getLoadedEPackage(),
                signature,
                newRoots,
                newSites,
                newNodes,
                newInnerNames,
                newOuterNames,
                newEdges)
        );
        builder.reset();
        return context;
    }

    public void readXml(String file) {
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            InputStream in = new FileInputStream(file);
            Path pathToSignature;
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
            int portIndexCounter = 0;
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    if (startElement.getName().getLocalPart().equals("signature")) {
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        while (attributes.hasNext()) {
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals("src")) {
                                String urlValue = attribute.getValue();
                                pathToSignature = Paths.get(Paths.get(file).getParent().toAbsolutePath().toString(), urlValue).normalize();
                                sxl.readXml(pathToSignature.toString());
                            }
                        }
                    }
                    if (startElement.getName().getLocalPart().equals("outername")) {
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        while (attributes.hasNext()) {
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals("name")) {
                                String outernameLabel = attribute.getValue();
                                newOuterNames.put(outernameLabel, (BigraphEntity.OuterName) builder.createNewOuterName(outernameLabel));
                            }
                        }
                    }
                    if (startElement.getName().getLocalPart().equals("innername")) {
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        String linkLabel = "", name = "";
                        while (attributes.hasNext()) {
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals("name")) {
                                name = attribute.getValue();
                            }
                            if (attribute.getName().toString().equals("link")) {
                                linkLabel = attribute.getValue();
                            }
                        }
                        Optional<BigraphEntity.Link> linkWithName = findLinkWithName(linkLabel);
                        BigraphEntity newInnerName = builder.createNewInnerName(name);
                        newInnerNames.put(name, (BigraphEntity.InnerName) newInnerName);
                        if (linkWithName.isPresent()) {
                            builder.connectInnerToLink((BigraphEntity.InnerName) newInnerName, linkWithName.get());
                        }
                    }
                    if (startElement.getName().getLocalPart().equals("edge")) {
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        while (attributes.hasNext()) {
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals("name")) {
                                String edgeLabel = attribute.getValue();
                                BigraphEntity newEdge = builder.createNewEdge(edgeLabel);
                                newEdges.put(edgeLabel, (BigraphEntity.Edge) newEdge);
                            }
                        }
                    }
                    if (startElement.getName().getLocalPart().equals("root")) {
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        while (attributes.hasNext()) {
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals("name")) {
                                int index = Integer.parseInt(attribute.getValue());
                                BigraphEntity newRoot = builder.createNewRoot(index);
                                parentStack.push(newRoot);
                                newRoots.put(index, (BigraphEntity.RootEntity) newRoot);
                            }
                        }
                    }
                    if (startElement.getName().getLocalPart().equals("node")) {
                        portIndexCounter = 0;
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        DefaultDynamicControl controlByName = null;
                        String label = "";
                        while (attributes.hasNext()) {
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals("control")) {
                                String controlValue = attribute.getValue();
                                controlByName = signature.getControlByName(controlValue);
                            }
                            if (attribute.getName().toString().equals("name")) {
                                label = attribute.getValue();
                            }
                        }
                        BigraphEntity newNode = builder.createNewNode(controlByName, label);
                        newNodes.put(label, (BigraphEntity.NodeEntity) newNode);
                        parentStack.push(newNode);
                    }
                    if (startElement.getName().getLocalPart().equals("port")) {
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        String linkLabel = "";
                        while (attributes.hasNext()) {
                            Attribute attribute = attributes.next();
                            if (attribute.getName().toString().equals("link")) {
                                linkLabel = attribute.getValue();
                                Optional<BigraphEntity.Link> linkWithName = findLinkWithName(linkLabel);
                                if (linkWithName.isPresent() && !parentStack.empty()) {
                                    BigraphEntity peek = parentStack.peek();
                                    builder.connectToLinkUsingIndex((BigraphEntity.NodeEntity<Control>) peek,
                                            linkWithName.get(), portIndexCounter);
                                }
                            }
                        }
                    }
                }

                if (event.isEndElement()) {
                    EndElement endElement = event.asEndElement();
                    if (endElement.getName().getLocalPart().equals("signature")) {
                        signature = sxl.importObject();
                        builder = PureBigraphBuilder.newMutableBuilder(signature);
                    }
                    if (endElement.getName().getLocalPart().equals("node")) {
                        portIndexCounter = 0;
                        if (!parentStack.isEmpty()) {
                            BigraphEntity pop = parentStack.pop();
                            BigraphEntity peek = parentStack.peek();
                            setParentOfNode(pop, peek);
                        }
                    }
                    if (endElement.getName().getLocalPart().equals("port")) {
                        portIndexCounter++;
                    }
                    if (endElement.getName().getLocalPart().equals("root")) {
                        if (!parentStack.isEmpty()) {
                            BigraphEntity pop = parentStack.pop(); // must be a root
                            assert BigraphEntityType.isRoot(pop);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    Optional<BigraphEntity.Link> findLinkWithName(String name) {
        if (newOuterNames.containsKey(name)) return Optional.ofNullable(newOuterNames.get(name));
        if (newEdges.containsKey(name)) return Optional.ofNullable(newEdges.get(name));
        return Optional.empty();
    }

    // TODO: UTIL MACHEN: wird häufig verwendet!
    private void setParentOfNode(final BigraphEntity node, final BigraphEntity parent) {
        EStructuralFeature prntRef = node.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        node.getInstance().eSet(prntRef, parent.getInstance()); // child is automatically added to the parent according to the ecore model
    }
}
