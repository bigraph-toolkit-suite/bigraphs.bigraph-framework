package org.bigraphs.framework.converter.jlibbig;

import org.bigraphs.framework.converter.BigraphObjectDecoder;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.MutableBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicControl;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import it.uniud.mads.jlibbig.core.attachedProperties.Property;
import it.uniud.mads.jlibbig.core.attachedProperties.PropertyTarget;
import it.uniud.mads.jlibbig.core.std.*;
import org.eclipse.emf.ecore.EPackage;

import java.util.*;

import static org.bigraphs.framework.core.factory.BigraphFactory.createOrGetBigraphMetaModel;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

public class JLibBigBigraphDecoder implements BigraphObjectDecoder<PureBigraph, it.uniud.mads.jlibbig.core.std.Bigraph> {
    private HashMap<Integer, BigraphEntity.RootEntity> newRoots = new LinkedHashMap<>();
    private HashMap<Integer, BigraphEntity.SiteEntity> newSites = new LinkedHashMap<>();
    private HashMap<String, BigraphEntity.NodeEntity> newNodes = new LinkedHashMap<>();
    private HashMap<String, BigraphEntity.Edge> newEdges = new LinkedHashMap<>();
    private HashMap<String, BigraphEntity.OuterName> newOuterNames = new LinkedHashMap<>();
    private HashMap<String, BigraphEntity.InnerName> newInnerNames = new LinkedHashMap<>();
    private MutableBuilder<DefaultDynamicSignature> builder;
    private DefaultDynamicSignature signature;
    private it.uniud.mads.jlibbig.core.std.Bigraph jBigraph;

    private Map<String, it.uniud.mads.jlibbig.core.std.OuterName> jLibBigOuterNames = new LinkedHashMap<>();
    private Map<String, it.uniud.mads.jlibbig.core.std.InnerName> jLibBigInnerNames = new LinkedHashMap<>();
    private Map<String, it.uniud.mads.jlibbig.core.std.Edge> jLibBigEdges = new LinkedHashMap<>();
    private Map<Integer, it.uniud.mads.jlibbig.core.std.Root> jLibBigRegions = new LinkedHashMap<>(); //PlaceEntity
    private Map<String, it.uniud.mads.jlibbig.core.std.Node> jLibBigNodes = new LinkedHashMap<>(); // PlaceEntity
    private Map<Integer, it.uniud.mads.jlibbig.core.std.Site> jLibBigSites = new LinkedHashMap<>(); // PlaceEntity

    public synchronized PureBigraph decode(it.uniud.mads.jlibbig.core.std.Bigraph bigraph, DefaultDynamicSignature signature) {
        this.jBigraph = bigraph;
        this.signature = signature;
        EPackage ePackage = createOrGetBigraphMetaModel(this.signature);
//        this.builder = new MutableBuilder<>(signature, EMetaModelData.builder()
//                .setName(ePackage.getName())
//                .setNsPrefix(ePackage.getNsPrefix())
//                .setNsUri(ePackage.getNsURI())
//                .create()
//        );
        this.builder = new MutableBuilder<>(signature, ePackage, null);

        clearAllMaps();

        parseOuterNames(bigraph);
        parseInnerNames(bigraph);
        parseRegions(bigraph);

        // The linking all the points to links now
        performLinkage(bigraph);

        // Create bigraph
        PureBigraphBuilder<DefaultDynamicSignature>.InstanceParameter meta = this.builder.new InstanceParameter(
                this.builder.getMetaModel(),
                this.signature,
                this.newRoots,
                this.newSites,
                this.newNodes,
                this.newInnerNames,
                this.newOuterNames,
                this.newEdges);
        this.builder.reset();
        PureBigraph result = new PureBigraph(meta);
        return result;
    }

    @Override
    public synchronized PureBigraph decode(it.uniud.mads.jlibbig.core.std.Bigraph bigraph) {
        return this.decode(bigraph, parseSignature(bigraph.getSignature()));
    }

    private void performLinkage(Bigraph bigraph) {
        // Connect outer names first
        for (Map.Entry<String, OuterName> eachEntry : jLibBigOuterNames.entrySet()) {
            BigraphEntity.OuterName correspondingOuterName = newOuterNames.get(eachEntry.getKey());

            OuterName jOuterName = eachEntry.getValue();
            for (Point point : jOuterName.getPoints()) {
                if (point.isPort()) {
//                    System.out.println("is port");
                    EditableNode node = ((EditableNode.EditablePort) ((Point) point)).getNode();
                    Node jNode = jLibBigNodes.get(node.getName());
                    BigraphEntity.NodeEntity correspondingNode = newNodes.get(node.getName());
                    int portIndex = jNode.getPorts().indexOf(point);
                    // create port at specified index
                    // connect port to outer name
                    builder.connectToLinkUsingIndex(correspondingNode, correspondingOuterName, portIndex);
                } else if (point.isInnerName()) {
//                    System.out.println("is innername");
                    BigraphEntity.InnerName correspondingInnerName = newInnerNames.get(((InnerName) point).getName());
                    // connect inner name to outer name
                    builder.connectInnerToOuter(correspondingInnerName, correspondingOuterName);
                }
            }
        }

        // Iterate through edges

        for (Edge eachEdge : bigraph.getEdges()) {
            BigraphEntity.Edge correspondingEdge = newEdges.get(eachEdge.getEditable().getName());
            if (correspondingEdge == null) {
                correspondingEdge = (BigraphEntity.Edge) builder.createNewEdge(eachEdge.getEditable().getName());
                newEdges.put(correspondingEdge.getName(), correspondingEdge);
            }
            for (Point point : eachEdge.getPoints()) {
                if (point.isPort()) {
                    EditableNode node = ((EditableNode.EditablePort) ((Point) point)).getNode();
                    Node jNode = jLibBigNodes.get(node.getName());
                    BigraphEntity.NodeEntity correspondingNode = newNodes.get(node.getName());
                    int portIndex = jNode.getPorts().indexOf(point);
                    builder.connectToLinkUsingIndex(correspondingNode, correspondingEdge, portIndex);
                } else if (point.isInnerName()) {
                    BigraphEntity.InnerName correspondingInnerName = newInnerNames.get(((InnerName) point).getName());
                    builder.connectInnerToLink(correspondingInnerName, correspondingEdge);
                }
            }
        }
    }

    private void parseOuterNames(Bigraph bigraph) {
        for (OuterName each : bigraph.getOuterNames()) {
            String name = each.getName();
            jLibBigOuterNames.put(name, each);
            BigraphEntity.OuterName newOuterName = (BigraphEntity.OuterName) builder.createNewOuterName(name);
            newOuterNames.put(newOuterName.getName(), newOuterName);
        }
    }

    private void parseInnerNames(Bigraph bigraph) {
        for (InnerName each : bigraph.getInnerNames()) {
            String name = each.getName();
            jLibBigInnerNames.put(name, each);
            BigraphEntity.InnerName x = (BigraphEntity.InnerName) builder.createNewInnerName(name);
            newInnerNames.put(x.getName(), x);
        }
    }

    private void parseRegions(Bigraph bigraph) {
        for (Root each : bigraph.getRoots()) {
            Stack<it.uniud.mads.jlibbig.core.PlaceEntity> s = new Stack<>();
            int rootIndex = bigraph.getRoots().indexOf(each);
            jLibBigRegions.put(rootIndex, each);
            BigraphEntity<?> newRoot = builder.createNewRoot(rootIndex);
            newRoots.put(rootIndex, (BigraphEntity.RootEntity) newRoot);
            traverseNode(each, newRoot, bigraph);
        }
    }

    private void traverseNode(it.uniud.mads.jlibbig.core.std.PlaceEntity currentNode,
                              BigraphEntity currentParent,
                              Bigraph bigraph) {
        if (currentNode.isRoot()) {
            jLibBigRegions.putIfAbsent(bigraph.getRoots().indexOf(currentNode), (Root) currentNode);
        } else if (currentNode.isNode()) {
            String name = ((Node) currentNode).getEditable().getName();
            jLibBigNodes.putIfAbsent(name, (Node) currentNode);
            if (newNodes.get(name) == null) {
                createNode((Node) currentNode, currentParent);
            }
        } else if (currentNode.isSite()) {
            int siteIndex = bigraph.getSites().indexOf(((Site) currentNode));
            jLibBigSites.putIfAbsent(siteIndex, (Site) currentNode);
            if (newSites.get(siteIndex) == null) {
                createSite((Site) currentNode, currentParent);
            }
        }

        if (currentNode.isParent()) {
            Collection<Child> childNodes = (Collection<Child>) ((Parent) currentNode).getChildren();
            for (Child n : childNodes) {
                BigraphEntity nextParent = getParentOfNode(n, bigraph);
                assert nextParent != null;
                traverseNode(n, nextParent, bigraph);
            }
        }
    }

    private BigraphEntity getParentOfNode(PlaceEntity node, Bigraph jBigraph) {
        if (node.isNode() || node.isSite()) {
            Parent parent = ((Child) node).getParent();
            if (parent.isNode()) {
                return newNodes.get(((Node) parent).getEditable().getName());
            } else if (parent.isRoot()) {
                return newRoots.get(jBigraph.getRoots().indexOf((Root) parent));
            }
        }
        return null;
    }

    private BigraphEntity.NodeEntity createNode(Node n, BigraphEntity parent) {
        String control = n.getControl().getName();
        String nodeId = n.getEditable().getName();
        DefaultDynamicControl controlByName = signature.getControlByName(control);
        // Check if node has an ID property - use that instead
        if(n.getProperty("_id") != null) {
            nodeId = String.valueOf(n.getProperty("_id").get());
        }
        BigraphEntity.NodeEntity newNode = (BigraphEntity.NodeEntity) builder.createNewNode(controlByName, nodeId);
        if (n instanceof PropertyTarget) {
            Map<String, Object> attributes = ((BigraphEntity.NodeEntity<?>) newNode).getAttributes();
            for(Property<?> each: n.getProperties()) {
                if(each.getName().equals("Owner")) continue;
                attributes.put(each.getName(), each.get());
            }
            newNode.setAttributes(attributes);
        }
        newNodes.put(nodeId, newNode);
        builder.setParentOfNode(newNode, parent);


        return newNode;
    }

    private BigraphEntity.SiteEntity createSite(Site n, BigraphEntity currentParent) {
        int siteIndex = jBigraph.getSites().indexOf(n);
        BigraphEntity.SiteEntity newSite = (BigraphEntity.SiteEntity) builder.createNewSite(siteIndex);
        newSites.put(newSite.getIndex(), newSite);
        builder.setParentOfNode(newSite, currentParent);
        return newSite;
    }

    private DefaultDynamicSignature parseSignature(it.uniud.mads.jlibbig.core.std.Signature sig) {
        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();
        for (Iterator<Control> it = sig.iterator(); it.hasNext(); ) {
            Control control = it.next();
            signatureBuilder.addControl(control.getName(), control.getArity(), control.isActive() ? ControlStatus.ACTIVE : ControlStatus.PASSIVE);
        }
        return signatureBuilder.create();
    }

    private void clearAllMaps() {
        newRoots.clear();
        newNodes.clear();
        newSites.clear();
        newEdges.clear();
        newOuterNames.clear();
        newInnerNames.clear();

        jLibBigOuterNames.clear();
        jLibBigInnerNames.clear();
        jLibBigEdges.clear();
        jLibBigRegions.clear();
        jLibBigNodes.clear();
        jLibBigSites.clear();

        builder.reset();
    }
}
