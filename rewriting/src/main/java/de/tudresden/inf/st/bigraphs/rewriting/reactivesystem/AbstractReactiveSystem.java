package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem;

import com.google.common.base.Stopwatch;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.rewriting.BigraphCanonicalForm;
import de.tudresden.inf.st.bigraphs.rewriting.ReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.ReactiveSystem;
import de.tudresden.inf.st.bigraphs.rewriting.ReactiveSystemOptions;
import de.tudresden.inf.st.bigraphs.rewriting.matching.AbstractBigraphMatcher;
import de.tudresden.inf.st.bigraphs.rewriting.matching.BigraphMatch;
import de.tudresden.inf.st.bigraphs.rewriting.matching.MatchIterable;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.reactions.InOrderReactionRuleSupplier;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.reactions.ReactionRuleSupplier;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

//TODO: add tactics/order/priorities for RR execution (here?)

/**
 * Algorithm for reaction graph is from (Perrone, 2013) used to perform readability analysis.
 *
 * @author Dominik Grzelak
 */
public abstract class AbstractReactiveSystem<B extends Bigraph<? extends Signature<?>>> implements ReactiveSystem<B> {
    private final static ReactiveSystem.ReactiveSystemListener DEFAULT_LISTENER = new EmptyReactiveSystemListener();

    protected ReactiveSystemListener<B> reactiveSystemListener;

    protected BiMap<String, ReactionRule<B>> reactionRules = HashBiMap.create();
    protected ReactiveSystemOptions options;
    protected AbstractBigraphMatcher<B> matcher;
    protected ReactionGraph<B> reactionGraph;
    protected BigraphCanonicalForm canonicalForm = BigraphCanonicalForm.getInstance();
    private PredicateChecker<B> predicateChecker = null;

    public AbstractReactiveSystem() {
        onAttachListener(this);
        matcher = AbstractBigraphMatcher.create(getGenericTypeClass());
        reactionGraph = new ReactionGraph<>();
    }

    @Override
    public synchronized Collection<ReactionRule<B>> getReactionRules() {
        return reactionRules.values();
    }

    @SuppressWarnings("unused")
    public synchronized boolean addReactionRule(ReactionRule<B> reactionRule) {
        if (!reactionRules.containsValue(reactionRule)) {
            reactionRules.put(rSupplier.get(), reactionRule);
            return true;
        }
        return false;
    }

    /**
     * Compute the transition system of a bigraph with all added reaction rules so far.
     *
     * @param agent   the initial agent
     * @param options additional options
     */
    public synchronized void computeTransitionSystem(final B agent, final ReactiveSystemOptions options) {
        computeTransitionSystem(agent, options, Collections.emptyList());

    }

    /**
     * Compute the transition system of a bigraph with all added reaction rules so far.
     *
     * @param agent      the initial agent
     * @param options    additional options
     * @param predicates additional predicates to check at each states
     */
    public synchronized void computeTransitionSystem(final B agent, final ReactiveSystemOptions options, final Collection<TransitionPredicates<B>> predicates) {
        this.predicateChecker = new PredicateChecker<>(predicates);
        this.options = options;
        this.reactionGraph.reset();
        final Queue<B> workingQueue = new ConcurrentLinkedDeque<>();
        String rootBfcs = canonicalForm.bfcs(agent);
        workingQueue.add(agent);
        int transitionCnt = 0;
        ReactiveSystemOptions.TransitionOptions transitionOptions = this.options.get(ReactiveSystemOptions.Options.TRANSITION);
        while (!workingQueue.isEmpty() && transitionCnt < transitionOptions.getMaximumTransitions()) {
            // "Remove the first element w of the work queue Q."
            final B theAgent = workingQueue.remove();
            // "For each reaction rule, find all matches m1 ...mn in w"
            String bfcfOfW = canonicalForm.bfcs(theAgent);
            //TODO: generate appropriate supplier for the given option
            InOrderReactionRuleSupplier<B> inOrder = ReactionRuleSupplier.<B>createInOrder(getReactionRules());
            Stream.generate(inOrder)
                    .limit(getReactionRules().size())
                    .peek(x -> reactiveSystemListener.onCheckingReactionRule(x))
                    .forEachOrdered(eachRule -> {
                        MatchIterable<BigraphMatch<B>> match = watch(() -> matcher.match(theAgent, eachRule.getRedex()));
//                        MatchIterable<BigraphMatch<B>> match = matcher.match(theAgent, eachRule.getRedex());
                        Iterator<BigraphMatch<B>> iterator = match.iterator();
                        while (iterator.hasNext()) {
                            BigraphMatch<B> next = iterator.next();
//                            System.out.println("NEXT: " + next);
                            B reaction = null;
                            if (next.getParameters().size() == 0) {
                                reaction = buildGroundReaction(theAgent, next, eachRule);
                            } else {
                                //TODO: beachte instantiation map
                                reaction = buildParametricReaction(theAgent, next, eachRule);
                            }
//                            assert Objects.nonNull(reaction);
                            if (Objects.nonNull(reaction)) {
                                String bfcf = canonicalForm.bfcs(reaction);
                                String reactionLbl = reactionRules.inverse().get(eachRule);
                                if (!reactionGraph.containsBigraph(bfcf)) {
                                    reactionGraph.addEdge(theAgent, bfcfOfW, reaction, bfcf, next.getRedex(), reactionLbl);
                                    workingQueue.add(reaction);
                                } else {
                                    reactionGraph.addEdge(theAgent, bfcfOfW, reaction, bfcf, next.getRedex(), reactionLbl);
                                }
                            }
                        }
                    });
            if (predicateChecker.getPredicates().size() > 0) {
                // "Check each property p ∈ P against w."
                if (!predicateChecker.checkAll(theAgent)) {
                    // compute counter-example trace from w back to the root

//                DijkstraShortestPath<String, String> dijkstraShortestPath = new DijkstraShortestPath<>(reactionGraph.getGraph());
                    GraphPath<String, ReactionGraph.LabeledEdge> pathBetween = DijkstraShortestPath.findPathBetween(reactionGraph.getGraph(), bfcfOfW, rootBfcs);
                    //TODO: report violation of the predicates
                    for (Map.Entry<TransitionPredicates<B>, Boolean> eachPredciate : predicateChecker.getChecked().entrySet()) {
                        if (!eachPredciate.getValue()) {
                            System.out.println("Counter-example trace for predicate violation: " + pathBetween);
//                        System.out.println("Violation of predicate = " + eachPredciate.getKey());
                            reactiveSystemListener.onPredicateViolated(theAgent, eachPredciate.getKey(), pathBetween);
                        }
                    }
                } else {
//                System.out.println("Matched");
                    reactiveSystemListener.onAllPredicateMatched(theAgent);
                }
            }

            // "Repeat the procedure for the next item in the work queue, terminating successfully if the work queue is empty."
            transitionCnt++;
        }

        prepareOutput();
    }

    public void prepareOutput() {
        ReactiveSystemOptions.ExportOptions opts = options.get(ReactiveSystemOptions.Options.EXPORT);
        if (Objects.nonNull(opts.getTraceFile())) {
            exportGraph(reactionGraph.getGraph(), opts.getTraceFile());
        }
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

    private void exportGraph(Graph g, File imgFile) {
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

    public synchronized ReactionGraph<B> getReactionGraph() {
        return reactionGraph;
    }

    //TODO: tasks for parallel jobs
    // https://stackify.com/java-thread-pools/
    // https://www.baeldung.com/java-executor-service-tutorial
    // https://github.com/pivovarit/parallel-collectors
    // https://www.baeldung.com/java-8-parallel-streams-custom-threadpool

    // TODO: WIP simulation
    public synchronized void simulate(final B agent, final ReactiveSystemOptions options) {
//        AtomicInteger maximumTransitions = new AtomicInteger(options.getMaximumTransitions());
        ReactiveSystemOptions.TransitionOptions transitionOptions = this.options.get(ReactiveSystemOptions.Options.TRANSITION);
        AtomicInteger maximumTransitions = new AtomicInteger(transitionOptions.getMaximumTransitions());
        AtomicInteger currentTransitionCount = new AtomicInteger(0);

        reactiveSystemListener.onReactiveSystemStarted();
        // while...
        // call appropriate listener methods

        //for each "available" agents not in visitedAgents
        // add here the "tactic" supplier: now is in-order
        InOrderReactionRuleSupplier<B> inOrder = ReactionRuleSupplier.createInOrder(getReactionRules());
        Stream.generate(inOrder)
                .limit(getReactionRules().size())
                .peek(x -> {
                    reactiveSystemListener.onCheckingReactionRule(x);
                })
                .forEach(eachRule -> {
                    //check if eachRule would create infinite cycles with the current agent
                    MatchIterable match = matcher.match(agent, (B) eachRule.getRedex());
                    Iterator<BigraphMatch<?>> iterator = match.iterator();
                    while (iterator.hasNext()) {
                        BigraphMatch<B> next = (BigraphMatch<B>) iterator.next();
                        System.out.println("NEXT: " + next);
                        if (next.getParameters().size() == 0) {
                            buildGroundReaction(agent, next, eachRule);
                        } else {
                            buildParametricReaction(agent, next, eachRule);
                        }
                    }
                });
        currentTransitionCount.incrementAndGet();


        //end
        reactiveSystemListener.onReactiveSystemFinished();
    }

    protected abstract B buildGroundReaction(final B agent, final BigraphMatch<B> match, ReactionRule<B> rule);

    protected abstract B buildParametricReaction(final B agent, final BigraphMatch<B> match, ReactionRule<B> rule);

    @Override
    public synchronized void setReactiveSystemListener(ReactiveSystemListener<B> reactiveSystemListener) {
        this.reactiveSystemListener = reactiveSystemListener;
    }

    public synchronized ReactiveSystemOptions getOptions() {
        return options;
    }

    private void onAttachListener(ReactiveSystem<B> reactiveSystem) {
        if (reactiveSystem instanceof ReactiveSystem.ReactiveSystemListener) {
            reactiveSystem.setReactiveSystemListener((ReactiveSystemListener) this);
        } else {
            reactiveSystem.setReactiveSystemListener((ReactiveSystemListener<B>) DEFAULT_LISTENER);
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


    private static class EmptyReactiveSystemListener<B extends Bigraph<? extends Signature<?>>> implements ReactiveSystem.ReactiveSystemListener<B> {
    }

    private Supplier<String> rSupplier = new Supplier<String>() {
        private int id = 0;

        @Override
        public String get() {
            return "r" + id++;
        }
    };
}
