package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.predicates;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.rewriting.encoding.BigraphCanonicalForm;

/**
 * Predicate implementation that returns {@code true} if two bigraphs are isomorphic (i.e., structurally equivalent).
 * <p>
 * A predicate can be created by using the static methods the class provides.
 * <p>
 * Bigraph isomorphism is checked using the canonical string encoding.
 *
 * @author Dominik Grzelak
 */
public class BigraphIsoPredicate<B extends Bigraph<? extends Signature<?>>> extends ReactiveSystemPredicates<B> {

    private final B bigraphToMatch;
    private BigraphCanonicalForm canonicalForm;
    private final String bigraphEncoded;

    private BigraphIsoPredicate(B bigraphToMatch) {
        this(bigraphToMatch, false);
    }

    private BigraphIsoPredicate(B bigraphToMatch, boolean negate) {
        this.bigraphToMatch = bigraphToMatch;
        super.negate = negate;
        this.canonicalForm = BigraphCanonicalForm.createInstance();
        this.bigraphEncoded = this.canonicalForm.bfcs(this.bigraphToMatch);
    }

    public static <B extends Bigraph<? extends Signature<?>>> BigraphIsoPredicate<B> create(B bigraphToMatch) {
        return new BigraphIsoPredicate<B>(bigraphToMatch);
    }

    public static <B extends Bigraph<? extends Signature<?>>> BigraphIsoPredicate<B> create(B bigraphToMatch, boolean negate) {
        return new BigraphIsoPredicate<B>(bigraphToMatch, negate);
    }

    @Override
    public boolean test(B agent) {
        String bfcs = this.canonicalForm.bfcs(agent);
        return bigraphEncoded.equals(bfcs);
    }

    public B getBigraphToMatch() {
        return bigraphToMatch;
    }
}
