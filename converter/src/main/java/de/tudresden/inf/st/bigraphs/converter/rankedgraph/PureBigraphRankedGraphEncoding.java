package de.tudresden.inf.st.bigraphs.converter.rankedgraph;

import com.google.common.collect.Lists;
import com.google.common.graph.Traverser;
import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;

import java.util.*;

/**
 * @author Dominik Grzelak
 */
public class PureBigraphRankedGraphEncoding extends AbstractRankedGraph<PureBigraph, AbstractRankedGraph.LabeledNode, AbstractRankedGraph.LabeledEdge> {

    public PureBigraphRankedGraphEncoding(PureBigraph bigraph) {
        super(bigraph);
    }

    @Override
    protected void init() {
        this.roots = new HashMap<>();
        this.variables = new HashMap<>();
        this.graph = this.<AbstractRankedGraph.LabeledNode, AbstractRankedGraph.LabeledEdge>getDirectedGraph();
        assert Objects.nonNull(bigraph);
    }

    //var map is a bijection, root map: nothing mentioned in paper (possibly a surjection)
    @Override
    public void encode() {

        int i_interfaceCounter = bigraph.getSites().size();
        for (BigraphEntity.InnerName eachInner : bigraph.getInnerNames()) {
            String id = "" + (++i_interfaceCounter);
            graph.addVertex(new LabeledNode(id, eachInner.getType()));
        }

        int j_interfaceCounter = bigraph.getRoots().size();
        for (BigraphEntity.OuterName eachOuter : bigraph.getOuterNames()) {
            String id = "" + (++j_interfaceCounter);
            graph.addVertex(new LabeledNode(id, eachOuter.getType()));
        }

        Map<BigraphEntity, String> hierarchyIdMap = new HashMap<>();
        Traverser<BigraphEntity> childrenTraverser = Traverser.forTree(x -> {
            if (BigraphEntityType.isNode(x)) {
                String n = ((BigraphEntity.NodeEntity) x).getName() + ":" + hierarchyIdMap.get(bigraph.getParent(x));
                hierarchyIdMap.put(x, n);
            }
            if (BigraphEntityType.isRoot(x)) {
                hierarchyIdMap.put(x, String.valueOf(((BigraphEntity.RootEntity) x).getIndex()));
            }
            Collection<BigraphEntity<?>> childrenOf = bigraph.getChildrenOf(x);
            return childrenOf;
        });
        BigraphEntity.RootEntity firstRoot = bigraph.getRoots().iterator().next();

        ArrayList<BigraphEntity> bigraphEntities = Lists.newArrayList(childrenTraverser.breadthFirst(firstRoot));
        for (BigraphEntity each : bigraphEntities) {
            System.out.println(each);
            switch (each.getType()) {
                case NODE:
                    String idNode = hierarchyIdMap.get(each);
                    graph.addVertex(new LabeledNode(idNode, each.getType(), each.getControl()));
                    if (bigraph.getParent(each).getType() == BigraphEntityType.ROOT) {
                        String targetId0 = "" + ((BigraphEntity.RootEntity) bigraph.getParent(each)).getIndex();

                    } else if (bigraph.getParent(each).getType() == BigraphEntityType.NODE) {

                    }
                    break;
                case ROOT:
                    String idRoot = String.valueOf(((BigraphEntity.RootEntity) each).getIndex());
//                    GraphMLDomBuilder.addNode(id2, each.getType().name(), null);
                    LabeledNode rootNode = new LabeledNode(idRoot, each.getType());
                    graph.addVertex(rootNode);
                    roots.put(idRoot, rootNode);//the "dashed arrow" pointing to the root
                    break;
                case SITE:
                    String idSite = String.valueOf(((BigraphEntity.SiteEntity) each).getIndex());
                    LabeledNode varNode = new LabeledNode(idSite, each.getType());
                    graph.addVertex(varNode); //but don't connect it to a node
                    variables.put(idSite, varNode);
                    if (bigraph.getParent(each).getType() == BigraphEntityType.ROOT) {
                        LabeledNode byIdType = getByIdAndType("" + ((BigraphEntity.RootEntity) bigraph.getParent(each)).getIndex(), bigraph.getParent(each).getType());
                        variableMap.getOrDefault(idSite, new ArrayList<>()).add(byIdType);
                    } else if (bigraph.getParent(each).getType() == BigraphEntityType.NODE) {
                        LabeledNode byId = getById(hierarchyIdMap.get(bigraph.getParent(each)));
                        variableMap.getOrDefault(idSite, new ArrayList<>()).add(byId);
                    }
                    break;
            }

            //TODO if site addNode, addEdge to Parent
            //TODO if node: addNode,
            //TODO: if parent is root: addEdge
        }

    }

    protected LabeledNode getByIdAndType(String id, BigraphEntityType type) {
        return graph.vertexSet().stream().filter(x -> x.getId().equals(id) && x.getType().equals(type)).findFirst().get();
    }

    protected LabeledNode getById(String id) {
        return graph.vertexSet().stream().filter(x -> x.getId().equals(id)).findFirst().get();
    }
}
