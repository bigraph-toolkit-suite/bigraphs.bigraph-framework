package org.bigraphs.framework.core.analysis;

import org.bigraphs.framework.core.AbstractEcoreSignature;
import org.bigraphs.framework.core.BigraphEntityType;
import org.bigraphs.framework.core.Control;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.MutableBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicControl;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This class implements the Union-Find-Algorithm to check whether a graph is connected or not.
 * The number of connected components is computed as well.
 * <p>
 * This implementation is suited for hypergraphs (i.e., link graphs).
 * Each hyperedge is treated as a set of vertices that are "unioned" together in the algorithm.
 * <p>
 * Complexity: O(E * α(V)), where E is the number of hyperedges, V is the number of vertices, and α the inverse of
 * the Ackermann function.
 *
 * @author Dominik Grzelak
 */
public class PureLinkGraphConnectedComponents implements BigraphDecompositionStrategy<PureBigraph> {

    Map<BigraphEntity<?>, Integer> idMap = new HashMap<>();
    UnionFind uf;
    PureBigraph originalBigraph;



    @Override
    public DecompositionStrategy getDecompositionStrategyType() {
        return BigraphDecompositionStrategy.DecompositionStrategy.UnionFind_PureBigraphs;
    }

    /**
     * Entrypoint of the algorithm.
     * Computes the number of connected components of a link graph.
     * It can be used to determine whether it is connected or not.
     *
     * @param linkGraph the bigraph
     */
    @Override
    public void decompose(PureBigraph linkGraph) {
        this.originalBigraph = linkGraph;
        int n = linkGraph.getNodes().size() + linkGraph.getInnerNames().size();
        uf = new UnionFind(n);
        List<BigraphEntity.Link> allLinks = linkGraph.getAllLinks();
        idMap.clear();
        // We convert bigraph nodes and inner names to simple unique integers along the way
        AtomicInteger nodeIDSupplier = new AtomicInteger(0);
        // the union method for each hyperedge in the graph.
        for (BigraphEntity.Link eachEdge : allLinks) {
            // can be ports (nodes) and inner names
            List<BigraphEntity<?>> vertices = linkGraph.getPointsFromLink(eachEdge);
            Set<BigraphEntity<?>> ports = vertices.stream()
                    .filter(BigraphEntityType::isPort).collect(Collectors.toSet());
            Set<BigraphEntity.NodeEntity<DefaultDynamicControl>> portsToNodes = ports.stream()
                    .map(x -> linkGraph.getNodeOfPort((BigraphEntity.Port) x)).collect(Collectors.toSet());
            vertices.removeAll(ports); // inner names will remain in the list
            vertices.addAll(portsToNodes); // we just swap ports with their nodes
            // Nodes to integers
            vertices.forEach(x -> {
                if (!idMap.containsKey(x)) {
                    idMap.put(x, nodeIDSupplier.getAndIncrement());
                }
            });
            Set<Integer> verticesToIntegers = idMap.entrySet().stream()
                    .filter(x -> vertices.contains(x.getKey())).map(Map.Entry::getValue).collect(Collectors.toSet());
            // perform the algorithm incrementally until all links are processed
            uf.union(verticesToIntegers);
        }
        // and now fill idMap with rest of nodes that are not connected by a hyperedge
        linkGraph.getNodes().forEach(eachNode -> {
            if (!idMap.containsKey(eachNode)) {
                idMap.putIfAbsent(eachNode, nodeIDSupplier.getAndIncrement());
            }
        });
        linkGraph.getInnerNames().forEach(eachInner -> {
            if (!idMap.containsKey(eachInner)) {
                idMap.putIfAbsent(eachInner, nodeIDSupplier.getAndIncrement());
            }
        });
    }

    /**
     * Returns a map of partitions and their entities (which can be nodes and inner names).
     * Method {@link #decompose(PureBigraph)} must be called before.
     * <p>
     * The unique key signifies not only that it's a unique partition, but the integer value also refers to the "representative"
     * of this partition as determined by the Union-Find algorithm.
     * It can be identified via the {@code idMap}.
     *
     * @return partitions
     */
    public Map<Integer, List<BigraphEntity<?>>> getPartitions() {
        Map<Integer, List<BigraphEntity<?>>> partitions = new HashMap<>();
        UnionFind uf = getUnionFindDataStructure();
        Map<Integer, Integer> childParentMap = new HashMap<>(uf.getChildParentMap());
        Set<Integer> rootsOfPartitions = uf.getRootsOfPartitions(childParentMap);
        for (Integer eachRootIx : rootsOfPartitions) {
            partitions.putIfAbsent(eachRootIx, new LinkedList<>());
            // bigraphEntityRepresentative
            getChildrenForRoot(childParentMap, eachRootIx).forEach(m -> {
                Optional<? extends BigraphEntity<?>> memberBigraph = getFirstKeyForRoot(idMap, m);
                assert memberBigraph.isPresent();
                partitions.get(eachRootIx).add(memberBigraph.get());
            });
        }
        return partitions;
    }

    /**
     * Get the connected component as disjoint bigraphs. They can be composed from left to right.
     * Therefore, a linked list is returned that determines the order. Otherwise, the interfaces have to be checked manually.
     * <p>
     * The bigraphs are separated according to the link graph structure.
     * If the bigraph is just a tree or if there is a single path through all nodes w.r.t. the link graph, then the tree will be returned.
     * That is, the number of connected components is 1.
     *
     * @return the connected components as bigraphs
     */
    public List<PureBigraph> getConnectedComponents() {
        //TODO: copy bigraph if CC.size()==1

        //TODO combine partitions that are only nodes. do matching to extract context + params.
        // so we always get at most 2 components extra if the rest of the graph is not fully connected.

        Supplier<String> edgeLblSupplier;
        Supplier<String> vertexLabelSupplier;
        vertexLabelSupplier = vertexLabelSupplier();
        edgeLblSupplier = edgeLabelSupplier();
        AtomicInteger rootCnt = new AtomicInteger(0);
        AtomicInteger siteCnt = new AtomicInteger(0);
        Map<BigraphEntity, Integer> bigraphEntityToSiteIndex = new LinkedHashMap<>(); // the bigraph node containing a site
        Map<BigraphEntity, Integer> bigraphEntityToRootIndex = new LinkedHashMap<>(); // the bigraph node below a root Index

        List<PureBigraph> components = new LinkedList<>();
        Map<Integer, List<BigraphEntity<?>>> partitions = getPartitions();
        for (Map.Entry<Integer, List<BigraphEntity<?>>> eachPartition : partitions.entrySet()) {
            HashMap<Integer, BigraphEntity.RootEntity> newRoots = new LinkedHashMap<>();
            HashMap<String, BigraphEntity.NodeEntity> newNodes = new LinkedHashMap<>();
            HashMap<Integer, BigraphEntity.SiteEntity> newSites = new LinkedHashMap<>();
            HashMap<String, BigraphEntity.Edge> newEdges = new LinkedHashMap<>();
            HashMap<String, BigraphEntity.OuterName> newOuterNames = new LinkedHashMap<>();
            HashMap<String, BigraphEntity.InnerName> newInnerNames = new LinkedHashMap<>();
            MutableBuilder<DefaultDynamicSignature> builder = MutableBuilder.newMutableBuilder(this.originalBigraph.getSignature(), this.originalBigraph.getMetaModel());
            List<BigraphEntity<?>> nodes = eachPartition.getValue();

            // Build the tree
            // If they have children, put a site below

            // TODO we need a map: node -> siteIX, node -> rootIx
            // these have to be checked before to get the right root/site index when we construct the PG

            nodes.forEach(n -> {
                if(BigraphEntityType.isNode(n)) {
                    String vLbl = ((BigraphEntity.NodeEntity) n).getName();

//                    BigraphEntity newNode = builder.createNewNode(n.getControl(), vLbl);
//                    newNodes.put(vLbl, (BigraphEntity.NodeEntity) newNode);
                    BigraphEntity newNode = newNodes.computeIfAbsent(vLbl, s -> {
                        return (BigraphEntity.NodeEntity)builder.createNewNode(n.getControl(), vLbl);
//                        return newNodes.put(vLbl,  (BigraphEntity.NodeEntity)builder.createNewNode(n.getControl(), vLbl));
                    });

                    // Check out the original root
                    // we may need to nest the new node under a new root
                    BigraphEntity<?> parent = originalBigraph.getParent(n);
                    assert parent != null;
                    if(!nodes.contains(parent)) {
                        // before creating root, check if there is a corresponding site index
                        int rootIx = bigraphEntityToRootIndex.get(parent) == null ? rootCnt.getAndIncrement() : bigraphEntityToRootIndex.get(parent);
                        bigraphEntityToRootIndex.putIfAbsent(parent, rootIx);
                        BigraphEntity newRoot = newRoots.computeIfAbsent(rootIx, integer -> (BigraphEntity.RootEntity) builder.createNewRoot(integer));
                        builder.setParentOfNode(newNode, newRoot);
                        bigraphEntityToSiteIndex.put(parent, rootIx);
                    } else {

                    }


                    // If the node does contain children, we may need to create a site
//                    if(!originalBigraph.getChildrenOf(n).isEmpty()) {
//                        long count = originalBigraph.getChildrenOf(n).stream().filter(nodes::contains).count();
//
//                        // There is at least one node that doesn't belong to the current partition
//                        // That means, we need to create a site for later
//                        if (count > 0 && count < originalBigraph.getChildrenOf(n).size()) {
//                            // Get any child that doesn't belong to the current partition
//                            Optional<BigraphEntity<?>> anyChild = originalBigraph.getChildrenOf(n).stream().filter(e -> !nodes.contains(e)).findFirst();
//                            int siteIx = anyChild.isPresent() && bigraphEntityToSiteIndex.get(anyChild.get()) == null ? siteCnt.getAndIncrement() : bigraphEntityToSiteIndex.get(anyChild.get());
//                            bigraphEntityToSiteIndex.putIfAbsent(anyChild.get(), siteIx);
//                            BigraphEntity newSite = builder.createNewSite(siteIx);
//                            newSites.put(siteIx, (BigraphEntity.SiteEntity) newSite);
//                            bigraphEntityToSiteIndex.put(n, siteIx);
//                            builder.setParentOfNode(newSite, newNode);
//                        }
//
//                    }
                }
            });

            // Create the component bigraph
            PureBigraphBuilder.InstanceParameter meta = builder.new InstanceParameter(
                    builder.getMetaModel(),
                    originalBigraph.getSignature(),
                    newRoots,
                    newSites,
                    newNodes,
                    newInnerNames, newOuterNames, newEdges);
            builder.reset();
            PureBigraph generated = new PureBigraph(meta);
            components.add(generated);
        }

        //TODO: merge components that have the same root index?

        //TODO
//        assert getUnionFindDataStructure().getCount() == components.size();
        return components;
    }

    public static Set<Integer> getChildrenForRoot(Map<Integer, Integer> idMap, int rootIndex) {
        Set<Integer> children = new HashSet<>();
        for (Map.Entry<Integer, Integer> entry : idMap.entrySet()) {
            int vertex = entry.getKey();
            int currentRoot = findRoot(idMap, vertex);
            if (currentRoot == rootIndex) {
                children.add(vertex);
            }
        }
        return children;
    }

    private static Optional<? extends BigraphEntity<?>> getFirstKeyForRoot(Map<BigraphEntity<?>, Integer> idMap, int rootIndex) {
        return idMap.entrySet().stream()
                .filter(x -> Objects.equals(x.getValue(), rootIndex))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    /**
     * Get the map from {@link BigraphEntity} to simple Integer labels.
     * For the Union-Find algorithm, the original bigraph nodes and edges are assigned a simple integer label for identification.
     *
     * @return the map from bigraph entities to simple integer labels
     */
    public Map<BigraphEntity<?>, Integer> getIdMap() {
        return idMap;
    }

    /**
     * Returns the abstract data structure to solve the Union-Find problem.
     *
     * @return the data structure
     */
    public UnionFind getUnionFindDataStructure() {
        return uf;
    }

    /**
     * Generic subclass that solves the connectedness problem of a graph.
     * Vertices are integers.
     * This implementation is suited for hypergraphs (see {@link #union(Set)}).
     */
    public static class UnionFind {
        private final Map<Integer, Integer> parent;
        private final Map<Integer, Integer> rank;
        private int numOfSets;

        public UnionFind(int numVertices) {
            parent = new HashMap<>();
            rank = new HashMap<>();
            numOfSets = numVertices;
            // this initialization step does not represent the original graph itself, but rather the initial state of the
            // disjoint set data structure used by the Union-Find algorithm to determine the connected components of the graph.
            for (int i = 0; i < numVertices; i++) {
                parent.put(i, i);
                rank.put(i, 0);
            }
        }

        public int find(int x) {
            if (parent.get(x) != x) {
                parent.put(x, find(parent.get(x))); // Path compression
            }
            return parent.get(x);
        }

        /**
         * The union method finds the representative (root) of each vertex in the hyperedge,
         * and then merges the sets containing those representatives using the standard Union-Find operations.
         * <p>
         * It takes a Set<Integer> of vertices instead of just two vertices.
         * This allows to handle hyperedges that connect multiple vertices.
         *
         * @param vertices the vertices connected to a hyperedge
         */
        public void union(Set<Integer> vertices) {
            int representative = -1;
            for (int vertex : vertices) {
                if (representative == -1) {
                    representative = find(vertex);
                } else {
                    int vertexRoot = find(vertex);
                    if (vertexRoot != representative) {
                        if (rank.get(representative) < rank.get(vertexRoot)) {
                            parent.put(representative, vertexRoot);
                        } else if (rank.get(representative) > rank.get(vertexRoot)) {
                            parent.put(vertexRoot, representative);
                        } else {
                            parent.put(vertexRoot, representative);
                            rank.put(representative, rank.get(representative) + 1);
                        }
                        numOfSets--;
                    }
                }
            }
        }

        public int getCount() {
            return numOfSets;
        }

        public Map<Integer, Integer> getChildParentMap() {
            return parent;
        }

        public Map<Integer, Integer> getRank() {
            return rank;
        }

        public Set<Integer> getRootsOfPartitions(Map<Integer, Integer> parentMap) {
            Set<Integer> roots = new HashSet<>();
            for (int vertex : parentMap.keySet()) {
                int root = findRoot(parentMap, vertex);
                roots.add(root);
            }
            return roots;
        }

        public int countRoots(Map<Integer, Integer> parentMap) {
            return getRootsOfPartitions(parentMap).size();
        }
    }

    private static int findRoot(Map<Integer, Integer> childParentMap, int vertex) {
        if (childParentMap.get(vertex) == vertex) {
            return vertex;
        }
        return findRoot(childParentMap, childParentMap.get(vertex));
    }

    protected Supplier<String> vertexLabelSupplier() {
        return new Supplier<String>() {
            private int id = 0;

            @Override
            public String get() {
                return "v" + id++;
            }
        };
    }

    protected Supplier<String> edgeLabelSupplier() {
        return new Supplier<String>() {
            private int id = 0;

            @Override
            public String get() {
                return "e" + id++;
            }
        };
    }
}
