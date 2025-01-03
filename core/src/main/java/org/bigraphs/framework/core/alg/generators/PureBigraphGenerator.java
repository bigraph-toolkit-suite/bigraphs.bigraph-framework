package org.bigraphs.framework.core.alg.generators;

import org.bigraphs.framework.core.AbstractEcoreSignature;
import org.bigraphs.framework.core.Control;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.pure.MutableBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.utils.DistributedRandomNumberGenerator;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.emf.ecore.EPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A random generator for <i>pure bigraphs</i>. Uses {@link RandomBigraphGeneratorSupport}.
 *
 * @author Dominik Grzelak
 */
public class PureBigraphGenerator extends RandomBigraphGeneratorSupport {
    private final Logger logger = LoggerFactory.getLogger(PureBigraphGenerator.class);
    DefaultDynamicSignature signature;
    private HashMap<Integer, BigraphEntity.RootEntity> newRoots = new LinkedHashMap<>();
    private HashMap<String, BigraphEntity.NodeEntity> newNodes = new LinkedHashMap<>();
    private HashMap<Integer, BigraphEntity.SiteEntity> newSites = new LinkedHashMap<>();
    private HashMap<String, BigraphEntity.Edge> newEdges = new LinkedHashMap<>();
    private HashMap<String, BigraphEntity.OuterName> newOuterNames = new LinkedHashMap<>();
    private HashMap<String, BigraphEntity.InnerName> newInnerNames = new LinkedHashMap<>();
    private DistributedRandomNumberGenerator drng;
    private Supplier<String> edgeLblSupplier;
    private MutableBuilder<AbstractEcoreSignature<? extends Control>> builder;
    private int numOfLinkings;
    private int cntE = 0, cntO = 0;
    private Set<BigraphEntity> nodesWithPositiveArity;

    public PureBigraphGenerator(DefaultDynamicSignature signature) {
        this(signature, null);
    }

    public PureBigraphGenerator(DefaultDynamicSignature signature, EPackage metaModel) {
        this.signature = signature;
        this.builder = Objects.nonNull(metaModel) ?
                MutableBuilder.newMutableBuilder(signature, metaModel) :
                MutableBuilder.newMutableBuilder(signature);
    }

    public EPackage getModelPackage() {
        if (Objects.nonNull(builder))
            return builder.getMetaModel();
        return null;
    }

    public PureBigraph generate(int t, int n, int s) {
        return this.generate(t, n, s, 0.0f, 0.0f, 0.0f);
    }

    public PureBigraph generate(int t, int n, float p) {
        return this.generate(t, n, 0, p, 0.5f, 0.5f);
    }

    public PureBigraph generate(int t, int n, int s, float p) {
        return this.generate(t, n, s, p, 0.5f, 0.5f);
    }


    public PureBigraph generate(int t, int n, float p, float p_l, float p_e) {
        return this.generate(t, n, 0, p, p_l, p_e);
    }

    /**
     * Number of roots {@literal t} must be greater or equal {@literal 0}.
     * <p>
     * Note that the number of nodes {@literal m} is: {@literal m = n - t}.
     *
     * @param t   number of roots
     * @param n   number of nodes (inclusive {@literal t})
     * @param s   number of sites (additionally to {@literal m})
     * @param p   proportion of the nodes {@literal n} being used for linking at all
     * @param p_l probability (or "weight") that an outer name will be created
     * @param p_e probability (or "weight") that an edge will be created
     * @return a random bigraph according to the provided parameters
     */
    public PureBigraph generate(int t, int n, int s, float p, float p_l, float p_e) {
        drng = new DistributedRandomNumberGenerator();
        newRoots.clear();
        newNodes.clear();
        newSites.clear();
        newEdges.clear();
        newOuterNames.clear();
        newInnerNames.clear();
        builder.reset();
        Supplier<Control> controlSupplier = provideControlSupplier(signature);
        Supplier<String> vertexLabelSupplier = vertexLabelSupplier();
        edgeLblSupplier = edgeLabelSupplier();
        List<BigraphEntity> nodes = new ArrayList<>(n);

        if (t <= 0) {
            throw new RuntimeException("Number of roots t must be greater or equal zero.");
        }

        logger.debug("Number of roots: {}", t);
        logger.debug("Number of nodes: {}", (n - t));

        // Place graph generation
        int i;
        for (i = 0; i < t; ++i) {
            BigraphEntity<?> newRoot = builder.createNewRoot(i);
            newRoots.put(i, (BigraphEntity.RootEntity) newRoot);
            nodes.add(newRoot);
        }

        for (i = t; i < n; i++) {
            BigraphEntity<?> entity = nodes.get(rnd.nextInt(nodes.size()));
            String vlbl = vertexLabelSupplier.get();
            // create a new node with a random control
            BigraphEntity<?> newNode = builder.createNewNode(controlSupplier.get(), vlbl);
            newNodes.put(vlbl, (BigraphEntity.NodeEntity<?>) newNode);

            //add as parent
            setParentOfNode(newNode, entity);
            nodes.add(newNode);
//            if (i > 1) { // not necessary since we check for t >= 0
            nodes.add(entity);
//            }
        }

        if (s > 0) {
            AtomicInteger siteCnt = new AtomicInteger(s);
            // Add sites to leave
            // Take the first leaves to add sites ...
            nodes.stream().filter(x -> !builder.isRoot(x.getInstance()))
                    .forEach(x -> {
                        boolean b = builder.hasChildNodes(x);
//                        System.out.println("Node " + x + " is Leave? " + (!b));
                        if (!b) {
//                            System.out.println("site added to leave");
                            int i1 = siteCnt.decrementAndGet();
                            if (i1 >= 0) {
                                BigraphEntity newSite = builder.createNewSite(i1);
                                newSites.put(i1, (BigraphEntity.SiteEntity) newSite);
                                setParentOfNode(newSite, x);
                            }
                        }
                    });
            // add remaining sites anywhere
            if (siteCnt.get() > 0) {
                nodes.stream().filter(x -> !builder.isRoot(x.getInstance()))
                        .limit(siteCnt.get())
                        .forEach(x -> {
                            // add site
                            int i1 = siteCnt.decrementAndGet();
                            if (i1 >= 0) {
                                BigraphEntity newSite = builder.createNewSite(i1);
                                newSites.put(i1, (BigraphEntity.SiteEntity) newSite);
                                setParentOfNode(newSite, x);
                            }
                        });
            }

        }

        // Link graph generation
        // select number of percentage for linking, check if possible
        drng.addNumber(1, p_l);
        drng.addNumber(2, p_e);
        logger.debug("Probability that outer names will be created: {}", p_l);
        logger.debug("Probability that edges will be created: {}", p_e);

        // default: maximum one linking between two nodes
        nodesWithPositiveArity =
                newNodes.values().stream()
                        .filter(x -> Objects.nonNull(x.getControl()) &&
                                x.getControl().getArity().getValue().intValue() > 0)
                        .collect(Collectors.toSet());
        numOfLinkings = (int) Math.floor((nodesWithPositiveArity.size() * p) / 2);
        logger.debug("Total number of links being created: {}", numOfLinkings);
        cntE = 0;
        cntO = 0;
        if (linkStrategy != LinkStrategy.NONE && numOfLinkings >= 1) {
            switch (linkStrategy) {
                case MIN_LINKING:
                    pairwiseLinking(numOfLinkings);
                    break;
                case MAXIMAL_DEGREE_ASSORTATIVE:
                case MAXIMAL_DEGREE_DISASSORTATIVE:
                    int nnn = (int) Math.floor((nodesWithPositiveArity.size() * p));
                    maximalDegreeLinking(nnn);
                    break;
                default:
                    break;
            }
        }

        stats = new double[]{nodesWithPositiveArity.size(), cntO, cntE, numOfLinkings};

        PureBigraphBuilder.InstanceParameter meta = builder.new InstanceParameter(
                builder.getMetaModel(),
                signature,
                newRoots,
                newSites,
                newNodes,
                Collections.emptyMap(), newOuterNames, newEdges);
        builder.reset();
        return new PureBigraph(meta);
    }

    private void maximalDegreeLinking(int numOfLinkings) {
        int connectionCount = 0;
        MutableList<BigraphEntity> collect = Lists.mutable.withAll(nodesWithPositiveArity);
//        List<BigraphEntity> collect = new ArrayList<>(nodesWithPositiveArity);
//        HashMap<BigraphEntity, Boolean> visited = new HashMap<>();
        int max = collect.size() - numOfLinkings;
        while (collect.size() >= max && collect.size() >= 4) { //>= 4 && (connectionCount < numOfLinkings)) {

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

            if (linkStrategy == LinkStrategy.MAXIMAL_DEGREE_ASSORTATIVE) {
                try {
                    builder.checkIfNodeIsConnectable(entityList.get(0));
                    builder.checkIfNodeIsConnectable(entityList.get(1));
                    BigraphEntity.Edge edge = (BigraphEntity.Edge) builder.createNewEdge(edgeLblSupplier.get());
                    builder.connectToEdge(entityList.get(0), edge); // lowest degree
                    builder.connectToEdge(entityList.get(1), edge); // lowest degree
                    newEdges.put(edge.getName(), edge);
                    connectionCount++;
                } catch (Exception e) {
                    logger.error(e.toString());
                }

                try {
                    builder.checkIfNodeIsConnectable(entityList.get(2));
                    builder.checkIfNodeIsConnectable(entityList.get(3));
                    BigraphEntity.Edge edge = (BigraphEntity.Edge) builder.createNewEdge(edgeLblSupplier.get());
                    builder.connectToEdge(entityList.get(2), edge); // highest degree
                    builder.connectToEdge(entityList.get(3), edge); // highest degree
                    newEdges.put(edge.getName(), edge);
                    connectionCount++;
                } catch (Exception e) {
                    logger.error(e.toString());
                }
            } else if (linkStrategy == LinkStrategy.MAXIMAL_DEGREE_DISASSORTATIVE) {
                try {
                    builder.checkIfNodeIsConnectable(entityList.get(0));
                    builder.checkIfNodeIsConnectable(entityList.get(3));
                    BigraphEntity.Edge edge = (BigraphEntity.Edge) builder.createNewEdge(edgeLblSupplier.get());
                    builder.connectToEdge(entityList.get(0), edge); // lowest degree
                    builder.connectToEdge(entityList.get(3), edge); // highest degree
                    newEdges.put(edge.getName(), edge);
                    connectionCount++;
                } catch (Exception e) {
                    logger.error(e.toString());
                }

                try {
                    builder.checkIfNodeIsConnectable(entityList.get(1));
                    builder.checkIfNodeIsConnectable(entityList.get(2));
                    BigraphEntity.Edge edge = (BigraphEntity.Edge) builder.createNewEdge(edgeLblSupplier.get());
                    builder.connectToEdge(entityList.get(1), edge); // low-high degree
                    builder.connectToEdge(entityList.get(2), edge); // low-high degree
                    newEdges.put(edge.getName(), edge);
                    connectionCount++;
                } catch (Exception e) {
                    logger.error(e.toString());
                }
            }
        }
    }

    @Override
    public PureBigraphGenerator setLinkStrategy(LinkStrategy linkStrategy) {
        return (PureBigraphGenerator) super.setLinkStrategy(linkStrategy);
    }

    private boolean areNotEqual(int... nums) {
        Arrays.sort(nums);
        for (int i = 0; i < nums.length - 1; i++) {
            if (nums[i] == nums[i + 1])
                return false;
        }
        return true;
    }

    private void maxLinking2() {

    }

    /**
     * Creates at maximum only one connection between two nodes for the given number of nodes
     * {@code numOfLinkings}, generated before.
     *
     * @param numOfLinkings maximum number of pairwise connections to create
     */
    private void pairwiseLinking(final int numOfLinkings) {
        int connectionCount = 0;
//        List<BigraphEntity> collect = new ArrayList<>(nodesWithPositiveArity);
        MutableList<BigraphEntity> collect = Lists.mutable.withAll(nodesWithPositiveArity);

        while ((connectionCount < numOfLinkings)) {
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
                newOuterNames.put(edgeName, newOuterName);
                builder.connectNodeToOuterName2(a, newOuterName);
                builder.connectNodeToOuterName2(b, newOuterName);
                cntO++;
            } else if (random == 2) {
                BigraphEntity.Edge edge;
                edge = (BigraphEntity.Edge) builder.createNewEdge(edgeName);
                newEdges.put(edge.getName(), edge);
                builder.connectToEdge(a, edge);
                builder.connectToEdge(b, edge);
                cntE++;
            }
            collect.remove(a);
            collect.remove(b);
            connectionCount++;

        }
    }

}
