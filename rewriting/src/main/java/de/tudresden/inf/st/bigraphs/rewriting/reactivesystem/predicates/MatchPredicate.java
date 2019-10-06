package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.predicates;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.rewriting.matching.AbstractBigraphMatcher;
import de.tudresden.inf.st.bigraphs.rewriting.matching.BigraphMatch;
import de.tudresden.inf.st.bigraphs.rewriting.matching.MatchIterable;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.TransitionPredicates;

/**
 * Default implementation of a simple predicate to just match a bigraph.
 * <p>
 * A predicate can be created by using the static methods the class provides.
 * <p>
 * Internally a bigraph matcher instance for the concrete bigraph kind is created automatically.
 * Therefore, {@link AbstractBigraphMatcher} is used. The {@code matcher} is used inside the
 * {@link MatchPredicate#test(Bigraph)} method.
 *
 * @author Dominik Grzelak
 */
public class MatchPredicate<B extends Bigraph<? extends Signature<?>>> extends TransitionPredicates<B> {

    private final B bigraphToMatch;
    private AbstractBigraphMatcher<B> matcher;
    private boolean negate;

    private MatchPredicate(B bigraphToMatch) {
        this(bigraphToMatch, false);
    }

    private MatchPredicate(B bigraphToMatch, boolean negate) {
        this.bigraphToMatch = bigraphToMatch;
        this.negate = negate;
        this.matcher = AbstractBigraphMatcher.create((Class<B>) bigraphToMatch.getClass());
    }

    public static <B extends Bigraph<? extends Signature<?>>> MatchPredicate<B> create(B bigraphToMatch) {
        return new MatchPredicate<B>(bigraphToMatch);
    }

    public static <B extends Bigraph<? extends Signature<?>>> MatchPredicate<B> create(B bigraphToMatch, boolean negate) {
        return new MatchPredicate<B>(bigraphToMatch, negate);
    }

    @Override
    public boolean test(B o) {
        MatchIterable<BigraphMatch<B>> match = matcher.match(o, bigraphToMatch);
        if (negate) {
            return !match.iterator().hasNext();
        }
        return match.iterator().hasNext();
    }

    public B getBigraphToMatch() {
        return bigraphToMatch;
    }
}
