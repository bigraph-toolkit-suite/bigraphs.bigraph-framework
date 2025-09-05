package org.bigraphs.framework.core;

import com.google.common.graph.Traverser;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.InvalidArityOfControlException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.builder.LinkTypeNotExistsException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.factory.FactoryCreationContext;
import org.bigraphs.framework.core.factory.PureBigraphFactory;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.elementary.DiscreteIon;
import org.bigraphs.framework.core.impl.elementary.Linkings;
import org.bigraphs.framework.core.impl.signature.DynamicControl;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.pure.MutableBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.*;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Dominik Grzelak
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@Disabled
public class BigraphCreationUnitTest {

    @Test
    void thesis_example() throws IncompatibleSignatureException, IncompatibleInterfaceException, IOException, InvalidConnectionException, LinkTypeNotExistsException {
        PureBigraphFactory factory = FactoryCreationContext.findFactoryFor(PureBigraph.class);
//        FactoryCreationContext.current().orElse(FactoryCreationContext.begin()).getFactory();
        DynamicSignatureBuilder sigBuilder = factory.createSignatureBuilder(); //new DynamicSignatureBuilder();
        sigBuilder = sigBuilder
                .newControl(StringTypedName.of("K"), FiniteOrdinal.ofInteger(1))
                .status(ControlStatus.ACTIVE)
                .assign();
        sigBuilder.newControl(StringTypedName.of("L"), FiniteOrdinal.ofInteger(1))
                .status(ControlStatus.ATOMIC)
                .assign();
        DynamicSignature sig0 = sigBuilder.create();

        DynamicSignature sig = pureSignatureBuilder()
                .add("K", 1)
                .add("L", 1, ControlStatus.ATOMIC)
                .create();

        assert sig.equals(sig0);
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig);
        PureBigraph bigraph = builder.root()
                .connectByEdge("K", "L")
                .create();

        builder.root().child("K").linkInner("tmp").child("L").linkInner("tmp").create();builder.closeInner();

        DiscreteIon<DynamicSignature> K_x = pureDiscreteIon(sig, "K", "x");
        DiscreteIon<DynamicSignature> L_x = pureDiscreteIon(sig, "L", "x");
        Linkings<DynamicSignature>.Closure x = pureLinkings(sig).closure("x");
        BigraphComposite<DynamicSignature> G = ops(x).compose(ops(K_x).merge(L_x));
        BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) G.getOuterBigraph(), System.out);
    }

    @Test
    void mutableBuilder_connectLinkUsingPortIndex() {
        DynamicSignature signature = createExampleSignature();
        HashMap<Integer, BigraphEntity.RootEntity> newRoots = new LinkedHashMap<>();
        HashMap<String, BigraphEntity.NodeEntity> newNodes = new LinkedHashMap<>();
        HashMap<Integer, BigraphEntity.SiteEntity> newSites = new LinkedHashMap<>();
        HashMap<String, BigraphEntity.Edge> newEdges = new LinkedHashMap<>();
        HashMap<String, BigraphEntity.OuterName> newOuterNames = new LinkedHashMap<>();
        HashMap<String, BigraphEntity.InnerName> newInnerNames = new LinkedHashMap<>();
        MutableBuilder<DynamicSignature> builder = MutableBuilder.newMutableBuilder(signature);

        BigraphEntity.OuterName y1 = (BigraphEntity.OuterName) builder.createNewOuterName("y1");
        newOuterNames.put(y1.getName(), y1);

        BigraphEntity<?> newRoot = builder.createNewRoot(0);
        newRoots.put(0, (BigraphEntity.RootEntity) newRoot);
        DynamicControl controlByName = signature.getControlByName("Printer");
        BigraphEntity.NodeEntity newNode = (BigraphEntity.NodeEntity) builder.createNewNode(controlByName, "v0");
        newNodes.put("v0", newNode);
        builder.setParentOfNode(newNode, newRoot);
        builder.connectToLinkUsingIndex(newNode, y1, 1);
        builder.connectToLinkUsingIndex(newNode, y1, 0);

        PureBigraphBuilder.InstanceParameter meta = builder.new InstanceParameter(
                builder.getMetaModel(),
                signature,
                newRoots,
                newSites,
                newNodes,
                newInnerNames,
                newOuterNames,
                newEdges);
        builder.reset();
        PureBigraph bigraph = new PureBigraph(meta);
        Assertions.assertDoesNotThrow(() -> {
            BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph, System.out);
        });
        List<BigraphEntity.Port> ports = bigraph.getPorts(newNode);
        System.out.println(ports.size());
        for (BigraphEntity.Port each : ports) {
            System.out.println(each.getIndex());
        }
    }

    @Test
    @DisplayName("Connecting Nodes via an Edge using connectByEdge() method")
    void connect_nodesByEdge_test_01() throws InvalidArityOfControlException {
        DynamicSignature sig = pureSignatureBuilder().newControl("A", 1).assign().create();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig);

        PureBigraph bigraph = builder.root().connectByEdge("A", "A").create();

        assertEquals(1, bigraph.getEdges().size());
        assertEquals(2, bigraph.getPointsFromLink(bigraph.getEdges().get(0)).size());
    }

    @Test
    @DisplayName("Connecting Nodes via an Edge by closing inner name")
    void connect_nodesByEdge_test_02() throws InvalidConnectionException, TypeNotExistsException {
        DynamicSignature sig = pureSignatureBuilder().newControl("A", 1).assign().create();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(sig);

        BigraphEntity.InnerName tmp = builder.createInner("tmp");
        builder.root().child("A").linkInner(tmp).child("A").linkInner(tmp);
        builder.closeInner(tmp);
        PureBigraph bigraph = builder.create();

        assertEquals(1, bigraph.getEdges().size());
        assertEquals(2, bigraph.getPointsFromLink(bigraph.getEdges().get(0)).size());
        assertEquals(0, bigraph.getInnerNames().size());
    }

    @Test
    void traversal_tests() throws InvalidConnectionException, TypeNotExistsException, IOException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        builder
                .root()
                .child("A").down().child("D").up()
                .child("B").down().child("D").child("D").up()
                .child("E").down().child("D").child("D").child("D").top()
        ;
        PureBigraph bigraph = builder.create();
        Traverser<BigraphEntity> traverser = Traverser.forTree(x -> {
            List<BigraphEntity<?>> children = bigraph.getChildrenOf(x);
            System.out.format("%s has %d children\n", x.getType(), children.size());
            return children;
        });
        Iterable<BigraphEntity> bigraphEntities = traverser.breadthFirst(bigraph.getRoots());
        bigraphEntities.forEach(x -> {
            System.out.println(x);
        });

        builder = pureBuilder(signature);
        BigraphEntity.OuterName y1 = builder.createOuter("y1");
        BigraphEntity.OuterName y2 = builder.createOuter("y2");
        BigraphEntity.OuterName y3 = builder.createOuter("y3");
        BigraphEntity.InnerName x1 = builder.createInner("x1");
        builder.root()
                .connectByEdge("D", "D", "D").linkOuter(y1)
                .child("D").linkOuter(y1).down()
                .child("D").linkOuter(y2).child("D").linkOuter(y3)
                .child("User").linkOuter(y1).up()
                .child("D").linkInner(x1);
        PureBigraph bigraph1 = builder.create();
        BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph1, System.out);

        bigraph1.getAllLinks().forEach(l -> {
            List<BigraphEntity<?>> pointsFromLink = bigraph1.getPointsFromLink(l);
            pointsFromLink.forEach(p -> {
                if (BigraphEntityType.isPort(p)) {
                    BigraphEntity.NodeEntity<DynamicControl> nodeOfPort = bigraph1.getNodeOfPort((BigraphEntity.Port) p);
                    int ix = bigraph1.getPorts(nodeOfPort).indexOf(p);
                    System.out.format("Node %s with port at index %d is connected to link %s\n", nodeOfPort.getName(), ix, l.getName());
                } else if (BigraphEntityType.isInnerName(p)) {
                    System.out.format("Inner name %s is connected to link %s\n", ((BigraphEntity.InnerName) p).getName(), l.getName());
                }
            });
        });

    }

    @Test
    void attach_multiple_innerNames() throws InvalidConnectionException, TypeNotExistsException, IOException {
        DynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.InnerName x1 = builder.createInner("x1");
        BigraphEntity.InnerName tmp = builder.createInner("tmp");
        builder
                .root()
                .child("D").linkInner(tmp)
                .child("D").linkInner(tmp)
        ;
        builder.linkInner(x1, tmp);
        builder.closeInner(tmp);
        PureBigraph bigraph = builder.create();

        BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph, System.out);
        assertEquals(1, bigraph.getRoots().size());
        assertEquals(2, bigraph.getNodes().size());
        assertEquals(0, bigraph.getPortCount(bigraph.getNodes().get(0)));
        assertEquals(0, bigraph.getPortCount(bigraph.getNodes().get(1)));
        assertEquals(1, bigraph.getInnerNames().size());
        assertEquals(1, bigraph.getEdges().size());
        assertEquals(1, bigraph.getPointsFromLink(bigraph.getEdges().get(0)).size());
        assertEquals("x1", ((BigraphEntity.InnerName) bigraph.getPointsFromLink(bigraph.getEdges().get(0)).get(0)).getName());
        assertEquals("e1", ((BigraphEntity.Edge) bigraph.getLinkOfPoint(x1)).getName());


        builder = pureBuilder(signature);
        x1 = builder.createInner("x1");
        tmp = builder.createInner("tmp");
        builder
                .root()
                .child("D").linkInner(tmp)
                .child("D").linkInner(tmp)
        ;
        builder.addInnerTo(tmp, x1);
        builder.closeInner(tmp);
        bigraph = builder.create();

        BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph, System.out);
        assertEquals(1, bigraph.getRoots().size());
        assertEquals(2, bigraph.getNodes().size());
        assertEquals(1, bigraph.getPortCount(bigraph.getNodes().get(0)));
        assertEquals(1, bigraph.getPortCount(bigraph.getNodes().get(1)));
        assertEquals(1, bigraph.getInnerNames().size());
        assertEquals(1, bigraph.getEdges().size());
        assertEquals(3, bigraph.getPointsFromLink(bigraph.getEdges().get(0)).size());
        assertEquals("e0", ((BigraphEntity.Edge) bigraph.getLinkOfPoint(x1)).getName());

        builder = pureBuilder(signature);
        x1 = builder.createInner("x1");
        BigraphEntity.InnerName x2 = builder.createInner("x2");
        BigraphEntity.OuterName o1 = builder.createOuter("o1");
        builder
                .root()
                .child("D").linkOuter(o1)
                .child("D").linkOuter(o1)
        ;
        builder.linkInnerToOuter(x1, o1);
        builder.addInnerTo(x1, x2);
        builder.closeInner(x1);
        bigraph = builder.create();
        BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph, System.out);
    }

    @Test
    void lean_factory_creation() throws IncompatibleSignatureException, IncompatibleInterfaceException, InvalidConnectionException {
//        pure();
        DynamicSignature signature = createExampleSignature();

        PureBigraph bigraph = pureBuilder(signature)
                .root()
                .child("A").child("C")
                .create();

        PureBigraph bigraph2 = pureBuilder(signature)
                .root().child("User", "alice").site()
                .create();

        BigraphComposite bigraphComposite = ops(bigraph2).compose(bigraph);
        assertNotNull(bigraphComposite);
        assertEquals(0, bigraphComposite.getOuterBigraph().getSites().size());
        assertEquals(1, bigraphComposite.getOuterBigraph().getRoots().size());
        assertEquals(3, bigraphComposite.getOuterBigraph().getNodes().size());
        assertEquals(4, bigraphComposite.getOuterBigraph().getAllPlaces().size());
        assertEquals(1, bigraphComposite.getOuterBigraph().getOuterNames().size());
        assertEquals(0, bigraphComposite.getOuterBigraph().getInnerNames().size());

//        end();
    }

    @Test
    void lean_bigraph_api_signature_creation_test() {
        DynamicSignature signature = pureSignatureBuilder()
                .newControl().identifier(StringTypedName.of("A")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("B")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .create();

        end();
    }

    @Test
    void lean_bigraph_usage_from_readme() throws InvalidConnectionException, IncompatibleSignatureException, IncompatibleInterfaceException {
        DynamicSignature signature = pureSignatureBuilder()
                .newControl().identifier(StringTypedName.of("A")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("C")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .create();

        // create two bigraphs
        PureBigraph bigraph1 = pureBuilder(signature)
                .root()
                .child("A").child("C")
                .create();

        PureBigraph bigraph2 = pureBuilder(signature)
                .root().child("User", "alice").site()
                .create();

        // compose two bigraphs
        BigraphComposite bigraphComposite = ops(bigraph2).compose(bigraph1);
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("Create Bigraphs test series")
    class ArityChecks {
        DynamicSignature signature;
        PureBigraphBuilder<DynamicSignature> builder;

        @BeforeAll
        void createSignature() {
            signature = createExampleSignature();
        }

        @BeforeEach
        void setUp() {
            builder = pureBuilder(signature);
        }

        @Test
        @DisplayName("Connect a node to an outername where the control's arity is 0")
        void connect_to_outername_1() {
            BigraphEntity.OuterName jeff = builder.createOuter("jeff");
            InvalidArityOfControlException am = assertThrows(InvalidArityOfControlException.class, () -> {
                DynamicControl selected = signature.getControlByName("Job");
                System.out.println("Node of control will be added: " + selected + " and connected with outer name " + jeff);
                builder.root()
                        .site()
                        .child(selected)
                        .linkOuter(jeff);
            });
            PureBigraph bigraph = builder.create();
            System.out.println(bigraph);
//            am.printStackTrace();
        }

        @Test
        @DisplayName("Connect a node to the same outername twice and then add another where the control's arity is 1")
        void connect_to_outername_2() {
            BigraphEntity.OuterName jeff = builder.createOuter("jeff");
            BigraphEntity.OuterName bob = builder.createOuter("bob");
            System.out.println("exceeding a node's ports w.r.t to the corresponding control's arity");
            InvalidArityOfControlException am2 = assertThrows(InvalidArityOfControlException.class, () -> {
                DynamicControl selected = signature.getControlByName("Computer");

                builder.root()
                        .child(selected)
                        .linkOuter(jeff)
                        .linkOuter(jeff)
                        .linkOuter(bob);
            });
//            am2.printStackTrace();
        }

    }

    @Test
    void biraph_is_discrete() {
        DynamicSignature signature = createExampleSignature();

        assertAll(() -> {
            PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
            BigraphEntity.OuterName a = builder.createOuter("a");
            BigraphEntity.OuterName b = builder.createOuter("b");
            builder.root().child(signature.getControlByName("Room")).linkOuter(a)
                    .child(signature.getControlByName("User")).linkOuter(b);
            PureBigraph bigraph = builder.create();
            EObject model = bigraph.getInstanceModel();
            EList<EObject> bRoots = (EList<EObject>) model.eGet(model.eClass().getEStructuralFeature("bRoots"));
            Assertions.assertEquals(1, bRoots.size());
            EObject eObject = bRoots.get(0);
            EList<EObject> bChild = (EList<EObject>) eObject.eGet(eObject.eClass().getEStructuralFeature("bChild"));
            Assertions.assertEquals(2, bChild.size());
            assertTrue(bigraph.isDiscrete());
        });

        assertAll(() -> {
            PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
            BigraphEntity.OuterName a = builder.createOuter("a");
            builder.root().child(signature.getControlByName("Room")).linkOuter(a)
                    .child(signature.getControlByName("User"));
            PureBigraph bigraph = builder.create();
            assertFalse(bigraph.isDiscrete());
        });

        assertAll(() -> {
            PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
            BigraphEntity.OuterName a = builder.createOuter("a");
            builder.root().child(signature.getControlByName("Room")).linkOuter(a)
                    .child(signature.getControlByName("User")).linkOuter(a);
            PureBigraph bigraph = builder.create();
            assertFalse(bigraph.isDiscrete());
        });
        assertAll(() -> {
            PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
            BigraphEntity.InnerName a = builder.createInner("a");
            builder.root().child(signature.getControlByName("Room")).linkInner(a)
                    .child(signature.getControlByName("User")).linkInner(a);
            builder.closeInner(a);
            PureBigraph bigraph = builder.create();
            assertFalse(bigraph.isDiscrete());
        });

        assertAll(() -> {
            PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
            BigraphEntity.OuterName a = builder.createOuter("a");
            BigraphEntity.OuterName b = builder.createOuter("b");
            BigraphEntity.OuterName c = builder.createOuter("c");
            BigraphEntity.OuterName d = builder.createOuter("d");
            builder.root().child(signature.getControlByName("Room")).linkOuter(a)
                    .child(signature.getControlByName("User")).linkOuter(b)
                    .child(signature.getControlByName("User")).linkOuter(c)
            ;
            PureBigraph bigraph = builder.create();
            assertFalse(bigraph.isDiscrete());
        });

        assertAll(() -> {
            PureBigraphBuilder<DynamicSignature> builder = pureBuilder(signature);
            BigraphEntity.OuterName a = builder.createOuter("a");
            BigraphEntity.OuterName b = builder.createOuter("b");
            builder.root().child(signature.getControlByName("Room")).linkOuter(a)
                    .child(signature.getControlByName("User")).linkOuter(b)
                    .child(signature.getControlByName("User")).linkOuter(b)
            ;
            PureBigraph bigraph = builder.create();
            assertFalse(bigraph.isDiscrete());
        });
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ConnectionTestSeries_InnerOuterNames {
        PureBigraphBuilder<DynamicSignature> builder;
        DynamicSignature signature;

        @BeforeAll
        void createSignature() {
            signature = createExampleSignature();
        }

        @BeforeEach
        void setUp() {
            builder = pureBuilder(signature);
        }

        @Test
        @Order(1)
        void to_many_connections() {
            assertThrows(InvalidArityOfControlException.class, () -> {
                builder.root()
                        .connectByEdge(signature.getControlByName("Job"),
                                signature.getControlByName("Job"),
                                signature.getControlByName("Job"));
            });

            assertAll(() -> {
                builder.root()
                        .connectByEdge(signature.getControlByName("Computer"),
                                signature.getControlByName("Computer"),
                                signature.getControlByName("Printer"));
            });
        }

        @Test
        @Order(2)
        void connect_to_inner_name() {
            BigraphEntity.InnerName x = builder.createInner("x");
            BigraphEntity.InnerName y = builder.createInner("y");
            BigraphEntity.InnerName z = builder.createInner("z");
            BigraphEntity.OuterName jeff = builder.createOuter("jeff");
            BigraphEntity.OuterName jeff2 = builder.createOuter("jeff");
            assertEquals(jeff, jeff2);

            assertAll(() -> {
                builder.linkInner(x, y);
            });

            assertAll(() -> {
                builder.root()
                        .child(signature.getControlByName("Printer"))
                        .linkInner(x)
                        .linkInner(y)

                        .child(signature.getControlByName("Printer"))
                        .linkInner(z)
                        .linkInner(z)
                        .linkOuter(jeff)
                        .linkOuter(jeff);
            });

            assertAll(() -> {
                builder.root()
                        .child(signature.getControlByName("Printer"))
                        .linkInner(x)
                        .linkInner(y)

                        .child(signature.getControlByName("Printer"))
                        .linkInner(z)
                        .linkInner(z)
                        .linkOuter(jeff)
                        .linkOuter(jeff);
            });

            assertThrows(InvalidConnectionException.class, () -> {
                builder.linkInnerToOuter(z, jeff);
            });
        }

        @Test
        @DisplayName("Connect a node with three distinct inner names by one edge")
        void connect_to_multiple_InnerNames() throws InvalidConnectionException, LinkTypeNotExistsException {
            BigraphEntity.InnerName x1 = builder.createInner("x1");
            BigraphEntity.InnerName x2 = builder.createInner("x2");
            BigraphEntity.InnerName x3 = builder.createInner("x3");

            builder.root().child("D").connectInnerNamesToNode(x1, x2, x3);
            PureBigraph bigraph = builder.create();

            assertEquals(3, bigraph.getInnerNames().size());
            assertEquals(3, bigraph.getInnerFace().getValue().size());
            assertEquals(1, bigraph.getNodes().size());

            BigraphEntity.NodeEntity<DynamicControl> D = bigraph.getNodes().iterator().next();
            assertEquals(1, bigraph.getPortCount(D));
            Collection<BigraphEntity.Port> ports = bigraph.getPorts(D);
            assertEquals(1, ports.size());
            BigraphEntity.Port port = new ArrayList<>(ports).get(0);
            BigraphEntity<?> linkOfPoint = bigraph.getLinkOfPoint(port);
            assertNotNull(linkOfPoint);
            Collection<BigraphEntity<?>> pointsFromLink = bigraph.getPointsFromLink((BigraphEntity.Link) linkOfPoint);
            assertEquals(4, pointsFromLink.size());
            int portCnt = 0, innerCnt = 0;
            for (BigraphEntity<?> eachPoint : pointsFromLink) {
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
            BigraphEntity.InnerName x1 = builder.createInner("x1");
            BigraphEntity.InnerName x2 = builder.createInner("x2");
            BigraphEntity.InnerName x3 = builder.createInner("x3");
            BigraphEntity.OuterName jeff = builder.createOuter("jeff");

            assertAll(() -> {
                builder.linkInnerToOuter(x3, jeff);
            });

            assertAll(() -> {
                builder.root()
                        .child(signature.getControlByName("Printer"))
                        .linkInner(x1)
                        .linkInner(x2)
                        .connectByEdge(signature.getControlByName("Printer"), signature.getControlByName("Printer"))
                        .linkInner(x1)
                        .child(signature.getControlByName("Printer"))
                        .linkOuter(jeff);

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
            BigraphEntity.OuterName jeff = builder.createOuter("jeff");
            BigraphEntity.InnerName x = builder.createInner("x");

            assertAll(() -> {
                builder.linkInnerToOuter(x, jeff);
                builder.root()
                        .child(signature.getControlByName("Printer"))
                        .linkOuter(jeff);
                builder.closeInner(x, true);
//                builder.closeInnerName(x);


                builder.root()
                        .child(signature.getControlByName("Computer"))
                        .linkInner(x);

                builder.closeInner();
                PureBigraph bigraph = builder.create();
                System.out.println(bigraph);
            });
        }

        @Test
        void close_outer_name() {
            BigraphEntity.OuterName jeff = builder.createOuter("jeff");
            BigraphEntity.OuterName jeff2 = builder.createOuter("jeff2");
            BigraphEntity.InnerName x = builder.createInner("x");

            assertAll(() -> {
                builder.linkInnerToOuter(x, jeff);
                builder.root()
                        .child(signature.getControlByName("Printer"))
                        .linkOuter(jeff)
                        .linkOuter(jeff2);

                builder.closeOuter(jeff);
                builder.closeInner(x);

                //the inner name shall not have a reference to jeff now
//                BigraphArtifactHelper.exportBigraph(builder.createBigraph());
                PureBigraph bigraph = builder.create();
                System.out.println(bigraph);
            });
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class GroundBigraphTestSeries {
        PureBigraphBuilder<DynamicSignature> builder;
        DynamicSignature signature;

        @BeforeAll
        void createSignature() {
            signature = createExampleSignature();
        }

        @BeforeEach
        void setUp() {
            builder = pureBuilder(signature);
        }

        @Test
        void makeGround() throws InvalidConnectionException, LinkTypeNotExistsException, ControlIsAtomicException {
            SignatureBuilder<StringTypedName, FiniteOrdinal<Integer>, ?, ?> signatureBuilder = new DynamicSignatureBuilder();
            signatureBuilder
                    .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(1)).assign();
//            Signature signature2 = signatureBuilder.createSignature();
//            builder.createRoot().addChild(signature2.getControlByName("Printer"));

            BigraphEntity.InnerName x1 = builder.createInner("x1");
            builder.root()
                    .child(this.signature.getControlByName("Computer"))
                    .child(this.signature.getControlByName("Computer"))
                    .site()
                    .down()
                    .child(this.signature.getControlByName("Job"))
                    .site()
                    .up()
                    .nodeWithInner(x1, this.signature.getControlByName("Printer"));

//            builder.new Hierarchy(signature.getControlByName("User"));

            builder.makeGround();
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class NestedHierarchyTestSeries {
        PureBigraphBuilder<DynamicSignature> builder;
        DynamicSignature signature;

        @BeforeAll
        void createSignature() {
            signature = createExampleSignature();
        }

        @BeforeEach
        void setUp() {
            builder = pureBuilder(signature);
        }

        @Test
        void build_simple() throws ControlIsAtomicException {
            BigraphEntity.InnerName tmp1 = builder.createInner("tmp1");
            BigraphEntity.InnerName tmp2 = builder.createInner("tmp2");

            //"umweg": verbinde nodes über hierarchie grenzen hinweg: über Inner name, und dann schließen.
            //so besitzen sie die gleiche Kante
            builder.root()
                    .child(signature.getControlByName("Spool"))
                    .child(signature.getControlByName("Room"))
                    .down()
                    .child(signature.getControlByName("Computer"))
                    .child(signature.getControlByName("Printer"))
                    .up()
                    .child(signature.getControlByName("Room"));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class HierarchyTestSeries {
        PureBigraphBuilder<DynamicSignature> builder;
        DynamicSignature signature;

        @BeforeAll
        void createSignature() {
            signature = createExampleSignature();
        }

        @BeforeEach
        void setUp() {
            builder = pureBuilder(signature);
        }

        @Test
        void simple_hierarchy_test() {
            BigraphEntity.InnerName tmp1 = builder.createInner("tmp1");
            BigraphEntity.OuterName jeff = builder.createOuter("jeff");

            assertAll(() -> {

                PureBigraphBuilder<DynamicSignature>.Hierarchy room = builder.hierarchy(signature.getControlByName("Room"));
                room.linkInner(tmp1)
                        .child(signature.getControlByName("User")).linkOuter(jeff)
                        .child(signature.getControlByName("Job"));

                builder.root()
                        .child(room) // easily build complex hierarchies of many different parts
                        .child(room) // easily build complex hierarchies of many different parts
                        .child(signature.getControlByName("Room")).linkInner(tmp1);

                builder.closeInner(tmp1);
            });

            PureBigraph bigraph = builder.create();
            System.out.println(bigraph);
        }

    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ControlAtomicityTests {
        PureBigraphBuilder<DynamicSignature> builder;
        DynamicSignature signature;

        @BeforeAll
        void createSignature() {
            signature = createExampleSignature();
        }

        @BeforeEach
        void setUp() {
            builder = pureBuilder(signature);
        }

        @Test
        void controls_are_atomic() {

            assertThrows(ControlIsAtomicException.class, () -> {
                builder.root()
                        .child(signature.getControlByName("A"))
                        .child(signature.getControlByName("C"))
                        .down().child(signature.getControlByName("B"));
            });
            assertThrows(ControlIsAtomicException.class, () -> {
                builder.root()
                        .child(signature.getControlByName("C"))
                        .down().child(signature.getControlByName("A"));
            });
            assertThrows(ControlIsAtomicException.class, () -> {
                builder.root()
                        .child(signature.getControlByName("C"))
                        .down().site();
            });

            assertAll(() -> {
                builder.root()
                        .child(signature.getControlByName("Room"))
                        .down().child(signature.getControlByName("Room"))
                        .down().child(signature.getControlByName("Room"))
                        .site()
                        .down().child(signature.getControlByName("Room"))
                        .down().site();
            });


            PureBigraph bigraph = builder.create();

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
        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();
        signatureBuilder
                .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Spool")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().status(ControlStatus.ACTIVE).identifier(StringTypedName.of("A")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().status(ControlStatus.PASSIVE).identifier(StringTypedName.of("B")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().status(ControlStatus.ACTIVE).identifier(StringTypedName.of("E")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().status(ControlStatus.ATOMIC).identifier(StringTypedName.of("C")).arity(FiniteOrdinal.ofInteger(0)).assign()
                .newControl().status(ControlStatus.ACTIVE).identifier(StringTypedName.of("D")).arity(FiniteOrdinal.ofInteger(4)).assign()
        ;
        DynamicSignature controlSignature = signatureBuilder.create();
        return (S) controlSignature;
    }
}
