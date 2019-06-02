package de.tudresden.inf.st.bigraphs.matching;

import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;

/**
 * Works like a factory here .... for the iterator
 * matching engine can get later access also to the custom constraints matching method
 */
public class BigraphMatcher {
    private PureBigraph agent;
    private PureBigraph redex;

    public MatchIterable match(PureBigraph agent, PureBigraph redex) throws IncompatibleSignatureException {
        this.agent = agent;
        this.redex = redex;
        BigraphMatchingEngine<PureBigraph> matchingEngine = new BigraphMatchingEngine<>(this.agent, this.redex);
        return new MatchIterable(new DefaultMatchIteratorImpl(matchingEngine));
    }

    public PureBigraph getAgent() {
        return agent;
    }

    public PureBigraph getRedex() {
        return redex;
    }

    //TODO: let user override a matchConstraints(...) method here
}
