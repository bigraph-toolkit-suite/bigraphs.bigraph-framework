package de.tudresden.inf.st.bigraphs.simulation.modelchecking.predicates;

import de.tudresden.inf.st.bigraphs.converter.jlibbig.JLibBigBigraphEncoder;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactiveSystemPredicate;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.simulation.matching.AbstractBigraphMatcher;
import it.uniud.mads.jlibbig.core.std.AgentMatch;
import it.uniud.mads.jlibbig.core.std.AgentMatcher;

/**
 * Simple predicate implementation that returns true if one bigraph is contained in another (subbigraph matching problem).
 * Thus, it represents a partial predicate.
 * <p>
 * A predicate can be created by using the static methods the class provides.
 * <p>
 * This predicate internally creates a bigraph matcher instance for the concrete bigraph kind automatically.
 * Therefore, {@link AbstractBigraphMatcher} is used. The class member {@code matcher} is used inside the
 * {@link SubBigraphMatchPredicate#test(Bigraph)} method.
 *
 * @author Dominik Grzelak
 * @see BigraphIsoPredicate
 */
public class SubBigraphMatchPredicate<B extends Bigraph<? extends Signature<?>>> extends ReactiveSystemPredicate<B> {

    private final B bigraphToMatch;
    private final it.uniud.mads.jlibbig.core.std.Bigraph jBigraphToMatch;
    private AbstractBigraphMatcher<B> matcher;
    private JLibBigBigraphEncoder enc = new JLibBigBigraphEncoder();

    private SubBigraphMatchPredicate(B bigraphToMatch) {
        this(bigraphToMatch, false);
    }

    private SubBigraphMatchPredicate(B bigraphToMatch, boolean negate) {
        this.bigraphToMatch = bigraphToMatch;
        super.negate = negate;
        this.matcher = AbstractBigraphMatcher.create((Class<B>) bigraphToMatch.getClass());
        this.jBigraphToMatch = enc.encode((PureBigraph) bigraphToMatch);
    }

    public static <B extends Bigraph<? extends Signature<?>>> SubBigraphMatchPredicate<B> create(B bigraphToMatch) {
        return new SubBigraphMatchPredicate<B>(bigraphToMatch);
    }

    public static <B extends Bigraph<? extends Signature<?>>> SubBigraphMatchPredicate<B> create(B bigraphToMatch, boolean negate) {
        return new SubBigraphMatchPredicate<B>(bigraphToMatch, negate);
    }

    @Override
    public B getBigraph() {
        return bigraphToMatch;
    }

    @Override
    public boolean test(B agent) {
        //TODO substitute this with may fastersubtree+hypergraph matcher?
        AgentMatcher matcher = new AgentMatcher();
        it.uniud.mads.jlibbig.core.std.Bigraph a = enc.encode((PureBigraph) agent, jBigraphToMatch.getSignature());
        Iterable<? extends AgentMatch> match = matcher.match(a, jBigraphToMatch);
        return match.iterator().hasNext();
    }

    public B getBigraphToMatch() {
        return bigraphToMatch;
    }
}
