package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.*;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultControl;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DefaultSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.EcoreBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.DefaultEcoreBigraph;
import org.junit.jupiter.api.*;

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
            EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>>.EcoreOuterName jeff = builder.createOuterName("jeff");
            ArityMismatch am = Assertions.assertThrows(ArityMismatch.class, () -> {
                DefaultControl<StringTypedName, FiniteOrdinal<Integer>> selected = getControlByName("Job", signature);
                assert selected != null;
                System.out.println("Node of control will be added: " + selected);
                builder.addChild(selected);
                EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>>.EcoreNode lastCreatedNode
                        = builder.getLastCreatedNode();
                builder.connectNodeToOuterName(lastCreatedNode, jeff);
            });
        }

        @Test
        @DisplayName("Connect a node to the same outername twice and then add another where the control's arity is 1")
        void connect_to_outername_2() {
            EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>>.EcoreOuterName jeff = builder.createOuterName("jeff");
            // exceeding a node's ports w.r.t to the corresponding control's arity
            ArityMismatch am2 = Assertions.assertThrows(ArityMismatch.class, () -> {
                DefaultControl<StringTypedName, FiniteOrdinal<Integer>> selected = getControlByName("Computer", signature);
                builder.addChild(selected);
                EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>>.EcoreNode lastCreatedNode
                        = builder.getLastCreatedNode();
                assert selected != null;
                builder.connectNodeToOuterName(lastCreatedNode, jeff);
                builder.connectNodeToOuterName(lastCreatedNode, jeff);
                EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>>.EcoreOuterName bob = builder.createOuterName("bob");
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
    class SiteCreation {
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
            EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>>.EcoreNode nodeLeft = builder.createChild(signature.getControlByName("Computer"));
            EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>>.EcoreNode nodeRight = builder.createChild(signature.getControlByName("Printer"));

            ToManyConnections exc = Assertions.assertThrows(ToManyConnections.class, () -> {
                builder.connectByEdge(nodeLeft, nodeRight);
                Assertions.assertTrue(builder.areNodesConnected(nodeLeft, nodeRight));
                builder.connectByEdge(nodeLeft, nodeRight);
            });
            System.out.println("Error: " + exc.getMessage());
        }

        @Test
        void connect_to_inner_name() throws ArityMismatch, InvalidConnectionException, LinkTypeNotExistsException {
            EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>>.EcoreNode nodeLeft = builder.createChild(signature.getControlByName("Printer"));
            EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>>.EcoreNode nodeRight = builder.createChild(signature.getControlByName("Printer"));

            EcoreBigraphBuilder.EcoreInnerName x = builder.createInnerName("x");
            EcoreBigraphBuilder.EcoreInnerName y = builder.createInnerName("y");

//            boolean b = builder.areInnerNamesConnectedByEdge(x, y);
//            System.out.println(b);

            try {
                builder.connectInnerNames(x, y);
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }

            EcoreBigraphBuilder.EcoreNode printer = builder.createChild(signature.getControlByName("Printer"));
            builder.connectNodeToInnerName(printer, x);
            builder.connectNodeToInnerName(printer, y);

            EcoreBigraphBuilder.EcoreNode freshPrinter = builder.createChild(signature.getControlByName("Printer"));
            EcoreBigraphBuilder.EcoreInnerName z = builder.createInnerName("z");
            builder.connectNodeToInnerName(freshPrinter, z);
            builder.connectNodeToInnerName(freshPrinter, z);


            EcoreBigraphBuilder.EcoreOuterName jeff = builder.createOuterName("jeff");
            builder.connectNodeToOuterName(freshPrinter, jeff);
            EcoreBigraphBuilder.EcoreInnerName z2 = builder.createInnerName("z2");
            builder.connectInnerToOuterName(z2, jeff);
            builder.connectInnerToOuterName(z2, jeff);

            builder.connectNodeToInnerName(freshPrinter, z2);

        }

        @Test
        @Disabled
        void createSites() {
            builder.createSite().createSite();
        }
    }

    @Test
    void write_to_dot() {
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
        builder.createRoot().addChild(signature.getControlByName("Computer"));
        builder.createRoot().addChild(signature.getControlByName("User"));

        DefaultEcoreBigraph bigraph = builder.createBigraph();

        builder.WRITE_DEBUG();
//        EcoreBigraphBuilder.EcoreRoot root = (EcoreBigraphBuilder.EcoreRoot) bigraph.getRoot();
//        if(root instanceof EcoreBigraphBuilder.EcoreRoot) {
//            System.out.println("nice");
//        }

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
