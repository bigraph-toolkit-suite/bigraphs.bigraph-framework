package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidArityOfControlException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.SignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Collection;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Dominik Grzelak
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@Disabled
public class BigraphCreationUnitTest {

    private PureBigraphFactory factory = AbstractBigraphFactory.createPureBigraphFactory();
    private PureBigraphFactory factoryWithArgs = AbstractBigraphFactory.createPureBigraphFactory();

    @Test
    void lean_factory_creation() throws IncompatibleSignatureException, IncompatibleInterfaceException, InvalidConnectionException {
        pure();
        DefaultDynamicSignature signature = createExampleSignature();

        PureBigraph bigraph = pureBuilder(signature)
                .createRoot()
                .addChild("A").addChild("C")
                .createBigraph();

        PureBigraph bigraph2 = pureBuilder(signature)
                .createRoot().addChild("User", "alice").addSite()
                .createBigraph();

        BigraphComposite bigraphComposite = ops(bigraph2).compose(bigraph);
        assertNotNull(bigraphComposite);
        assertEquals(0, bigraphComposite.getOuterBigraph().getSites().size());
        assertEquals(1, bigraphComposite.getOuterBigraph().getRoots().size());
        assertEquals(3, bigraphComposite.getOuterBigraph().getNodes().size());
        assertEquals(4, bigraphComposite.getOuterBigraph().getAllPlaces().size());
        assertEquals(1, bigraphComposite.getOuterBigraph().getOuterNames().size());
        assertEquals(0, bigraphComposite.getOuterBigraph().getInnerNames().size());

        end();
    }

    @Test
    void lean_bigraph_api_signature_creation_test() {
        pure();

        DefaultDynamicSignature signature = pureSignatureBuilder()
                .newControl().identifier(StringTypedName.of("A")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("B")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .create();

        end();
    }

    @Test
    void lean_bigraph_usage_from_readme() throws InvalidConnectionException, IncompatibleSignatureException, IncompatibleInterfaceException {
        DefaultDynamicSignature signature = pureSignatureBuilder()
                .newControl().identifier(StringTypedName.of("A")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("C")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .create();

        // create two bigraphs
        PureBigraph bigraph1 = pureBuilder(signature)
                .createRoot()
                .addChild("A").addChild("C")
                .createBigraph();

        PureBigraph bigraph2 = pureBuilder(signature)
                .createRoot().addChild("User", "alice").addSite()
                .createBigraph();

        // compose two bigraphs
        BigraphComposite bigraphComposite = ops(bigraph2).compose(bigraph1);
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("Create Bigraphs test series")
    class ArityChecks {
        Signature<DefaultDynamicControl> signature;
        PureBigraphBuilder<DefaultDynamicSignature> builder;

        @BeforeAll
        void createSignature() {
            signature = createExampleSignature();
        }

        @BeforeEach
        void setUp() {
            builder = factory.createBigraphBuilder(signature);
        }

        @Test
        @DisplayName("Connect a node to an outername where the control's arity is 0")
        void connect_to_outername_1() {
            BigraphEntity.OuterName jeff = builder.createOuterName("jeff");
            InvalidArityOfControlException am = assertThrows(InvalidArityOfControlException.class, () -> {
                DefaultDynamicControl selected = signature.getControlByName("Job");
                System.out.println("Node of control will be added: " + selected + " and connected with outer name " + jeff);
                builder.createRoot()
                        .addSite()
                        .addChild(selected)
                        .linkToOuter(jeff);
            });
            PureBigraph bigraph = builder.createBigraph();
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
                DefaultDynamicControl selected = signature.getControlByName("Computer");

                builder.createRoot()
                        .addChild(selected)
                        .linkToOuter(jeff)
                        .linkToOuter(jeff)
                        .linkToOuter(bob);
            });
//            am2.printStackTrace();
        }

    }

    @Test
    void biraph_is_discrete() {
        Signature<DefaultDynamicControl> signature = createExampleSignature();

        assertAll(() -> {
            PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
            BigraphEntity.OuterName a = builder.createOuterName("a");
            BigraphEntity.OuterName b = builder.createOuterName("b");
            builder.createRoot().addChild(signature.getControlByName("Room")).linkToOuter(a)
                    .addChild(signature.getControlByName("User")).linkToOuter(b);
            PureBigraph bigraph = builder.createBigraph();
            EObject model = bigraph.getModel();
            EList<EObject> bRoots = (EList<EObject>) model.eGet(model.eClass().getEStructuralFeature("bRoots"));
            Assertions.assertEquals(1, bRoots.size());
            EObject eObject = bRoots.get(0);
            EList<EObject> bChild = (EList<EObject>) eObject.eGet(eObject.eClass().getEStructuralFeature("bChild"));
            Assertions.assertEquals(2, bChild.size());
            assertTrue(bigraph.isDiscrete());
        });

        assertAll(() -> {
            PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
            BigraphEntity.OuterName a = builder.createOuterName("a");
            builder.createRoot().addChild(signature.getControlByName("Room")).linkToOuter(a)
                    .addChild(signature.getControlByName("User"));
            PureBigraph bigraph = builder.createBigraph();
            assertFalse(bigraph.isDiscrete());
        });

        assertAll(() -> {
            PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
            BigraphEntity.OuterName a = builder.createOuterName("a");
            builder.createRoot().addChild(signature.getControlByName("Room")).linkToOuter(a)
                    .addChild(signature.getControlByName("User")).linkToOuter(a);
            PureBigraph bigraph = builder.createBigraph();
            assertFalse(bigraph.isDiscrete());
        });
        assertAll(() -> {
            PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
            BigraphEntity.InnerName a = builder.createInnerName("a");
            builder.createRoot().addChild(signature.getControlByName("Room")).linkToInner(a)
                    .addChild(signature.getControlByName("User")).linkToInner(a);
            builder.closeInnerName(a);
            PureBigraph bigraph = builder.createBigraph();
            assertFalse(bigraph.isDiscrete());
        });

        assertAll(() -> {
            PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
            BigraphEntity.OuterName a = builder.createOuterName("a");
            BigraphEntity.OuterName b = builder.createOuterName("b");
            BigraphEntity.OuterName c = builder.createOuterName("c");
            BigraphEntity.OuterName d = builder.createOuterName("d");
            builder.createRoot().addChild(signature.getControlByName("Room")).linkToOuter(a)
                    .addChild(signature.getControlByName("User")).linkToOuter(b)
                    .addChild(signature.getControlByName("User")).linkToOuter(c)
            ;
            PureBigraph bigraph = builder.createBigraph();
            assertFalse(bigraph.isDiscrete());
        });

        assertAll(() -> {
            PureBigraphBuilder<DefaultDynamicSignature> builder = factory.createBigraphBuilder(signature);
            BigraphEntity.OuterName a = builder.createOuterName("a");
            BigraphEntity.OuterName b = builder.createOuterName("b");
            builder.createRoot().addChild(signature.getControlByName("Room")).linkToOuter(a)
                    .addChild(signature.getControlByName("User")).linkToOuter(b)
                    .addChild(signature.getControlByName("User")).linkToOuter(b)
            ;
            PureBigraph bigraph = builder.createBigraph();
            assertFalse(bigraph.isDiscrete());
        });
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ConnectionTestSeries_InnerOuterNames {
        PureBigraphBuilder<DefaultDynamicSignature> builder;
        Signature<DefaultDynamicControl> signature;

        @BeforeAll
        void createSignature() {
            signature = createExampleSignature();
        }

        @BeforeEach
        void setUp() {
            builder = factory.createBigraphBuilder(signature);
        }

        @Test
        @Order(1)
        void to_many_connections() {
            assertThrows(InvalidArityOfControlException.class, () -> {
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
            BigraphEntity.OuterName jeff2 = builder.createOuterName("jeff");
            assertEquals(jeff, jeff2);

            assertAll(() -> {
                builder.connectInnerNames(x, y);
            });

            assertAll(() -> {
                builder.createRoot()
                        .addChild(signature.getControlByName("Printer"))
                        .linkToInner(x)
                        .linkToInner(y)

                        .addChild(signature.getControlByName("Printer"))
                        .linkToInner(z)
                        .linkToInner(z)
                        .linkToOuter(jeff)
                        .linkToOuter(jeff);
            });

            assertAll(() -> {
                builder.createRoot()
                        .addChild(signature.getControlByName("Printer"))
                        .linkToInner(x)
                        .linkToInner(y)

                        .addChild(signature.getControlByName("Printer"))
                        .linkToInner(z)
                        .linkToInner(z)
                        .linkToOuter(jeff)
                        .linkToOuter(jeff);
            });

            assertThrows(InvalidConnectionException.class, () -> {
                builder.connectInnerToOuterName(z, jeff);
            });
        }

        @Test
        @DisplayName("Connect a node with three distinct inner names by one edge")
        void connect_to_multiple_InnerNames() throws InvalidConnectionException, LinkTypeNotExistsException {
            BigraphEntity.InnerName x1 = builder.createInnerName("x1");
            BigraphEntity.InnerName x2 = builder.createInnerName("x2");
            BigraphEntity.InnerName x3 = builder.createInnerName("x3");

            builder.createRoot().addChild("D").connectInnerNamesToNode(x1, x2, x3);
            PureBigraph bigraph = builder.createBigraph();

            assertEquals(3, bigraph.getInnerNames().size());
            assertEquals(3, bigraph.getInnerFace().getValue().size());
            assertEquals(1, bigraph.getNodes().size());

            BigraphEntity.NodeEntity<DefaultDynamicControl> D = bigraph.getNodes().iterator().next();
            assertEquals(1, bigraph.getPortCount(D));
            Collection<BigraphEntity.Port> ports = bigraph.getPorts(D);
            assertEquals(1, ports.size());
            BigraphEntity.Port port = new ArrayList<>(ports).get(0);
            BigraphEntity linkOfPoint = bigraph.getLinkOfPoint(port);
            assertNotNull(linkOfPoint);
            Collection<BigraphEntity> pointsFromLink = bigraph.getPointsFromLink(linkOfPoint);
            assertEquals(4, pointsFromLink.size());
            int portCnt = 0, innerCnt = 0;
            for (BigraphEntity eachPoint : pointsFromLink) {
                if (BigraphEntityType.isPort(eachPoint)) portCnt++;
                if (BigraphEntityType.isInnerName(eachPoint)) {
                    innerCnt++;
                    String name = ((BigraphEntity.InnerName) eachPoint).getName();
                    assertTrue(name.equals(x1.getName()) || name.equals(x2.getName()) || name.equals(x3.getName()));
                }
            }
            assertEquals(1, portCnt);
            assertEquals(3, innerCnt);

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
                        .linkToInner(x1)
                        .linkToInner(x2)
                        .connectByEdge(signature.getControlByName("Printer"), signature.getControlByName("Printer"))
                        .linkToInner(x1)
                        .addChild(signature.getControlByName("Printer"))
                        .linkToOuter(jeff);

            });

//            assertThrows(InnerNameConnectedToOuterNameException.class, () -> {
//                builder.connectInnerNames(x1, x2, true);
//                builder.connectInnerNames(x2, x3, false);
//            });
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
                        .linkToOuter(jeff);
                builder.closeInnerName(x, true);
//                builder.closeInnerName(x);


                builder.createRoot()
                        .addChild(signature.getControlByName("Computer"))
                        .linkToInner(x);

                builder.closeAllInnerNames();
                PureBigraph bigraph = builder.createBigraph();
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
                        .linkToOuter(jeff)
                        .linkToOuter(jeff2);

                builder.closeOuterName(jeff);
                builder.closeInnerName(x);

                //the inner name shall not have a reference to jeff now
//                BigraphArtifactHelper.exportBigraph(builder.createBigraph());
                PureBigraph bigraph = builder.createBigraph();
                System.out.println(bigraph);
            });
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class GroundBigraphTestSeries {
        PureBigraphBuilder<DefaultDynamicSignature> builder;
        Signature<DefaultDynamicControl> signature;

        @BeforeAll
        void createSignature() {
            signature = createExampleSignature();
        }

        @BeforeEach
        void setUp() {
            builder = factory.createBigraphBuilder(signature);
        }

        @Test
        void makeGround() throws InvalidConnectionException, LinkTypeNotExistsException, ControlIsAtomicException {
            SignatureBuilder<StringTypedName, FiniteOrdinal<Integer>, ?, ?> signatureBuilder = new DynamicSignatureBuilder();
            signatureBuilder
                    .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(1)).assign();
//            Signature signature2 = signatureBuilder.createSignature();
//            builder.createRoot().addChild(signature2.getControlByName("Printer"));

            BigraphEntity.InnerName x1 = builder.createInnerName("x1");
            builder.createRoot()
                    .addChild(this.signature.getControlByName("Computer"))
                    .addChild(this.signature.getControlByName("Computer"))
                    .addSite()
                    .down()
                    .addChild(this.signature.getControlByName("Job"))
                    .addSite()
                    .up()
                    .connectNodeToInnerName(x1, this.signature.getControlByName("Printer"));

//            builder.new Hierarchy(signature.getControlByName("User"));

            builder.makeGround();
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class NestedHierarchyTestSeries {
        PureBigraphBuilder<DefaultDynamicSignature> builder;
        Signature<DefaultDynamicControl> signature;

        @BeforeAll
        void createSignature() {
            signature = createExampleSignature();
        }

        @BeforeEach
        void setUp() {
            builder = factory.createBigraphBuilder(signature);
        }

        @Test
        void build_simple() throws ControlIsAtomicException {
            BigraphEntity.InnerName tmp1 = builder.createInnerName("tmp1");
            BigraphEntity.InnerName tmp2 = builder.createInnerName("tmp2");

            //"umweg": verbinde nodes über hierarchie grenzen hinweg: über Inner name, und dann schließen.
            //so besitzen sie die gleiche Kante
            builder.createRoot()
                    .addChild(signature.getControlByName("Spool"))
                    .addChild(signature.getControlByName("Room"))
                    .down()
                    .addChild(signature.getControlByName("Computer"))
                    .addChild(signature.getControlByName("Printer"))
                    .up()
                    .addChild(signature.getControlByName("Room"));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class HierarchyTestSeries {
        PureBigraphBuilder<DefaultDynamicSignature> builder;
        Signature<DefaultDynamicControl> signature;

        @BeforeAll
        void createSignature() {
            signature = createExampleSignature();
        }

        @BeforeEach
        void setUp() {
            builder = factory.createBigraphBuilder(signature);
        }

        @Test
        void simple_hierarchy_test() {
            BigraphEntity.InnerName tmp1 = builder.createInnerName("tmp1");
            BigraphEntity.OuterName jeff = builder.createOuterName("jeff");

            assertAll(() -> {

                PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy room = builder.hierarchy(signature.getControlByName("Room"));
                room.linkToInner(tmp1)
                        .addChild(signature.getControlByName("User")).linkToOuter(jeff)
                        .addChild(signature.getControlByName("Job"));

                builder.createRoot()
                        .addChild(room) // easily build complex hierarchies of many different parts
                        .addChild(room) // easily build complex hierarchies of many different parts
                        .addChild(signature.getControlByName("Room")).linkToInner(tmp1);

                builder.closeInnerName(tmp1);
            });

            PureBigraph bigraph = builder.createBigraph();
            System.out.println(bigraph);
        }

    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ControlAtomicityTests {
        PureBigraphBuilder<DefaultDynamicSignature> builder;
        Signature<DefaultDynamicControl> signature;

        @BeforeAll
        void createSignature() {
            signature = createExampleSignature();
        }

        @BeforeEach
        void setUp() {
            builder = factory.createBigraphBuilder(signature);
        }

        @Test
        void controls_are_atomic() {

            assertThrows(ControlIsAtomicException.class, () -> {
                builder.createRoot()
                        .addChild(signature.getControlByName("A"))
                        .addChild(signature.getControlByName("C"))
                        .down().addChild(signature.getControlByName("B"));
            });
            assertThrows(ControlIsAtomicException.class, () -> {
                builder.createRoot()
                        .addChild(signature.getControlByName("C"))
                        .down().addChild(signature.getControlByName("A"));
            });
            assertThrows(ControlIsAtomicException.class, () -> {
                builder.createRoot()
                        .addChild(signature.getControlByName("C"))
                        .down().addSite();
            });

            assertAll(() -> {
                builder.createRoot()
                        .addChild(signature.getControlByName("Room"))
                        .down().addChild(signature.getControlByName("Room"))
                        .down().addChild(signature.getControlByName("Room"))
                        .addSite()
                        .down().addChild(signature.getControlByName("Room"))
                        .down().addSite();
            });


            PureBigraph bigraph = builder.createBigraph();

            assertTrue(bigraph.isActiveAtSite(0));
            assertTrue(bigraph.isActiveAtSite(1));

            // the room hierarchy is completely active
            bigraph.getNodes().stream()
                    .filter(x -> x.getControl().getNamedType().stringValue().equals("Room"))
                    .forEach(x -> {
                        assertTrue(bigraph.isActiveAtNode(x));
                    });

            System.out.println(bigraph);
        }

    }

    private <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder signatureBuilder = factory.createSignatureBuilder();
        signatureBuilder
                .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Spool")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().kind(ControlKind.ACTIVE).identifier(StringTypedName.of("A")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().kind(ControlKind.PASSIVE).identifier(StringTypedName.of("B")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().kind(ControlKind.ATOMIC).identifier(StringTypedName.of("C")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().kind(ControlKind.ACTIVE).identifier(StringTypedName.of("D")).arity(FiniteOrdinal.ofInteger(4)).assign()
        ;
        DefaultDynamicSignature controlSignature = signatureBuilder.create();
        return (S) controlSignature;
    }
}
