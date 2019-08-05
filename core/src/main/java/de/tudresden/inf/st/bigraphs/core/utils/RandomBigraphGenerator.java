package de.tudresden.inf.st.bigraphs.core.utils;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.MutableBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.security.SecureRandom;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Generates random bigraphs.
 *
 * @author Dominik Grzelak
 */
public class RandomBigraphGenerator {
    private SecureRandom rnd;

    public enum LinkStrategy {
        MAX_LINKING, MIN_LINKING
    }

    public RandomBigraphGenerator() {
        rnd = new SecureRandom();
    }

    double[] stats;

    public PureBigraph generate(DefaultDynamicSignature signature, int t, int n, float p) {
//        rnd = new Random();
        DistributedRandomNumberGenerator drng = new DistributedRandomNumberGenerator();
//        Random mRandom = new Random();
        HashMap<Integer, BigraphEntity.RootEntity> myRoots = new LinkedHashMap<>();
        HashMap<String, BigraphEntity.NodeEntity> myNodes = new LinkedHashMap<>();
        HashMap<Integer, BigraphEntity.SiteEntity> mySites = new LinkedHashMap<>();
        HashMap<String, BigraphEntity.Edge> myEdges = new LinkedHashMap<>();
        HashMap<String, BigraphEntity.OuterName> myOutername = new LinkedHashMap<>();
        MutableBuilder<Signature<? extends Control>> builder = PureBigraphBuilder.newMutableBuilder(signature);
        Supplier<Control> controlSupplier = provideControlSupplier(signature);
        Supplier<String> vertexLabelSupplier = vertexLabelSupplier();
        List<BigraphEntity> nodes = new ArrayList<>(n);
        int i;
        for (i = 0; i < t; ++i) {
            BigraphEntity newRoot = builder.createNewRoot(i);
            myRoots.put(i, (BigraphEntity.RootEntity) newRoot);
            nodes.add(newRoot);
        }
        int edgeCnt = 0, nodeCnt = t;
        for (i = t; i < n; i++) {
            // create a new node with a random control
//                    V v = target.addVertex();


            // get an existing node randomly
//                    V u = nodes.get(this.rng.nextInt(nodes.size()));
            BigraphEntity entity = nodes.get(rnd.nextInt(nodes.size()));
//            boolean created_edge = false;
//            do {
//                entity = nodes.get(rnd.nextInt(nodes.size()));
//                double degree = degreeOf(entity);
////            double attach_prob = (degree + 1) / (mGraph.getEdgeCount() + mGraph.getVertexCount() - 1);
//                double attach_prob = (degree + 1) / (edgeCnt + nodeCnt);
//                if (attach_prob >= mRandom.nextDouble())
//                    created_edge = true;
//
//            } while (!created_edge);
            String vlbl = vertexLabelSupplier.get();
            BigraphEntity newNode = builder.createNewNode(controlSupplier.get(), vertexLabelSupplier.get());
            myNodes.put(vlbl, (BigraphEntity.NodeEntity) newNode);

            //add as parent
            setParentOfNode(newNode, entity);
            nodes.add(newNode);
            if (i > 1) {
                nodes.add(entity);
            }
            edgeCnt++;
            nodeCnt++;
        }

        // select number of percentage for linking, check if possible
//        float p = 1f;
        float p_l = 0.5f;
        float p_e = 1 - p_l;
        drng.addNumber(1, p_l);
        drng.addNumber(2, p_e);

        //TODO switch between different linking strategies:

        // default: maximum one linking between two nodes


        Supplier<String> edgeLblSupplier = edgeLabelSupplier();

        Set<BigraphEntity> tmpFiltered =
                myNodes.values().stream()
                        .filter(x -> Objects.nonNull(x.getControl()) &&
                                x.getControl().getArity().getValue().intValue() > 0)
                        .collect(Collectors.toSet());
        int numOfLinkings = (int) Math.floor((tmpFiltered.size() * p) / 2);
//        System.out.println("#ofEdges: " + numOfLinkings);
        int cntE = 0, cntO = 0;
        if (numOfLinkings >= 1) {
            int connections = 0;
            List<BigraphEntity> collect = new ArrayList<>(tmpFiltered);
//            System.out.println("#nodes= " + collect.size());
            //solange connection != max || keine nodes mehr vorhanden
//            Collections.shuffle(collect);
            while ((connections < numOfLinkings)) { //collect.size() >= 2 &&
                int i1 = rnd.nextInt(collect.size());
                int i2 = rnd.nextInt(collect.size());
                if (i1 == i2) continue;

                int random = drng.getDistributedRandomNumber();
                //determines of outer names or edges should be constructed

                BigraphEntity.NodeEntity<Control> a = (BigraphEntity.NodeEntity<Control>) collect.get(i1);
                BigraphEntity.NodeEntity<Control> b = (BigraphEntity.NodeEntity<Control>) collect.get(i2);
                String edgeName = edgeLblSupplier.get();
                if (random == 1) {
                    BigraphEntity.OuterName newOuterName = (BigraphEntity.OuterName) builder.createNewOuterName(edgeName);
                    myOutername.put(edgeName, newOuterName);
                    builder.connectNodeToOuterName2(a, newOuterName);
                    builder.connectNodeToOuterName2(b, newOuterName);
                    cntO++;
                } else if (random == 2) {
                    BigraphEntity.Edge edge;
                    edge = (BigraphEntity.Edge) builder.createNewEdge(edgeName);
                    myEdges.put(edge.getName(), edge);
                    builder.connectToEdge(a, edge);
                    builder.connectToEdge(b, edge);
                    cntE++;
                }
                collect.remove(a);
                collect.remove(b);
                connections++;

            }

        }
//        System.out.println("CntO=" + cntO + " // CntE=" + cntE);
        stats = new double[]{tmpFiltered.size(), cntO, cntE, numOfLinkings};

        PureBigraphBuilder.InstanceParameter meta = builder.new InstanceParameter(
                builder.getLoadedEPackage(),
                signature,
                myRoots,
                mySites,
                myNodes,
                Collections.emptyMap(), myOutername, myEdges);
        builder.reset();
        return new PureBigraph(meta);
    }

    public int degreeOf(BigraphEntity nodeEntity) {
        //get all edges
        EObject instance = nodeEntity.getInstance();
        int cnt = 0;
        EStructuralFeature chldRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
        if (Objects.nonNull(chldRef)) {
            EList<EObject> childs = (EList<EObject>) instance.eGet(chldRef);
            cnt += childs.size();
        }
        EStructuralFeature prntRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if (Objects.nonNull(prntRef) && Objects.nonNull(instance.eGet(prntRef))) {
            cnt++;
        }
        return cnt;
    }

    public double[] getStats() {
        return stats;
    }

    private void setParentOfNode(final BigraphEntity node, final BigraphEntity parent) {
        EStructuralFeature prntRef = node.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        node.getInstance().eSet(prntRef, parent.getInstance());
    }

    private Supplier<Control> provideControlSupplier(Signature<? extends Control> signature) {
        return new Supplier<Control>() {
            private List<Control> controls = new ArrayList<>(signature.getControls());
//            private final Random controlRnd = new Random();

            @Override
            public Control get() {
                return controls.get(rnd.nextInt(controls.size()));
            }
        };
    }

    private Supplier<String> vertexLabelSupplier() {
        return new Supplier<String>() {
            private int id = 0;

            @Override
            public String get() {
                return "v" + id++;
            }
        };
    }

    private Supplier<String> edgeLabelSupplier() {
        return new Supplier<String>() {
            private int id = 0;

            @Override
            public String get() {
                return "e" + id++;
            }
        };
    }

    private static Graph<String, DefaultEdge> buildEmptySimpleDirectedGraph(Supplier<String> controlSupplier) {
//        vSupplier.get();
//        return GraphTypeBuilder.<String, DefaultEdge>directed()
        return GraphTypeBuilder.<String, DefaultEdge>undirected()
//                .vertexClass()
                .vertexSupplier(controlSupplier)
                .allowingMultipleEdges(false)
                .allowingSelfLoops(false)
                .edgeClass(DefaultEdge.class)
                .weighted(false)
                .buildGraph();
    }

//    private static void exportGraph(Graph g, String filename) {
//        JGraphXAdapter<String, DefaultEdge> graphAdapter = new JGraphXAdapter<String, DefaultEdge>(g);
////        mxIGraphLayout layout = new mxCircleLayout(graphAdapter);
////        mxIGraphLayout layout = new mxCompactTreeLayout(graphAdapter);
////        mxIGraphLayout layout = new mxOrthogonalLayout(graphAdapter);
//        mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter, SwingConstants.SOUTH);
//        layout.execute(graphAdapter.getDefaultParent());
//
//        BufferedImage image =
//                mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
//        try {
//            Path currentRelativePath = Paths.get("");
//            Path completePath = Paths.get(currentRelativePath.toAbsolutePath().toString(), filename + ".png");
////            String s = currentRelativePath.toAbsolutePath().toString();
//            File imgFile = new File(completePath.toUri());
//            if (!imgFile.exists()) {
//                imgFile.createNewFile();
//            }
////            URL location = JGraphTTests.class.getProtectionDomain().getCodeSource().getLocation();
////            File imgFile = new File(JGraphTTests.class.getClassLoader().getResource("somefile").toURI());
////            imgFile.createNewFile();
//            ImageIO.write(image, "PNG", imgFile);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
////        assertTrue(imgFile.exists());
//    }

}
