package de.tudresden.inf.st.bigraphs.simulation.modelchecking;

import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.simulation.ReactiveSystem;
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

    public PureBigraphModelChecker(ReactiveSystem<PureBigraph> reactiveSystem, SimulationType simulationType, ModelCheckingOptions options, ReactiveSystemListener<PureBigraph> listener) {
        super(reactiveSystem, simulationType, options, listener);
    }

    public PureBigraphModelChecker(ReactiveSystem<PureBigraph> reactiveSystem, SimulationType simulationType, ModelCheckingOptions options) {
        super(reactiveSystem, simulationType, options);
    }
}
