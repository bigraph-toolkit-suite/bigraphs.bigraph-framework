package de.tudresden.inf.st.bigraphs.visualization;

import com.google.common.collect.Lists;
import com.google.common.graph.Traverser;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.building.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.factory.SimpleBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.DynamicEcoreBigraph;
import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.*;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.model.Factory.*;

public class Test {
    private SimpleBigraphFactory<StringTypedName, FiniteOrdinal<Integer>> factory = new SimpleBigraphFactory<>();

    @org.junit.jupiter.api.Test
    void placegraph_export2() throws LinkTypeNotExistsException, InvalidConnectionException, IOException {
        DynamicEcoreBigraph bigraph_a = createBigraph_b();

        final GraphicalFeatureSupplier<String> labelSupplier = new DefaultLabelSupplier();
        final GraphicalFeatureSupplier<Shape> shapeSupplier = new DefaultShapeSupplier();
        final GraphicalFeatureSupplier<Color> colorSupplier = new DefaultColorSupplier();


        // create the data structure first for creating the node hierarchy
        final Map<String, Set<GraphVizLink>> graphMap = new HashMap<>();
        final Map<String, Set<GraphVizLink>> graphLinkMap = new HashMap<>();
        bigraph_a.getNodes().forEach(
                s -> {
                    graphMap.put(labelSupplier.with(s).get(), new HashSet<>());
                    graphLinkMap.put(labelSupplier.with(s).get(), new HashSet<>());
                }
        );
        bigraph_a.getRoots().forEach(
                s -> graphMap.put(labelSupplier.with(s).get(), new HashSet<>())
        );
        bigraph_a.getSites().forEach(
                s -> graphMap.put(labelSupplier.with(s).get(), new HashSet<>())
        );

        // connects children to parent
        Consumer<BigraphEntity> nestingConsumer = t -> {
            BigraphEntity parent = bigraph_a.getParent(t);
            graphMap.get(labelSupplier.with(t).get()).add(
                    new GraphVizLink(labelSupplier.with(parent).get(), "", t, parent)
            );
        };
        bigraph_a.getNodes().forEach(nestingConsumer);
        bigraph_a.getSites().forEach(nestingConsumer);

        MutableGraph theGraph = mutGraph("Edge Graph").setDirected(false).graphAttrs().add(RankDir.BOTTOM_TO_TOP);
//        MutableGraph graph = mutGraph("Place Graph").setDirected(false);


        //RANK NODES FIRST
        // collect node heights first
        Map<BigraphEntity, Integer> levelMap = new HashMap<>();
        for (BigraphEntity each : bigraph_a.getAllPlaces()) {
            int levelUtil = getNodeHeight(bigraph_a, each, 0);
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
        allLinks.addAll(bigraph_a.getOuterNames());
//        allLinks.addAll(bigraph_a.getEdges());
        theGraph.add(
                graph().graphAttr().with(Rank.SINK).with(allLinks.stream()
                        .map(x -> node(labelSupplier.with(x).get())
                                .with(shapeSupplier.with(x).get(), colorSupplier.with(x).get())
                        )
                        .toArray(LinkSource[]::new))

        );

        allLinks.clear();
        allLinks.addAll(bigraph_a.getInnerNames());
//        allLinks.addAll(bigraph_a.getEdges());
        theGraph.add(
                graph().graphAttr().with(Rank.SOURCE).with(allLinks.stream()
                        .map(x -> node(labelSupplier.with(x).get())
                                .with(shapeSupplier.with(x).get())
                                .with("style", "rounded")
                                .with("color", "black")
                                .with("fillcolor", colorSupplier.with(x).get())


                        )
                        .toArray(LinkSource[]::new)
                )
        );


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
        Collection<BigraphEntity.Edge> edges = bigraph_a.getEdges();

        // now we consider outer name graphviz edges
        for (BigraphEntity.OuterName eachOuterName : bigraph_a.getOuterNames()) {
            Collection<BigraphEntity> pointsFromLink = bigraph_a.getPointsFromLink(eachOuterName);
            Node grOuter = node(labelSupplier.with(eachOuterName).get())
                    .with(shapeSupplier.with(eachOuterName).get(), colorSupplier.with(eachOuterName).get());
            for (BigraphEntity each : pointsFromLink) {
                switch (each.getType()) {
                    case PORT:
                        BigraphEntity.NodeEntity<DefaultDynamicControl> nodeOfPort = bigraph_a.getNodeOfPort((BigraphEntity.Port) each);
                        Node grNode = node(labelSupplier.with(nodeOfPort).get());
                        theGraph.add(grNode.link(Link.to(grOuter))); //.with(Style.BOLD, Label.of(""), Color.GREEN, Arrow.NONE)));
                        break;
                    case INNER_NAME:
                        Node grInner = node(labelSupplier.with(each).get()); //.with(Shape.RECTANGLE, Color.BLUE);
                        theGraph.add(grInner.link(Link.to(grOuter))); //.with(Style.BOLD, Label.of(""), Color.GREEN, Arrow.NONE)));
                        break;
                }
            }
        }


        //TODO: directly connect nodes together if ports == 1
        for (BigraphEntity.Edge eachEdge : edges) {
            // edges for graphviz are created as "hidden nodes"
            Node hiddenEdgeNode = node(labelSupplier.with(eachEdge).get()).with(Shape.POINT);
            //find the nodes that are connected to it
            Collection<BigraphEntity> pointsFromLink = bigraph_a.getPointsFromLink(eachEdge);
            if (pointsFromLink.size() != 0) {
                //if inner name: create new node
                for (BigraphEntity eachPointNode : pointsFromLink) {
                    switch (eachPointNode.getType()) {
                        case INNER_NAME:
                            Node grInner = node(labelSupplier.with(eachPointNode).get());
                            theGraph.add(hiddenEdgeNode.link(
                                    Link.to(grInner)));
//                                            .with(Style.BOLD, Label.of(""), Color.GREEN, Arrow.NONE)
//                            ));
                            break;
                        case PORT:
                            BigraphEntity.NodeEntity<DefaultDynamicControl> nodeOfPort = bigraph_a.getNodeOfPort((BigraphEntity.Port) eachPointNode);
                            Node grNode = node(labelSupplier.with(nodeOfPort).get());
                            theGraph.add(grNode.link(Link.to(hiddenEdgeNode))); //.with(Style.BOLD, Label.of(""), Color.GREEN, Arrow.NONE)));
                            break;
                    }
                }
            }
        }



        ;
        System.out.println(theGraph.toString());
        Graphviz.fromGraph(theGraph).render(Format.PNG).toFile(new File("test/resources/graphviz/ex13.png"));
    }

    //TODO move into bigraph
    int getNodeHeight(Bigraph<?> bigraph, BigraphEntity data, int level) {
        BigraphEntity parent = bigraph.getParent(data);
        if (data == null || parent == null) //|| BigraphEntityType.isRoot(parent))
            return level;
        if (BigraphEntityType.isRoot(parent) && level == 0) {
            return 1;
        }
        return getNodeHeight(bigraph, parent, level + 1);
    }

    @org.junit.jupiter.api.Test
    void place_graph_export() throws LinkTypeNotExistsException, InvalidConnectionException, IOException {
        DynamicEcoreBigraph bigraph_a = createBigraph_A();
        List<BigraphEntity.NodeEntity<DefaultDynamicControl>> computerNodes = bigraph_a
                .getNodes()
                .stream()
                .filter(x -> x.getControl().equals(bigraph_a.getSignature().getControlByName("Computer")))
                .collect(Collectors.toList());

//        for (BigraphEntity.NodeEntity a : computerNodes) {
//            for (BigraphEntity.NodeEntity b : computerNodes) {
//                boolean b1 = bigraph_a.areConnected(a, b);
//                System.out.println("are connected=" + b1);
//            }
//        }

//        List<BigraphEntity.NodeEntity> topLevel = new ArrayList<>();

        List<MutableGraph> graphList = new ArrayList<>();
        AtomicInteger c = null;
        Traverser<BigraphEntity> stringTraverser = Traverser.forTree(x -> {
            List<BigraphEntity> childrenOf = new ArrayList<>(bigraph_a.getChildrenOf(x));
//            if (childrenOf.size() == 0) return childrenOf;

            for (BigraphEntity eachNode : childrenOf) {
                MutableGraph g = mutGraph("example2").setDirected(true);
                g.use((gr, ctc) -> {
                    String namex = "root";
                    if (!x.getType().equals(BigraphEntityType.ROOT)) {
                        namex = x.getControl().getNamedType().stringValue() + "." + ((BigraphEntity.NodeEntity) x).getName();
                    }
                    String name = eachNode.getControl().getNamedType().stringValue() + "." + ((BigraphEntity.NodeEntity) eachNode).getName();
                    System.out.println(namex + "->" + name);
//                    mutNode(name);
//                    mutNode(namex);
                    mutNode(namex).addLink(mutNode(name));
//                    mutNode(namex).addTo(g);
//                    mutNode(name).addTo(g);
                });
                graphList.add(g);
//                    g.add(mutNode(x.getControl().getNamedType().stringValue())).addLink(
//                            mutNode(eachNode.getControl().getNamedType().stringValue())
//                    );
            }

            return childrenOf;
        });


//        for (MutableGraph g : graphList)
//        g.use((gr, ctc) -> {
//            mutNode("root").linkTo(mutNode("Room.v4")); //.linkTo(mutNode("Room.v0"));
//
//        });
//            Node root = node("main").with(Label.html("<b>main</b><br/>start"), Color.rgb("1020d0").font());
        Iterable<BigraphEntity> v0 = stringTraverser.depthFirstPostOrder(bigraph_a.getRoots().iterator().next());
        List<BigraphEntity> iterator = Lists.newArrayList(v0).stream().filter(x -> x.getType().equals(BigraphEntityType.NODE)).collect(Collectors.toList());


        List<MutableGraph> graphList2 = new ArrayList<>();
        for (BigraphEntity a : iterator) {
            for (BigraphEntity b : iterator) {
//                if(a.getType())
                String an = a.getControl().getNamedType().stringValue() + "." + ((BigraphEntity.NodeEntity) a).getName();
                if (bigraph_a.areConnected((BigraphEntity.NodeEntity) a, (BigraphEntity.NodeEntity) b)) {


                    String bn = b.getControl().getNamedType().stringValue() + "." + ((BigraphEntity.NodeEntity) b).getName();

                    MutableGraph g2 = mutGraph().setDirected(true).use((mutableGraph, creationContext) -> {
//                        node(an).link(to(node(bn)).with(Color.RED));
                        mutNode(an).linkTo(mutNode(bn)).with(Color.RED);
                    });
//                    MutableGraph g = mutGraph().setDirected(true);
//                    MutableGraph g2 = g.use((gr, ctx) -> {
//                        mutNode(an).linkTo(mutNode(bn));
//
//                    });
                    graphList.add(g2);
                }
            }
        }

        MutableGraph g = graphList.stream().reduce((mutableGraph, mutableGraph2) -> {
            mutableGraph.addTo(mutableGraph2);
            return mutableGraph2;
        }).get();

//        MutableGraph mutableGraph1 = graphList2.stream().reduce((mutableGraph, mutableGraph2) -> {
//            mutableGraph.addTo(mutableGraph2);
//            return mutableGraph2;
//        }).get();

//        MutableGraph g = mutGraph("example1").setDirected(true).use((gr, ctx) -> {
//            mutNode("b");
//            nodeAttrs().add(Color.RED);
//            mutNode("a").addLink(mutNode("b"));
//        });
        Graphviz.fromGraph(g).height(800).render(Format.PNG).toFile(new File("test/resources/graphviz/ex12.png"));
//        Graphviz.fromGraph(mutableGraph1).height(800).render(Format.PNG).toFile(new File("test/resources/graphviz/ex12.png"));


    }

    public DynamicEcoreBigraph createBigraph_A() throws
            LinkTypeNotExistsException, InvalidConnectionException, IOException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        BigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        BigraphEntity.InnerName roomLink = builder.createInnerName("tmp1_room");
        BigraphEntity.OuterName a = builder.createOuterName("a");
        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
//        BigraphEntity.OuterName b2 = builder.createOuterName("b2");
        BigraphEntity.OuterName jeff = builder.createOuterName("jeff");
        BigraphEntity.OuterName jeff2 = builder.createOuterName("jeff2");

        builder.createRoot()
                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(roomLink)
                .withNewHierarchy()
                .addSite()
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1)
                .withNewHierarchy()
                .addChild(signature.getControlByName("Job"))
                .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff2)
                .goBack()
                .goBack()

                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(roomLink)
                .withNewHierarchy()
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1)
                .withNewHierarchy()
                .addSite()
                .addChild(signature.getControlByName("Job"))
                .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff2)
                .goBack()
                .goBack()

//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(a)
        ;
//        builder.closeAllInnerNames();
//        builder.makeGround();

        DynamicEcoreBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

    public DynamicEcoreBigraph createBigraph_b() throws
            LinkTypeNotExistsException, InvalidConnectionException, IOException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        BigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        BigraphEntity.InnerName roomLink = builder.createInnerName("tmp1_room");
        BigraphEntity.InnerName spoolLink = builder.createInnerName("tmp_spoolLink");
        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
        BigraphEntity.OuterName jeff2 = builder.createOuterName("jeff2");

        builder.createRoot()
                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(roomLink)
                .withNewHierarchy()
                .addChild(signature.getControlByName("Printer")).connectNodeToInnerName(spoolLink).connectNodeToOuterName(b1)
                .withNewHierarchy()
                .addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job"))
                .goBack()
                .goBack()

                .addChild(signature.getControlByName("Room")).connectNodeToInnerName(roomLink)
                .withNewHierarchy()
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1)
                .withNewHierarchy().addSite().addChild(signature.getControlByName("Spool")).connectNodeToInnerName(spoolLink)
                .goBack()
                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(b1)
                .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff2)
                .goBack()
        ;
        builder.closeInnerName(roomLink);
        builder.closeInnerName(spoolLink);
//        builder.closeAllInnerNames();
//        builder.makeGround();

        DynamicEcoreBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

    private <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>> defaultBuilder = factory.createSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Spool")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign();

        return (S) defaultBuilder.create();
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
