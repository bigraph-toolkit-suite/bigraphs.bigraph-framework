package org.bigraphs.framework.simulation.examples.bigrid;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.BigraphComposite;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.alg.generators.BigridGenerator;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicControl;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.elementary.Placings;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.reactivesystem.*;
import org.bigraphs.framework.core.utils.BigraphUtil;
import org.bigraphs.framework.simulation.modelchecking.predicates.SubBigraphMatchPredicate;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.collections.impl.factory.Maps;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.IntStream;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

public class BigridObject {
    String basePath = "/home/dominik/git/BigraphFramework/simulation/src/test/resources/dump/bigrid/circular/";

    BigridGenerator generator;
    DefaultDynamicSignature signature;
    PureBigraph bigraph;
    PureBigraph groundBigraph;
    BigridGenerator.DiscreteIons discreteIons;
    int numOfCols = 3, numOfRows = 3;

    public BigridObject(DefaultDynamicSignature signature) throws Exception {
        this(signature, 3, 3);
    }

    public BigridObject(DefaultDynamicSignature signature, int numOfCols, int numOfRows) throws Exception {
        this.numOfCols = numOfCols;
        this.numOfRows = numOfRows;
        this.generator = new BigridGenerator(signature);
        bigraph = generator.generate(numOfRows, numOfCols);
        this.signature = bigraph.getSignature();
        discreteIons = new BigridGenerator.DiscreteIons(this.signature);

        Bigraph tmp = pureBuilder(getSignature()).createRoot()
                .addSite().createBigraph();
        Bigraph d1 = tmp;
        for (int i = 1, n = numOfCols * numOfRows; i < n; i++) {
            d1 = (Bigraph) ops(d1).parallelProduct(tmp);
        }
        bigraph = (PureBigraph) ops(bigraph).nesting(d1).getOuterBigraph();

        // special cases for 2x2 bigrid
//        metaRule(BigridGenerator.DiscreteIons.NodeType.TOP_LEFT, BigridGenerator.DiscreteIons.NodeType.TOP_RIGHT);
//        metaRule(BigridGenerator.DiscreteIons.NodeType.BOTTOM_LEFT, BigridGenerator.DiscreteIons.NodeType.BOTTOM_RIGHT);
//        metaRule(BigridGenerator.DiscreteIons.NodeType.TOP_LEFT, BigridGenerator.DiscreteIons.NodeType.BOTTOM_LEFT);
//        metaRule(BigridGenerator.DiscreteIons.NodeType.TOP_RIGHT, BigridGenerator.DiscreteIons.NodeType.BOTTOM_RIGHT);

//        metaRule(BigridGenerator.DiscreteIons.NodeType.TOP_LEFT, BigridGenerator.DiscreteIons.NodeType.TOP_EDGE);
//        metaRule(BigridGenerator.DiscreteIons.NodeType.TOP_EDGE, BigridGenerator.DiscreteIons.NodeType.TOP_EDGE);
//        metaRule(BigridGenerator.DiscreteIons.NodeType.TOP_EDGE, BigridGenerator.DiscreteIons.NodeType.TOP_RIGHT);

//        metaRule(BigridGenerator.DiscreteIons.NodeType.TOP_LEFT, BigridGenerator.DiscreteIons.NodeType.LEFT_EDGE);
//        metaRule(BigridGenerator.DiscreteIons.NodeType.TOP_EDGE, BigridGenerator.DiscreteIons.NodeType.CENTER);
//        metaRule(BigridGenerator.DiscreteIons.NodeType.TOP_RIGHT, BigridGenerator.DiscreteIons.NodeType.RIGHT_EDGE);

//        metaRule(BigridGenerator.DiscreteIons.NodeType.LEFT_EDGE, BigridGenerator.DiscreteIons.NodeType.CENTER);
        //center generate 4 bigraphs for every direction
//        metaRule(BigridGenerator.DiscreteIons.NodeType.CENTER, BigridGenerator.DiscreteIons.NodeType.CENTER);
//        metaRule(BigridGenerator.DiscreteIons.NodeType.CENTER, BigridGenerator.DiscreteIons.NodeType.RIGHT_EDGE);

//        metaRule(BigridGenerator.DiscreteIons.NodeType.LEFT_EDGE, BigridGenerator.DiscreteIons.NodeType.BOTTOM_LEFT);
//        metaRule(BigridGenerator.DiscreteIons.NodeType.CENTER, BigridGenerator.DiscreteIons.NodeType.BOTTOM_EDGE);
//        metaRule(BigridGenerator.DiscreteIons.NodeType.RIGHT_EDGE, BigridGenerator.DiscreteIons.NodeType.BOTTOM_RIGHT);

//        metaRule(BigridGenerator.DiscreteIons.NodeType.BOTTOM_LEFT, BigridGenerator.DiscreteIons.NodeType.BOTTOM_EDGE);
//        metaRule(BigridGenerator.DiscreteIons.NodeType.BOTTOM_EDGE, BigridGenerator.DiscreteIons.NodeType.BOTTOM_EDGE);
//        metaRule(BigridGenerator.DiscreteIons.NodeType.BOTTOM_EDGE, BigridGenerator.DiscreteIons.NodeType.BOTTOM_RIGHT);
    }

    // we use a soft variant of a role-oriented concept (i.e., we specifically ascribing a "context" by the notion of a "role")
    //- Roles can encapsulate context-dependent behavior and properties
    // - Roles act as intermediates between objects: they mae the relationships explicit and making the complex
    // interactions between objects easier to grasp

    public Bigraph prepareItemAtNode(int indexRoot, DefaultDynamicControl control) throws Exception {
        return this.prepareItemAtNode(indexRoot, control, null, false);
    }

    public Bigraph prepareItemAtNode(int indexRoot, DefaultDynamicControl control, String linkName, boolean addSite) throws Exception {
        if (!getSignature().getControls().contains(control))
            throw new RuntimeException("Control does not exist in the given signature.");
        if (indexRoot >= getBigraph().getRoots().size())
            throw new RuntimeException("Index must be between [0, " + getBigraph().getRoots().size() + "]");

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy h = linkName != null ?
                (PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy) pureBuilder(getSignature()).createRoot()
                        .addChild(control, linkName) :
                pureBuilder(getSignature()).createRoot().addChild(control);
        PureBigraph tmp = addSite ?
                h.down()
                        .addSite().createBigraph() :
                h.createBigraph();

        //        %-----------------
        List<Bigraph> discreteParameterList = new LinkedList<>();
        discreteParameterList.add(tmp); //first entry reserved for node in question
//        eb(tmp, "tmp");
        // Fill the rest
        Placings<DefaultDynamicSignature>.Identity1 id1 = purePlacings(getSignature()).identity1();
        for (int i = 1; i < numOfCols * numOfRows; i++) {
            discreteParameterList.add(id1);
        }
        InstantiationMap map = InstantiationMap.create(numOfCols * numOfRows).map(0, indexRoot).map(indexRoot, 0);
        Bigraph bigraph = BigraphUtil.reorderBigraphs(discreteParameterList, map);
//        eb(bigraph, "xxxbigraphxxx");
        return bigraph;
//        %-----------------
//        Placings<DefaultDynamicSignature>.Permutation f1 = generator.getPlacings().permutation(indexRoot);
//        eb(f1, "f1");
//        Bigraph<DefaultDynamicSignature> f2 = ops(f1).juxtapose(tmp).getOuterBigraph();
////        eb(f1, "f2a");
//        if (getBigraph().getRoots().size() - indexRoot - 1 > 0) {
//            Placings<DefaultDynamicSignature>.Permutation p1 = generator.getPlacings().permutation(getBigraph().getRoots().size() - indexRoot - 1);
//            f2 = ops(f2).juxtapose(p1).getOuterBigraph();
////            eb(f2, "f2b");
//        }
//        return f2;
    }

    // TODO:

    //make it prime and ground
    public PureBigraph makeAgentCompatible(PureBigraph originalBigraph, Bigraph agentParams) throws Exception {
        if (agentParams == null || agentParams.getSites().size() > 0) {
            int siteCount = agentParams == null ? originalBigraph.getSites().size() - 1 :
                    agentParams.getSites().size() - 1;
            Bigraph holeFiller = IntStream.range(0, siteCount)
                    .mapToObj(x -> (Bigraph) generator.getPlacings().barren())
                    .reduce(generator.getPlacings().barren(), (a, b) -> {
                        try {
                            return ops(a).juxtapose(b).getOuterBigraph();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return generator.getPlacings().barren();
                    });
            agentParams = (agentParams != null) ? ops(agentParams).compose(holeFiller).getOuterBigraph() :
                    holeFiller;
//            eb(agentParams, "agentParams");
        }

        BigraphComposite<DefaultDynamicSignature> compose =
                ops(generator.getPlacings().merge(originalBigraph.getRoots().size())).compose(
                        originalBigraph
                );
//        eb(compose.getOuterBigraph(), "compose");
        Bigraph finished = compose.nesting(agentParams).getOuterBigraph();
//        eb(finished, "finished");

        return (PureBigraph) finished;
    }


    /**
     * A predicate that indicates that an entity has reached its target
     */
    public ReactiveSystemPredicate<PureBigraph> targetReached() throws Exception {
        PureBigraphBuilder<DefaultDynamicSignature> b = pureBuilder(getSignature());
        PureBigraph bigraph = b.createRoot().addChild("target", "reference")
                .addChild("source", "reference").down().addSite().createBigraph();
        return SubBigraphMatchPredicate.create(bigraph);
    }

    public List<ReactionRule<PureBigraph>> constructRule(BigridGenerator.DiscreteIons.NodeType from, BigridGenerator.DiscreteIons.NodeType to) throws Exception {
        List<PureBigraph> rSet = metaRule(from, to);
        if (rSet.size() > 1) {
            List<ReactionRule<PureBigraph>> reactionRules = new ArrayList<>();
            for (int i = 0; i < rSet.size(); i++) {
                PureBigraph pureBigraph = rSet.get(i);
                PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(pureBigraph.getSignature());
                builder.createRoot()
                        .addChild("occupiedBy").down()
                        .addSite()
                        .addChild("blocked").down().addChild("T").up()
                        .addChild("source", "reference").down().addSite()
                ;
                PureBigraph redex = builder.createRoot()
                        .addChild("occupiedBy").down()
                        .addChild("blocked").down().addChild("F").up()
                        .addSite()
                        .createBigraph();
                PureBigraph reactum = ops(getGenerator().getPlacings().symmetry11()).nesting(redex).getOuterBigraph();
                redex = ops(pureBigraph).nesting(redex).getOuterBigraph();
                reactum = ops(pureBigraph).nesting(reactum).getOuterBigraph();
                InstantiationMap map = InstantiationMap.create(3).map(0, 2).map(2, 0);
                //2: to South
                ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum, map, true).withLabel(from.name() + "_" + to.name() + "_" + i);
                reactionRules.add(rr);
            }
            return reactionRules;
        } else if (rSet.size() == 1) {
            PureBigraph pureBigraph = rSet.get(0);
            PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(pureBigraph.getSignature());
            builder.createRoot()
                    .addChild("occupiedBy").down()
                    .addSite()
                    .addChild("blocked").down().addChild("T").up()
                    .addChild("source", "reference").down().addSite()
            ;
            PureBigraph redex = builder.createRoot()
                    .addChild("occupiedBy").down()
                    .addChild("blocked").down().addChild("F").up()
                    .addSite()
                    .createBigraph();
//        PureBigraph redex = builder.createBigraph();

            PureBigraph reactum = ops(getGenerator().getPlacings().symmetry11()).nesting(redex).getOuterBigraph();
            redex = ops(pureBigraph).nesting(redex).getOuterBigraph();
            reactum = ops(pureBigraph).nesting(reactum).getOuterBigraph();

            InstantiationMap map = InstantiationMap.create(3).map(0, 2).map(2, 0);
            ReactionRule<PureBigraph> rr = new ParametricReactionRule<>(redex, reactum, map, true).withLabel(from.name() + "_" + to.name());
            return Collections.singletonList(rr);
        } else {
            return Collections.emptyList();
        }
    }

    public BigridGenerator getGenerator() {
        return generator;
    }

    public List<PureBigraph> metaRule(BigridGenerator.DiscreteIons.NodeType nt1, BigridGenerator.DiscreteIons.NodeType nt2) throws Exception {
        if (nt1.equals(nt2) && nt1.equals(BigridGenerator.DiscreteIons.NodeType.CENTER)) {
            return metaRuleCenter();
        } else {
            PureBigraph bnt1 = discreteIons.createByType(nt1);
            PureBigraph bnt2 = discreteIons.createByType(nt2);

            Pair<Optional<Bigraph>, Optional<Bigraph>> suitableWirings = createSuitableWirings(nt1, bnt1, nt2, bnt2);
            Optional<Bigraph> wiring = suitableWirings.getLeft();
            Optional<Bigraph> nextWiring = suitableWirings.getRight();

            bnt1 = (PureBigraph) ops(wiring.get()).compose(bnt1).getOuterBigraph();
            bnt2 = (PureBigraph) ops(nextWiring.get()).compose(bnt2).getOuterBigraph();
            // eb(bnt1 "bnt1");
            // eb(bnt2, "bnt1");
            PureBigraph d_ij = ops(bnt1).parallelProduct(bnt2).getOuterBigraph();
//            eb(d_ij, "d_ij");
//            print(d_ij);
            return Collections.singletonList(d_ij);
        }
    }

    public List<PureBigraph> metaRuleCenter() throws Exception {
        BigridGenerator.DiscreteIons.NodeType nt = BigridGenerator.DiscreteIons.NodeType.CENTER;
        PureBigraph bnt1 = discreteIons.createByType(nt);
        PureBigraph bnt2 = discreteIons.createByType(nt);

        //Horizontal East->West //Horizontal West->East
        Pair<Optional<Bigraph>, Optional<Bigraph>> wiringHorizontals = Pair.of(discreteIons.createRenaming(
                Maps.immutable.of(
                        "north", "north0",
                        "east", "ew",
                        "south", "south0",
                        "west", "west0").castToMap(), bnt1, generator.getLinkings()
        ), discreteIons.createRenaming(
                Maps.immutable.of(
                        "north", "north1",
                        "east", "east1",
                        "south", "south1",
                        "west", "ew").castToMap(), bnt2, generator.getLinkings()
        ));
        //Vertical South->North //Vertical North->South
        Pair<Optional<Bigraph>, Optional<Bigraph>> wiringsVerticals = Pair.of(discreteIons.createRenaming(
                Maps.immutable.of(
                        "north", "north0",
                        "east", "east0",
                        "south", "ns",
                        "west", "west0").castToMap(), bnt1, generator.getLinkings()
        ), discreteIons.createRenaming(
                Maps.immutable.of(
                        "north", "ns",
                        "east", "east1",
                        "south", "south1",
                        "west", "west1").castToMap(), bnt2, generator.getLinkings()
        ));


        Optional<Bigraph> hLeft = wiringHorizontals.getLeft();
        Optional<Bigraph> hRight = wiringHorizontals.getRight();
        Optional<Bigraph> vLeft = wiringsVerticals.getLeft();
        Optional<Bigraph> vRight = wiringsVerticals.getRight();

        PureBigraph tmp0, tmp1;
        List<PureBigraph> bigraphs = new ArrayList<>();
        tmp0 = (PureBigraph) ops(hLeft.get()).compose(bnt1).getOuterBigraph();
        tmp1 = (PureBigraph) ops(hRight.get()).compose(bnt2).getOuterBigraph();
        bigraphs.add(ops(tmp0).parallelProduct(tmp1).getOuterBigraph());
        tmp0 = (PureBigraph) ops(hRight.get()).compose(bnt1).getOuterBigraph();
        tmp1 = (PureBigraph) ops(hLeft.get()).compose(bnt2).getOuterBigraph();
        bigraphs.add(ops(tmp0).parallelProduct(tmp1).getOuterBigraph());

        tmp0 = (PureBigraph) ops(vLeft.get()).compose(bnt1).getOuterBigraph();
        tmp1 = (PureBigraph) ops(vRight.get()).compose(bnt2).getOuterBigraph();
        bigraphs.add(ops(tmp0).parallelProduct(tmp1).getOuterBigraph());
        tmp0 = (PureBigraph) ops(vRight.get()).compose(bnt1).getOuterBigraph();
        tmp1 = (PureBigraph) ops(vLeft.get()).compose(bnt2).getOuterBigraph();
        bigraphs.add(ops(tmp0).parallelProduct(tmp1).getOuterBigraph());

//        eb(bigraphs.get(0), "d_0");
//        eb(bigraphs.get(1), "d_1");
//        eb(bigraphs.get(2), "d_2");
//        eb(bigraphs.get(3), "d_3");
//        print((PureBigraph) d_ij);
        return bigraphs;
    }

    private Pair<Optional<Bigraph>, Optional<Bigraph>> createSuitableWirings(BigridGenerator.DiscreteIons.NodeType nt1,
                                                                             PureBigraph bNt1,
                                                                             BigridGenerator.DiscreteIons.NodeType nt2,
                                                                             PureBigraph bNt2) {

        try {
            String mName = nt1.name() + "2" + nt2.name();
            Method wiringMethod = BigridObject.class.getMethod(mName, PureBigraph.class, PureBigraph.class);
            System.out.println(wiringMethod);
            Pair<Optional<Bigraph>, Optional<Bigraph>> result
                    = (Pair<Optional<Bigraph>, Optional<Bigraph>>) wiringMethod.invoke(this, bNt1, bNt2);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("No suitable wirings found. Please check the supplied node types and bigrid nodes.");
        }
    }


    public Pair<Optional<Bigraph>, Optional<Bigraph>> TOP_LEFT2TOP_RIGHT(PureBigraph bNt1, PureBigraph bNt2) {
        return Pair.of(discreteIons.createRenaming(
                Maps.immutable.of("east", "ew0", "south", "south0")
                        .castToMap(),
                bNt1, generator.getLinkings()
        ), discreteIons.createRenaming(
                Maps.immutable.of("south", "south1", "west", "ew0").castToMap(),
                bNt2,
                generator.getLinkings()
        ));
    }

    public Pair<Optional<Bigraph>, Optional<Bigraph>> BOTTOM_LEFT2BOTTOM_RIGHT(PureBigraph bNt1, PureBigraph bNt2) {
        return Pair.of(discreteIons.createRenaming(
                Maps.immutable.of("north", "north0", "east", "ew")
                        .castToMap(),
                bNt1, generator.getLinkings()
        ), discreteIons.createRenaming(
                Maps.immutable.of("north", "north1", "west", "ew").castToMap(),
                bNt2,
                generator.getLinkings()
        ));
    }

    public Pair<Optional<Bigraph>, Optional<Bigraph>> BOTTOM_RIGHT2BOTTOM_LEFT(PureBigraph bNt1, PureBigraph bNt2) {
        return Pair.of(discreteIons.createRenaming(
                Maps.immutable.of("north", "north0", "west", "ew")
                        .castToMap(),
                bNt1, generator.getLinkings()
        ), discreteIons.createRenaming(
                Maps.immutable.of("north", "north1", "east", "ew").castToMap(),
                bNt2,
                generator.getLinkings()
        ));
    }

    public Pair<Optional<Bigraph>, Optional<Bigraph>> TOP_LEFT2BOTTOM_LEFT(PureBigraph bNt1, PureBigraph bNt2) {
        return Pair.of(discreteIons.createRenaming(
                Maps.immutable.of("east", "east0", "south", "ns").castToMap(),
                bNt1, generator.getLinkings()
        ), discreteIons.createRenaming(
                Maps.immutable.of("north", "ns", "east", "east1").castToMap(),
                bNt2,
                generator.getLinkings()
        ));
    }

    public Pair<Optional<Bigraph>, Optional<Bigraph>> TOP_RIGHT2BOTTOM_RIGHT(PureBigraph bNt1, PureBigraph bNt2) {
        return Pair.of(discreteIons.createRenaming(
                Maps.immutable.of("west", "west0", "south", "ns")
                        .castToMap(),
                bNt1, generator.getLinkings()
        ), discreteIons.createRenaming(
                Maps.immutable.of("north", "ns", "west", "west1").castToMap(),
                bNt2,
                generator.getLinkings()
        ));
    }

    public Pair<Optional<Bigraph>, Optional<Bigraph>> TOP_LEFT2TOP_EDGE(PureBigraph bNt1, PureBigraph bNt2) {
        return Pair.of(discreteIons.createRenaming(
                Maps.immutable.of("east", "ew0",
                                "south", "south0")
                        .castToMap(),
                bNt1, generator.getLinkings()
        ), discreteIons.createRenaming(
                Maps.immutable.of("east", "east",
                        "south", "south1",
                        "west", "ew0").castToMap(),
                bNt2,
                generator.getLinkings()
        ));
    }

    public Pair<Optional<Bigraph>, Optional<Bigraph>> TOP_EDGE2TOP_EDGE(PureBigraph bNt1, PureBigraph bNt2) {
        return Pair.of(discreteIons.createRenaming(
                Maps.immutable.of("east", "ew0",
                        "south", "south0",
                        "west", "west").castToMap(),
                bNt1, generator.getLinkings()
        ), discreteIons.createRenaming(
                Maps.immutable.of("east", "east",
                        "south", "south1",
                        "west", "ew0").castToMap(),
                bNt2,
                generator.getLinkings()
        ));
    }

    public Pair<Optional<Bigraph>, Optional<Bigraph>> TOP_EDGE2TOP_RIGHT(PureBigraph bNt1, PureBigraph bNt2) {
        return Pair.of(discreteIons.createRenaming(
                Maps.immutable.of("east", "ew0",
                                "south", "south0",
                                "west", "west")
                        .castToMap(),
                bNt1, generator.getLinkings()
        ), discreteIons.createRenaming(
                Maps.immutable.of(
                        "south", "south1",
                        "west", "ew0").castToMap(),
                bNt2,
                generator.getLinkings()
        ));
    }

    public Pair<Optional<Bigraph>, Optional<Bigraph>> TOP_LEFT2LEFT_EDGE(PureBigraph bNt1, PureBigraph bNt2) {
        return Pair.of(discreteIons.createRenaming(
                Maps.immutable.of(
                        "east", "east0",
                        "south", "ns0").castToMap(), bNt1, generator.getLinkings()
        ), discreteIons.createRenaming(
                Maps.immutable.of(
                        "east", "east1",
                        "north", "ns0").castToMap(), bNt2, generator.getLinkings()
        ));
    }

    public Pair<Optional<Bigraph>, Optional<Bigraph>> TOP_EDGE2CENTER(PureBigraph bNt1, PureBigraph bNt2) {
        return Pair.of(discreteIons.createRenaming(
                Maps.immutable.of(
                        "east", "east0",
                        "west", "west0",
                        "south", "ns0").castToMap(), bNt1, generator.getLinkings()
        ), discreteIons.createRenaming(
                Maps.immutable.of(
                        "east", "east1",
                        "west", "west1",
                        "north", "ns0").castToMap(), bNt2, generator.getLinkings()
        ));
    }

    public Pair<Optional<Bigraph>, Optional<Bigraph>> TOP_RIGHT2RIGHT_EDGE(PureBigraph bNt1, PureBigraph bNt2) {
        return Pair.of(discreteIons.createRenaming(
                Maps.immutable.of(
                        "west", "west0",
                        "south", "ns0").castToMap(), bNt1, generator.getLinkings()
        ), discreteIons.createRenaming(
                Maps.immutable.of(
                        "west", "west1",
                        "north", "ns0").castToMap(), bNt2, generator.getLinkings()
        ));
    }

    public Pair<Optional<Bigraph>, Optional<Bigraph>> LEFT_EDGE2CENTER(PureBigraph bNt1, PureBigraph bNt2) {
        return Pair.of(discreteIons.createRenaming(
                Maps.immutable.of(
                        "north", "north0",
                        "east", "ew0",
                        "south", "south0").castToMap(), bNt1, generator.getLinkings()
        ), discreteIons.createRenaming(
                Maps.immutable.of(
                        "north", "north1",
                        "east", "east1",
                        "south", "south1",
                        "west", "ew0").castToMap(), bNt2, generator.getLinkings()
        ));
    }

    public Pair<Optional<Bigraph>, Optional<Bigraph>> CENTER2CENTER(PureBigraph bNt1, PureBigraph bNt2) {
        return Pair.of(discreteIons.createRenaming(
                Maps.immutable.of(
                        "north", "north0",
                        "east", "ew0",
                        "south", "south0",
                        "west", "west0").castToMap(), bNt1, generator.getLinkings()
        ), discreteIons.createRenaming(
                Maps.immutable.of(
                        "north", "north1",
                        "east", "east1",
                        "south", "south1",
                        "west", "ew0").castToMap(), bNt2, generator.getLinkings()
        ));
    }

    public Pair<Optional<Bigraph>, Optional<Bigraph>> CENTER2RIGHT_EDGE(PureBigraph bNt1, PureBigraph bNt2) {
        return Pair.of(discreteIons.createRenaming(
                Maps.immutable.of(
                        "north", "north0",
                        "east", "ew0",
                        "south", "south0",
                        "west", "west0").castToMap(), bNt1, generator.getLinkings()
        ), discreteIons.createRenaming(
                Maps.immutable.of(
                        "north", "north1",
                        "south", "south1",
                        "west", "ew0").castToMap(), bNt2, generator.getLinkings()
        ));
    }

    public Pair<Optional<Bigraph>, Optional<Bigraph>> LEFT_EDGE2BOTTOM_LEFT(PureBigraph bNt1, PureBigraph bNt2) {
        return Pair.of(discreteIons.createRenaming(
                Maps.immutable.of(
                        "north", "north0",
                        "east", "east0",
                        "south", "ns").castToMap(), bNt1, generator.getLinkings()
        ), discreteIons.createRenaming(
                Maps.immutable.of(
                        "north", "ns",
                        "east", "east1").castToMap(), bNt2, generator.getLinkings()
        ));
    }

    public Pair<Optional<Bigraph>, Optional<Bigraph>> CENTER2BOTTOM_EDGE(PureBigraph bNt1, PureBigraph bNt2) {
        return Pair.of(discreteIons.createRenaming(
                Maps.immutable.of(
                        "north", "north0",
                        "east", "east0",
                        "south", "ns",
                        "west", "west0").castToMap(), bNt1, generator.getLinkings()
        ), discreteIons.createRenaming(
                Maps.immutable.of(
                        "north", "ns",
                        "east", "east1",
                        "west", "west1").castToMap(), bNt2, generator.getLinkings()
        ));
    }

    public Pair<Optional<Bigraph>, Optional<Bigraph>> RIGHT_EDGE2BOTTOM_RIGHT(PureBigraph bNt1, PureBigraph bNt2) {
        return Pair.of(discreteIons.createRenaming(
                Maps.immutable.of(
                        "north", "north0",
                        "south", "ns",
                        "west", "west0").castToMap(), bNt1, generator.getLinkings()
        ), discreteIons.createRenaming(
                Maps.immutable.of(
                        "north", "ns",
                        "west", "west1").castToMap(), bNt2, generator.getLinkings()
        ));
    }

    public Pair<Optional<Bigraph>, Optional<Bigraph>> BOTTOM_LEFT2BOTTOM_EDGE(PureBigraph bNt1, PureBigraph bNt2) {
        return Pair.of(discreteIons.createRenaming(
                Maps.immutable.of(
                        "north", "north0",
                        "east", "ew").castToMap(), bNt1, generator.getLinkings()
        ), discreteIons.createRenaming(
                Maps.immutable.of(
                        "north", "north1",
                        "east", "east1",
                        "west", "ew").castToMap(), bNt2, generator.getLinkings()
        ));
    }

    public Pair<Optional<Bigraph>, Optional<Bigraph>> BOTTOM_EDGE2BOTTOM_EDGE(PureBigraph bNt1, PureBigraph bNt2) {
        return Pair.of(discreteIons.createRenaming(
                Maps.immutable.of(
                        "north", "north0",
                        "east", "ew",
                        "west", "west0").castToMap(), bNt1, generator.getLinkings()
        ), discreteIons.createRenaming(
                Maps.immutable.of(
                        "north", "north1",
                        "east", "east1",
                        "west", "ew").castToMap(), bNt2, generator.getLinkings()
        ));
    }

    public Pair<Optional<Bigraph>, Optional<Bigraph>> BOTTOM_EDGE2BOTTOM_RIGHT(PureBigraph bNt1, PureBigraph bNt2) {
        return Pair.of(discreteIons.createRenaming(
                Maps.immutable.of(
                        "north", "north0",
                        "east", "ew",
                        "west", "west0").castToMap(), bNt1, generator.getLinkings()
        ), discreteIons.createRenaming(
                Maps.immutable.of(
                        "north", "north1",
                        "west", "ew").castToMap(), bNt2, generator.getLinkings()
        ));
    }

    public PureBigraph getBigraph() {
        return bigraph;
    }

    public DefaultDynamicSignature getSignature() {
        return signature;
    }

    public void eb(Bigraph<?> bigraph, String name) {
        eb(bigraph, name, true);
    }

    public void eb(Bigraph<?> bigraph, String name, boolean asTree) {
        try {
            BigraphGraphvizExporter.toPNG(bigraph, asTree, new File(basePath + name + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void print(PureBigraph bigraph) {
        try {
            BigraphFileModelManagement.Store.exportAsInstanceModel(bigraph, System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Bigraph preparePathSTNodeAt(int indexRoot, String sourceOrTarget, String linkName, String TF_Blocked, boolean addSite) throws Exception {
        if (indexRoot >= getBigraph().getRoots().size())
            throw new RuntimeException("Index must be between [0, " + getBigraph().getRoots().size() + "]");

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy h = pureBuilder(getSignature()).createRoot()
                .addChild("occupiedBy").down().addChild("blocked").down().addChild(TF_Blocked).up().up(); //.down().addSite();
        h = linkName != null ?
                h.down()
                        .addChild(sourceOrTarget, linkName).down() :
                h.down().addChild(sourceOrTarget).down();
        PureBigraph tmp = addSite ?
                h//.down()
                        .addSite().createBigraph() :
                h.createBigraph();

        List<Bigraph> discreteParameterList = new LinkedList<>();
        discreteParameterList.add(tmp); //first entry reserved for node in question
        // Fill the rest
        Placings<DefaultDynamicSignature>.Identity1 id1 = purePlacings(getSignature()).identity1();
        for (int i = 1; i < numOfCols * numOfRows; i++) {
            discreteParameterList.add(id1);
        }
        InstantiationMap map = InstantiationMap.create(numOfCols * numOfRows).map(0, indexRoot).map(indexRoot, 0);
        Bigraph bigraph = BigraphUtil.reorderBigraphs(discreteParameterList, map);
        return bigraph;
    }

    public Bigraph prepareSourceNodeAt(int indexRoot, String linkName, String TF_Blocked, boolean addSite) throws Exception {
        return preparePathSTNodeAt(indexRoot, "source", linkName, TF_Blocked, addSite);
    }

    public Bigraph prepareTargetNodeAt(int indexRoot, String linkName, String TF_Blocked, boolean addSite) throws Exception {
        return preparePathSTNodeAt(indexRoot, "target", linkName, TF_Blocked, addSite);
    }

    public Bigraph prepareOccupiedBlockedNodeAt(int indexRoot, String TF_Blocked, boolean addSite) throws Exception {
        if (indexRoot >= getBigraph().getRoots().size())
            throw new RuntimeException("Index must be between [0, " + getBigraph().getRoots().size() + "]");

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy h = pureBuilder(getSignature()).createRoot()
                .addChild("occupiedBy").down().addChild("blocked").down().addChild(TF_Blocked).up().up(); //.down().addSite();
        PureBigraph tmp = addSite ?
                h
                        .addSite().createBigraph() :
                h.createBigraph();

        List<Bigraph> discreteParameterList = new LinkedList<>();
        discreteParameterList.add(tmp); //first entry reserved for node in question
        // Fill the rest
        Placings<DefaultDynamicSignature>.Identity1 id1 = purePlacings(getSignature()).identity1();
        for (int i = 1; i < numOfCols * numOfRows; i++) {
            discreteParameterList.add(id1);
        }
        InstantiationMap map = InstantiationMap.create(numOfCols * numOfRows).map(0, indexRoot).map(indexRoot, 0);
        Bigraph bigraph = BigraphUtil.reorderBigraphs(discreteParameterList, map);
        return bigraph;
    }
}
