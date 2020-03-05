package de.tudresden.inf.st.bigraphs.simulation.modelchecking;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphComposite;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.simulation.ReactionRule;
import de.tudresden.inf.st.bigraphs.simulation.ReactiveSystem;
import de.tudresden.inf.st.bigraphs.simulation.matching.BigraphMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
