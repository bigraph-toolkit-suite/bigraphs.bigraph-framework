package de.tudresden.inf.st.bigraphs.core;

import com.google.common.graph.Traverser;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidArityOfControlException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.MutableBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.SignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.*;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Dominik Grzelak
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@Disabled
public class BigraphCreationUnitTest {

    @Test
    void name() throws IOException {
        DefaultDynamicSignature sig = pureSignatureBuilder()
                .addControl("User", 3, ControlStatus.ACTIVE)
                .addControl("PC", 3, ControlStatus.ACTIVE)
                .addControl("Computer", 3, ControlStatus.ACTIVE)
                .create();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(sig);
        builder.createRoot().addSite();
        builder.createOuterName("ko");
        builder.createInnerName("cp");
        PureBigraph bigraph = builder.createBigraph();
        BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph, System.out);

        String pathLoaded = "src/test/resources/ecore-test-models/dolore_agent_5.xmi";
        EPackage orGetBigraphMetaModel = createOrGetBigraphMetaModel(sig);
        PureBigraphBuilder<DefaultDynamicSignature> loadedBuilder =
                PureBigraphBuilder.create(sig, orGetBigraphMetaModel, pathLoaded);
        BigraphFileModelManagement.Store.exportAsInstanceModel(loadedBuilder.createBigraph(), System.out);
    }

    @Test
    void mutableBuilder_connectLinkUsingPortIndex() {
        DefaultDynamicSignature signature = createExampleSignature();
        HashMap<Integer, BigraphEntity.RootEntity> newRoots = new LinkedHashMap<>();
        HashMap<String, BigraphEntity.NodeEntity> newNodes = new LinkedHashMap<>();
        HashMap<Integer, BigraphEntity.SiteEntity> newSites = new LinkedHashMap<>();
        HashMap<String, BigraphEntity.Edge> newEdges = new LinkedHashMap<>();
        HashMap<String, BigraphEntity.OuterName> newOuterNames = new LinkedHashMap<>();
        HashMap<String, BigraphEntity.InnerName> newInnerNames = new LinkedHashMap<>();
        MutableBuilder<DefaultDynamicSignature> builder = MutableBuilder.newMutableBuilder(signature);

        BigraphEntity.OuterName y1 = (BigraphEntity.OuterName) builder.createNewOuterName("y1");
        newOuterNames.put(y1.getName(), y1);

        BigraphEntity<?> newRoot = builder.createNewRoot(0);
        newRoots.put(0, (BigraphEntity.RootEntity) newRoot);
        DefaultDynamicControl controlByName = signature.getControlByName("Printer");
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
        DefaultDynamicSignature sig = pureSignatureBuilder().newControl("A", 1).assign().create();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(sig);

        PureBigraph bigraph = builder.createRoot().connectByEdge("A", "A").createBigraph();

        assertEquals(1, bigraph.getEdges().size());
        assertEquals(2, bigraph.getPointsFromLink(bigraph.getEdges().get(0)).size());
    }

    @Test
    @DisplayName("Connecting Nodes via an Edge by closing inner name")
    void connect_nodesByEdge_test_02() throws InvalidConnectionException, TypeNotExistsException {
        DefaultDynamicSignature sig = pureSignatureBuilder().newControl("A", 1).assign().create();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(sig);

        BigraphEntity.InnerName tmp = builder.createInnerName("tmp");
        builder.createRoot().addChild("A").linkToInner(tmp).addChild("A").linkToInner(tmp);
        builder.closeInnerName(tmp);
        PureBigraph bigraph = builder.createBigraph();

        assertEquals(1, bigraph.getEdges().size());
        assertEquals(2, bigraph.getPointsFromLink(bigraph.getEdges().get(0)).size());
        assertEquals(0, bigraph.getInnerNames().size());
    }

    @Test
    void traversal_tests() throws InvalidConnectionException, TypeNotExistsException, IOException {
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        builder
                .createRoot()
                .addChild("A").down().addChild("D").up()
                .addChild("B").down().addChild("D").addChild("D").up()
                .addChild("E").down().addChild("D").addChild("D").addChild("D").top()
        ;
        PureBigraph bigraph = builder.createBigraph();
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
        BigraphEntity.OuterName y1 = builder.createOuterName("y1");
        BigraphEntity.OuterName y2 = builder.createOuterName("y2");
        BigraphEntity.OuterName y3 = builder.createOuterName("y3");
        BigraphEntity.InnerName x1 = builder.createInnerName("x1");
        builder.createRoot()
                .connectByEdge("D", "D", "D").linkToOuter(y1)
                .addChild("D").linkToOuter(y1).down()
                .addChild("D").linkToOuter(y2).addChild("D").linkToOuter(y3)
                .addChild("User").linkToOuter(y1).up()
                .addChild("D").linkToInner(x1);
        PureBigraph bigraph1 = builder.createBigraph();
        BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph1, System.out);

        bigraph1.getAllLinks().forEach(l -> {
            List<BigraphEntity<?>> pointsFromLink = bigraph1.getPointsFromLink(l);
            pointsFromLink.forEach(p -> {
                if (BigraphEntityType.isPort(p)) {
                    BigraphEntity.NodeEntity<DefaultDynamicControl> nodeOfPort = bigraph1.getNodeOfPort((BigraphEntity.Port) p);
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
        DefaultDynamicSignature signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
        BigraphEntity.InnerName x1 = builder.createInnerName("x1");
        BigraphEntity.InnerName tmp = builder.createInnerName("tmp");
        builder
                .createRoot()
                .addChild("D").linkToInner(tmp)
                .addChild("D").linkToInner(tmp)
        ;
        builder.connectInnerNames(x1, tmp);
        builder.closeInnerName(tmp);
        PureBigraph bigraph = builder.createBigraph();

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
        x1 = builder.createInnerName("x1");
        tmp = builder.createInnerName("tmp");
        builder
                .createRoot()
                .addChild("D").linkToInner(tmp)
                .addChild("D").linkToInner(tmp)
        ;
        builder.addInnerNameTo(tmp, x1);
        builder.closeInnerName(tmp);
        bigraph = builder.createBigraph();

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
        x1 = builder.createInnerName("x1");
        BigraphEntity.InnerName x2 = builder.createInnerName("x2");
        BigraphEntity.OuterName o1 = builder.createOuterName("o1");
        builder
                .createRoot()
                .addChild("D").linkToOuter(o1)
                .addChild("D").linkToOuter(o1)
        ;
        builder.connectInnerToOuterName(x1, o1);
        builder.addInnerNameTo(x1, x2);
        builder.closeInnerName(x1);
        bigraph = builder.createBigraph();
        BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph, System.out);
    }

    @Test
    void lean_factory_creation() throws IncompatibleSignatureException, IncompatibleInterfaceException, InvalidConnectionException {
//        pure();
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

//        end();
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
        DefaultDynamicSignature signature;
        PureBigraphBuilder<DefaultDynamicSignature> builder;

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
        DefaultDynamicSignature signature = createExampleSignature();

        assertAll(() -> {
            PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
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
            PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
            BigraphEntity.OuterName a = builder.createOuterName("a");
            builder.createRoot().addChild(signature.getControlByName("Room")).linkToOuter(a)
                    .addChild(signature.getControlByName("User"));
            PureBigraph bigraph = builder.createBigraph();
            assertFalse(bigraph.isDiscrete());
        });

        assertAll(() -> {
            PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
            BigraphEntity.OuterName a = builder.createOuterName("a");
            builder.createRoot().addChild(signature.getControlByName("Room")).linkToOuter(a)
                    .addChild(signature.getControlByName("User")).linkToOuter(a);
            PureBigraph bigraph = builder.createBigraph();
            assertFalse(bigraph.isDiscrete());
        });
        assertAll(() -> {
            PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
            BigraphEntity.InnerName a = builder.createInnerName("a");
            builder.createRoot().addChild(signature.getControlByName("Room")).linkToInner(a)
                    .addChild(signature.getControlByName("User")).linkToInner(a);
            builder.closeInnerName(a);
            PureBigraph bigraph = builder.createBigraph();
            assertFalse(bigraph.isDiscrete());
        });

        assertAll(() -> {
            PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
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
            PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(signature);
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
        DefaultDynamicSignature signature;

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
        DefaultDynamicSignature signature;

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
        DefaultDynamicSignature signature;

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
        DefaultDynamicSignature signature;

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
        DefaultDynamicSignature signature;

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
        DefaultDynamicSignature controlSignature = signatureBuilder.create();
        return (S) controlSignature;
    }
}
