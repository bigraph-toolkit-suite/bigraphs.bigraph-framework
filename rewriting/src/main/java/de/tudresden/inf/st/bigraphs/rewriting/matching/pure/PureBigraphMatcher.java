package de.tudresden.inf.st.bigraphs.rewriting.matching.pure;

import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;
import de.tudresden.inf.st.bigraphs.rewriting.matching.AbstractBigraphMatcher;
import de.tudresden.inf.st.bigraphs.rewriting.matching.MatchIterable;

/**
 * A matcher for {@link PureBigraph}'s.
 * <p>
 * Creates the correct matching engine and iterator to return the matches.
 */
public class PureBigraphMatcher extends AbstractBigraphMatcher<PureBigraph> {

    public PureBigraphMatcher() {
        super();
    }

    public MatchIterable match(PureBigraph agent, PureBigraph redex) {
        super.agent = agent;
        super.redex = redex;
        PureBigraphMatchingEngine<PureBigraph> matchingEngine = new PureBigraphMatchingEngine<>(this.agent, this.redex);
        return new MatchIterable(new PureMatchIteratorImpl(matchingEngine));
    }

}
