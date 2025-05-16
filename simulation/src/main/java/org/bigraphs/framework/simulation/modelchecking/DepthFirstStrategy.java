package org.bigraphs.framework.simulation.modelchecking;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * This algorithm implements a depth-first model checking algorithm.
 * It also detects cycles.
 * This algorithm can be used to conduct reachability analysis.
 *
 * @author Dominik Grzelak
 */
public class DepthFirstStrategy<B extends Bigraph<? extends Signature<?>>> extends ModelCheckingStrategySupport<B> {
    public DepthFirstStrategy(BigraphModelChecker<B> modelChecker) {
        super(modelChecker);
    }

    @Override
    protected Collection<B> createWorklist() {
        return new ConcurrentLinkedDeque<>();
    }

    @Override
    protected B removeNext(Collection<B> worklist) {
        return ((Deque<B>) worklist).removeLast(); // LIFO
    }

    @Override
    protected void addToWorklist(Collection<B> worklist, B bigraph) {
        ((Deque<B>) worklist).addLast(bigraph);
    }
}


