package de.tudresden.inf.st.bigraphs.visualization;

import com.google.common.collect.Lists;
import com.google.common.graph.Traverser;
import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.building.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DefaultSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.DynamicEcoreBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.factory.SimpleBigraphFactory;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.model.Factory.*;

public class Test {
    private SimpleBigraphFactory<StringTypedName, FiniteOrdinal<Integer>> factory = new SimpleBigraphFactory<>();

    @org.junit.jupiter.api.Test
    void place_graph_export() throws LinkTypeNotExistsException, InvalidConnectionException, IOException {
        DynamicEcoreBigraph bigraph_a = createBigraph_A();
        List<BigraphEntity.NodeEntity<DefaultDynamicControl>> computerNodes = bigraph_a.getNodes().stream().filter(x -> {
            return x.getControl().equals(bigraph_a.getSignature().getControlByName("Computer"));
        }).collect(Collectors.toList());

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
                .addChild(signature.getControlByName("Job"))
                .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff2)
                .goBack()
                .goBack()

//                .addChild(signature.getControlByName("Computer")).connectNodeToOuterName(a)
        ;
        builder.closeAllInnerNames();
        builder.makeGround();

        DynamicEcoreBigraph bigraph = builder.createBigraph();
        return bigraph;

    }

    private <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DefaultSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>> defaultBuilder = factory.createSignatureBuilder();
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
