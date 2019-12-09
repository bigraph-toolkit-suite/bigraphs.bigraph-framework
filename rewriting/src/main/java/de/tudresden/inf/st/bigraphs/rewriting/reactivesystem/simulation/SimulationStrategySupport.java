package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dominik Grzelak
 */
public abstract class SimulationStrategySupport<B extends Bigraph<? extends Signature<?>>> implements SimulationStrategy<B> {
    private Logger logger = LoggerFactory.getLogger(SimulationStrategySupport.class);

    protected BigraphModelChecker<B> modelChecker;

    public SimulationStrategySupport(BigraphModelChecker<B> modelChecker) {
        this.modelChecker = modelChecker;
    }

}
