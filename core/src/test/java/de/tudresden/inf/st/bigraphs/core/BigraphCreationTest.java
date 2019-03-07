package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.*;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultControl;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DefaultSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.EcoreBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.DynamicEcoreBigraph;
import de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.BInnerName;
import de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.BOuterName;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BigraphCreationTest {

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("Create Bigraphs test series")
    class ArityChecks {
        EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> builder;
        Signature<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> signature;

        @BeforeAll
        void createSignature() {
            signature = createExampleSignature();
            builder = EcoreBigraphBuilder.start(signature); //TODO factory methode casted sowas dann
            // define some nodes
            builder.createRoot(); // create a root node first
        }

        @Test
        @DisplayName("Connect a node to an outername where the control's arity is 0")
        void connect_to_outername_1() {
            EcoreBigraphBuilder.BigraphEntity jeff = builder.createOuterName("jeff");
            ArityMismatch am = Assertions.assertThrows(ArityMismatch.class, () -> {
                DefaultControl<StringTypedName, FiniteOrdinal<Integer>> selected = signature.getControlByName("Job"); //getControlByName("Job", signature);
                assert selected != null;
                System.out.println("Node of control will be added: " + selected);
                builder.addChild(selected);
                EcoreBigraphBuilder.BigraphEntity lastCreatedNode = builder.getLastCreatedNode();
                builder.connectNodeToOuterName(lastCreatedNode, jeff);
            });
        }

        @Test
        @DisplayName("Connect a node to the same outername twice and then add another where the control's arity is 1")
        void connect_to_outername_2() {
            EcoreBigraphBuilder.BigraphEntity jeff = builder.createOuterName("jeff");
            // exceeding a node's ports w.r.t to the corresponding control's arity
            ArityMismatch am2 = Assertions.assertThrows(ArityMismatch.class, () -> {
                DefaultControl<StringTypedName, FiniteOrdinal<Integer>> selected = getControlByName("Computer", signature);
                builder.addChild(selected);
                EcoreBigraphBuilder.BigraphEntity lastCreatedNode
                        = builder.getLastCreatedNode();
                assert selected != null;
                builder.connectNodeToOuterName(lastCreatedNode, jeff);
                builder.connectNodeToOuterName(lastCreatedNode, jeff);
                EcoreBigraphBuilder.BigraphEntity bob = builder.createOuterName("bob");
                builder.connectNodeToOuterName(lastCreatedNode, bob);
            });
        }

        @AfterAll
        void write_debug() {
            builder.WRITE_DEBUG();
        }
    }


    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ConnectionTestSeries {
        EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> builder;
        Signature<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> signature;

        @BeforeAll
        void createSignature() {
            signature = createExampleSignature();
            builder = EcoreBigraphBuilder.start(signature); //TODO factory methode casted sowas dann
            // define some nodes
            builder.createRoot(); // create a root node first
        }

        @Test
        void to_many_connections() {
            EcoreBigraphBuilder.BigraphEntity nodeLeft = builder.createChild(signature.getControlByName("Computer"));
            EcoreBigraphBuilder.BigraphEntity nodeRight = builder.createChild(signature.getControlByName("Printer"));

            ToManyConnections exc = Assertions.assertThrows(ToManyConnections.class, () -> {
                builder.connectByEdge(nodeLeft, nodeRight);
                Assertions.assertTrue(builder.areNodesConnected(nodeLeft, nodeRight));
                builder.connectByEdge(nodeLeft, nodeRight);
            });
            System.out.println("Error: " + exc.getMessage());
        }

        @Test
        void connect_to_inner_name() throws ArityMismatch, InvalidConnectionException, LinkTypeNotExistsException {
            EcoreBigraphBuilder.BigraphEntity nodeLeft = builder.createChild(signature.getControlByName("Printer"));
            EcoreBigraphBuilder.BigraphEntity nodeRight = builder.createChild(signature.getControlByName("Printer"));

            EcoreBigraphBuilder.BigraphEntity x = builder.createInnerName("x");
            EcoreBigraphBuilder.BigraphEntity y = builder.createInnerName("y");

            try {
                builder.connectInnerNames(x, y);
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }

            EcoreBigraphBuilder.BigraphEntity printer = builder.createChild(signature.getControlByName("Printer"));
            builder.connectNodeToInnerName(printer, x);
            builder.connectNodeToInnerName(printer, y);

            EcoreBigraphBuilder.BigraphEntity freshPrinter = builder.createChild(signature.getControlByName("Printer"));
            EcoreBigraphBuilder.BigraphEntity z = builder.createInnerName("z");
            builder.connectNodeToInnerName(freshPrinter, z);
            builder.connectNodeToInnerName(freshPrinter, z);


            EcoreBigraphBuilder.BigraphEntity jeff = builder.createOuterName("jeff");
            builder.connectNodeToOuterName(freshPrinter, jeff);
            EcoreBigraphBuilder.BigraphEntity z2 = builder.createInnerName("z2");
            builder.connectInnerToOuterName(z2, jeff);
            builder.connectInnerToOuterName(z2, jeff);

            Assertions.assertThrows(ToManyConnections.class, () -> {
                builder.connectNodeToInnerName(freshPrinter, z2);
            });
        }

//        @Test
//        @Disabled
//        void createSites() {
//            builder.createSite().createSite();
//        }
    }

    @Test
    void write_to_dot() throws IOException {
        //This belongs in the visu module
        EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> builder;
        Signature<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> signature;
        signature = createExampleSignature();
        builder = EcoreBigraphBuilder.start(signature); //TODO factory methode casted sowas dann
        // define some nodes
        builder.createRoot(); // create a root node first
        builder.addChild(signature.getControlByName("Printer"))
                .addChild(signature.getControlByName("Printer"))
                .addChild(signature.getControlByName("Printer"));
//        builder.connectByEdge();
        builder.createRoot().addChild(signature.getControlByName("Computer"));
        builder.createRoot().addChild(signature.getControlByName("User"));

        DynamicEcoreBigraph bigraph = builder.createBigraph();

        builder.WRITE_DEBUG();
//        EcoreBigraphBuilder.EcoreRoot root = (EcoreBigraphBuilder.EcoreRoot) bigraph.getRoot();
//        if(root instanceof EcoreBigraphBuilder.EcoreRoot) {
//            System.out.println("nice");
//        }

        MutableGraph g = Parser.read(getClass().getResourceAsStream("/color.dot"));
        Graphviz.fromGraph(g).width(700).render(Format.PNG).toFile(new File("example/ex4-1.png"));

        g.graphAttrs()
                .add(Color.WHITE.gradient(Color.rgb("888888")).background().angle(90))
                .nodeAttrs().add(Color.WHITE.fill())
                .nodes().forEach(node ->
                node.add(
                        Color.named(node.name().toString()),
                        Style.lineWidth(4).and(Style.FILLED)));
        Graphviz.fromGraph(g).width(700).render(Format.PNG).toFile(new File("example/ex4-2.png"));
    }

    @Deprecated
    private DefaultControl<StringTypedName, FiniteOrdinal<Integer>> getControlByName(String name, Signature signature) {
        DefaultControl selected = null;
        Iterator<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> iterator = signature.getControls().iterator();
        while (iterator.hasNext()) {
            DefaultControl<StringTypedName, FiniteOrdinal<Integer>> next1 = iterator.next();
            if (next1.getNamedType().stringValue().equals(name)) {
                selected = next1;
                break;
            }
        }
        return selected;
    }

    private static <C extends Control<?, ?>> Signature<C> createExampleSignature() {
        DefaultSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>> defaultBuilder = new DefaultSignatureBuilder<>();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Spool")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign();

        return (Signature<C>) defaultBuilder.create();
    }
}
