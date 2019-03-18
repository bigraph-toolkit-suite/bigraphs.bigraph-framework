package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.*;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultControl;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphArtifactHelper;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DefaultSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.EcoreBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.DynamicEcoreBigraph;
import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BigraphCreationTest {

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("Create Bigraphs test series")
    class ArityChecks {
        Signature<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> signature;
        EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> builder;

        @BeforeAll
        void createSignature() {
            signature = createExampleSignature();
            // define some nodes
//            builder.createRoot(); // create a root node first
        }

        @BeforeEach
        void setUp() {
            builder = EcoreBigraphBuilder.start(signature); //TODO factory methode casted sowas dann
        }

        @Test
        @DisplayName("Connect a node to an outername where the control's arity is 0")
        void connect_to_outername_1() {
            BigraphEntity.OuterName jeff = builder.createOuterName("jeff");
            InvalidArityOfControlException am = assertThrows(InvalidArityOfControlException.class, () -> {
                DefaultControl<StringTypedName, FiniteOrdinal<Integer>> selected = signature.getControlByName("Job");
                System.out.println("Node of control will be added: " + selected + " and connected with outer name " + jeff);
                builder.createRoot()
                        .addSite()
                        .addChild(selected)
                        .connectNodeToOuterName(jeff);
            });
            DynamicEcoreBigraph bigraph = builder.createBigraph();
            System.out.println(bigraph);
//            am.printStackTrace();
        }

        @Test
        @DisplayName("Connect a node to the same outername twice and then add another where the control's arity is 1")
        void connect_to_outername_2() {
            BigraphEntity.OuterName jeff = builder.createOuterName("jeff");
            BigraphEntity.OuterName bob = builder.createOuterName("bob");
            System.out.println("exceeding a node's ports w.r.t to the corresponding control's arity");
            InvalidArityOfControlException am2 = assertThrows(InvalidArityOfControlException.class, () -> {
                DefaultControl<StringTypedName, FiniteOrdinal<Integer>> selected = signature.getControlByName("Computer");

                builder.createRoot()
                        .addChild(selected)
                        .connectNodeToOuterName(jeff)
                        .connectNodeToOuterName(jeff)
                        .connectNodeToOuterName(bob);
            });
//            am2.printStackTrace();
        }

//        @AfterEach
//        void write_debug() {
//            assertAll(() -> builder.export());
//        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ConnectionTestSeries_InnerOuterNames {
        EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> builder;
        Signature<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> signature;

        @BeforeAll
        void createSignature() {
            signature = createExampleSignature();
        }

        @BeforeEach
        void setUp() {
            builder = EcoreBigraphBuilder.start(signature); //TODO factory methode casted sowas dann
        }

        @Test
        @Order(1)
        void to_many_connections() {
            ControlIsAtomicException exc = assertThrows(ControlIsAtomicException.class, () -> {
                builder.createRoot()
                        .connectByEdge(signature.getControlByName("Job"),
                                signature.getControlByName("Job"),
                                signature.getControlByName("Job"));
            });

            assertAll(() -> {
                builder.createRoot()
                        .connectByEdge(signature.getControlByName("Computer"),
                                signature.getControlByName("Computer"),
                                signature.getControlByName("Printer"));
            });
        }

        @Test
        @Order(2)
        void connect_to_inner_name() {
            BigraphEntity.InnerName x = builder.createInnerName("x");
            BigraphEntity.InnerName y = builder.createInnerName("y");
            BigraphEntity.InnerName z = builder.createInnerName("z");
            BigraphEntity.OuterName jeff = builder.createOuterName("jeff");

            assertAll(() -> {
                builder.connectInnerNames(x, y);
            });

            assertAll(() -> {
                builder.createRoot()
                        .addChild(signature.getControlByName("Printer"))
                        .connectNodeToInnerName(x)
                        .connectNodeToInnerName(y)

                        .addChild(signature.getControlByName("Printer"))
                        .connectNodeToInnerName(z)
                        .connectNodeToInnerName(z)
                        .connectNodeToOuterName(jeff)
                        .connectNodeToOuterName(jeff);
            });

            assertAll(() -> {
                builder.createRoot()
                        .addChild(signature.getControlByName("Printer"))
                        .connectNodeToInnerName(x)
                        .connectNodeToInnerName(y)

                        .addChild(signature.getControlByName("Printer"))
                        .connectNodeToInnerName(z)
                        .connectNodeToInnerName(z)
                        .connectNodeToOuterName(jeff)
                        .connectNodeToOuterName(jeff);
            });

            assertThrows(InvalidConnectionException.class, () -> {
                builder.connectInnerToOuterName(z, jeff);
            });
        }

        @Test
        void connect_to_inner_name_2() {
            BigraphEntity.InnerName x1 = builder.createInnerName("x1");
            BigraphEntity.InnerName x2 = builder.createInnerName("x2");
            BigraphEntity.InnerName x3 = builder.createInnerName("x3");
            BigraphEntity.OuterName jeff = builder.createOuterName("jeff");

            assertAll(() -> {
                builder.connectInnerToOuterName(x3, jeff);
            });

            assertAll(() -> {
                builder.createRoot()
                        .addChild(signature.getControlByName("Printer"))
                        .connectNodeToInnerName(x1)
                        .connectNodeToInnerName(x2)
                        .connectByEdge(signature.getControlByName("Printer"), signature.getControlByName("Printer"))
                        .connectNodeToInnerName(x1)
                        .addChild(signature.getControlByName("Printer"))
                        .connectNodeToOuterName(jeff);
                //TODO addChild(Hierarchy. instance als argument)
                //somit kann man teilhierarchien aufbauen

            });

            assertThrows(InnerNameConnectedToOuterNameException.class, () -> {
                builder.connectInnerNames(x1, x2, true);
                builder.connectInnerNames(x2, x3, false);
            });
        }

        @Test
        @DisplayName("Connects a node and an outer name to an inner name, " +
                "closes it (keeps the idle name) and connects it to a new node again")
        void closeinnerName() {
            BigraphEntity.OuterName jeff = builder.createOuterName("jeff");
            BigraphEntity.InnerName x = builder.createInnerName("x");

            assertAll(() -> {
                builder.connectInnerToOuterName(x, jeff);
                builder.createRoot()
                        .addChild(signature.getControlByName("Printer"))
                        .connectNodeToOuterName(jeff);
                builder.closeInnerName(x, true);
//                builder.closeInnerName(x);


                builder.createRoot()
                        .addChild(signature.getControlByName("Computer"))
                        .connectNodeToInnerName(x);

                builder.closeAllInnerNames();
                DynamicEcoreBigraph bigraph = builder.createBigraph();
                System.out.println(bigraph);
            });
        }

        @Test
        void close_outer_name() {
            BigraphEntity.OuterName jeff = builder.createOuterName("jeff");
            BigraphEntity.OuterName jeff2 = builder.createOuterName("jeff2");
            BigraphEntity.InnerName x = builder.createInnerName("x");

            assertAll(() -> {
                builder.connectInnerToOuterName(x, jeff);
                builder.createRoot()
                        .addChild(signature.getControlByName("Printer"))
                        .connectNodeToOuterName(jeff)
                        .connectNodeToOuterName(jeff2);

                builder.closeOuterName(jeff);
                builder.closeInnerName(x);

                //the inner name shall not have a reference to jeff now
//                BigraphArtifactHelper.exportBigraph(builder.createBigraph());
                DynamicEcoreBigraph bigraph = builder.createBigraph();
                System.out.println(bigraph);
            });
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class GroundBigraphTestSeries {
        EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> builder;
        Signature<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> signature;

        @BeforeAll
        void createSignature() {
            signature = createExampleSignature();
        }

        @BeforeEach
        void setUp() {
            builder = EcoreBigraphBuilder.start(signature); //TODO factory methode casted sowas dann
        }

        @Test
        void makeGround() throws InvalidConnectionException, LinkTypeNotExistsException {
            BigraphEntity.InnerName x1 = builder.createInnerName("x1");
            builder.createRoot()
                    .addChild(signature.getControlByName("Computer"))
                    .addChild(signature.getControlByName("Computer"))
                    .addSite()
                    .withNewHierarchy()
                    .addChild(signature.getControlByName("Job"))
                    .addSite()
                    .goBack()
                    .connectNodeToInnerName(signature.getControlByName("Printer"), x1);

//            builder.new Hierarchy(signature.getControlByName("User"));

            builder.makeGround();
        }

//        @AfterEach
//        void debug() {
//            assertAll(() -> {
//                builder.export();
//            });
//        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class NestedHierarchyTestSeries {
        EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> builder;
        Signature<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> signature;

        @BeforeAll
        void createSignature() {
            signature = createExampleSignature();
        }

        @BeforeEach
        void setUp() {
            builder = EcoreBigraphBuilder.start(signature); //TODO factory methode casted sowas dann
        }

        @Test
        void build_simple() {
            BigraphEntity.InnerName tmp1 = builder.createInnerName("tmp1");
            BigraphEntity.InnerName tmp2 = builder.createInnerName("tmp2");
            //TODO: über command pattern wird dann der builder aufgerufen (übersetzung von gui schritten und builder)
            //test methoden ob verbindung möglich sind, wird über andere klasse gemacht

            //"umweg": verbinde nodes über hierarchie grenzen hinweg: über Inner name, und dann schließen.
            //so besitzen sie die gleiche Kante
            builder.createRoot()
                    .addChild(signature.getControlByName("Spool"))
                    .addChild(signature.getControlByName("Room"))
                    .withNewHierarchy()
                    .addChild(signature.getControlByName("Computer"))
                    .addChild(signature.getControlByName("Printer"))
                    .goBack()
                    .addChild(signature.getControlByName("Room"));

        }


//        @AfterEach
//        void debug() {
//            assertAll(() -> {
//                builder.export();
//            });
//        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class HierarchyTestSeries {
        EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> builder;
        Signature<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> signature;

        @BeforeAll
        void createSignature() {
            signature = createExampleSignature();
        }

        @BeforeEach
        void setUp() {
            builder = EcoreBigraphBuilder.start(signature); //TODO factory methode casted sowas dann
        }

        @Test
        void simple_hierarchy_test() {
            BigraphEntity.InnerName tmp1 = builder.createInnerName("tmp1");
            BigraphEntity.OuterName jeff = builder.createOuterName("jeff");

            assertAll(() -> {

                EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>>.Hierarchy room = builder.newHierarchy(signature.getControlByName("Room"));
                room.connectNodeToInnerName(tmp1)
                        .addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff)
                        .addChild(signature.getControlByName("Job"));

                builder.createRoot()
                        .addHierarchyToParent(room)
                        .addChild(signature.getControlByName("Room")).connectNodeToInnerName(tmp1);

                builder.closeInnerName(tmp1);
            });

            DynamicEcoreBigraph bigraph = builder.createBigraph();
            System.out.println(bigraph);
        }

    }

    @Test
    void write_to_dot() throws IOException {
        //This belongs in the visu module
//        EcoreBigraphBuilder<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> builder;
//        Signature<DefaultControl<StringTypedName, FiniteOrdinal<Integer>>> signature;
//        signature = createExampleSignature();
//        builder = EcoreBigraphBuilder.start(signature); //TODO factory methode casted sowas dann
//        // define some nodes
//        builder.createRoot(); // create a root node first
//        builder.addChild(signature.getControlByName("Printer"))
//                .addChild(signature.getControlByName("Printer"))
//                .addChild(signature.getControlByName("Printer"));
////        builder.connectByEdge();
//        builder.createRoot().addChild(signature.getControlByName("Computer"));
//        builder.createRoot().addChild(signature.getControlByName("User"));
//
//        DynamicEcoreBigraph bigraph = builder.createBigraph();
//
//        builder.export();
////        EcoreBigraphBuilder.EcoreRoot root = (EcoreBigraphBuilder.EcoreRoot) bigraph.getRoot();
////        if(root instanceof EcoreBigraphBuilder.EcoreRoot) {
////            System.out.println("nice");
////        }
//
//        MutableGraph g = Parser.read(getClass().getResourceAsStream("/color.dot"));
//        Graphviz.fromGraph(g).width(700).render(Format.PNG).toFile(new File("example/ex4-1.png"));
//
//        g.graphAttrs()
//                .add(Color.WHITE.gradient(Color.rgb("888888")).background().angle(90))
//                .nodeAttrs().add(Color.WHITE.fill())
//                .nodes().forEach(node ->
//                node.add(
//                        Color.named(node.name().toString()),
//                        Style.lineWidth(4).and(Style.FILLED)));
//        Graphviz.fromGraph(g).width(700).render(Format.PNG).toFile(new File("example/ex4-2.png"));
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
