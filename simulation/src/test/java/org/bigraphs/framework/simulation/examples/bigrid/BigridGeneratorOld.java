package org.bigraphs.framework.simulation.examples.bigrid;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.datatypes.NamedType;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.elementary.Linkings;
import org.bigraphs.framework.core.impl.elementary.Placings;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.utils.BigraphUtil;
import org.eclipse.collections.impl.factory.Maps;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

/**
 * THIS CLASS IS RETAINED FOR LEGACY PURPOSES.
 * This class exists to ensure support and demonstration for older implementations.
 * <p>
 * A class to generate bigrids (legacy variant).
 * <p>
 * The bigrid is constructed via a composition-based approach using only elementary bigraphs instead of using
 * the bigraph builder (e.g., {@link PureBigraphBuilder}).
 * <p>
 * The elementary bigraphs are collected within the inner class {@link DiscreteIons} and are accessible
 * via its methods, e.g., {@link DiscreteIons#bottomLeft()} for the bottom-left corner bigrid node.
 *
 * @author Dominik Grzelak
 */
@Deprecated
public class BigridGeneratorOld {

    //    private int n = 2, m = 2;
    protected final DefaultDynamicSignature signature;
    private final Linkings<DefaultDynamicSignature> linkings;
    private final Placings<DefaultDynamicSignature> placings;

    /**
     * Default constructor for a bigrid.
     * <p>
     * Another signature can be provided that is merged with the internal bigrid's signature.
     * This is not to be confused with the signature that should be provided in order that
     * the concrete bigrid nodes can be created.
     * <p>
     * This might be necessary in some cases, e.g., when the bigrid itself is only one part of a larger model.
     * <p>
     * If the signature is {@code null}, the internal bigrid signature is used, see {@link #signature()}.
     *
     * @param signature another signature to be merged with the internal bigrid signature
     * @see BigridGeneratorOld#signature()
     */
    public BigridGeneratorOld(DefaultDynamicSignature signature) {
        this.signature = BigraphUtil.mergeSignatures(signature(), signature);
        this.linkings = pureLinkings(this.signature);
        this.placings = purePlacings(this.signature);
    }

    public Linkings<DefaultDynamicSignature> getLinkings() {
        return linkings;
    }

    public Placings<DefaultDynamicSignature> getPlacings() {
        return placings;
    }

    public PureBigraph generate(int numOfRows, int numOfCols) throws InvalidConnectionException, TypeNotExistsException, IncompatibleSignatureException, IncompatibleInterfaceException {
        assertDimensionsAreCorrect(numOfRows, numOfCols);
        PureBigraph bigrid = null;
        PureBigraph[] rows = new PureBigraph[numOfRows];
        DiscreteIons ions = new DiscreteIons(getSignature());
        for (int i = 0; i < numOfRows; i++) {
            PureBigraph previous = null;
            Optional<Bigraph> wiring;
            int ixNorth0 = i * numOfCols + 0 % numOfCols - numOfCols; //(i - 1) + 0 % numOfCols;
            int ixSouth0 = i * numOfCols + 0 % numOfCols; //(i) + 0 % numOfCols;
            if (i == 0) { // top row
                previous = ions.topLeft();
                wiring = ions.createRenaming(
                        Maps.immutable.of("east", "ew0",
                                        "south", "south" + ixSouth0)
                                .castToMap(),
                        previous, linkings
                );
            } else if (i == numOfRows - 1) { // bottom row
                previous = ions.bottomLeft();
                wiring = ions.createRenaming(
                        Maps.immutable.of(
//                                "north", "north" + ((i - 1) * (numOfRows - 1)),
                                "north", "north" + ixNorth0,
                                "east", "ew0").castToMap(),
                        previous, linkings
                );
            } else { // middle row
                previous = ions.leftEdge();
                wiring = ions.createRenaming(
                        Maps.immutable.of(
//                                "north", "north" + ((i - 1) * (numOfRows - 1)),
                                "north", "north" + ixNorth0,
                                "east", "ew0",
//                                "south", "south" + (i * (numOfRows - 1))
                                "south", "south" + ixSouth0
                        ).castToMap(),
                        previous, linkings
                );
            }
            assert wiring != null && wiring.isPresent();
            previous = (PureBigraph) ops(wiring.get()).compose(previous).getOuterBigraph();
            for (int j = 1; j < numOfCols; j++) {
                PureBigraph next;
                Optional<Bigraph> nextWiring;
//                int ixNorth = (i - 1) + j % numOfCols;
//                int ixSouth = (i) + j % numOfCols;
                int ixNorth = i * numOfCols + j % numOfCols - numOfCols; //(i - 1) + 0 % numOfCols;
                int ixSouth = i * numOfCols + j % numOfCols; //(i) + 0 % numOfCols;
                if (i == 0) { // top row
                    next = j != numOfCols - 1 ?
                            ions.topEdge() :
                            ions.topRight();
                    nextWiring = ions.createRenaming(
                            Maps.immutable.of("east", "ew" + (j),
//                                    "south", "south" + (j + i * (numOfCols - 1)),
                                    "south", "south" + ixSouth,
                                    "west", "ew" + (j - 1)).castToMap(),
                            next,
                            linkings
                    );
                } else if (i == numOfRows - 1) { // bottom row
                    next = j != numOfCols - 1 ?
                            ions.bottomEdge() :
                            ions.bottomRight();
                    nextWiring = ions.createRenaming(
                            Maps.immutable.of(
//                                    "north", "north" + (j + (i - 1) * (numOfCols - 1)),
                                    "north", "north" + ixNorth,
                                    "east", "ew" + j,
                                    "west", "ew" + (j - 1)).castToMap(),
                            next, linkings
                    );
                } else { // middle row
                    next = j != numOfCols - 1 ?
                            ions.center() :
                            ions.rightEdge();
                    nextWiring = ions.createRenaming(
                            Maps.immutable.of(
//                                    "north", "north" + (j + (i - 1) * (numOfCols - 1)),
                                    "north", "north" + ixNorth,
                                    "east", "ew" + j,
//                                    "south", "south" + (j + i * (numOfCols - 1)),
                                    "south", "south" + ixSouth,
                                    "west", "ew" + (j - 1)).castToMap(),
                            next, linkings
                    );
                }
                assert nextWiring != null && nextWiring.isPresent();
                Bigraph<DefaultDynamicSignature> nextWithWiring = ops(nextWiring.get()).compose(next).getOuterBigraph();
                Bigraph<DefaultDynamicSignature> d_ij = ops(previous).parallelProduct(nextWithWiring).getOuterBigraph();
                previous = (PureBigraph) d_ij;
            }

            Optional<Bigraph> ew = ions.createClosure("ew", numOfCols - 2, previous, linkings);
            previous = (PureBigraph) ops(ew.get()).compose(previous).getOuterBigraph();
            Optional<Bigraph> northSouthConnector = ions.createNorthSouthConnector(previous, linkings);
            previous = (PureBigraph) ops(northSouthConnector.get()).compose(previous).getOuterBigraph();
            rows[i] = previous;
        }

        PureBigraph composite = rows[0];
        for (int i = 1; i < numOfRows; i++) {
            composite = ops(composite).parallelProduct(rows[i]).getOuterBigraph();
        }
        // close all north-south connectors (i.e., outer names)
        Optional<Bigraph> northsouthCls = ions.createClosure("ns", (numOfRows * numOfCols) - numOfCols - 1, composite, linkings);
        composite = (PureBigraph) ops(northsouthCls.get()).compose(composite).getOuterBigraph();
        return composite;
    }

    /**
     * Returns the (probably merged) signature of the bigrid.
     *
     * @return the signature of the current bigrid
     */
    public DefaultDynamicSignature getSignature() {
        return signature;
    }

    /**
     * Checks some initial conditions before a bigrid can be created.
     *
     * @param n row count
     * @param m column count
     */
    protected void assertDimensionsAreCorrect(int n, int m) {
        if (n < 2 || m < 2) {
            throw new RuntimeException(
                    String.format(
                            "The minimum dimension must be (2,2) for creating a bigrid. However, the following dimensions were given: ( + %d + , + %d + )",
                            n, m)
            );
        }
    }

    /**
     * Get the bigrids original signature that is used to build the grid structure.
     *
     * @return the original signature of the bigrid
     */
    public DefaultDynamicSignature signature() {
        return pureSignatureBuilder()
                .newControl("TopLeftCorner", 4).assign()
                .newControl("TopEdge", 4).assign()
                .newControl("TopRightCorner", 4).assign()
                .newControl("LeftEdge", 4).assign()
                .newControl("Center", 4).assign()
                .newControl("RightEdge", 4).assign()
                .newControl("BottomLeftCorner", 4).assign()
                .newControl("BottomEdge", 4).assign()
                .newControl("BottomRightCorner", 4).assign()
                .create();
    }

    /**
     * Inner class that provides the basic building blocks for constructing the bigrid.
     */
    public static class DiscreteIons {
        DefaultDynamicSignature signature;

        public enum NodeType {
            TOP_LEFT, TOP_EDGE, TOP_RIGHT,
            LEFT_EDGE, CENTER, RIGHT_EDGE,
            BOTTOM_LEFT, BOTTOM_EDGE, BOTTOM_RIGHT
        }

        public DiscreteIons(DefaultDynamicSignature signature) {
            this.signature = signature;
        }

        public PureBigraph createByType(NodeType nodeType) throws Exception {
            switch (nodeType) {
                case TOP_LEFT:
                    return topLeft();
                case TOP_EDGE:
                    return topEdge();
                case TOP_RIGHT:
                    return topRight();
                case LEFT_EDGE:
                    return leftEdge();
                case CENTER:
                    return center();
                case RIGHT_EDGE:
                    return rightEdge();
                case BOTTOM_LEFT:
                    return bottomLeft();
                case BOTTOM_EDGE:
                    return bottomEdge();
                case BOTTOM_RIGHT:
                    return bottomRight();
                default:
                    throw new RuntimeException("Node type does not exist");
            }
        }

        /**
         * Helper methods that creates a wiring to connect rows of a bigrid
         */
        public Optional<Bigraph> createNorthSouthConnector(Bigraph<?> rowBigraph, Linkings<DefaultDynamicSignature> linkings) {
            AtomicInteger cnt = new AtomicInteger(0);
            return rowBigraph.getOuterNames().stream()
                    .map(o -> {
                        int j = cnt.getAndIncrement();
                        if (o.getName().startsWith("south")) {
                            return (Bigraph) linkings.substitution("ns" + o.getName().replaceAll("[^0-9]", ""), o.getName());
                        } else if (o.getName().startsWith("north") /* && rowIx > 0 */) {
                            return (Bigraph) linkings.substitution("ns" + o.getName().replaceAll("[^0-9]", ""), o.getName());
                        } else {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .reduce((l, r) -> {
                        try {
                            return ops(l).juxtapose(r).getOuterBigraph();
                        } catch (IncompatibleSignatureException | IncompatibleInterfaceException e) {
                            e.printStackTrace();
                            throw new RuntimeException("");
                        }
                    });
        }

        /**
         * Helper method to create a closure (node-free bigraph, a linking)
         */
        public Optional<Bigraph> createClosure(String name, int length, Bigraph<?> b, Linkings<DefaultDynamicSignature> linkings) throws IncompatibleSignatureException, IncompatibleInterfaceException {
            Set<NamedType<?>> names = IntStream.rangeClosed(0, length)
                    .mapToObj(i -> (NamedType<?>) StringTypedName.of(name + i))
                    .collect(Collectors.toSet());
            NamedType<?>[] rest = b.getOuterNames().stream()
                    .filter(o -> !o.getName().startsWith(name))
                    .map(o -> (NamedType<?>) StringTypedName.of(o.getName()))
                    .toArray(StringTypedName[]::new);
            if (rest.length > 0) {
                return Optional.of(ops(linkings.identity(rest)).juxtapose(linkings.closure(names)).getOuterBigraph());
            } else {
                return Optional.of(linkings.closure(names));
            }
        }

        /**
         * Helper method to create a renaming (node-free bigraph, a bijective substitution)
         */
        public Optional<Bigraph> createRenaming(Map<String, String> renamings, PureBigraph previous, Linkings<DefaultDynamicSignature> linkings) {
            //TODO: simply by using `Substitution(final NamedType<?>... names)`
            return previous.getOuterNames().stream()
                    .map(x -> {
                        if (renamings.containsKey(x.getName())) {
                            return (Bigraph) linkings.substitution(renamings.get(x.getName()), x.getName());
                        } else {
                            return (Bigraph) linkings.substitution(x.getName(), x.getName());
                        }
                    })
                    .reduce((l, r) -> {
                        try {
                            return ops(l).juxtapose(r).getOuterBigraph();
                        } catch (IncompatibleSignatureException | IncompatibleInterfaceException e) {
                            throw new RuntimeException("");
                        }
                    });
        }


        /**
         * Top-left corner of a bigrid
         *
         * @return a discrete ion (bigraph) representing the top-left corner
         */
        public PureBigraph topLeft() throws InvalidConnectionException, TypeNotExistsException {
            PureBigraphBuilder<DefaultDynamicSignature> b = pureBuilder(signature);
            BigraphEntity.InnerName tmp = b.createInnerName("tmp");
            BigraphEntity.InnerName tmp2 = b.createInnerName("tmp2");
            BigraphEntity.OuterName east = b.createOuterName("east");
            BigraphEntity.OuterName south = b.createOuterName("south");
            b.createRoot()
                    .addChild("TopLeftCorner")
                    .linkToInner(tmp)
                    .linkToOuter(east)
                    .linkToOuter(south)
                    .linkToInner(tmp2)
                    .down().addSite();
            b.closeInnerName(tmp); // leaves the edge intact
            b.closeInnerName(tmp2); // leaves the edge intact
            return b.createBigraph();
        }

        /**
         * Top edge of a bigrid
         *
         * @return a discrete ion (bigraph) representing the top edge of a bigrid
         */
        public PureBigraph topEdge() throws InvalidConnectionException, TypeNotExistsException {
            PureBigraphBuilder<DefaultDynamicSignature> b = pureBuilder(signature);
            BigraphEntity.InnerName tmp = b.createInnerName("tmp");
            BigraphEntity.OuterName east = b.createOuterName("east");
            BigraphEntity.OuterName south = b.createOuterName("south");
            BigraphEntity.OuterName west = b.createOuterName("west");
            b.createRoot()
                    .addChild("TopEdge")
                    .linkToInner(tmp)
                    .linkToOuter(east)
                    .linkToOuter(south)
                    .linkToOuter(west)
                    .down().addSite();
            b.closeInnerName(tmp); // leaves the edge intact
            return b.createBigraph();
        }

        public PureBigraph topRight() throws InvalidConnectionException, TypeNotExistsException {
            PureBigraphBuilder<DefaultDynamicSignature> b = pureBuilder(signature);
            BigraphEntity.InnerName tmp = b.createInnerName("tmp");
            BigraphEntity.InnerName tmp2 = b.createInnerName("tmp2");
            BigraphEntity.OuterName west = b.createOuterName("west");
            BigraphEntity.OuterName south = b.createOuterName("south");
            b.createRoot()
                    .addChild("TopRightCorner")
                    .linkToInner(tmp)
                    .linkToInner(tmp2)
                    .linkToOuter(south)
                    .linkToOuter(west)
                    .down().addSite()
            ;
            b.closeInnerName(tmp); // leaves the edge intact
            b.closeInnerName(tmp2); // leaves the edge intact
            return b.createBigraph();
        }

        public PureBigraph leftEdge() throws InvalidConnectionException, TypeNotExistsException {
            PureBigraphBuilder<DefaultDynamicSignature> b = pureBuilder(signature);
            BigraphEntity.OuterName north = b.createOuterName("north");
            BigraphEntity.OuterName east = b.createOuterName("east");
            BigraphEntity.OuterName south = b.createOuterName("south");
            BigraphEntity.InnerName tmp = b.createInnerName("tmp");
            b.createRoot()
                    .addChild("LeftEdge")
                    .linkToOuter(north)
                    .linkToOuter(east)
                    .linkToOuter(south)
                    .linkToInner(tmp)
                    .down().addSite();
            b.closeInnerName(tmp); // leaves the edge intact
            return b.createBigraph();
        }

        public PureBigraph rightEdge() throws InvalidConnectionException, TypeNotExistsException {
            PureBigraphBuilder<DefaultDynamicSignature> b = pureBuilder(signature);
            BigraphEntity.OuterName north = b.createOuterName("north");
            BigraphEntity.InnerName tmp = b.createInnerName("tmp");
            BigraphEntity.OuterName south = b.createOuterName("south");
            BigraphEntity.OuterName west = b.createOuterName("west");
            b.createRoot()
                    .addChild("RightEdge")
                    .linkToOuter(north)
                    .linkToInner(tmp)
                    .linkToOuter(south)
                    .linkToOuter(west)
                    .down().addSite();
            b.closeInnerName(tmp); // leaves the edge intact
            return b.createBigraph();
        }

        public PureBigraph center() throws InvalidConnectionException, TypeNotExistsException {
            PureBigraphBuilder<DefaultDynamicSignature> b = pureBuilder(signature);
            BigraphEntity.OuterName north = b.createOuterName("north");
            BigraphEntity.OuterName east = b.createOuterName("east");
            BigraphEntity.OuterName south = b.createOuterName("south");
            BigraphEntity.OuterName west = b.createOuterName("west");
            b.createRoot()
                    .addChild("Center")
                    .linkToOuter(north)
                    .linkToOuter(east)
                    .linkToOuter(south)
                    .linkToOuter(west)
                    .down().addSite();
            return b.createBigraph();
        }

        public PureBigraph bottomLeft() throws InvalidConnectionException, TypeNotExistsException {
            PureBigraphBuilder<DefaultDynamicSignature> b = pureBuilder(signature);
            BigraphEntity.OuterName north = b.createOuterName("north");
            BigraphEntity.OuterName east = b.createOuterName("east");
            BigraphEntity.InnerName tmp = b.createInnerName("tmp");
            BigraphEntity.InnerName tmp2 = b.createInnerName("tmp2");
            b.createRoot()
                    .addChild("BottomLeftCorner")
                    .linkToOuter(north)
                    .linkToOuter(east)
                    .linkToInner(tmp)
                    .linkToInner(tmp2)
                    .down().addSite();
            b.closeInnerName(tmp); // leaves the edge intact
            b.closeInnerName(tmp2); // leaves the edge intact
            return b.createBigraph();
        }

        public PureBigraph bottomEdge() throws InvalidConnectionException, TypeNotExistsException {
            PureBigraphBuilder<DefaultDynamicSignature> b = pureBuilder(signature);
            BigraphEntity.OuterName north = b.createOuterName("north");
            BigraphEntity.OuterName east = b.createOuterName("east");
            BigraphEntity.OuterName west = b.createOuterName("west");
            BigraphEntity.InnerName tmp = b.createInnerName("tmp");
            b.createRoot()
                    .addChild("BottomEdge")
                    .linkToOuter(north)
                    .linkToOuter(east)
                    .linkToInner(tmp)
                    .linkToOuter(west)
                    .down().addSite();
            b.closeInnerName(tmp); // leaves the edge intact
            return b.createBigraph();
        }

        public PureBigraph bottomRight() throws InvalidConnectionException, TypeNotExistsException {
            PureBigraphBuilder<DefaultDynamicSignature> b = pureBuilder(signature);
            BigraphEntity.OuterName north = b.createOuterName("north");
            BigraphEntity.InnerName tmp = b.createInnerName("tmp");
            BigraphEntity.InnerName tmp2 = b.createInnerName("tmp2");
            BigraphEntity.OuterName west = b.createOuterName("west");
            b.createRoot()
                    .addChild("BottomRightCorner")
                    .linkToOuter(north)
                    .linkToInner(tmp)
                    .linkToInner(tmp2)
                    .linkToOuter(west)
                    .down().addSite();
            b.closeInnerName(tmp); // leaves the edge intact
            b.closeInnerName(tmp2); // leaves the edge intact
            return b.createBigraph();
        }
    }
}
