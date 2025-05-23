package org.bigraphs.framework.visualization;

import org.bigraphs.framework.core.*;
import org.bigraphs.framework.core.alg.generators.PureBigraphGenerator;
import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.builder.LinkTypeNotExistsException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.elementary.DiscreteIon;
import org.bigraphs.framework.core.impl.elementary.Linkings;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.visualization.supplier.GraphvizColorSupplier;
import org.bigraphs.framework.visualization.supplier.GraphvizShapeSupplier;
import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.MutableGraph;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static guru.nidi.graphviz.model.Factory.*;

public class PNGVisualizationUnitTests {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/graphviz/";

    @BeforeAll
    static void setUp() {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
    }

    @Test
    void random_bigraph() throws IOException {
        DemoBigraphProvider provider = new DemoBigraphProvider();
        PureBigraphGenerator pureBigraphGenerator = pureRandomBuilder(provider.createAlphabetSignature());
        PureBigraph randomBigraph = pureBigraphGenerator.generate(10, 100, 0.5f);
        BigraphGraphvizExporter.toPNG(randomBigraph, false, new File(TARGET_DUMP_PATH + "random_output.png"));
    }

    @Test
    void thesis_example() throws IncompatibleSignatureException, IncompatibleInterfaceException, IOException {
        DefaultDynamicSignature sig = pureSignatureBuilder()
                .addControl("K", 1, ControlStatus.ATOMIC)
                .addControl("L", 1, ControlStatus.ATOMIC)
                .create();
        pureBuilder(sig)
                .createRoot()
                .addChild("K");
        DiscreteIon<DefaultDynamicSignature> K_x = pureDiscreteIon(sig, "K", "x");
        DiscreteIon<DefaultDynamicSignature> L_x = pureDiscreteIon(sig, "L", "x");
        Linkings<DefaultDynamicSignature>.Closure x = pureLinkings(sig).closure("x");
        BigraphComposite<DefaultDynamicSignature> G = ops(x).compose(ops(K_x).merge(L_x));
//        BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) G.getOuterBigraph(), System.out);
        BigraphGraphvizExporter.toPNG(G.getOuterBigraph(), true, new File(TARGET_DUMP_PATH + "thesis-tree-variant.png"));
        BigraphGraphvizExporter.toPNG(G.getOuterBigraph(), false, new File(TARGET_DUMP_PATH + "thesis-container-variant.png"));
    }

    @Test
    void simple_bigraph_visualization_test() throws TypeNotExistsException, InvalidConnectionException, IOException {
//        PureBigraph bigraph_a = createBigraph_b();

        final GraphicalFeatureSupplier<String> labelSupplier = new DefaultLabelSupplier();
        final GraphicalFeatureSupplier<Shape> shapeSupplier = new GraphvizShapeSupplier();
        final GraphicalFeatureSupplier<Color> colorSupplier = new GraphvizColorSupplier();

        String convert = BigraphGraphvizExporter.toPNG(createSimpleBigraphHierarchy(),
                true,
                new File(TARGET_DUMP_PATH + "ex_simple_tree.png")
        );
        System.out.println(convert);
        String convert2 = BigraphGraphvizExporter.toPNG(createSimpleBigraphHierarchy(),
                false,
                new File(TARGET_DUMP_PATH + "ex_simple_nesting.png")
        );
        System.out.println(convert2);
        BigraphGraphvizExporter.toPNG(createBigraph_A(), true, new File(TARGET_DUMP_PATH + "ex_A_tree.png"));
        BigraphGraphvizExporter.toPNG(createBigraph_A(), false, new File(TARGET_DUMP_PATH + "ex_A_nesting.png"));
        BigraphGraphvizExporter.toPNG(createBigraph_b(), true, new File(TARGET_DUMP_PATH + "ex_b_tree.png"));
        BigraphGraphvizExporter.toPNG(createBigraph_b(), false, new File(TARGET_DUMP_PATH + "ex_b_nesting.png"));

        BigraphGraphvizExporter.toPNG(bigraphWithTwoRoots(), true, new File(TARGET_DUMP_PATH + "ex_2roots_tree.png"));
        BigraphGraphvizExporter.toPNG(bigraphWithTwoRoots(), false, new File(TARGET_DUMP_PATH + "ex_2roots_nesting.png"));
    }


    @Test
    @DisplayName("Creating containment structure using graphviz-java directly")
    void graphviz_hierarchy_test() throws LinkTypeNotExistsException, InvalidConnectionException, IOException {
        PureBigraph simpleBigraphHierarchy = createSimpleBigraphHierarchy();

        final GraphicalFeatureSupplier<String> labelSupplier = new DefaultLabelSupplier();


        List<MutableGraph> rootGraphs = new LinkedList<>();
        for (BigraphEntity.RootEntity eachRoot : simpleBigraphHierarchy.getRoots()) {
            MutableGraph graphRoot = mutGraph("root")
                    .setDirected(false)
                    .setCluster(true)
                    .graphAttrs()
//                    .add(Rank.dir(Rank.RankDir.BOTTOM_TO_TOP), Label.of("root"), Style.DASHED);
                    .add(Rank.dir(Rank.RankDir.BOTTOM_TO_TOP), Label.of("root"), Style.ROUNDED);
            MutableGraph graph1 = makeHierarchyCluster(simpleBigraphHierarchy, eachRoot, graphRoot, labelSupplier);
            rootGraphs.add(graph1);
        }

        Graph mega = graph("mega").with(rootGraphs);

        System.out.println(mega.toString());
        Graphviz.fromGraph(mega).height(800).render(Format.PNG).toFile(new File(TARGET_DUMP_PATH + "ex_hierarchy.png"));
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
//                        .graphAttrs().add(Label.of(labelSupplier.with(each).get()), Rank.dir(Rank.RankDir.BOTTOM_TO_TOP), Style.BOLD);
                        .graphAttrs().add(Label.of(labelSupplier.with(each).get()), Rank.dir(Rank.RankDir.BOTTOM_TO_TOP), Style.ROUNDED);
                MutableGraph mutableGraph = makeHierarchyCluster(bigraph, each, cluster, labelSupplier);
                graphList.add(mutableGraph);
            }
        }
        graphList.forEach(currentParent::add);
        return currentParent;
    }

    private PureBigraph createSimpleBigraphHierarchy() {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        builder.createRoot().addChild(signature.getControlByName("Room"))
                .down()
                .addChild(signature.getControlByName("User")).addChild(signature.getControlByName("Computer"))
                .down()
                .addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job"))
                .up()
                .up()
        ;

        return builder.createBigraph();
    }

    public PureBigraph bigraphWithTwoRoots() throws InvalidConnectionException, TypeNotExistsException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.OuterName network = builder.createOuterName("network");
        builder.createRoot().addChild(signature.getControlByName("Room"))
                .down()
                .addChild(signature.getControlByName("User")).addChild(signature.getControlByName("Computer")).linkToOuter(network)
                .down()
                .addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job"))
        ;

        builder.createRoot().addChild(signature.getControlByName("Room"))
                .down()
                .addChild(signature.getControlByName("User")).addChild(signature.getControlByName("Computer")).linkToOuter(network)
                .down()
                .addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job"))
        ;

        return builder.createBigraph();
    }

    public PureBigraph createBigraph_A() throws
            TypeNotExistsException, InvalidConnectionException, IOException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        BigraphEntity.InnerName roomLink = builder.createInnerName("tmp1_room");
        BigraphEntity.OuterName a = builder.createOuterName("a");
        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
        BigraphEntity.OuterName b2 = builder.createOuterName("b2");
        BigraphEntity.InnerName jeff = builder.createInnerName("jeff_inner");
        BigraphEntity.OuterName jeff2 = builder.createOuterName("jeff2");

        builder.createRoot()
                .addChild(signature.getControlByName("Room")).linkToInner(roomLink)
                .down()
                .addSite()
                .addChild(signature.getControlByName("Computer")).linkToOuter(b1)
                .down()
                .addChild(signature.getControlByName("Job"))
                .addChild(signature.getControlByName("User")).linkToOuter(jeff2)
                .up()
                .up()

                .addChild(signature.getControlByName("Room")).linkToInner(roomLink)
                .down()
                .addChild(signature.getControlByName("Computer")).linkToOuter(b1)
                .down()
                .addSite()
                .addChild(signature.getControlByName("Job"))
                .addChild(signature.getControlByName("User")).linkToOuter(jeff2)
                .up()
                .up()
        ;
        builder.connectInnerToOuterName(jeff, b2);
//        builder.closeAllInnerNames();
//        builder.makeGround();

        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

    public PureBigraph createBigraph_b() throws
            TypeNotExistsException, InvalidConnectionException, IOException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);

        BigraphEntity.InnerName roomLink = builder.createInnerName("tmp1_room");
        BigraphEntity.InnerName spoolLink = builder.createInnerName("tmp_spoolLink");
        BigraphEntity.OuterName b1 = builder.createOuterName("b1");
        BigraphEntity.OuterName jeff2 = builder.createOuterName("jeff2");

        builder.createRoot()
                .addChild(signature.getControlByName("Room")).linkToInner(roomLink)
                .down()
                .addChild(signature.getControlByName("Printer")).linkToInner(spoolLink).linkToOuter(b1)
                .down()
                .addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job")).addChild(signature.getControlByName("Job"))
                .up()
                .up()

                .addChild(signature.getControlByName("Room")).linkToInner(roomLink)
                .down()
                .addChild(signature.getControlByName("Computer")).linkToOuter(b1)
                .down().addSite().addChild(signature.getControlByName("Spool")).linkToInner(spoolLink)
                .up()
                .addChild(signature.getControlByName("Computer")).linkToOuter(b1)
                .addChild(signature.getControlByName("User")).linkToOuter(jeff2)
                .up()
        ;
        builder.closeInnerName(roomLink);
        builder.closeInnerName(spoolLink);
//        builder.closeAllInnerNames();
//        builder.makeGround();

        PureBigraph bigraph = builder.createBigraph();
        return bigraph;
    }

    private <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder defaultBuilder = pureSignatureBuilder();
        defaultBuilder
                .addControl("Printer",2)
                .addControl("User", 1)
                .addControl("Room", 1)
                .addControl("Spool", 1)
                .addControl("Computer", 1)
                .addControl("Job", 0);

        return (S) defaultBuilder.create();
    }
}
