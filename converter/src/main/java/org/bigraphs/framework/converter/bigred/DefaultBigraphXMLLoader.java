package org.bigraphs.framework.converter.bigred;

import org.bigraphs.framework.core.BigraphEntityType;
import org.bigraphs.framework.core.Control;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.signature.DynamicControl;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.pure.MutableBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.apache.commons.lang3.tuple.Pair;

import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Class to load a BigRed XML file containing a bigraph instance.
 *
 * @author Dominik Grzelak
 */
public class DefaultBigraphXMLLoader extends BigraphXmlLoaderSupport implements BigRedXmlLoader {

    private DefaultSignatureXMLLoader sxl = new DefaultSignatureXMLLoader();
    private Path pathToSignature;
    protected int portIndexCounter = 0;
    protected DynamicSignature signature = null;
    protected MutableBuilder<DynamicSignature> builder;

    public DefaultBigraphXMLLoader() {
        super();
    }

    /**
     * If the signature is provided, the signature XML file of the bigraph won't be parsed.
     *
     * @param signature the signature
     */
    public DefaultBigraphXMLLoader(DynamicSignature signature) {
        super();
        this.signature = signature;
    }

    public Map<String, List<Pair<String, Integer>>> getSignatureControlPortIndexMapping() {
        return sxl.getControlPortNamePortIndexMapping();
    }

    public PureBigraph importObject() {
        PureBigraph context = new PureBigraph(builder.new InstanceParameter(
                builder.getMetaModel(),
                signature,
                builder.availableRoots(),
                builder.availableSites(),
                builder.availableNodes(),
                builder.availableInnerNames(),
                builder.availableOuterNames(),
                builder.availableEdges())
        );
        builder.reset();
        return context;
    }


    @Override
    public void readXml(String file) {
        reset();
        super.readXml(file);
    }

    private void reset() {
        portIndexCounter = 0;
        if (Objects.nonNull(builder))
            builder.reset();
        if (Objects.nonNull(parentStack))
            parentStack.clear();
    }

    public DynamicSignature getSignature() {
        return signature;
    }

    @Override
    protected void processStartSignature(StartElement startElement) {
        if (Objects.nonNull(signature)) return;
        Iterator<Attribute> attributes = startElement.getAttributes();
        while (attributes.hasNext()) {
            Attribute attribute = attributes.next();
            if (attribute.getName().toString().equals("src")) {
                String urlValue = attribute.getValue();
                pathToSignature = Paths.get(Paths.get(getXmlFile()).getParent().toAbsolutePath().toString(), urlValue).normalize();
                sxl.readXml(pathToSignature.toString());
            }
        }
    }

    @Override
    protected void processEndSignature(EndElement endElement) {
        if (Objects.isNull(signature)) {
            signature = sxl.importObject();
        }
        builder = MutableBuilder.newMutableBuilder(signature);
    }

    @Override
    protected void processStartOutername(StartElement startElement) {
        Iterator<Attribute> attributes = startElement.getAttributes();
        while (attributes.hasNext()) {
            Attribute attribute = attributes.next();
            if (attribute.getName().toString().equals("name")) {
                String outernameLabel = attribute.getValue();
                builder.availableOuterNames().put(outernameLabel, (BigraphEntity.OuterName) builder.createNewOuterName(outernameLabel));
            }
        }
    }

    @Override
    protected void processEndOutername(EndElement endElement) {
        // nothing to do here
    }

    @Override
    protected void processStartInnername(StartElement startElement) {
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
        Optional<BigraphEntity.Link> linkWithName = findLinkWithName(linkLabel, builder.availableOuterNames(), builder.availableEdges());
        BigraphEntity newInnerName = builder.createNewInnerName(name);
        builder.availableInnerNames().put(name, (BigraphEntity.InnerName) newInnerName);
        if (linkWithName.isPresent()) {
            builder.connectInnerToLink((BigraphEntity.InnerName) newInnerName, linkWithName.get());
        }
    }

    @Override
    protected void processEndInnername(EndElement endElement) {
        // nothing to do here
    }

    @Override
    protected void processStartEdge(StartElement startElement) {
        Iterator<Attribute> attributes = startElement.getAttributes();
        while (attributes.hasNext()) {
            Attribute attribute = attributes.next();
            if (attribute.getName().toString().equals("name")) {
                String edgeLabel = attribute.getValue();
                BigraphEntity newEdge = builder.createNewEdge(edgeLabel);
                builder.availableEdges().put(edgeLabel, (BigraphEntity.Edge) newEdge);
            }
        }
    }

    @Override
    protected void processEndEdge(EndElement endElement) {
        // nothing to do here
    }

    @Override
    protected void processStartRoot(StartElement startElement) {
        Iterator<Attribute> attributes = startElement.getAttributes();
        while (attributes.hasNext()) {
            Attribute attribute = attributes.next();
            if (attribute.getName().toString().equals("name")) {
                int index = Integer.parseInt(attribute.getValue());
                BigraphEntity newRoot = builder.createNewRoot(index);
                parentStack.push(newRoot);
                builder.availableRoots().put(index, (BigraphEntity.RootEntity) newRoot);
            }
        }
    }

    @Override
    protected void processEndRoot(EndElement endElement) {
        if (!parentStack.isEmpty()) {
            BigraphEntity pop = parentStack.pop(); // must be a root
            assert BigraphEntityType.isRoot(pop);
        }
    }

    @Override
    protected void processStartNode(StartElement startElement) {
        portIndexCounter = 0;
        Iterator<Attribute> attributes = startElement.getAttributes();
        DynamicControl controlByName = null;
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
        builder.availableNodes().put(label, (BigraphEntity.NodeEntity) newNode);
        parentStack.push(newNode);
    }

    @Override
    protected void processEndNode(EndElement endElement) {
        portIndexCounter = 0;
        if (!parentStack.isEmpty()) {
            BigraphEntity pop = parentStack.pop();
            BigraphEntity peek = parentStack.peek();
            setParentOfNode(pop, peek);
        }
    }

    @Override
    protected void processStartSite(StartElement startElement) {
        Iterator<Attribute> attributes = startElement.getAttributes();
        int name = -1;
        while (attributes.hasNext()) {
            Attribute attribute = attributes.next();
            if (attribute.getName().toString().equals("name")) {
                name = Integer.parseInt(attribute.getValue());
            }
        }
        if (name >= 0) {
            BigraphEntity newSite = builder.createNewSite(name);
            builder.availableSites().put(name, (BigraphEntity.SiteEntity) newSite);
            parentStack.push(newSite);
        }
    }

    @Override
    protected void processEndSite(EndElement endElement) {
        if (!parentStack.isEmpty()) {
            BigraphEntity pop = parentStack.pop();
            BigraphEntity peek = parentStack.peek();
            setParentOfNode(pop, peek);
        }
    }

    @Override
    protected void processStartPort(StartElement startElement) {
        Iterator<Attribute> attributes = startElement.getAttributes();
        String linkLabel = "";
        while (attributes.hasNext()) {
            Attribute attribute = attributes.next();
            if (attribute.getName().toString().equals("link")) {
                linkLabel = attribute.getValue();
                Optional<BigraphEntity.Link> linkWithName = findLinkWithName(linkLabel, builder.availableOuterNames(), builder.availableEdges());
                if (linkWithName.isPresent() && !parentStack.empty()) {
                    BigraphEntity peek = parentStack.peek();
                    builder.connectToLinkUsingIndex((BigraphEntity.NodeEntity<Control>) peek,
                            linkWithName.get(), portIndexCounter);
                }
            }
        }
    }

    @Override
    protected void processEndPort(EndElement endElement) {
        portIndexCounter++;
    }
}
