package org.bigraphs.framework.visualization;

import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.*;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.BigraphEntityType;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicControl;
import org.bigraphs.framework.visualization.auxiliary.GraphvizProcess;
import org.bigraphs.framework.visualization.supplier.GraphvizColorSupplier;
import org.bigraphs.framework.visualization.supplier.GraphvizShapeSupplier;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.attribute.Rank.RankDir.BOTTOM_TO_TOP;
import static guru.nidi.graphviz.model.Factory.*;

/**
 * This class visualizes a bigraph by means of GraphViz.
 * <p>
 * Several styling can be configured, such as color, shape and the label format of each node.
 *
 * @author Dominik Grzelak
 */
public class BigraphGraphvizExporter implements BigraphGraphicsExporter<Bigraph<?>> {

    private static final GraphicalFeatureSupplier<String> DEFAULT_labelSupplier = new DefaultLabelSupplier();
    private static final GraphicalFeatureSupplier<Shape> DEFAULT_shapeSupplier = new GraphvizShapeSupplier();
    private static final GraphicalFeatureSupplier<Color> DEFAULT_colorSupplier = new GraphvizColorSupplier();

    private GraphicalFeatureSupplier<String> labelSupplier = DEFAULT_labelSupplier;
    private GraphicalFeatureSupplier<Shape> shapeSupplier = DEFAULT_shapeSupplier;
    private GraphicalFeatureSupplier<Color> colorSupplier = DEFAULT_colorSupplier;


    @Override
    public void toPNG(Bigraph<?> bigraph, File output) throws IOException {
        BigraphGraphvizExporter.toPNG(bigraph, true, output);
    }

    @Override
    public BigraphGraphicsExporter<Bigraph<?>> with(GraphicalFeatureSupplier<?> supplier) {
        if (supplier instanceof GraphvizColorSupplier) {
            colorSupplier = (GraphicalFeatureSupplier<Color>) supplier;
        } else if (supplier instanceof GraphvizShapeSupplier) {
            shapeSupplier = (GraphicalFeatureSupplier<Shape>) supplier;
        } else if (supplier instanceof DefaultLabelSupplier) {
            labelSupplier = (GraphicalFeatureSupplier<String>) supplier;
        } else {
            throw new IllegalArgumentException("This supplier is not supported by this graphics exporter: " + supplier);
        }
        return this;
    }


    public static String toPNG(Bigraph<?> bigraph, boolean asTree, File output) throws IOException {
        return new BigraphGraphvizExporter()
                .convert(bigraph, output, Format.PNG, asTree);
    }

    public static String toDOT(Bigraph<?> bigraph, boolean asTree) {
        try {
            return new BigraphGraphvizExporter()
                    .convert(bigraph, null, Format.DOT, asTree);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private BigraphGraphvizExporter() {

    }

    /**
     * Converts a bigraph into DOT format ready to be visualized. If no output file is specified the
     * resulting file will not be written.
     * The DOT representation of the bigraph will still be returned.
     * <p>
     *
     * @param bigraph
     * @param output  if {@code null} no output file is created
     * @param format
     * @param <S>
     * @return the DOT representation of the bigraph
     * @throws IOException
     */
    public <S extends Signature<?>> String convert(Bigraph<S> bigraph,
                                                   File output,
                                                   Format format,
                                                   boolean asTreeStructure) throws IOException {
//        Graphviz.useEngine(new GraphvizJdkEngine());
        final MutableGraph theGraph = mutGraph("Bigraph").setDirected(false)
//                .graphAttrs().add(RankDir.BOTTOM_TO_TOP);
                .graphAttrs().add(Rank.dir(BOTTOM_TO_TOP));

        if (!asTreeStructure) {
            List<MutableGraph> rootGraphs = new LinkedList<>();
            for (BigraphEntity.RootEntity eachRoot : bigraph.getRoots()) {
                MutableGraph graphRoot = createRootNodeCluster(eachRoot);
                MutableGraph graph1 = makeHierarchyCluster(bigraph, eachRoot, graphRoot);
                rootGraphs.add(graph1);
            }
            theGraph.add(rootGraphs);
        }

        //RANK NODES
        // collect node heights first
        Map<BigraphEntity, Integer> levelMap = new LinkedHashMap<>();
//        for (BigraphEntity each : bigraph.getAllPlaces()) {
//            int levelUtil = getNodeHeight(bigraph, each, 0);
//            levelMap.put(each, levelUtil);
//        }

        // create the data structure first for creating the node hierarchy
        final Map<String, Set<GraphVizLink>> graphMap = new LinkedHashMap<>();
        bigraph.getAllPlaces().forEach(s -> {
            graphMap.put(labelSupplier.with(s).get(), new HashSet<>());
//            int levelUtil = bigraph.getLevelOf(s); //getNodeHeight(bigraph, s, 0);
            levelMap.put(s, bigraph.getLevelOf(s));
        });//TODO: move down with putIFAbsent
        // connects children to parent
        Consumer<BigraphEntity> nestingConsumer = t -> {
            BigraphEntity parent = bigraph.getParent(t);
            graphMap.get(labelSupplier.with(t).get()).add(
                    new GraphVizLink(labelSupplier.with(parent).get(), "", t, parent)
            );
        };
        bigraph.getNodes().forEach(nestingConsumer);
        bigraph.getSites().forEach(nestingConsumer);


//        //RANK NODES
//        // collect node heights first
//        Map<BigraphEntity, Integer> levelMap = new HashMap<>();
//        for (BigraphEntity each : bigraph.getAllPlaces()) {
//            int levelUtil = getNodeHeight(bigraph, each, 0);
//            levelMap.put(each, levelUtil);
//        }
        Map<Integer, List<BigraphEntity>> collect = levelMap.entrySet().stream()
                .collect(
                        Collectors.groupingBy(
                                Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                        )
                );

        if (asTreeStructure) {
            // created ranked node graph

            theGraph.add(collect.values().stream().map(bigraphEntities ->
                            graph().graphAttr().with(Rank.dir(Rank.RankDir.TOP_TO_BOTTOM)).with(
                                    bigraphEntities.stream()
                                            // (not necessary now to specify the appearance of the nodes. will be done later)
                                            .map(x -> node(labelSupplier.with(x).get()))
                                            .toArray(LinkSource[]::new)))
                    .collect(Collectors.toList()));


            // Create "node-edge-node" hieararchy for the Graph
            graphMap.forEach((key, targetSet) -> {
                Node node = node(key);
                if (targetSet.size() != 0) {
                    BigraphEntity sourceEntity = targetSet.iterator().next().getSourceEntity();
                    if (BigraphEntityType.isSite(sourceEntity)) {
                        node = createSiteNode(sourceEntity);
                    }
                }
                final Node finalNode = node;
                targetSet.forEach(l -> {
                            theGraph.add(
                                    finalNode.with(colorSupplier.with(l.getSourceEntity()).get(),
                                                    shapeSupplier.with(l.getSourceEntity()).get())
                                            .link(
                                                    Link.to(node(l.getTarget())).with(Label.of(l.getLabel()))
                                            )
                            );
                        }
                );
            });

            bigraph.getRoots().forEach(x -> theGraph.add(createRootNode(x)));
        }

        // Put inner names to the bottom of the graph
        List<BigraphEntity> allLinks = new LinkedList<>(bigraph.getOuterNames());
        List<String> outerLabels = allLinks.stream().map(x -> ((BigraphEntity.OuterName) x).getName()).collect(Collectors.toList());
        List<BigraphEntity> allPoints = new LinkedList<>(bigraph.getInnerNames());
        List<String> duplicates = allPoints.stream().map(x -> ((BigraphEntity.InnerName) x).getName())
                .filter(x -> outerLabels.contains(x))
                .collect(Collectors.toList());
        Rank.RankType rankPoints = asTreeStructure ? Rank.RankType.SOURCE : Rank.RankType.MIN;
        theGraph.add(
                graph().graphAttr().with(Rank.inSubgraph(rankPoints)).with(allPoints.stream()
                        .map(x -> {
                                    String label = labelSupplier.with(x).get();
                                    if (duplicates.contains(label)) label += "i";
                                    return node(label)
                                            .with(shapeSupplier.with(x).get(), colorSupplier.with(x).get(), Label.of(labelSupplier.with(x).get()));
                                }
                        )
                        .toArray(LinkSource[]::new)
                )
        );
        // make nicer graph (ranked, etc.)

        if (asTreeStructure) allLinks.addAll(bigraph.getEdges());
        Rank.RankType rankLinks = asTreeStructure ? Rank.RankType.SINK : Rank.RankType.MAX;
        theGraph.add(
                graph().graphAttr().with(Rank.inSubgraph(rankLinks)).with(allLinks.stream()
                        .map(x -> node(labelSupplier.with(x).get())
                                .with(shapeSupplier.with(x).get(), colorSupplier.with(x).get())
                        )
                        .toArray(LinkSource[]::new))

        );

        // this creates hyperedges for nodes and inner names
        Collection<BigraphEntity.Edge> edges = bigraph.getEdges();

        // now we consider outer name graphviz edges
        // outer names are connected with inner names or nodes (respective ports)
        for (BigraphEntity.OuterName eachOuterName : bigraph.getOuterNames()) {
            Collection<BigraphEntity<?>> pointsFromLink = bigraph.getPointsFromLink(eachOuterName);
            if (pointsFromLink.size() == 0) continue;
            Node grOuter = node(labelSupplier.with(eachOuterName).get())
                    .with(shapeSupplier.with(eachOuterName).get(), colorSupplier.with(eachOuterName).get());
            for (BigraphEntity<?> point : pointsFromLink) {
                switch (point.getType()) {
                    case PORT:
                        BigraphEntity.NodeEntity<DefaultDynamicControl> nodeOfPort = bigraph.getNodeOfPort((BigraphEntity.Port) point);
                        Optional<MutableNode> first = theGraph.nodes().stream().filter(x -> x.name().toString().equals(labelSupplier.with(nodeOfPort).get()))
                                .findFirst();
                        if (first.isPresent()) {
                            first.get().addLink(Link.to(grOuter).with(Color.GREEN));
                        } else {
                            theGraph.add(mutNode(labelSupplier.with(nodeOfPort).get()).addLink(Link.to(grOuter).with(Color.GREEN)));
                        }

                        //.get()
                        //      .addLink(Link.to(grOuter).with(Color.GREEN));
                        break;
                    case INNER_NAME:
                        String label = labelSupplier.with(point).get();
                        if (duplicates.contains(label)) label += "i";
                        Node grInner = node(label).with(Label.of(labelSupplier.with(point).get()));
                        theGraph.add(grInner.link(Link.to(grOuter).with(Color.GREEN)));
                        break;
                }
            }
        }


        //TODO: directly connect nodes together if ports == 1
        for (BigraphEntity.Edge eachEdge : edges) {
            // edges for graphviz are created as "hidden nodes"
            //find the nodes that are connected to it
            Collection<BigraphEntity<?>> pointsFromLink = bigraph.getPointsFromLink(eachEdge);
            if (pointsFromLink.size() == 0) {
                continue;
            }
            final Node hiddenEdgeNode = node(labelSupplier.with(eachEdge).get()).with(Shape.POINT, Color.GREEN);
//            if (pointsFromLink.size() != 0) {
            //if inner name: create new node
            for (BigraphEntity<?> point : pointsFromLink) {
                switch (point.getType()) {
                    case PORT:
                        BigraphEntity.NodeEntity<DefaultDynamicControl> nodeOfPort = bigraph.getNodeOfPort((BigraphEntity.Port) point);
                        Node grNode = node(labelSupplier.with(nodeOfPort).get())
//                                .link(Link.to(hiddenEdgeNode).with(Style.FILLED, colorSupplier.with(eachEdge).get()));
                                .link(Link.to(hiddenEdgeNode).with(Style.SOLID, colorSupplier.with(eachEdge).get()));
                        theGraph.add(grNode);
                        break;
                    case INNER_NAME:
                        String label = labelSupplier.with(point).get();
                        if (duplicates.contains(label)) label += "i";
                        Node grInner = node(label);
                        theGraph.add(hiddenEdgeNode.link(
                                Link.to(grInner).with(Color.GREEN)));
                        break;
                }
            }
        }

        if (Objects.nonNull(output)) {
            try {
                GraphvizProcess process = new GraphvizProcess();
                Process convert = process.convert(theGraph.toString(), output.getAbsolutePath().toString());
                boolean hasExited = convert.waitFor(5, TimeUnit.SECONDS);
                if (!hasExited) {
                    Graphviz.fromGraph(theGraph).totalMemory(204800).render(format).toFile(output);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return theGraph.toString();
    }

    private MutableGraph createRootNodeCluster(BigraphEntity eachRoot) {
        return mutGraph(labelSupplier.with(eachRoot).get())
                .setDirected(false).setCluster(true)
                .graphAttrs()
                .add(
                        Rank.dir(Rank.RankDir.BOTTOM_TO_TOP),
                        Label.of(labelSupplier.with(eachRoot).get())
//                        Style.DASHED
//                        Style.FILLED
                );
    }

    private Node createSiteNode(BigraphEntity site) {
        return node(labelSupplier.with(site).get())
                .with(shapeSupplier.with(site).get(),
//                        Style.FILLED.and(Style.DOTTED),
                        Style.FILLED,
//                        Style.
//                        Style.DOTTED,
//                        Style.DOTTED,
                        Color.GRAY69.fill(),
                        Color.WHITE.font());
    }

    private Node createRootNode(BigraphEntity root) {
        return node(labelSupplier.with(root).get())
                .with(shapeSupplier.with(root).get(),
//                        Style.lineWidth(3),
                        Style.FILLED,
                        Color.WHITE
//                        Style.tapered(2),
//                        colorSupplier.with(root).get().font()
                );
    }

    private MutableGraph makeHierarchyCluster(Bigraph bigraph, BigraphEntity nodeEntity, MutableGraph currentParent) {
        //if children: iterate and check
        List<BigraphEntity> childrenOf = new ArrayList<BigraphEntity>(bigraph.getChildrenOf(nodeEntity));

        List<MutableGraph> graphList = new LinkedList<>();
        for (BigraphEntity each : childrenOf) {
            Collection childrenOfEach = bigraph.getChildrenOf(each);
            //create a child
            if (childrenOfEach.size() == 0) {
                if (BigraphEntityType.isSite(each)) {
                    currentParent.add(createSiteNode(each));
                } else {
                    currentParent.add(node(labelSupplier.with(each).get()).with(shapeSupplier.with(each).get()));
                }
            } else { //create a new hierarchy graph
                MutableGraph cluster = mutGraph(labelSupplier.with(each).get()).setCluster(true)
                        .setDirected(true)
//                        .graphAttrs().add(Label.of(labelSupplier.with(each).get()), Style.SOLID);
                        .graphAttrs().add(Label.of(labelSupplier.with(each).get()));
                cluster.add(node(labelSupplier.with(each).get()).with(Style.INVIS));
                MutableGraph mutableGraph = makeHierarchyCluster(bigraph, each, cluster);
                graphList.add(mutableGraph);
            }
        }
        graphList.forEach(currentParent::add);
        return currentParent;
    }

    private class GraphVizLink {
        private final String target;
        private final String label;
        private final BigraphEntity sourceEntity;
        private final BigraphEntity targetEntity;

        public GraphVizLink(String target, String label, BigraphEntity sourceEntity, BigraphEntity targetEntity) {
            this.target = target;
            this.label = label;
            this.sourceEntity = sourceEntity;
            this.targetEntity = targetEntity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GraphVizLink)) return false;
            GraphVizLink that = (GraphVizLink) o;
            return Objects.equals(target, that.target) &&
                    Objects.equals(label, that.label);
        }

        public String getTarget() {
            return target;
        }

        public String getLabel() {
            return label;
        }

        public BigraphEntity getSourceEntity() {
            return sourceEntity;
        }

        public BigraphEntity getTargetEntity() {
            return targetEntity;
        }

        @Override
        public int hashCode() {
            return Objects.hash(target, label);
        }
    }
}
