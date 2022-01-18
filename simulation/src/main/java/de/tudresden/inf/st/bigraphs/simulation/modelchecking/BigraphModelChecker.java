package de.tudresden.inf.st.bigraphs.simulation.modelchecking;

import com.google.common.base.Stopwatch;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphFileModelManagement;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.EcoreBigraph;
import de.tudresden.inf.st.bigraphs.core.exceptions.AgentIsNullException;
import de.tudresden.inf.st.bigraphs.core.exceptions.AgentNotGroundException;
import de.tudresden.inf.st.bigraphs.core.exceptions.AgentNotPrimeException;
import de.tudresden.inf.st.bigraphs.core.exceptions.ReactiveSystemException;
import de.tudresden.inf.st.bigraphs.core.providers.ExecutorServicePoolProvider;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.*;
import de.tudresden.inf.st.bigraphs.simulation.encoding.BigraphCanonicalForm;
import de.tudresden.inf.st.bigraphs.simulation.exceptions.*;
import de.tudresden.inf.st.bigraphs.simulation.matching.AbstractBigraphMatcher;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.export.mxReactionGraph;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.export.StyleConstants;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.predicates.PredicateChecker;
import de.tudresden.inf.st.bigraphs.visualization.BigraphGraphvizExporter;
import org.jgrapht.GraphPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
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
 * A bigraph model checker that allows to simulate a BRS by reaction rules. A reactive system, the strategy and
 * options must be provided.
 * <p>
 * The model checker allows to react on live predicate matches in the course of the simulation by <i>listeners</i>.
 * They can also be evaluated later by getting the reaction graph/system.
 *
 * @author Dominik Grzelak
 */
public abstract class BigraphModelChecker<B extends Bigraph<? extends Signature<?>>> {

    private Logger logger = LoggerFactory.getLogger(BigraphModelChecker.class);
    ExecutorService executorService;
    private final Class<B> genericType;

    protected ModelCheckingStrategy<B> modelCheckingStrategy;
    protected SimulationStrategy.Type simulationStrategyType;
    protected BigraphModelChecker.ReactiveSystemListener<B> reactiveSystemListener;
    protected BigraphCanonicalForm canonicalForm = BigraphCanonicalForm.createInstance(true);
    protected PredicateChecker<B> predicateChecker = null;
    protected ModelCheckingOptions options;
//    protected AbstractBigraphMatcher<B> matcher;

    final ReactiveSystem<B> reactiveSystem;
    ReactionGraph<B> reactionGraph;

    /**
     * Enum-like class that holds all kind of simulations.
     */
    public static class SimulationStrategy {

        public enum Type {
            BFS, RANDOM;
        }

        public static <B extends Bigraph<? extends Signature<?>>> Class<? extends ModelCheckingStrategy> getSimulationStrategyClass(Type type, Class<B> bigraphClass) {
            switch (type) {
                case BFS:
                    return BreadthFirstStrategy.class;
                case RANDOM:
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
        ModelCheckingOptions.TransitionOptions opts = options.get(ModelCheckingOptions.Options.TRANSITION);
        if (opts.allowReducibleClasses()) {
            this.canonicalForm = BigraphCanonicalForm.createInstance();
        }

        this.simulationStrategyType = simulationStrategyType;
        try {
            this.modelCheckingStrategy = createStrategy(this.simulationStrategyType);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    public BigraphCanonicalForm acquireCanonicalForm() {
        if (((ModelCheckingOptions.TransitionOptions) options.get(ModelCheckingOptions.Options.TRANSITION)).allowReducibleClasses()) {
            return BigraphCanonicalForm.createInstance();
        } else {
            return BigraphCanonicalForm.createInstance(true);
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

        if ((executorService) == null) {
            throw new ModelCheckerExecutorServiceNotProvided();
        }
    }

    private ModelCheckingStrategy<B> createStrategy(SimulationStrategy.Type simulationStrategyType)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<? extends ModelCheckingStrategy> simulationStrategyClass =
                SimulationStrategy.getSimulationStrategyClass(simulationStrategyType, this.genericType);
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

    //TODO: tasks for parallel jobs
    // https://stackify.com/java-thread-pools/
    // https://www.baeldung.com/java-executor-service-tutorial
    // https://github.com/pivovarit/parallel-collectors

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
                    BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) bigraph, new FileOutputStream(
                            Paths.get(opts.getOutputStatesFolder().toString(), label) + ".xmi"));
                    logger.debug("Exporting state {}", label);
                    BigraphGraphvizExporter.toPNG(bigraph,
                            true,
                            Paths.get(opts.getOutputStatesFolder().toString(), label + ".png").toFile()
                    );
                    return label;
//                BigraphFileModelManagement.exportAsInstanceModel(agentReacted, new FileOutputStream(String.format("instance-model_%s.xmi", cnt)));
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
        exportReactionGraph(getReactionGraph());
    }

    public void exportReactionGraph(ReactionGraph<B> reactionGraph) {
        ModelCheckingOptions.ExportOptions opts = options.get(ModelCheckingOptions.Options.EXPORT);
        if (Objects.nonNull(opts.getReactionGraphFile())) {
            if (!reactionGraph.isEmpty()) {
                exportGraph(reactionGraph, opts.getReactionGraphFile());
            } else {
                logger.debug("Trace is not exported because reaction graph is empty.");
            }
        } else {
            logger.debug("Output path for Trace wasn't set. Will not export.");
        }
    }

    void exportGraph(ReactionGraph<B> bReactionGraph, File imgFile) {

//        Graph g = bReactionGraph.getGraph();
        mxReactionGraph graphAdapter = new mxReactionGraph(bReactionGraph, reactiveSystem);
        graphAdapter.getStylesheet().putCellStyle("MATCHED", StyleConstants.predicateMatchedNodeStylesheet());
        graphAdapter.getStylesheet().putCellStyle("DEFAULT", StyleConstants.defaultNodeStylesheet());
        graphAdapter.getStylesheet().putCellStyle("DEFAULT_EDGE", StyleConstants.defaultEdgeStylesheet());
//        mxIGraphLayout layout = new mxCompactTreeLayout(graphAdapter);
        mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter, SwingConstants.NORTH);
//        ((mxHierarchicalLayout) layout).setFineTuning(true);
//        ((mxHierarchicalLayout) layout).setResizeParent(true);
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
         * This method is called if all available predicates of a reactive system evaluated to true in one state.
         * In this case, the method {@link ReactiveSystemListener#onPredicateMatched(Bigraph, ReactiveSystemPredicate)}
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
        default void onPredicateMatched(B currentAgent, ReactiveSystemPredicate<B> predicate) {

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
