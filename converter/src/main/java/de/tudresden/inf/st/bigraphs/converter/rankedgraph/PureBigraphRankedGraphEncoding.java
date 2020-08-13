package de.tudresden.inf.st.bigraphs.converter.rankedgraph;

import com.google.common.collect.Lists;
import com.google.common.graph.Traverser;
import de.tudresden.inf.st.bigraphs.core.AbstractRankedGraph;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Dominik Grzelak
 */
public class PureBigraphRankedGraphEncoding extends AbstractRankedGraph<PureBigraph, AbstractRankedGraph.LabeledNode, AbstractRankedGraph.LabeledEdge> {

    public PureBigraphRankedGraphEncoding(PureBigraph bigraph) {
        super(bigraph);
    }

    @Override
    protected void init() {
        assert Objects.nonNull(bigraph);
        this.encodingStarted = false;
        this.encodingFinished = false;
        this.roots = new HashMap<>();
        this.variables = new HashMap<>();
        this.variableMap = new HashMap<>();
        this.rootMap = new HashMap<>();
        this.graph = this.getDirectedGraph();
    }

    //var map is a bijection, root map: nothing mentioned in paper (possibly a surjection)
    @Override
    public void encode() {
        encodingStarted = true;
        encodingFinished = false;
        AtomicInteger i_interfaceCounter = new AtomicInteger(bigraph.getSites().size());
        Map<BigraphEntity<?>, String> innerFaceIdMap = new HashMap<>();
        bigraph.getInnerNames().stream().sorted(Comparator.comparing(BigraphEntity.InnerName::getName)).forEachOrdered(eachInner -> {
            String innerNameId = "" + (i_interfaceCounter.getAndIncrement());
            LabeledNode innerNameVariableNode = new LabeledNode(innerNameId, eachInner.getType());
            innerFaceIdMap.put(eachInner, innerNameId);
            variables.put(innerNameId, innerNameVariableNode);
            graph.addVertex(innerNameVariableNode);
        });

        AtomicInteger j_interfaceCounter = new AtomicInteger(bigraph.getRoots().size());
        Map<BigraphEntity<?>, String> outerFaceIdMap = new HashMap<>();
        bigraph.getOuterNames().stream().sorted(Comparator.comparing(BigraphEntity.OuterName::getName)).forEachOrdered(eachOuter -> {
            String outerNameId = "" + (j_interfaceCounter.getAndIncrement());
            LabeledNode outerNameRootNode = new LabeledNode(outerNameId, eachOuter.getType());
            roots.put(outerNameId, outerNameRootNode);
            outerFaceIdMap.put(eachOuter, outerNameId);
            graph.addVertex(outerNameRootNode);
        });

        Map<BigraphEntity<?>, String> placeGraphIdMap = new HashMap<>();
        Traverser<BigraphEntity> childrenTraverser = Traverser.forTree(x -> {
            if (BigraphEntityType.isNode(x)) {
                String n = ((BigraphEntity.NodeEntity) x).getName() + ":" + placeGraphIdMap.get(bigraph.getParent(x));
                placeGraphIdMap.put(x, n);
            }
            if (BigraphEntityType.isRoot(x)) {
                placeGraphIdMap.put(x, id((BigraphEntity.RootEntity) x));
            }
            Collection<BigraphEntity<?>> childrenOf = bigraph.getChildrenOf(x);
            return childrenOf;
        });

        ArrayList<BigraphEntity> bigraphEntities = Lists.newArrayList(childrenTraverser.breadthFirst(bigraph.getRoots()));
        for (BigraphEntity<?> each : bigraphEntities) {
//            System.out.println(each);
            BigraphEntity<?> parent = bigraph.getParent(each);
            LabeledNode nodePlaceNode;
            switch (each.getType()) {
                case NODE:
                    String idNode = placeGraphIdMap.get(each);
                    nodePlaceNode = new LabeledNode(idNode, each.getType(), each.getControl());
                    graph.addVertex(nodePlaceNode);
                    LabeledNode parentPlaceNode = null;
                    if (BigraphEntityType.isRoot(parent)) {
                        String targetParentId = placeGraphIdMap.get(parent);
                        parentPlaceNode = roots.get(targetParentId);
                        graph.addEdge(parentPlaceNode, nodePlaceNode, new LabeledEdge(""));
                    } else if (BigraphEntityType.isNode(parent)) {
                        String targetParentId = placeGraphIdMap.get(parent);
                        parentPlaceNode = getById(targetParentId).orElse(new LabeledNode(targetParentId, parent.getType(), parent.getControl()));
                        graph.addEdge(nodePlaceNode, parentPlaceNode, new LabeledEdge(""));
                    }
                    break;
                case ROOT:
                    String idRoot = String.valueOf(((BigraphEntity.RootEntity) each).getIndex());
                    LabeledNode rootNode = new LabeledNode(idRoot, each.getType());
                    graph.addVertex(rootNode);
                    roots.put(idRoot, rootNode); //the "dashed arrow" pointing to the root
                    break;
                case SITE:
                    String idSite = String.valueOf(((BigraphEntity.SiteEntity) each).getIndex());
                    LabeledNode siteVariableNode = new LabeledNode(idSite, each.getType());
                    graph.addVertex(siteVariableNode); //but don't connect it to a node
                    variables.put(idSite, siteVariableNode);
                    nodePlaceNode = null;
                    if (BigraphEntityType.isRoot(parent)) {
                        nodePlaceNode = getByIdAndType("" + ((BigraphEntity.RootEntity) parent).getIndex(), parent.getType()).get();
                        variableMap.getOrDefault(idSite, new ArrayList<>()).add(nodePlaceNode);
                    } else if (BigraphEntityType.isNode(parent)) {
                        nodePlaceNode = getById(placeGraphIdMap.get(parent)).get();
                        variableMap.getOrDefault(idSite, new ArrayList<>()).add(nodePlaceNode);
                    }
                    assert nodePlaceNode != null;
                    graph.addEdge(siteVariableNode, nodePlaceNode, new LabeledEdge(""));
                    break;
            }
        }

        bigraph.getAllLinks().forEach(l -> {
            List<BigraphEntity<?>> pointsFromLink = bigraph.getPointsFromLink(l);
            LabeledNode linkOrInterfaceNode;
            if (BigraphEntityType.isEdge(l)) {
                linkOrInterfaceNode = getByIdAndType(l.getName(), l.getType()).orElse(new LabeledNode(l.getName(), l.getType()));
                graph.addVertex(linkOrInterfaceNode);
            } else {
                assert BigraphEntityType.isOuterName(l);
                linkOrInterfaceNode = roots.get(outerFaceIdMap.get(l));
            }
            pointsFromLink.forEach(p -> {
                if (BigraphEntityType.isPort(p)) {
                    BigraphEntity.NodeEntity<DefaultDynamicControl> nodeOfPort = bigraph.getNodeOfPort((BigraphEntity.Port) p);
//                    System.out.format("Node %s is connected to link %s\n", nodeOfPort.getName(), l.getName());
                    String nodeId = placeGraphIdMap.get(nodeOfPort);
                    LabeledNode byId = getById(nodeId).get();
                    graph.addEdge(byId, linkOrInterfaceNode, new LabeledEdge(""));
                } else if (BigraphEntityType.isInnerName(p)) {
//                    System.out.format("Inner name %s is connected to link %s\n", ((BigraphEntity.InnerName) p).getName(), l.getName());
                    String innerNameId = innerFaceIdMap.get(p);
                    LabeledNode innerNameVariableNode = variables.get(innerNameId);
                    graph.addEdge(innerNameVariableNode, linkOrInterfaceNode, new LabeledEdge(""));
                }
            });
        });

        encodingFinished = true;
    }

    String id(BigraphEntity.RootEntity node) {
        return String.valueOf(node.getIndex());
    }

    String id(BigraphEntity.NodeEntity<?> node, Bigraph<?> bigraph) {
        BigraphEntity<?> parent = bigraph.getParent(node);
        if (BigraphEntityType.isRoot(parent)) {
            return node.getName() + ":" + (id((BigraphEntity.RootEntity) parent));
        } else if (BigraphEntityType.isNode(parent)) {
            return node.getName() + ":" + (id((BigraphEntity.NodeEntity<?>) parent, bigraph));
        }
        return node.getName();
    }

    protected Optional<LabeledNode> getByIdAndType(String id, BigraphEntityType type) {
        return graph.vertexSet().stream().filter(x -> x.getId().equals(id) && x.getType().equals(type)).findFirst();
    }

    protected Optional<LabeledNode> getById(String id) {
        return graph.vertexSet().stream().filter(x -> x.getId().equals(id)).findFirst();
    }
}
