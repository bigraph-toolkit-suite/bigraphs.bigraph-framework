package de.tudresden.inf.st.bigraphs.rewriting.encoding;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphIsNotPrimeException;
import de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.exceptions.BigraphIsNotGroundException;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.LinkedList;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
public class BigraphCanonicalForm {

    private static BigraphCanonicalForm instance = new BigraphCanonicalForm();
    private BigraphCanonicalFormStrategy canonicalFormStrategy;

    public static BigraphCanonicalForm getInstance() {
        return instance;
    }

    private BigraphCanonicalForm() {
    }

    //TODO check whether controls are atomic or not:
    // problem with alphabet, consider control "LL" and and control "L". one node "LL" and two nodes "L" and "L"
    // would be same but there aren't!: controls must be atomic!

    //TODO choose different prefix for edge name rewriting than what outer names have.

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
    public <B extends Bigraph<?>> String bfcs(B bigraph) {
        if (bigraph instanceof PureBigraph) {
            canonicalFormStrategy = new PureCanonicalForm(this);
        } else {
            throw new RuntimeException("Not implemented yet");
        }

        return canonicalFormStrategy.compute(bigraph);
    }

    void setParentOfNode(final BigraphEntity node, final BigraphEntity parent) {
        EStructuralFeature prntRef = node.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        node.getInstance().eSet(prntRef, parent.getInstance()); // child is automatically added to the parent according to the ecore model
    }

    /**
     * the children size is sorted in reversed order where control names sorting is in lexicographic order and has
     * precedence of the children size.
     * <p>
     * This is for map entries where children nodes are grouped by their parents.
     *
     * @param lhs left-hand side
     * @param rhs right-hand side
     * @return integer indicating the ordering
     */
    public int compareByControlThenChildrenSize(Map.Entry<BigraphEntity, LinkedList<Map.Entry<BigraphEntity, Control>>> lhs, Map.Entry<BigraphEntity, LinkedList<Map.Entry<BigraphEntity, Control>>> rhs) {
        if (lhs.getKey().getControl().getNamedType().stringValue().equals(rhs.getKey().getControl().getNamedType().stringValue())) {
//            if (rhs.getValue().size() - lhs.getValue().size() == 0) {
//                return bigraph.getPorts(rhs.getKey()).size();
//            }
            return rhs.getValue().size() - lhs.getValue().size();
        } else {
            return lhs.getKey().getControl().getNamedType().stringValue().compareTo(rhs.getKey().getControl().getNamedType().stringValue());
        }
    }

    static int compareByControl(LinkedList<Map.Entry<BigraphEntity, Control>> lhs, LinkedList<Map.Entry<BigraphEntity, Control>> rhs) {
        String s1 = lhs.stream().map(x -> x.getValue().getNamedType().stringValue()).sorted().collect(Collectors.joining(""));
        String s2 = rhs.stream().map(x -> x.getValue().getNamedType().stringValue()).sorted().collect(Collectors.joining(""));
        return s1.compareTo(s2);
//        if (s1.equals(s2)) {
//            return rhs.size() - lhs.size();
//        } else {
//            String prefix1 = lhs.size() >= rhs.size() ? "1" : "0";
//            String prefix2 = rhs.size() >= lhs.size() ? "1" : "0";
//            return (prefix1 + s1).compareTo(prefix2 + s2);
//        }
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

    Supplier<String> createNameSupplier(final String prefix) {
        return new Supplier<String>() {
            private int id = 0;

            @Override
            public String get() {
                return prefix + id++;
            }
        };
    }

}
