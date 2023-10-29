package org.bigraphs.framework.simulation.encoding;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.exceptions.BigraphIsNotPrimeException;
import org.bigraphs.framework.core.ElementaryBigraph;
import org.bigraphs.framework.core.exceptions.BigraphIsNotGroundException;
import org.bigraphs.framework.core.impl.elementary.DiscreteIon;
import org.bigraphs.framework.core.impl.elementary.Linkings;
import org.bigraphs.framework.core.impl.elementary.Placings;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;

/**
 * This helper class creates a unique (canonical) label for a place graph of a bigraph such that two isomorphic place graphs
 * have the same label. This is also known as string representation of a graph. With the implemented canonical forms in
 * this class, we create a unique representation for labelled rooted unordered trees, that are, place graphs without sites.
 * <p>
 * The string representations are the minimal of all these possible breadth-first/depth-first representation according
 * to the lexicographic order (= constraint). This guarantees the uniques of the string representation.
 * <p>
 * <b>Note:</b> This implementation only considers the first root of a bigraph.
 * <p>
 * <b>Note on the Implementation:</b> The algorithm used to generate the canonical form is adopted from {@code [1]}. The needed top-down BFS is an implementation
 * described in {@code [2]} (a sequential bottom-up BFS algorithm).
 * <p>
 * <b>References</b>
 * <p>
 * [1] Chi, Y., Yang, Y., Muntz, R.R.: Canonical forms for labelled trees and their applications in frequent subtree mining. Knowl Inf Syst. 8, 203–234 (2005). https://doi.org/10.1007/s10115-004-0180-7.
 * <br>
 * [2] Beamer, S., Asanović, K., Patterson, D.: Direction-optimizing Breadth-first Search. In: Proceedings of the International Conference on High Performance Computing, Networking, Storage and Analysis. pp. 12:1–12:10. IEEE Computer Society Press, Los Alamitos, CA, USA (2012).
 *
 * @author Dominik Grzelak
 */
public class BigraphCanonicalForm implements BigraphCanonicalFormSupport {

    private final MutableMap<Class<? extends Bigraph<?>>, BigraphCanonicalFormStrategy<? extends Bigraph<?>>> strategyMap =
            Maps.mutable.of(PureBigraph.class, new PureCanonicalForm(this));
    boolean withNodeIdentifiers = false;
    final static char PREFIX_BARREN = 'r';

    boolean rewriteOpenLinks = false;

    public static BigraphCanonicalForm createInstance() {
        return createInstance(false);
    }

    public static BigraphCanonicalForm createInstance(boolean withNodeIdentifiers) {
        return new BigraphCanonicalForm(withNodeIdentifiers);
    }

    /**
     * Private constructor.
     */
    private BigraphCanonicalForm() {
    }

    private BigraphCanonicalForm(boolean withNodeIdentifiers) {
        this.withNodeIdentifiers = withNodeIdentifiers;
    }

    //TODO check whether controls are atomic or not:
    // problem with alphabet, consider control "LL" and and control "L". one node "LL" and two nodes "L" and "L"
    // would be same but there aren't!: controls must be atomic!

    /**
     * Build a breadth-first canonical string (BFCS) for a pure bigraph
     * according to the lexicographic order of the control's labels. The representation is unique.
     * <p>
     * //     * The bigraph must be prime, i.e., the place graph must only have one root.
     *
     * @param bigraph the bigraph
     * @param <B>     the type of the bigraph
     * @return the BFCS of the place graph of the given bigraph
     */
    @SuppressWarnings("unchecked")
    public synchronized <B extends Bigraph<?>> String bfcs(B bigraph) {
        BigraphCanonicalFormStrategy<B> canonicalFormStrategy;
        if (bigraph instanceof PureBigraph) {
            canonicalFormStrategy = (BigraphCanonicalFormStrategy<B>) strategyMap.getOrDefault(
                            bigraph.getClass(),
                            new PureCanonicalForm(this)
                    )
                    // pass additional options here
                    .setPrintNodeIdentifiers(withNodeIdentifiers)
                    .setRewriteOpenLinks(rewriteOpenLinks);
            return canonicalFormStrategy.compute(bigraph);
        } else if (bigraph instanceof ElementaryBigraph) {
            return bfcs((ElementaryBigraph<?>) bigraph);
        } else {
            throw new RuntimeException("Not implemented yet");
        }
    }

    protected String bfcs(ElementaryBigraph<?> elementaryBigraph) {
        if (elementaryBigraph.isPlacing()) { // for no-arg elementary bigraphs
            if (elementaryBigraph instanceof Placings.Barren || elementaryBigraph instanceof Placings.Join) {
                return ELEMENTARY_ENCODINGS.get(elementaryBigraph.getClass()).apply((Void) null);
            } else {
                return ELEMENTARY_ENCODINGS.get((elementaryBigraph.getClass())).apply(elementaryBigraph.getSites().size());
            }
        } else if (elementaryBigraph.isLinking()) {
            if (elementaryBigraph instanceof Linkings.Closure || elementaryBigraph instanceof Linkings.Identity) {
                return ELEMENTARY_ENCODINGS.get((elementaryBigraph.getClass())).apply(elementaryBigraph.getInnerNames());
            } else if (elementaryBigraph instanceof Linkings.Substitution) {
                return ELEMENTARY_ENCODINGS.get((elementaryBigraph.getClass())).apply(new Object[]{elementaryBigraph.getOuterNames().iterator().next(), elementaryBigraph.getInnerNames()});
            }
            throw new RuntimeException("Not implemented yet");
        } else {
            // DiscreteIon
            assert elementaryBigraph instanceof DiscreteIon;
            throw new RuntimeException("Not implemented yet");
        }
    }

    public boolean isWithNodeIdentifiers() {
        return withNodeIdentifiers;
    }

    public BigraphCanonicalForm setWithNodeIdentifiers(boolean withNodeIdentifiers) {
        this.withNodeIdentifiers = withNodeIdentifiers;
        return this;
    }

    public boolean isRewriteOpenLinks() {
        return rewriteOpenLinks;
    }

    public BigraphCanonicalForm setRewriteOpenLinks(boolean rewriteOpenLinks) {
        this.rewriteOpenLinks = rewriteOpenLinks;
        return this;
    }

    <B extends Bigraph<?>> void assertBigraphIsGroundAndPrime(B bigraph) {
        if (!bigraph.isGround() || !bigraph.isPrime()) {
            throw new BigraphIsNotGroundException();
        }
    }

    /**
     * Checks if the alphabet of the signature contains only atomic labels.
     * Otherwise the BFCS method isn't reliable.
     *
     * @param bigraph the bigraph
     * @param <B>     the type of the bigraph
     */
    <B extends Bigraph<?>> void assertControlsAreAtomic(B bigraph) {
        //TODO: check
    }


    <B extends Bigraph<?>> void assertBigraphIsPrime(B bigraph) {
        if (!bigraph.isPrime()) {
            throw new BigraphIsNotPrimeException();
        }
    }

    public void assertBigraphHasRoots(PureBigraph bigraph) {
        if (bigraph.getRoots().size() == 0) {
            throw new RuntimeException("Bigraph has no roots. Cannot compute the canonical form");
        }
    }
}
