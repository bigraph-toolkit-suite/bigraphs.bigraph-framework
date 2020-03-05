package de.tudresden.inf.st.bigraphs.simulation.modelchecking;

import com.google.common.base.Stopwatch;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.providers.ExecutorServicePoolProvider;
import de.tudresden.inf.st.bigraphs.simulation.ReactionRule;
import de.tudresden.inf.st.bigraphs.simulation.ReactiveSystem;
import de.tudresden.inf.st.bigraphs.simulation.encoding.BigraphCanonicalForm;
import de.tudresden.inf.st.bigraphs.simulation.exceptions.AgentIsNullException;
import de.tudresden.inf.st.bigraphs.simulation.exceptions.BigraphSimulationException;
import de.tudresden.inf.st.bigraphs.simulation.exceptions.InvalidSimulationStrategy;
import de.tudresden.inf.st.bigraphs.simulation.exceptions.ModelCheckerExecutorServiceNotProvided;
import de.tudresden.inf.st.bigraphs.simulation.matching.AbstractBigraphMatcher;
import de.tudresden.inf.st.bigraphs.simulation.matching.BigraphMatch;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.PredicateChecker;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.predicates.ReactiveSystemPredicates;
import de.tudresden.inf.st.bigraphs.visualization.BigraphGraphvizExporter;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A bigraph model checker that allows to simulate a bigraph by reaction rules. A reactive system, the strategy and
 * options must be provided.
 * <p>
 * The model checker allows to react on live predicate matches in the course of the simulation by listeners. They can
 * also be evaluated later by getting the reaction graph/system.
 *
 * @author Dominik Grzelak
 */
public abstract class BigraphModelChecker<B extends Bigraph<? extends Signature<?>>> {

    private Logger logger = LoggerFactory.getLogger(BigraphModelChecker.class);
    ExecutorService executorService;

    /**
     * Enum-like class that holds all kind of simulations.
     */
    public static class SimulationType<B extends Bigraph<? extends Signature<?>>> {

        public static final SimulationType<Bigraph<? extends Signature<?>>> BREADTH_FIRST =
                new SimulationType<>(BreadthFirstStrategy.class);
        public static final SimulationType<Bigraph<? extends Signature<?>>> RANDOM_STATE =
                new SimulationType<>(RandomAgentModelCheckingStrategy.class);

        private Class<? extends ModelCheckingStrategy<B>> strategyClass;

        private SimulationType(Class<? extends ModelCheckingStrategy> strategyClass) {
            this.strategyClass = (Class<? extends ModelCheckingStrategy<B>>) strategyClass;
        }

        protected Class<? extends ModelCheckingStrategy<B>> getStrategyClass() {
            return strategyClass;
        }
    }

    private final static BigraphModelChecker.ReactiveSystemListener<Bigraph<? extends Signature<?>>> DEFAULT_LISTENER = new BigraphModelChecker.EmptyReactiveSystemListener<>();

    protected ModelCheckingStrategy<B> modelCheckingStrategy;
    protected SimulationType<B> simulationType;
    protected BigraphModelChecker.ReactiveSystemListener<B> reactiveSystemListener;
    protected BigraphCanonicalForm canonicalForm = BigraphCanonicalForm.createInstance(true);
    protected PredicateChecker<B> predicateChecker = null;
    protected ModelCheckingOptions options;
    protected AbstractBigraphMatcher<B> matcher;

    final ReactiveSystem<B> reactiveSystem;
    ReactionGraph<B> reactionGraph;
//    protected MutableList<ReactiveSystemPredicates<B>> predicates = Lists.mutable.empty();

    public BigraphModelChecker(ReactiveSystem<B> reactiveSystem, ModelCheckingOptions options) {
        this(reactiveSystem, (SimulationType<B>) SimulationType.BREADTH_FIRST, options);
    }

    public BigraphModelChecker(ReactiveSystem<B> reactiveSystem, SimulationType<B> simulationType, ModelCheckingOptions options) {
        this(reactiveSystem, simulationType, options, null);
        onAttachListener(this);
    }

    public BigraphModelChecker(ReactiveSystem<B> reactiveSystem, SimulationType<B> simulationType, ModelCheckingOptions options,
                               ReactiveSystemListener<B> listener) {
        Optional.ofNullable(listener).map(this::setReactiveSystemListener).orElseGet(() -> setReactiveSystemListener((ReactiveSystemListener<B>) DEFAULT_LISTENER));
        loadServiceExecutor();

        this.reactiveSystem = reactiveSystem;
        this.reactionGraph = new ReactionGraph<>();
        this.matcher = AbstractBigraphMatcher.create(getGenericTypeClass());

        this.options = options;
        ModelCheckingOptions.TransitionOptions opts = options.get(ModelCheckingOptions.Options.TRANSITION);
        if (opts.allowReducibleClasses()) {
            canonicalForm = BigraphCanonicalForm.createInstance();
        }

        this.simulationType = simulationType;
        try {
            this.modelCheckingStrategy = createStrategy(this.simulationType);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadServiceExecutor() {
        ServiceLoader<ExecutorServicePoolProvider> load = ServiceLoader.load(ExecutorServicePoolProvider.class);
        load.reload();
        Iterator<ExecutorServicePoolProvider> iterator = load.iterator();
        if (iterator.hasNext()) {
            ExecutorServicePoolProvider next = iterator.next();
            executorService = next.provide();
        }

        if (Objects.isNull(executorService)) {
            throw new ModelCheckerExecutorServiceNotProvided();
        }
    }

    private ModelCheckingStrategy<B> createStrategy(SimulationType<B> simulationType) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return simulationType.getStrategyClass().getConstructor(BigraphModelChecker.class).newInstance(this);
    }

    /**
     * Perform the simulation based on the provided reactive system and options.
     *
     * @throws BigraphSimulationException if agent is {@code null} or the simulation strategy was not selected
     */
    public void execute() throws BigraphSimulationException {
        assertReactionSystemValid();
        doWork();
        prepareOutput();
    }

    //TODO: tasks for parallel jobs
    // https://stackify.com/java-thread-pools/
    // https://www.baeldung.com/java-executor-service-tutorial
    // https://github.com/pivovarit/parallel-collectors

    /**
     * Asynchronously start the simulation based on the provided reactive system and options.
     *
     * @throws BigraphSimulationException if agent is {@code null} or the simulation strategy was not selected
     */
    public Future<ReactionGraph<B>> executeAsync() throws BigraphSimulationException {
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
    protected void assertReactionSystemValid() throws BigraphSimulationException {
        if (Objects.isNull(reactiveSystem.getAgent())) {
            throw new AgentIsNullException();
        }
        if (Objects.isNull(modelCheckingStrategy)) {
            throw new InvalidSimulationStrategy();
        }
    }

    public ReactiveSystem<B> getReactiveSystem() {
        return reactiveSystem;
    }

    public List<ReactiveSystemPredicates<B>> getPredicates() {
        return reactiveSystem.getPredicates();
    }

    public AbstractBigraphMatcher<B> getMatcher() {
        return matcher;
    }

    //    public void setPredicates(MutableList<ReactiveSystemPredicates<B>> predicates) {
//        this.predicates = predicates;
//    }

    protected abstract B buildGroundReaction(final B agent, final BigraphMatch<B> match, ReactionRule<B> rule);

    protected abstract B buildParametricReaction(final B agent, final BigraphMatch<B> match, ReactionRule<B> rule);


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

    protected String exportState(B bigraph, String canonicalForm, String suffix) {
        if (Objects.nonNull(options.get(ModelCheckingOptions.Options.EXPORT))) {
            ModelCheckingOptions.ExportOptions opts = options.get(ModelCheckingOptions.Options.EXPORT);
            if (opts.hasOutputStatesFolder()) {
                try {
                    String label = "";
                    if (reactionGraph.getLabeledNodeByCanonicalForm(canonicalForm).isPresent() &&
                            reactionGraph.getLabeledNodeByCanonicalForm(canonicalForm).get() instanceof ReactionGraph.DefaultLabeledNode) {
                        label = reactionGraph.getLabeledNodeByCanonicalForm(canonicalForm).get().getLabel();
                    } else {
                        label = String.format("state-%s.png", suffix);
                    }
                    BigraphGraphvizExporter.toPNG(bigraph,
                            true,
                            Paths.get(opts.getOutputStatesFolder().toString(), label).toFile()
                    );
                    logger.debug("Exporting state {}", suffix);
                    return label;
//                BigraphArtifacts.exportAsInstanceModel(agentReacted, new FileOutputStream(String.format("instance-model_%s.xmi", cnt)));
                } catch (IOException e) {
                    logger.error(e.toString());
                }
            }
        }
        return null;
    }

    private void onAttachListener(BigraphModelChecker<B> modelChecker) {
        if (modelChecker instanceof BigraphModelChecker.ReactiveSystemListener) {
            modelChecker.setReactiveSystemListener((BigraphModelChecker.ReactiveSystemListener) this);
        } else {
            modelChecker.setReactiveSystemListener((BigraphModelChecker.ReactiveSystemListener<B>) DEFAULT_LISTENER);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<B> getGenericTypeClass() {
        try {
            String className = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName();
            Class<?> clazz = Class.forName(className);
            return (Class<B>) clazz;
        } catch (Exception e) {
            throw new IllegalStateException("Class is not parametrized with a generic type!");
        }
    }

    private static class EmptyReactiveSystemListener<B extends Bigraph<? extends Signature<?>>> implements BigraphModelChecker.ReactiveSystemListener<B> {
    }

    void prepareOutput() {
        exportReactionGraph(getReactionGraph());
    }

    public void exportReactionGraph(ReactionGraph<B> reactionGraph) {
        ModelCheckingOptions.ExportOptions opts = options.get(ModelCheckingOptions.Options.EXPORT);
        if (Objects.nonNull(opts.getTraceFile())) {
            if (!reactionGraph.isEmpty()) {
                exportGraph(reactionGraph.getGraph(), opts.getTraceFile());
            } else {
                logger.debug("Trace is not exported because reaction graph is empty.");
            }
        } else {
            logger.debug("Output path for Trace wasn't set. Will not export.");
        }
    }

    void exportGraph(Graph g, File imgFile) {
        JGraphXAdapter<String, DefaultEdge> graphAdapter = new JGraphXAdapter<>(g);
//        mxIGraphLayout layout = new mxCompactTreeLayout(graphAdapter);
        mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter, SwingConstants.NORTH);
//        ((mxHierarchicalLayout)layout).setFineTuning(true);
//        ((mxHierarchicalLayout)layout).setResizeParent(true);
        layout.execute(graphAdapter.getDefaultParent());

        BufferedImage image =
                mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, false, null);
        try {
            if (!imgFile.exists()) {
                if (!imgFile.createNewFile()) {
                    throw new IOException("Create new file failed.");
                }
            }
            if (image == null) {
                logger.warn("Image is null, cannot write image.");
            } else {
                ImageIO.write(image, "PNG", imgFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.toString());
        }
    }

    public synchronized BigraphModelChecker<B> setReactiveSystemListener(BigraphModelChecker.ReactiveSystemListener<B> reactiveSystemListener) {
        this.reactiveSystemListener = reactiveSystemListener;
        return this;
    }

    public interface ReactiveSystemListener<B extends Bigraph<? extends Signature<?>>> {

        default void onReactiveSystemStarted() {
        }

        default void onCheckingReactionRule(ReactionRule<B> reactionRule) {
        }

        default void onReactiveSystemFinished() {
        }

        /**
         * This method is called by within a simulation, when the redex of a reaction rule could be matched within the
         * host bigraph (i.e., the last active agent of the reactive system).
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
         * This method is called if all available predicates of a reactive system evaluated to true in one state.
         * In this case, the method {@link ReactiveSystemListener#onPredicateMatched(Bigraph, ReactiveSystemPredicates)}
         * is not called.
         *
         * @param currentAgent the agent
         * @param label
         */
        default void onAllPredicateMatched(B currentAgent, String label) {
        }

        /**
         * This method is called if a predicate evaluated to {@code true} after a transition.
         * It is only called if not all predicates yielded {@code true}.
         *
         * @param currentAgent the agent
         * @param predicate    the predicate
         */
        default void onPredicateMatched(B currentAgent, ReactiveSystemPredicates<B> predicate) {

        }

        /**
         * Reports a violation of a predicate and supplies a counterexample trace from the initial state to the
         * violating state.
         *
         * @param currentAgent        the agent
         * @param predicate           the predicate
         * @param counterExampleTrace the trace representing a counterexample
         */
        default void onPredicateViolated(B currentAgent, ReactiveSystemPredicates<B> predicate, GraphPath<ReactionGraph.LabeledNode, ReactionGraph.LabeledEdge> counterExampleTrace) {
        }


        default void onError(Exception e) {

        }
    }
}
