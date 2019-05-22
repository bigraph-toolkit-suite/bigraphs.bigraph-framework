package de.tudresden.inf.st.bigraphs.visualization;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.LinkSource;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.Node;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.model.Factory.*;
import static guru.nidi.graphviz.model.Factory.node;

public class GraphvizConverter {

    private static final GraphicalFeatureSupplier<String> labelSupplier = new DefaultLabelSupplier();
    private static final GraphicalFeatureSupplier<Shape> shapeSupplier = new DefaultShapeSupplier();
    private static final GraphicalFeatureSupplier<Color> colorSupplier = new DefaultColorSupplier();

    public static <S extends Signature> String toPNG(Bigraph<S> bigraph, File output) throws IOException {
        return convert(bigraph, output, Format.PNG, labelSupplier, colorSupplier, shapeSupplier);
    }

    public static <S extends Signature> String toDOT(Bigraph<S> bigraph) {
        try {
            return convert(bigraph, null, null, labelSupplier, colorSupplier, shapeSupplier);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts a bigraph into DOT format ready to be visualized. If no output file is specified the
     * resulting file will not be written.
     * The DOT representation of the bigraph will still be returned.
     * <p>
     *
     * @param bigraph
     * @param output        if {@code null} no output file is created
     * @param format
     * @param labelSupplier
     * @param colorSupplier
     * @param shapeSupplier
     * @param <S>
     * @return the DOT representation of the bigraph
     * @throws IOException
     */
    public static <S extends Signature> String convert(Bigraph<S> bigraph,
                                                       File output,
                                                       Format format,
                                                       GraphicalFeatureSupplier<String> labelSupplier,
                                                       GraphicalFeatureSupplier<Color> colorSupplier,
                                                       GraphicalFeatureSupplier<Shape> shapeSupplier) throws IOException {

//        final GraphicalFeatureSupplier<String> labelSupplier = new DefaultLabelSupplier();
//        final GraphicalFeatureSupplier<Shape> shapeSupplier = new DefaultShapeSupplier();
//        final GraphicalFeatureSupplier<Color> colorSupplier = new DefaultColorSupplier();
//

        // create the data structure first for creating the node hierarchy
        final Map<String, Set<GraphVizLink>> graphMap = new HashMap<>();
        final Map<String, Set<GraphVizLink>> graphLinkMap = new HashMap<>();
        bigraph.getNodes().forEach(
                s -> {
                    graphMap.put(labelSupplier.with(s).get(), new HashSet<>());
                    graphLinkMap.put(labelSupplier.with(s).get(), new HashSet<>());
                }
        );
        bigraph.getRoots().forEach(
                s -> graphMap.put(labelSupplier.with(s).get(), new HashSet<>())
        );
        bigraph.getSites().forEach(
                s -> graphMap.put(labelSupplier.with(s).get(), new HashSet<>())
        );

        // connects children to parent
        Consumer<BigraphEntity> nestingConsumer = t -> {
            BigraphEntity parent = bigraph.getParent(t);
            graphMap.get(labelSupplier.with(t).get()).add(
                    new GraphVizLink(labelSupplier.with(parent).get(), "", t, parent)
            );
        };
        bigraph.getNodes().forEach(nestingConsumer);
        bigraph.getSites().forEach(nestingConsumer);

        MutableGraph theGraph = mutGraph("Edge Graph").setDirected(false).graphAttrs().add(RankDir.BOTTOM_TO_TOP);
//        MutableGraph graph = mutGraph("Place Graph").setDirected(false);


        //RANK NODES FIRST
        // collect node heights first
        Map<BigraphEntity, Integer> levelMap = new HashMap<>();
        for (BigraphEntity each : bigraph.getAllPlaces()) {
            int levelUtil = getNodeHeight(bigraph, each, 0);
            levelMap.put(each, levelUtil);
        }

        Map<Integer, List<BigraphEntity>> collect = levelMap.entrySet().stream().collect(Collectors.groupingBy(
                Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
        // created ranked node graph
        // not necessary now to specify the appearance of the nodes. will be done later
        theGraph.add(collect.values().stream().map(bigraphEntities ->
                graph().graphAttr().with(Rank.SAME).with(
                        bigraphEntities.stream()
                                .map(x -> node(labelSupplier.with(x).get()))
                                .toArray(LinkSource[]::new)))
                .collect(Collectors.toList()));
        // make nicer graph (ranked, etc.)
        List<BigraphEntity> allLinks = new LinkedList<>();
        allLinks.addAll(bigraph.getOuterNames());
//        allLinks.addAll(bigraph.getEdges());
        theGraph.add(
                graph().graphAttr().with(Rank.SINK).with(allLinks.stream()
                        .map(x -> node(labelSupplier.with(x).get())
                                .with(shapeSupplier.with(x).get(), colorSupplier.with(x).get())
                        )
                        .toArray(LinkSource[]::new))

        );
        List<BigraphEntity> allPoints = new LinkedList<>(bigraph.getInnerNames());
//        allLinks.addAll(bigraph.getEdges());
        theGraph.add(
                graph().graphAttr().with(Rank.SOURCE).with(allPoints.stream()
                                .map(x -> node(labelSupplier.with(x).get())
                                                .with(shapeSupplier.with(x).get())
//                                .with("style", "rounded")
//                                .with("color", "black")
//                                .with("fillcolor", colorSupplier.with(x).get())


                                )
                                .toArray(LinkSource[]::new)
                )
        );


        //HIERARCHY
        // Create node hieararchy for the Graph
        graphMap.forEach((key, targetSet) -> {
            Shape shape = targetSet.size() != 0 ? shapeSupplier.with(((GraphVizLink) targetSet.toArray()[0]).getSourceEntity()).get() : Shape.CIRCLE;
            Node node = node(key).with(shape);

//            if (currentState.getId().name().equals(node.name().toString())) col = Color.RED;
            Color finalCol = Color.BLACK;
            targetSet.forEach(l -> theGraph.add(
                    node.with(colorSupplier.with(l.getSourceEntity()).get(), shapeSupplier.with(l.getSourceEntity()).get())
                            .link(Link.to(node(l.getTarget())).with(Label.of(l.getLabel().toLowerCase())))
            ));
        });


        // this creates hyperedges for nodes and inner names
        Collection<BigraphEntity.Edge> edges = bigraph.getEdges();

        // now we consider outer name graphviz edges
        // outer names are connected with inner names or nodes (respective ports)
        for (BigraphEntity.OuterName eachOuterName : bigraph.getOuterNames()) {
            Collection<BigraphEntity> pointsFromLink = bigraph.getPointsFromLink(eachOuterName);
            Node grOuter = node(labelSupplier.with(eachOuterName).get())
                    .with(shapeSupplier.with(eachOuterName).get(), colorSupplier.with(eachOuterName).get());
            for (BigraphEntity point : pointsFromLink) {
                switch (point.getType()) {
                    case PORT:
                        BigraphEntity.NodeEntity<DefaultDynamicControl> nodeOfPort = bigraph.getNodeOfPort((BigraphEntity.Port) point);
                        Node grNode = node(labelSupplier.with(nodeOfPort).get());
                        theGraph.add(grNode.link(Link.to(grOuter).with(Style.DIAGONALS, Color.GREEN))); //.with(Style.BOLD, Label.of(""), Color.GREEN, Arrow.NONE)));
                        break;
                    case INNER_NAME:
                        Node grInner = node(labelSupplier.with(point).get()); //.with(Shape.RECTANGLE, Color.BLUE);
                        theGraph.add(grInner.link(grOuter)); //.with(Style.BOLD, Label.of(""), Color.GREEN, Arrow.NONE)));
                        break;
                }
            }
        }


        //TODO: directly connect nodes together if ports == 1
        for (BigraphEntity.Edge eachEdge : edges) {
            // edges for graphviz are created as "hidden nodes"
            final Node hiddenEdgeNode = node(labelSupplier.with(eachEdge).get()).with(Shape.POINT);
            //find the nodes that are connected to it
            Collection<BigraphEntity> pointsFromLink = bigraph.getPointsFromLink(eachEdge);
//            if (pointsFromLink.size() != 0) {
            //if inner name: create new node
            for (BigraphEntity point : pointsFromLink) {
                switch (point.getType()) {
                    case PORT:
                        BigraphEntity.NodeEntity<DefaultDynamicControl> nodeOfPort = bigraph.getNodeOfPort((BigraphEntity.Port) point);
                        Node grNode = node(labelSupplier.with(nodeOfPort).get())
                                .link(Link.to(hiddenEdgeNode).with(Style.FILLED, colorSupplier.with(eachEdge).get()));
                        theGraph.add(grNode); //.with(Style.BOLD, Label.of(""), Color.GREEN, Arrow.NONE)));
                        break;
                    case INNER_NAME:
                        Node grInner = node(labelSupplier.with(point).get());
                        theGraph.add(hiddenEdgeNode.link(
                                grInner));
//                                            .with(Style.BOLD, Label.of(""), Color.GREEN, Arrow.NONE)
//                            ));
                        break;
                }
            }
//            }
        }


//        theGraph.add(node("Job_v4").link(Link.to(node("Job_v2"))));
//        theGraph.add(node("Job_v2").link(Link.to(node("Job_v3"))));
//        theGraph.add(node("qq2").link(node("aa2")));


        ;
        if (Objects.nonNull(output)) {
            Graphviz.fromGraph(theGraph).render(format).toFile(output);
        }
        return theGraph.toString();
    }

    //TODO move into bigraph
    private static int getNodeHeight(Bigraph<?> bigraph, BigraphEntity data, int level) {
        BigraphEntity parent = bigraph.getParent(data);
        if (data == null || parent == null)
            return level;
        if (BigraphEntityType.isRoot(parent) && level == 0) {
            return 1;
        }
        return getNodeHeight(bigraph, parent, level + 1);
    }

    @Data
    private static class GraphVizLink {
        private final String target;
        private final String label;
        private final BigraphEntity sourceEntity;
        private final BigraphEntity targetEntity;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GraphVizLink)) return false;
            GraphVizLink that = (GraphVizLink) o;
            return Objects.equals(target, that.target) &&
                    Objects.equals(label, that.label);
        }

        @Override
        public int hashCode() {
            return Objects.hash(target, label);
        }
    }
}
