package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.predicates;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.rewriting.matching.AbstractBigraphMatcher;
import de.tudresden.inf.st.bigraphs.rewriting.matching.BigraphMatch;
import de.tudresden.inf.st.bigraphs.rewriting.matching.MatchIterable;

/**
 * Simple predicate implementation that returns true if one bigraph is contained in another (subbigraph matching problem).
 * <p>
 * A predicate can be created by using the static methods the class provides.
 * <p>
 * This predicate internally creates a bigraph matcher instance for the concrete bigraph kind automatically.
 * Therefore, {@link AbstractBigraphMatcher} is used. The class member {@code matcher} is used inside the
 * {@link SubBigraphMatchPredicate#test(Bigraph)} method.
 *
 * @author Dominik Grzelak
 */
public class SubBigraphMatchPredicate<B extends Bigraph<? extends Signature<?>>> extends ReactiveSystemPredicates<B> {

    private final B bigraphToMatch;
    private AbstractBigraphMatcher<B> matcher;

    private SubBigraphMatchPredicate(B bigraphToMatch) {
        this(bigraphToMatch, false);
    }

    private SubBigraphMatchPredicate(B bigraphToMatch, boolean negate) {
        this.bigraphToMatch = bigraphToMatch;
        super.negate = negate;
        this.matcher = AbstractBigraphMatcher.create((Class<B>) bigraphToMatch.getClass());
    }

    public static <B extends Bigraph<? extends Signature<?>>> SubBigraphMatchPredicate<B> create(B bigraphToMatch) {
        return new SubBigraphMatchPredicate<B>(bigraphToMatch);
    }

    public static <B extends Bigraph<? extends Signature<?>>> SubBigraphMatchPredicate<B> create(B bigraphToMatch, boolean negate) {
        return new SubBigraphMatchPredicate<B>(bigraphToMatch, negate);
    }

    @Override
    public boolean test(B o) {
        MatchIterable<BigraphMatch<B>> match = matcher.match(o, bigraphToMatch);
//        if (negate) {
//            return !match.iterator().hasNext();
//        }
        return match.iterator().hasNext();
    }

    public B getBigraphToMatch() {
        return bigraphToMatch;
    }
}
