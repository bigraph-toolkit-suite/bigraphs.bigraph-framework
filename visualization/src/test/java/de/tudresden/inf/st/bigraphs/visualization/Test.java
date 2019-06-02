package de.tudresden.inf.st.bigraphs.visualization;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidArityOfControlException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.building.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;
import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.MutableGraph;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static guru.nidi.graphviz.model.Factory.*;

public class Test {
    private PureBigraphFactory<StringTypedName, FiniteOrdinal<Integer>> factory = AbstractBigraphFactory.createPureBigraphFactory();

    @org.junit.jupiter.api.Test
    void placegraph_export2() throws LinkTypeNotExistsException, InvalidConnectionException, IOException {
//        PureBigraph bigraph_a = createBigraph_b();

        final GraphicalFeatureSupplier<String> labelSupplier = new DefaultLabelSupplier();
        final GraphicalFeatureSupplier<Shape> shapeSupplier = new DefaultShapeSupplier();
        final GraphicalFeatureSupplier<Color> colorSupplier = new DefaultColorSupplier();

        String convert = GraphvizConverter.toPNG(createSimpleBigraphHierarchy(),
                true,
                new File("src/test/resources/graphviz/ex_simple_tree.png")
        );
        System.out.println(convert);
        String convert2 = GraphvizConverter.toPNG(createSimpleBigraphHierarchy(),
                false,
                new File("src/test/resources/graphviz/ex_simple_nesting.png")
        );
        System.out.println(convert2);
        GraphvizConverter.toPNG(createBigraph_A(), true, new File("src/test/resources/graphviz/ex_A_tree.png"));
        GraphvizConverter.toPNG(createBigraph_A(), false, new File("src/test/resources/graphviz/ex_A_nesting.png"));
        GraphvizConverter.toPNG(createBigraph_b(), true, new File("src/test/resources/graphviz/ex_b_tree.png"));
        GraphvizConverter.toPNG(createBigraph_b(), false, new File("src/test/resources/graphviz/ex_b_nesting.png"));

        GraphvizConverter.toPNG(bigraphWithTwoRoots(), true, new File("src/test/resources/graphviz/ex_2roots_tree.png"));
        GraphvizConverter.toPNG(bigraphWithTwoRoots(), false, new File("src/test/resources/graphviz/ex_2roots_nesting.png"));
    }

    private MutableGraph makeHierarchyCluster(Bigraph bigraph, BigraphEntity nodeEntity, MutableGraph currentParent, GraphicalFeatureSupplier<String> labelSupplier) {
        //if children: iterate and check
        List<BigraphEntity> childrenOf = new ArrayList<>(bigraph.getChildrenOf(nodeEntity));

        List<MutableGraph> graphList = new LinkedList<>();
        for (BigraphEntity each : childrenOf) {
            Collection childrenOfEach = bigraph.getChildrenOf(each);
            //create a child
            if (childrenOfEach.size() == 0) {
                currentParent.add(node(labelSupplier.with(each).get()));
            } else { //create a new hierarchy graph
                MutableGraph cluster = mutGraph(labelSupplier.with(each).get()).setCluster(true)
                        .setDirected(true)
                        .graphAttrs().add(Label.of(labelSupplier.with(each).get()), RankDir.BOTTOM_TO_TOP, Style.BOLD);
                MutableGraph mutableGraph = makeHierarchyCluster(bigraph, each, cluster, labelSupplier);
                graphList.add(mutableGraph);
            }
        }
        graphList.forEach(currentParent::add);
        return currentParent;
    }

    @org.junit.jupiter.api.Test
    void graphviz_hierarchy_test() throws LinkTypeNotExistsException, InvalidConnectionException, IOException {
        PureBigraph simpleBigraphHierarchy = createSimpleBigraphHierarchy();

        final GraphicalFeatureSupplier<String> labelSupplier = new DefaultLabelSupplier();


        List<MutableGraph> rootGraphs = new LinkedList<>();
        for (BigraphEntity.RootEntity eachRoot : simpleBigraphHierarchy.getRoots()) {
            MutableGraph graphRoot = mutGraph("root")
                    .setDirected(false)
                    .setCluster(true)
                    .graphAttrs()
                    .add(RankDir.BOTTOM_TO_TOP, Label.of("root"), Style.DASHED);
            MutableGraph graph1 = makeHierarchyCluster(simpleBigraphHierarchy, eachRoot, graphRoot, labelSupplier);
            rootGraphs.add(graph1);
        }

        Graph mega = graph("mega").with(rootGraphs);

        System.out.println(mega.toString());
        Graphviz.fromGraph(mega).height(800).render(Format.PNG).toFile(new File("src/test/resources/graphviz/ex_hierarchy.png"));
    }

    public PureBigraph createSimpleBigraphHierarchy() {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        builder.createRoot().addChild(signature.getControlByName("Room"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("User")).addChild(signature.getControlByName("Computer"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job"))
                .goBack()
                .goBack()
        ;

        return builder.createBigraph();
    }

    public PureBigraph bigraphWithTwoRoots() throws InvalidArityOfControlException, LinkTypeNotExistsException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuterName("network");
        builder.createRoot().addChild(signature.getControlByName("Room"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("User")).addChild(signature.getControlByName("Computer")).connectNodeToOuterName(network)
                .withNewHierarchy()
                .addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job"))
        ;

        builder.createRoot().addChild(signature.getControlByName("Room"))
                .withNewHierarchy()
                .addChild(signature.getControlByName("User")).addChild(signature.getControlByName("Computer")).connectNodeToOuterName(network)
                .withNewHierarchy()
                .addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job"))
        ;

        return builder.createBigraph();
    }

    public PureBigraph createBigraph_A() throws
            LinkTypeNotExistsException, InvalidConnectionException, IOException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

        BigraphEntity.InnerName roomLink = builder.createInnerName("tmp1_room");
        BigraphEntity.OuterName a = builder.createOuterName("a");
        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
        BigraphEntity.OuterName b2 = builder.createOuterName("b2");
        BigraphEntity.InnerName jeff = builder.createInnerName("jeff_inner");
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
        ;
        builder.connectInnerToOuterName(jeff, b2);
//        builder.closeAllInnerNames();
//        builder.makeGround();

        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

    public PureBigraph createBigraph_b() throws
            LinkTypeNotExistsException, InvalidConnectionException, IOException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);

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

        PureBigraph bigraph = builder.createBigraph();
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
}
