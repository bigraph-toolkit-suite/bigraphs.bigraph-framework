package org.bigraphs.framework.simulation.modelchecking;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;

/**
 * Strategy pattern for implementing new model checking algorithms.
 * <p>
 * Current implementations:
 * <ul>
 *     <li>Breadth-first search (with cycle detection)</li>
 *     <li>Simulation (BFS without cycle checking)</li>
 *     <li>Random</li>
 * </ul>
 *
 * @author Dominik Grzelak
 */
public interface ModelCheckingStrategy<B extends Bigraph<? extends Signature<?>>> {

    /**
     * Entry point of the model checking strategy to implement.
     * <p>
     * The reaction graph (i.e., transition system) can be acquired and stored via the model checker object.
     */
    void synthesizeTransitionSystem();
}
