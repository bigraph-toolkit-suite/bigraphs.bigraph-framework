package de.tudresden.inf.st.bigraphs.matching;

import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.DynamicEcoreBigraph;

/**
 * Works like a factory here .... for the iterator
 * matching engine can get later access also to the custom constraints matching method
 */
public class BigraphMatcher {
    private DynamicEcoreBigraph agent;
    private DynamicEcoreBigraph redex;

    public MatchIterable match(DynamicEcoreBigraph agent, DynamicEcoreBigraph redex) throws IncompatibleSignatureException {
        this.agent = agent;
        this.redex = redex;
        BigraphMatchingEngine<DynamicEcoreBigraph> matchingEngine = new BigraphMatchingEngine<>(this.agent, this.redex);
        return new MatchIterable(new DefaultMatchIteratorImpl(matchingEngine));
    }

    public DynamicEcoreBigraph getAgent() {
        return agent;
    }

    public DynamicEcoreBigraph getRedex() {
        return redex;
    }

    //TODO: let user override a matchConstraints(...) method here
}
