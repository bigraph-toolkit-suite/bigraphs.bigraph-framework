package org.bigraphs.framework.simulation.modelchecking;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * The algorithm implemented here to synthesize the "reaction graph" is adopted from [1].
 * It is a breadth-first simulation, which also checks some given predicates.
 * It also detects cycles.
 * This algorithm can be used to conduct reachability analysis.
 *
 * @author Dominik Grzelak
 * @see <a href="https://pure.itu.dk/portal/files/39500908/thesis_GianDavidPerrone.pdf">[1] G. Perrone, “Domain-Specific Modelling Languages in Bigraphs,” IT University of Copenhagen, 2013.</a>
 */
public class BreadthFirstStrategy<B extends Bigraph<? extends Signature<?>>> extends ModelCheckingStrategySupport<B> {
    public BreadthFirstStrategy(BigraphModelChecker<B> modelChecker) {
        super(modelChecker);
    }

    @Override
    protected Collection<B> createWorklist() {
        return new ConcurrentLinkedDeque<>();
    }

    @Override
    protected B removeNext(Collection<B> worklist) {
        return ((Deque<B>) worklist).removeFirst(); // FIFO
    }

    @Override
    protected void addToWorklist(Collection<B> worklist, B bigraph) {
        ((Deque<B>) worklist).addLast(bigraph);
    }
}

