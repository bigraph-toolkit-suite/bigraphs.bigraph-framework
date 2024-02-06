package org.bigraphs.framework.simulation.modelchecking;

import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.ReactiveSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of a {@link BigraphModelChecker} for model checking of BRS with pure bigraphs
 * (see {@link PureBigraph}).
 *
 * @author Dominik Grzelak
 * @see PureBigraph
 */
public class PureBigraphModelChecker extends BigraphModelChecker<PureBigraph> {

    private Logger logger = LoggerFactory.getLogger(BigraphModelChecker.class);

    public PureBigraphModelChecker(ReactiveSystem<PureBigraph> reactiveSystem, ModelCheckingOptions options) {
        super(reactiveSystem, options);
    }

    public PureBigraphModelChecker(ReactiveSystem<PureBigraph> reactiveSystem, SimulationStrategy.Type simulationStrategyType, ModelCheckingOptions options, ReactiveSystemListener<PureBigraph> listener) {
        super(reactiveSystem, simulationStrategyType, options, listener);
    }

    public PureBigraphModelChecker(ReactiveSystem<PureBigraph> reactiveSystem, SimulationStrategy.Type simulationStrategyType, ModelCheckingOptions options) {
        super(reactiveSystem, simulationStrategyType, options);
    }
}
