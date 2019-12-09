package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation;

import com.google.common.base.Stopwatch;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.rewriting.ReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.ReactiveSystem;
import de.tudresden.inf.st.bigraphs.rewriting.ReactiveSystemOptions;
import de.tudresden.inf.st.bigraphs.rewriting.encoding.BigraphCanonicalForm;
import de.tudresden.inf.st.bigraphs.rewriting.matching.AbstractBigraphMatcher;
import de.tudresden.inf.st.bigraphs.rewriting.matching.BigraphMatch;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.PredicateChecker;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.ReactionGraph;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.exceptions.AgentIsNullException;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.exceptions.BigraphSimulationException;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.exceptions.InvalidSimulationStrategy;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.predicates.ReactiveSystemPredicates;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Objects;
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

    public static class SimulationType {

        public static final SimulationType BREADTH_FIRST = new SimulationType(BreadthFirstStrategy.class);
        public static final SimulationType RANDOM_STATE = new SimulationType(RandomAgentSimulationStrategy.class);

        private Class<? extends SimulationStrategy> strategyClass;

        private SimulationType(Class<? extends SimulationStrategy> strategyClass) {
            this.strategyClass = strategyClass;
        }

        protected Class<? extends SimulationStrategy> getStrategyClass() {
            return strategyClass;
        }
    }

    private final static BigraphModelChecker.ReactiveSystemListener DEFAULT_LISTENER = new BigraphModelChecker.EmptyReactiveSystemListener();

    protected SimulationStrategy<B> simulationStrategy;
    protected SimulationType simulationType;
    protected BigraphModelChecker.ReactiveSystemListener<B> reactiveSystemListener;
    protected BigraphCanonicalForm canonicalForm = BigraphCanonicalForm.createInstance();
    protected PredicateChecker<B> predicateChecker = null;
    protected ReactiveSystemOptions options;
    protected AbstractBigraphMatcher<B> matcher;

    final ReactiveSystem<B> reactiveSystem;
    ReactionGraph<B> reactionGraph;
//    protected MutableList<ReactiveSystemPredicates<B>> predicates = Lists.mutable.empty();

    public BigraphModelChecker(ReactiveSystem<B> reactiveSystem, SimulationType simulationType, ReactiveSystemOptions options) {
        onAttachListener(this);
        this.simulationType = simulationType;
        try {
            this.simulationStrategy = createStrategy(this.simulationType);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException(e);
        }
        this.reactiveSystem = reactiveSystem;
        this.options = options;
        this.reactionGraph = new ReactionGraph<>();
        this.matcher = AbstractBigraphMatcher.create(getGenericTypeClass());
    }

    private SimulationStrategy<B> createStrategy(SimulationType simulationType) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return (SimulationStrategy<B>) simulationType.getStrategyClass().getConstructor(BigraphModelChecker.class).newInstance(this);
    }

    // Compute transition system method
    //TODO: tasks for parallel jobs
    // https://stackify.com/java-thread-pools/
    // https://www.baeldung.com/java-executor-service-tutorial
    // https://github.com/pivovarit/parallel-collectors
    // https://www.baeldung.com/java-8-parallel-streams-custom-threadpool
    public void execute() throws BigraphSimulationException {
        assertReactionSystemValid();
        //TODO strategy execute in future
        simulationStrategy.synthesizeTransitionSystem();
        prepareOutput();
    }

    protected void assertReactionSystemValid() throws BigraphSimulationException {
        if (Objects.isNull(reactiveSystem.getAgent())) {
            throw new AgentIsNullException();
        }
        if (Objects.isNull(simulationStrategy)) {
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

    public <A> A watch(Supplier<A> function) {
        if (options.isMeasureTime()) {
            Stopwatch timer = Stopwatch.createStarted();
            A apply = function.get();
            long elapsed = timer.stop().elapsed(TimeUnit.MILLISECONDS);
            System.out.println("Time (ms) " + elapsed);
            return apply;
        } else {
            return function.get();
        }
    }


//    /**
//     * Use the agent which must be not {@code null}, before calling this method.
//     *
//     * @param options additional options
//     */
//    public synchronized void computeTransitionSystem(final ReactiveSystemOptions options) {
//        computeTransitionSystem(reactiveSystem.getAgent(), options, Collections.emptyList());
//    }
//
//    /**
//     * Compute the transition system of a bigraph with all added reaction rules so far.
//     *
//     * @param agent   the initial agent
//     * @param options additional options
//     */
//    public synchronized void computeTransitionSystem(final B agent, final ReactiveSystemOptions options) {
//        computeTransitionSystem(agent, options, Collections.emptyList());
//    }
//
//    public abstract void computeTransitionSystem(final B agent, final ReactiveSystemOptions options, final Collection<ReactiveSystemPredicates<B>> predicates);
//
//    public synchronized ReactiveSystemOptions getOptions() {
//        return options;
//    }

    private void onAttachListener(BigraphModelChecker<B> reactiveSystem) {
        if (reactiveSystem instanceof BigraphModelChecker.ReactiveSystemListener) {
            reactiveSystem.setReactiveSystemListener((BigraphModelChecker.ReactiveSystemListener) this);
        } else {
            reactiveSystem.setReactiveSystemListener((BigraphModelChecker.ReactiveSystemListener<B>) DEFAULT_LISTENER);
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
        ReactiveSystemOptions.ExportOptions opts = options.get(ReactiveSystemOptions.Options.EXPORT);
        if (Objects.nonNull(opts.getTraceFile())) {
            exportGraph(reactionGraph.getGraph(), opts.getTraceFile());
        }
    }

    void exportGraph(Graph g, File imgFile) {
        JGraphXAdapter<String, DefaultEdge> graphAdapter = new JGraphXAdapter<String, DefaultEdge>(g);
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
                System.out.println("Image is null, cannot write image.");
            } else {
                ImageIO.write(image, "PNG", imgFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //    @Override
    public synchronized void setReactiveSystemListener(BigraphModelChecker.ReactiveSystemListener<B> reactiveSystemListener) {
        this.reactiveSystemListener = reactiveSystemListener;
    }

    interface ReactiveSystemListener<B extends Bigraph<? extends Signature<?>>> {

        default void onReactiveSystemStarted() {
        }

        default void onCheckingReactionRule(ReactionRule<B> reactionRule) {
        }

        default void onReactiveSystemFinished() {
        }

        default void onUpdateReactionRuleApplies() {
        }

        default void onReactionIsNull() {
        }

        /**
         * Reports a violation of a predicate and supplies a counter-example trace from the starting state to the violating state
         * to the method.
         *
         * @param currentAgent
         * @param predicate
         * @param counterExampleTrace
         */
        default void onPredicateViolated(B currentAgent, ReactiveSystemPredicates<B> predicate, GraphPath<String, ReactionGraph.LabeledEdge> counterExampleTrace) {
        }

        default void onAllPredicateMatched(B currentAgent) {
        }

        default void onError(Exception e) {

        }
    }
}
