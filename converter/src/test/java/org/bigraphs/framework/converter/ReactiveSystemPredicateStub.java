package org.bigraphs.framework.converter;

import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.ReactiveSystemPredicate;

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
