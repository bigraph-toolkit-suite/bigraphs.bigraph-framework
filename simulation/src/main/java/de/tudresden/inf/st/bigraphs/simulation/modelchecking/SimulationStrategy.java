package de.tudresden.inf.st.bigraphs.simulation.modelchecking;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;

/**
 * @author Dominik Grzelak
 */
public interface SimulationStrategy<B extends Bigraph<? extends Signature<?>>> {

    void synthesizeTransitionSystem();
}
