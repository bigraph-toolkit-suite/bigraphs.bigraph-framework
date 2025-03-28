package org.bigraphs.framework.simulation.modelchecking;

import com.google.common.base.Stopwatch;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.BigraphFileModelManagement;
import org.bigraphs.framework.core.EcoreBigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.exceptions.AgentIsNullException;
import org.bigraphs.framework.core.exceptions.AgentNotGroundException;
import org.bigraphs.framework.core.exceptions.AgentNotPrimeException;
import org.bigraphs.framework.core.exceptions.ReactiveSystemException;
import org.bigraphs.framework.core.providers.ExecutorServicePoolProvider;
import org.bigraphs.framework.core.reactivesystem.*;
import org.bigraphs.framework.simulation.encoding.BigraphCanonicalForm;
import org.bigraphs.framework.simulation.exceptions.BigraphSimulationException;
import org.bigraphs.framework.simulation.exceptions.InvalidSimulationStrategy;
import org.bigraphs.framework.simulation.exceptions.ModelCheckerExecutorServiceNotProvided;
import org.bigraphs.framework.simulation.matching.AbstractBigraphMatcher;
import org.bigraphs.framework.visualization.BigraphGraphvizExporter;
import org.bigraphs.framework.visualization.ReactionGraphExporter;
import org.jgrapht.GraphPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A bigraph model checker that allows to simulate a BRS by reaction rules. A reactive system, the strategy and
 * options must be provided.
 * <p>
 * The model checker allows to react on live predicate matches in the course of the simulation by <i>listeners</i>.
 * They can also be evaluated later by getting the reaction graph/system.
 *
 * @author Dominik Grzelak
 */
public abstract class BigraphModelChecker<B extends Bigraph<? extends Signature<?>>> {

    private final Logger logger = LoggerFactory.getLogger(BigraphModelChecker.class);
    ExecutorService executorService;
    private final Class<B> genericType;

    protected ModelCheckingStrategy<B> modelCheckingStrategy;
    protected SimulationStrategy.Type simulationStrategyType;
    protected BigraphModelChecker.ReactiveSystemListener<B> reactiveSystemListener;
    protected BigraphCanonicalForm canonicalForm = BigraphCanonicalForm.createInstance(true);
    protected ModelCheckingOptions options;

    final ReactiveSystem<B> reactiveSystem;
    ReactionGraph<B> reactionGraph;

    /**
     * Enum-like class that holds all kind of simulations.
     */
    public static class SimulationStrategy {

        public enum Type {
            BFS, RANDOM;
        }

        public static <B extends Bigraph<? extends Signature<?>>> Class<? extends ModelCheckingStrategy> getSimulationStrategyClass(Type type) {
            switch (type) {
                case BFS:
                    return BreadthFirstStrategy.class;
                case RANDOM: //This is like simulation
                    return RandomAgentModelCheckingStrategy.class;
                default:
                    return BreadthFirstStrategy.class;
            }
        }
    }

    private final static BigraphModelChecker.ReactiveSystemListener<? extends Bigraph<? extends Signature<?>>> EMPTY_LISTENER =
            new BigraphModelChecker.EmptyReactiveSystemListener<>();


    private static class EmptyReactiveSystemListener<B extends Bigraph<? extends Signature<?>>>
            implements BigraphModelChecker.ReactiveSystemListener<B> {
    }


    public BigraphModelChecker(ReactiveSystem<B> reactiveSystem, ModelCheckingOptions options) {
        this(reactiveSystem, SimulationStrategy.Type.BFS, options);
    }

    public BigraphModelChecker(ReactiveSystem<B> reactiveSystem, SimulationStrategy.Type simulationStrategyType, ModelCheckingOptions options) {
        this(reactiveSystem, simulationStrategyType, options, null);
        onAttachListener(this);
    }

    public BigraphModelChecker(ReactiveSystem<B> reactiveSystem, SimulationStrategy.Type simulationStrategyType, ModelCheckingOptions options,
                               ReactiveSystemListener<B> listener) {
        Optional.ofNullable(listener).map(this::setReactiveSystemListener).orElseGet(() -> setReactiveSystemListener((ReactiveSystemListener<B>) EMPTY_LISTENER));
        loadServiceExecutor();
        this.genericType = getGenericTypeClass();

        this.reactiveSystem = reactiveSystem;
        this.reactionGraph = new ReactionGraph<>();
//        this.matcher = AbstractBigraphMatcher.create(this.genericType);

        this.options = options;
//        ModelCheckingOptions.TransitionOptions opts = options.get(ModelCheckingOptions.Options.TRANSITION);
//        if (opts.allowReducibleClasses()) {
//            this.canonicalForm = BigraphCanonicalForm.createInstance();
//        }

        this.simulationStrategyType = simulationStrategyType;
        try {
            this.modelCheckingStrategy = createStrategy(this.simulationStrategyType);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                 InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public BigraphCanonicalForm acquireCanonicalForm() {
        BigraphCanonicalForm inst;
        if (((ModelCheckingOptions.TransitionOptions) options.get(ModelCheckingOptions.Options.TRANSITION)).allowReducibleClasses()) {
            inst = BigraphCanonicalForm.createInstance();
        } else {
            inst = BigraphCanonicalForm.createInstance(true);
        }
        if (((ModelCheckingOptions.TransitionOptions) options.get(ModelCheckingOptions.Options.TRANSITION)).isRewriteOpenLinks()) {
            inst.setRewriteOpenLinks(true);
        }
        return inst;
    }

    private void loadServiceExecutor() {
        ServiceLoader<ExecutorServicePoolProvider> load = ServiceLoader.load(ExecutorServicePoolProvider.class);
        load.reload();
        Iterator<ExecutorServicePoolProvider> iterator = load.iterator();
        if (iterator.hasNext()) {
            ExecutorServicePoolProvider next = iterator.next();
            executorService = next.provide();
        }

        if ((executorService) == null) {
            throw new ModelCheckerExecutorServiceNotProvided();
        }
    }

    /**
     * Returns a specific model checking algorithm such as BFS.
     * The appropriate strategy is created by providing the type of the algorithm via the argument of type
     * {@link SimulationStrategy.Type}.
     *
     * @param simulationStrategyType type of the model checking algorithm
     * @return the model checking strategy according to the provided type
     * @throws NoSuchMethodException     if the strategy could not be created or does not exist
     * @throws IllegalAccessException    if the strategy could not be created or does not exist
     * @throws InvocationTargetException if the strategy could not be created or does not exist
     * @throws InstantiationException    if the strategy could not be created or does not exist
     */
    private ModelCheckingStrategy<B> createStrategy(SimulationStrategy.Type simulationStrategyType)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<? extends ModelCheckingStrategy> simulationStrategyClass =
                SimulationStrategy.getSimulationStrategyClass(simulationStrategyType);
        return simulationStrategyClass.getConstructor(BigraphModelChecker.class).newInstance(this);
    }

    /**
     * Perform the simulation based on the provided reactive system and options.
     *
     * @throws BigraphSimulationException if agent is {@code null} or the simulation strategy was not selected
     */
    public void execute() throws BigraphSimulationException, ReactiveSystemException {
        assertReactionSystemValid();
        doWork();
        prepareOutput();
    }

    /**
     * Asynchronously start the simulation based on the provided reactive system and options.
     *
     * @throws BigraphSimulationException if agent is {@code null} or the simulation strategy was not selected
     */
    public Future<ReactionGraph<B>> executeAsync() throws BigraphSimulationException, ReactiveSystemException {
        assertReactionSystemValid();
        return executorService.submit(() -> {
            doWork();
            return getReactionGraph();
        });
    }

    private void doWork() {
        reactiveSystemListener.onReactiveSystemStarted();
        modelCheckingStrategy.synthesizeTransitionSystem();
        if (modelCheckingStrategy instanceof ModelCheckingStrategySupport) {
            int occurrenceCount = ((ModelCheckingStrategySupport<B>) modelCheckingStrategy).getOccurrenceCount();
            getReactionGraph().getGraphStats().setOccurrenceCount(occurrenceCount);
        }
        reactiveSystemListener.onReactiveSystemFinished();
    }

    /**
     * Performs some checks if the reactive system is valid.
     *
     * @throws BigraphSimulationException if the system is not valid
     */
    protected void assertReactionSystemValid() throws ReactiveSystemException {
        if ((reactiveSystem.getAgent()) == null) {
            throw new AgentIsNullException();
        }
        if (!reactiveSystem.getAgent().isGround()) {
            throw new AgentNotGroundException();
        }
        if (!reactiveSystem.getAgent().isPrime()) {
            throw new AgentNotPrimeException();
        }
        if (Objects.isNull(modelCheckingStrategy)) {
            throw new InvalidSimulationStrategy();
        }
    }

    public ReactiveSystem<B> getReactiveSystem() {
        return reactiveSystem;
    }

    public List<ReactiveSystemPredicate<B>> getPredicates() {
        return new ArrayList<>(reactiveSystem.getPredicates());
    }

    public AbstractBigraphMatcher<B> getMatcher() {
        return AbstractBigraphMatcher.create(this.genericType); // matcher;
    }

    public synchronized ReactionGraph<B> getReactionGraph() {
        return reactionGraph;
    }

    public synchronized <A> A watch(Supplier<A> function) {
        if (options.isMeasureTime()) {
            Stopwatch timer = Stopwatch.createStarted();
            A apply = function.get();
            long elapsed = timer.stop().elapsed(TimeUnit.MILLISECONDS);
            logger.debug("Time (ms): {}", elapsed);
            return apply;
        } else {
            return function.get();
        }
    }

    /**
     * Exports a bigraph to the filesystem using the export options setting from the member variable {@link #options}.
     *
     * @param bigraph       the bigraph to be exported
     * @param canonicalForm its canonical form
     * @param suffix        a suffix for the filename
     * @return the exported state label, or {@code null}
     */
    protected String exportState(B bigraph, String canonicalForm, String suffix) {
        if (Objects.nonNull(options.get(ModelCheckingOptions.Options.EXPORT))) {
            ModelCheckingOptions.ExportOptions opts = options.get(ModelCheckingOptions.Options.EXPORT);
            if (opts.hasOutputStatesFolder()) {
                String label = "";
                try {
                    if (reactionGraph.getLabeledNodeByCanonicalForm(canonicalForm).isPresent() &&
                            reactionGraph.getLabeledNodeByCanonicalForm(canonicalForm).get() instanceof ReactionGraph.DefaultLabeledNode) {
                        label = reactionGraph.getLabeledNodeByCanonicalForm(canonicalForm).get().getLabel();
                    } else {
                        label = String.format("state-%s.png", suffix);
                    }

                    if (opts.isXMIEnabled()) {
                        BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) bigraph, new FileOutputStream(
                                Paths.get(opts.getOutputStatesFolder().toString(), label) + ".xmi"));
                        logger.debug("Exporting state as xmi {}", label);
                    }
                    if (opts.isPNGEnabled()) {
                        BigraphGraphvizExporter.toPNG(bigraph,
                                true,
                                Paths.get(opts.getOutputStatesFolder().toString(), label + ".png").toFile()
                        );
                        logger.debug("Exporting state as png {}", label);
                    }
                    return label;
                } catch (IOException e) {
                    logger.error(e.toString());
                    if (!label.isEmpty()) {
                        return label;
                    }
                }
            }
        }
        return null;
    }

    private void onAttachListener(BigraphModelChecker<B> modelChecker) {
        if (modelChecker instanceof BigraphModelChecker.ReactiveSystemListener) {
            modelChecker.setReactiveSystemListener((BigraphModelChecker.ReactiveSystemListener<B>) this);
        } else {
            modelChecker.setReactiveSystemListener((ReactiveSystemListener<B>) EMPTY_LISTENER);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<B> getGenericTypeClass() {
        try {
            //(Class<B>) GenericTypeResolver.resolveTypeArgument(getClass(), BigraphModelChecker.class);
            String className = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName();
            Class<?> clazz = Class.forName(className);
            return (Class<B>) clazz;
        } catch (Exception e) {
            throw new IllegalStateException("Class is not parametrized with a generic type!");
        }
    }


    void prepareOutput() {
        try {
            exportReactionGraph(getReactionGraph());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void exportReactionGraph(ReactionGraph<B> reactionGraph) throws IOException {
        ModelCheckingOptions.ExportOptions opts = options.get(ModelCheckingOptions.Options.EXPORT);
        if (opts != null && opts.getReactionGraphFile() != null) {
            if (!reactionGraph.isEmpty()) {
                ReactionGraphExporter<B> graphExporter = new ReactionGraphExporter<>(reactiveSystem);
                graphExporter.toPNG(reactionGraph, opts.getReactionGraphFile());
            } else {
                logger.debug("Trace is not exported because reaction graph is empty.");
            }
        } else {
            logger.debug("Output path for Trace wasn't set. Will not export.");
        }
    }

    public synchronized BigraphModelChecker<B> setReactiveSystemListener(BigraphModelChecker.ReactiveSystemListener<B> reactiveSystemListener) {
        this.reactiveSystemListener = reactiveSystemListener;
        return this;
    }

    public interface ReactiveSystemListener<B extends Bigraph<? extends Signature<?>>> {

        default void onReactiveSystemStarted() {
        }

        default void onReactiveSystemFinished() {
        }

        default void onCheckingReactionRule(ReactionRule<B> reactionRule) {
        }

        /**
         * This method is called within a running simulation (i.e., model checking operation), when the redex of a
         * reaction rule could be matched within the host bigraph (i.e., the last active agent of the reactive system).
         *
         * @param agent        the agent where the redex pattern was found
         * @param reactionRule the respective reaction rule
         * @param matchResult  the result of the matching
         */
        default void onUpdateReactionRuleApplies(B agent, ReactionRule<B> reactionRule, BigraphMatch<B> matchResult) {
        }

        default void onReactionIsNull() {
        }

        /**
         * This method is called if all available predicates of a reactive system evaluated to true for some state.
         * In this case, the method {@link ReactiveSystemListener#onPredicateMatched(Bigraph, ReactiveSystemPredicate)}
         * is not called.
         *
         * @param currentAgent the agent
         * @param label
         */
        default void onAllPredicateMatched(B currentAgent, String label) {
        }

        /**
         * This method is called if a predicate evaluated to {@code true} for some state.
         * It is only called if not all predicates yielded {@code true}.
         *
         * @param currentAgent the agent
         * @param predicate    the predicate
         */
        default void onPredicateMatched(B currentAgent, ReactiveSystemPredicate<B> predicate) {

        }

        /**
         * This method is called if a sub-bigraph-predicate evaluated to {@code true} for some state.
         * It is only called if not all predicates yielded {@code true}.
         *
         * @param currentAgent the agent
         * @param predicate    the predicate
         * @param subBigraph   the sub-bigraph as matched by the predicate in currentAgent
         */
        default void onSubPredicateMatched(B currentAgent, ReactiveSystemPredicate<B> predicate, B context, B subBigraph, B redexOnly, B paramsOnly) {

        }

        /**
         * Reports a violation of a predicate and supplies a counterexample trace from the initial state to the
         * violating state.
         *
         * @param currentAgent        the agent
         * @param predicate           the predicate
         * @param counterExampleTrace the trace representing a counterexample
         */
        default void onPredicateViolated(B currentAgent, ReactiveSystemPredicate<B> predicate, GraphPath<ReactionGraph.LabeledNode, ReactionGraph.LabeledEdge> counterExampleTrace) {
        }


        default void onError(Exception e) {

        }
    }
}
