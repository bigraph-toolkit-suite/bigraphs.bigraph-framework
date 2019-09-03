package de.tudresden.inf.st.bigraphs.core.utils;

import de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidArityOfControlException;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.MutableBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.tudresden.inf.st.bigraphs.core.utils.RandomBigraphGenerator.LinkStrategy.NONE;

/**
 * @author Dominik Grzelak
 */
public class PureBigraphGeneration extends RandomBigraphGenerator {

    private HashMap<Integer, BigraphEntity.RootEntity> myRoots = new LinkedHashMap<>();
    private HashMap<String, BigraphEntity.NodeEntity> myNodes = new LinkedHashMap<>();
    private HashMap<Integer, BigraphEntity.SiteEntity> mySites = new LinkedHashMap<>();
    private HashMap<String, BigraphEntity.Edge> myEdges = new LinkedHashMap<>();
    private HashMap<String, BigraphEntity.OuterName> myOutername = new LinkedHashMap<>();
    private HashMap<String, BigraphEntity.InnerName> myInnername = new LinkedHashMap<>();
    private DistributedRandomNumberGenerator drng;
    private Supplier<String> edgeLblSupplier;
    private MutableBuilder<Signature<? extends Control>> builder;
    private int numOfLinkings;
    private int cntE = 0, cntO = 0;
    private Set<BigraphEntity> tmpFiltered;

    public PureBigraph generate(DefaultDynamicSignature signature, int t, int n, float p) {
//        rnd = new Random();
        drng = new DistributedRandomNumberGenerator();
//        Random mRandom = new Random();
        myRoots.clear();
        myNodes.clear();
        mySites.clear();
        myEdges.clear();
        myOutername.clear();
        myInnername.clear();
        builder = PureBigraphBuilder.newMutableBuilder(signature);
        builder.reset();
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

            BigraphEntity entity = nodes.get(rnd.nextInt(nodes.size()));
            // get an existing node randomly
//                    V u = nodes.get(this.rng.nextInt(nodes.size()));
//            BigraphEntity entity = null; //nodes.get(rnd.nextInt(nodes.size()));
//            boolean created_edge = false;
////            do {
//                entity = nodes.get(rnd.nextInt(nodes.size()));
//                double degree = degreeOf(entity);
////            double attach_prob = (degree + 1) / (mGraph.getEdgeCount() + mGraph.getVertexCount() - 1);
//                double attach_prob = (degree + 1) / (edgeCnt + nodeCnt);
//                if (attach_prob >= rnd.nextDouble())
//                    created_edge = true;
//            } while (!created_edge);
//            if(!created_edge) continue;
            String vlbl = vertexLabelSupplier.get();
            BigraphEntity newNode = builder.createNewNode(controlSupplier.get(), vertexLabelSupplier.get());
            myNodes.put(vlbl, (BigraphEntity.NodeEntity) newNode);

            //add as parent
            setParentOfNode(newNode, entity);
            nodes.add(newNode);
//            if (i > 1) {
//                nodes.add(entity);
//            }
            edgeCnt++;
            nodeCnt++;
        }

        // select number of percentage for linking, check if possible
//        float p = 1f;
        float p_l = 0.5f;
        float p_e = 0.8f;
        drng.addNumber(1, p_l);
        drng.addNumber(2, p_e);

        //TODO switch between different linking strategies:

        // default: maximum one linking between two nodes


        edgeLblSupplier = edgeLabelSupplier();

        tmpFiltered =
                myNodes.values().stream()
                        .filter(x -> Objects.nonNull(x.getControl()) &&
                                x.getControl().getArity().getValue().intValue() > 0)
                        .collect(Collectors.toSet());
        numOfLinkings = (int) Math.floor((tmpFiltered.size() * p) / 2);
//        System.out.println("#ofEdges: " + numOfLinkings);
        cntE = 0;
        cntO = 0;
        if (linkStrategy != NONE && numOfLinkings >= 1) {
            switch (linkStrategy) {
                case MIN_LINKING:
                    minLinking();
                    break;
                case MAXIMAL_DEGREE_ASSORTATIVE:
                case MAXIMAL_DEGREE_DISASSORTATIVE:
                    maximalDegreeLinking();
                    break;
                default:
                    break;
            }
        }

        stats = new double[]{tmpFiltered.size(), cntO, cntE, numOfLinkings};

        PureBigraphBuilder.InstanceParameter meta = builder.new InstanceParameter(
                builder.getLoadedEPackage(),
                signature,
                myRoots,
                mySites,
                myNodes,
                Collections.emptyMap(), myOutername, myEdges);
//        builder.reset();
        return new PureBigraph(meta);
    }

    private void maximalDegreeLinking() {
        List<BigraphEntity> collect = new ArrayList<>(tmpFiltered);
        HashMap<BigraphEntity, Boolean> visited = new HashMap<>();
        int n = (int) Math.sqrt(collect.size());
        System.out.println("N iterations=" + n);
        while (collect.size() >= 4) {

            int a, b, c, d;
            do {
                a = rnd.nextInt(collect.size());
                b = rnd.nextInt(collect.size());
                c = rnd.nextInt(collect.size());
                d = rnd.nextInt(collect.size());
            } while (!areNotEqual(a, b, c, d));

            BigraphEntity.NodeEntity<Control> entity1 = (BigraphEntity.NodeEntity<Control>) collect.get(a);
            BigraphEntity.NodeEntity<Control> entity2 = (BigraphEntity.NodeEntity<Control>) collect.get(b);
            BigraphEntity.NodeEntity<Control> entity3 = (BigraphEntity.NodeEntity<Control>) collect.get(c);
            BigraphEntity.NodeEntity<Control> entity4 = (BigraphEntity.NodeEntity<Control>) collect.get(d);
//            List<BigraphEntity.NodeEntity<Control>> entityList = new ArrayList<>();
//            entityList.add(entity1);
//            entityList.add(entity2);
//            entityList.add(entity3);
//            entityList.add(entity4);
            List<BigraphEntity.NodeEntity<Control>> entityList = Stream.of(entity1, entity2, entity3, entity4)
//                    .sorted(Comparator.comparingInt(this::getPortCount))
                    .sorted(Comparator.comparingInt(x -> x.getControl().getArity().getValue().intValue()))
                    .collect(Collectors.toList());

            boolean good = true;
            for (BigraphEntity each : entityList) {
                try {
                    builder.checkIfNodeIsConnectable((BigraphEntity.NodeEntity<Control>) each);
                } catch (Exception e) {
//                    e.printStackTrace();
                    collect.remove(each);
                    good = false;
                }
//                if (getPortCount(each) >= each.getControl().getArity().getValue().longValue())
            }
            if (!good) continue;
//            x.getControl().getArity().compareTo(y.getControl().getArity())
//            if (random == 1) {
//                BigraphEntity.OuterName newOuterName = (BigraphEntity.OuterName) builder.createNewOuterName(edgeName);
//                myOutername.put(edgeName, newOuterName);
//                builder.connectNodeToOuterName2(a, newOuterName);
//                builder.connectNodeToOuterName2(b, newOuterName);
//                cntO++;
//            } else if (random == 2) {
            if (linkStrategy == LinkStrategy.MAXIMAL_DEGREE_ASSORTATIVE) {
                try {
                    builder.checkIfNodeIsConnectable(entityList.get(0));
                    builder.checkIfNodeIsConnectable(entityList.get(1));
                    BigraphEntity.Edge edge = (BigraphEntity.Edge) builder.createNewEdge(edgeLblSupplier.get());
                    builder.connectToEdge(entityList.get(0), edge); // lowest degree
                    builder.connectToEdge(entityList.get(1), edge); // lowest degree
                    myEdges.put(edge.getName(), edge);
                } catch (Exception e) {
//                    e.printStackTrace();
                }

                try {
                    builder.checkIfNodeIsConnectable(entityList.get(2));
                    builder.checkIfNodeIsConnectable(entityList.get(3));
                    BigraphEntity.Edge edge = (BigraphEntity.Edge) builder.createNewEdge(edgeLblSupplier.get());
                    builder.connectToEdge(entityList.get(2), edge); // highest degree
                    builder.connectToEdge(entityList.get(3), edge); // highest degree
                    myEdges.put(edge.getName(), edge);
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            } else if (linkStrategy == LinkStrategy.MAXIMAL_DEGREE_DISASSORTATIVE) {
                try {
                    builder.checkIfNodeIsConnectable(entityList.get(0));
                    builder.checkIfNodeIsConnectable(entityList.get(3));
                    BigraphEntity.Edge edge = (BigraphEntity.Edge) builder.createNewEdge(edgeLblSupplier.get());
                    builder.connectToEdge(entityList.get(0), edge); // lowest degree
                    builder.connectToEdge(entityList.get(3), edge); // highest degree
                    myEdges.put(edge.getName(), edge);
                } catch (Exception e) {
//                    e.printStackTrace();
                }

                try {
                    builder.checkIfNodeIsConnectable(entityList.get(1));
                    builder.checkIfNodeIsConnectable(entityList.get(2));
                    BigraphEntity.Edge edge = (BigraphEntity.Edge) builder.createNewEdge(edgeLblSupplier.get());
                    builder.connectToEdge(entityList.get(1), edge); // low-high degree
                    builder.connectToEdge(entityList.get(2), edge); // low-high degree
                    myEdges.put(edge.getName(), edge);
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            }


//            collect.remove(entity2);
//            collect.remove(entity3);
//            collect.remove(entity4);
        }
    }

    public int getPortCount(BigraphEntity node) {
//        if (!BigraphEntityType.isNode(node)) return 0;
        EObject instance = node.getInstance();
        EStructuralFeature portRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PORT);
        EList<EObject> portList = (EList<EObject>) instance.eGet(portRef);
        return portList.size();
    }

    boolean areNotEqual(int... nums) {
        Arrays.sort(nums);
        for (int i = 0; i < nums.length - 1; i++) {
            if (nums[i] == nums[i + 1])
                return false;
        }
        return true;
    }

    private void maxLinking2() {

    }


    private void minLinking() {
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

}
