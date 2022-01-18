package de.tudresden.inf.st.bigraphs.converter;

import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactiveSystemPredicate;

/**
 * A simple stub for predicates to test the converter
 */
public class ReactiveSystemPredicateStub extends ReactiveSystemPredicate<PureBigraph> {
    PureBigraph bigraph;

    public ReactiveSystemPredicateStub(PureBigraph bigraph) {
        this.bigraph = bigraph;
    }

    @Override
    public PureBigraph getBigraph() {
        return this.bigraph;
    }

    @Override
    public boolean test(PureBigraph agent) {
        return false;
    }
}
