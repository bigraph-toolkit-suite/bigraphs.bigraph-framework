package de.tudresden.inf.st.bigraphs.converter.bigred;

import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.EcoreBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.MutableBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ParametricReactionRule;

import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * Class to load a BigRed XML file containing a reaction rule.
 *
 * @author Dominik Grzelak
 */
public class DefaultReactionRuleXMLLoader implements BigraphXmlLoaderSupport.XmlProcessorCallback, BigRedXmlLoader {
    protected Queue<ChangeAction> actionStack = new ArrayDeque<>();
    protected MutableBuilder<DefaultDynamicSignature> builder;
    protected final DefaultBigraphXMLLoader bxl;
    protected boolean bigraphParsed = false;
    protected PureBigraph redex;
    protected PureBigraph reactum;
    protected DefaultDynamicSignature signature = null;

    private enum ChangeEvent {
        DISCONNECT("disconnect"), CONNECT("connect"),
        REMOVE("remove"), ADD("add"),
        NONE("none");
        private String tagName;

        ChangeEvent(String tagName) {
            this.tagName = tagName;
        }

        public static ChangeEvent fromString(String value) {
            for (ChangeEvent each : ChangeEvent.values()) {
                if (value.equalsIgnoreCase(each.getTagName()))
                    return each;
            }
            return NONE;
        }

        public String getTagName() {
            return tagName;
        }
    }

    private static class ChangeAction {
        ChangeEvent changeEvent;
        Map<String, String> params;

        public ChangeAction(ChangeEvent changeEvent) {
            this(changeEvent, new LinkedHashMap<>());
        }

        public ChangeAction(ChangeEvent changeEvent, Map<String, String> params) {
            this.changeEvent = changeEvent;
            this.params = params;
        }

        public void put(String key, String value) {
            this.params.put(key, value);
        }
    }

    public DefaultReactionRuleXMLLoader() {
        bxl = new DefaultBigraphXMLLoader();
        bxl.setCallback(this);
    }

    /**
     * If the signature is provided, the signature XML file of the reaction rule won't be parsed.
     *
     * @param signature the signature
     */
    public DefaultReactionRuleXMLLoader(DefaultDynamicSignature signature) {
//        this();
        this.signature = signature;
        this.bxl = new DefaultBigraphXMLLoader(this.signature);
        bxl.setCallback(this);
    }

    /**
     * Return the reaction rule object after calling {@link #readXml(String)}
     *
     * @return
     * @throws InvalidReactionRuleException
     */
    public ParametricReactionRule<PureBigraph> importObject() throws InvalidReactionRuleException {
        return new ParametricReactionRule<>(redex, reactum);
    }

    /**
     * Parse a BigRed XML reaction rule file
     *
     * @param file filename of a BigRed XML reaction rule file
     */
    public void readXml(String file) {
        bigraphParsed = false;
        bxl.readXml(file);
    }

    @Override
    public void processAfterHook(XMLEvent event) {
        if (event.isEndElement()) {
            EndElement endElement = event.asEndElement();
            if (endElement.getName().getLocalPart().equals("bigraph")) {
                bigraphParsed = true;
                redex = bxl.importObject();
                signature = bxl.getSignature();
                assert redex != null;
                createFreshBuilderFrom(redex);
                return;
            }
        }
        if (!bigraphParsed) return;

        if (event.isStartElement()) {
            StartElement startElement = event.asStartElement();
            String localPart = startElement.getName().getLocalPart();
            if (localPart.equals("group")) {
                if (!actionStack.isEmpty()) {
                    executeChangeActions();
                    return;
                }
            } else {
                ChangeEvent changeEvent = ChangeEvent.fromString(localPart);
                if (changeEvent != ChangeEvent.NONE) {
                    processChangeEvent(startElement, changeEvent);
                }
            }
        }
        if (event.isEndElement()) {
            EndElement endElement = event.asEndElement();
            String localPart = endElement.getName().getLocalPart();
            if (localPart.equals("group")) {
                executeChangeActions();
            }
            if (localPart.equals("changes")) {
                reactum = new PureBigraph(builder.new InstanceParameter(
                        builder.getMetaModel(),
                        redex.getSignature(),
                        builder.availableRoots(),
                        builder.availableSites(),
                        builder.availableNodes(),
                        builder.availableInnerNames(),
                        builder.availableOuterNames(),
                        builder.availableEdges())
                );
                builder.reset();
            }
        }
    }

    private void executeChangeActions() {
        if (actionStack.isEmpty()) return;
        while (!actionStack.isEmpty()) {
            ChangeAction pop = actionStack.remove();
            switch (pop.changeEvent) {
                case DISCONNECT:
                    executeDisconnectEvent(pop);
                    break;
                case CONNECT:
                    executeConnectEvent(pop);
                    break;
                case ADD:
                    executeAddEvent(pop);
                    break;
                case REMOVE:
                    executeRemoveEvent(pop);
                    break;
            }
        }
    }

    /**
     * An add change event has the form:
     * {@literal <change:add control="Job" name="b" parent="d" parent-type="node" type="node"/>}
     * {@literal <change:add name="3" parent="c" parent-type="node" type="site"/>}
     *
     * @param changeAction
     */
    private void executeAddEvent(ChangeAction changeAction) {
        String elementName = changeAction.params.get("name");
        String type = changeAction.params.get("type");
        String parentName = changeAction.params.get("parent");
        BigraphEntity parent = null;
        if (canParseInteger(parentName) && Objects.nonNull(builder.availableRoots().get(Integer.parseInt(parentName)))) {
            parent = builder.availableRoots().get(Integer.parseInt(parentName));
        } else if (Objects.nonNull(builder.availableNodes().get(parentName))) {
            parent = builder.availableNodes().get(parentName);
        }
        if (type.equalsIgnoreCase("node")) {
            DefaultDynamicControl control = redex.getSignature().getControlByName(changeAction.params.get("control"));
            BigraphEntity newNode = builder.createNewNode(control, elementName);
            builder.availableNodes().put(elementName, (BigraphEntity.NodeEntity) newNode);
            if (Objects.nonNull(parent)) {
                bxl.setParentOfNode(newNode, parent);
            }
        } else if (type.equalsIgnoreCase("site") && canParseInteger(elementName)) {
            BigraphEntity.SiteEntity newSite = (BigraphEntity.SiteEntity) builder.createNewSite(Integer.parseInt(elementName));
            builder.availableSites().put(newSite.getIndex(), newSite);
            if (Objects.nonNull(parent)) {
                bxl.setParentOfNode(newSite, parent);
            }
        } else {
            throw new RuntimeException("processAddEvent() is not supported yet for type=" + type);
        }
    }

    /**
     * A remove change event has the form:
     * {@literal <change:remove name="b" type="node"/>}
     *
     * @param changeAction
     */
    private void executeRemoveEvent(ChangeAction changeAction) {
        String elementName = changeAction.params.get("name");
        if (changeAction.params.get("type").equalsIgnoreCase("node")) {
            builder.availableNodes().remove(elementName);
        }
        if (changeAction.params.get("type").equalsIgnoreCase("root")) {
            builder.availableRoots().remove(Integer.parseInt(elementName));
        }
        if (changeAction.params.get("type").equalsIgnoreCase("site")) {
            builder.availableSites().remove(Integer.parseInt(elementName));
        }
        if (changeAction.params.get("type").equalsIgnoreCase("edge")) {
            builder.availableEdges().remove(elementName);
        }
        if (changeAction.params.get("type").equalsIgnoreCase("outername")) {
            builder.availableOuterNames().remove(elementName);
        }
        if (changeAction.params.get("type").equalsIgnoreCase("innername")) {
            builder.availableInnerNames().remove(elementName);
        }
    }

    /**
     * A connect change event has the form:
     * {@literal <change:connect link="b" name="owner" node="b"/>}
     *
     * @param changeAction
     */
    private void executeConnectEvent(ChangeAction changeAction) {
        final String nodeName = changeAction.params.get("node");
        final String portName = changeAction.params.get("name");
        final String linkName = changeAction.params.get("link");
        if (builder.availableNodes().get(nodeName) != null) {
            BigraphEntity.NodeEntity theNode = builder.availableNodes().get(nodeName);
            bxl.getSignatureControlPortIndexMapping().entrySet().stream()
                    // get the control of the signature
                    .filter(x -> x.getKey().equals(theNode.getControl().getNamedType().stringValue()))
                    .findFirst()
                    .ifPresent(x -> {
                        x.getValue().stream()
                                // get the mapping port name -> port index
                                .filter(y -> y.getLeft().equals(portName))
                                .findFirst()
                                .ifPresent(y -> {
                                    // connect the node with the link at this port index (corresponding name)
                                    int portIx = y.getRight();
                                    Optional<BigraphEntity.Link> linkWithName = bxl.findLinkWithName(linkName, builder.availableOuterNames(), builder.availableEdges());
                                    linkWithName.ifPresent(linkEntity -> {
                                        builder.connectToLinkUsingIndex(
                                                theNode,
                                                linkEntity,
                                                portIx);
                                    });
                                });
                    });
        }
    }

    /**
     * A disconnect change event has the form:
     * {@literal <change:disconnect name="owner" node="b"/>}
     *
     * @param changeAction
     */
    private void executeDisconnectEvent(ChangeAction changeAction) {
        String nodeName = changeAction.params.get("node");
        String portName = changeAction.params.get("name");
        if (builder.availableNodes().get(nodeName) != null) {
            BigraphEntity.NodeEntity theNode = builder.availableNodes().get(nodeName);
            bxl.getSignatureControlPortIndexMapping().entrySet().stream()
                    // get the control of the signature
                    .filter(x -> x.getKey().equals(theNode.getControl().getNamedType().stringValue()))
                    .findFirst()
                    .ifPresent(x -> {
                        x.getValue().stream()
                                // get the mapping port name -> port index
                                .filter(y -> y.getLeft().equals(portName))
                                .findFirst()
                                .ifPresent(y -> {
                                    int portIx = y.getRight();
                                    builder.disconnectPort(builder.availableNodes().get(nodeName), portIx);
                                });
                    });
        }
    }

    private void processChangeEvent(StartElement startElement, ChangeEvent event) {
        ChangeAction changeAction = new ChangeAction(event);
        Iterator<Attribute> attributes = startElement.getAttributes();
        while (attributes.hasNext()) {
            Attribute attribute = attributes.next();
            changeAction.put(attribute.getName().toString(), attribute.getValue());
        }
        actionStack.add(changeAction);
    }

    private void createFreshBuilderFrom(PureBigraph bigraph) {
        try {
            EcoreBigraph.Stub stub = new EcoreBigraph.Stub(bigraph);
            EcoreBigraph.Stub clone = stub.clone();
            builder = new MutableBuilder<>(bigraph.getSignature(), clone.getModelPackage(), clone.getModel());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    private boolean canParseInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public DefaultDynamicSignature getSignature() {
        return signature;
    }
}
