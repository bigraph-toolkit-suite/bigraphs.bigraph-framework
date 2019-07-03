package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.rewriting.BigraphCanonicalForm;
import de.tudresden.inf.st.bigraphs.rewriting.ReactiveSystemOptions;
import de.tudresden.inf.st.bigraphs.rewriting.ReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.ReactiveSystem;
import de.tudresden.inf.st.bigraphs.rewriting.matching.AbstractBigraphMatcher;
import de.tudresden.inf.st.bigraphs.rewriting.matching.BigraphMatch;
import de.tudresden.inf.st.bigraphs.rewriting.matching.MatchIterable;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.reactions.ReactionRuleSupplier;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;

//TODO: add tactics/order/priorities for RR execution (here?)

/**
 * Algorithm for reaction graph is from (Perrone, 2013)
 *
 * @author Dominik Grzelak
 */
public abstract class AbstractReactiveSystem<B extends Bigraph<? extends Signature<?>>> implements ReactiveSystem<B> {
    private final static ReactiveSystem.ReactiveSystemListener DEFAULT_LISTENER = new EmptyReactiveSystemListener();

    protected ReactiveSystemListener reactiveSystemListener;

    protected BiMap<String, ReactionRule<B>> reactionRules = HashBiMap.create();
    protected ReactiveSystemOptions options;
    protected AbstractBigraphMatcher<B> matcher;
    protected ReactionGraph<B> reactionGraph;
    protected BigraphCanonicalForm canonicalForm = BigraphCanonicalForm.getInstance();

    public AbstractReactiveSystem() {
        onAttachListener(this);
        matcher = AbstractBigraphMatcher.create(getGenericTypeClass(0));
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
        return true;
    }


    /**
     * Compute the transition system of a bigraph with all added reaction rules so far.
     *
     * @param agent   the initial agent
     * @param options additional options
     */
    //TODO add predicates with match functions as in bigmc
    public synchronized void computeTransitionSystem(final B agent, final ReactiveSystemOptions options) {
        final Queue<B> workingQueue = new ConcurrentLinkedDeque<>();
        workingQueue.add(agent);
        int transitionCnt = 0;
        while (!workingQueue.isEmpty() && transitionCnt < options.getMaximumTransitions()) {
            // Remove the first element w of the work queue Q.
            final B theAgent = workingQueue.remove();
            // For each reaction rule, find all matches m1 ...mn in w
            //TODO: generate appropriate supplier for the given option
            Stream.generate(ReactionRuleSupplier.createInOrder(getReactionRules()))
                    .limit(getReactionRules().size())
                    .peek(x -> reactiveSystemListener.onCheckingReactionRule((ReactionRule<B>) x))
                    .forEachOrdered(eachRule -> {
                        //                            List<B> currentMatches = new LinkedList<>();
                        String bfcfOfW = canonicalForm.bfcf(theAgent);
                        MatchIterable match = matcher.match(theAgent, (B) eachRule.getRedex());
                        Iterator<BigraphMatch<?>> iterator = match.iterator();
                        while (iterator.hasNext()) {
                            BigraphMatch<B> next = (BigraphMatch<B>) iterator.next();
//                            System.out.println("NEXT: " + next);
                            B reaction = null;
                            if (next.getParameters().size() == 0) {
                                reaction = buildGroundReaction(theAgent, next, (ReactionRule<B>) eachRule);
                            } else {
                                reaction = buildParametricReaction(theAgent, next, (ReactionRule<B>) eachRule);
                            }
                            if (Objects.nonNull(reaction)) {
                                String bfcf = canonicalForm.bfcf(reaction);
                                String reactionLbl = reactionRules.inverse().get(eachRule);
                                if (!reactionGraph.containsBigraph(bfcf)) {
                                    reactionGraph.addEdge(theAgent, bfcfOfW, reaction, bfcf, (B) next.getRedex(), reactionLbl);
                                    workingQueue.add(reaction);
                                } else {
                                    reactionGraph.addEdge(theAgent, bfcfOfW, reaction, bfcf, (B) next.getRedex(), reactionLbl);
                                }
                            }
                        }
                    });
            //TODO  Check each property p ∈ P against w.

            //TODO  Repeat the procedure for the next item in the work queue, terminating
            //successfully if the work queue is empty.
            transitionCnt++;
        }

        exportGraph(reactionGraph.getGraph(), "bla");
    }

    public void exportGraph(Graph g, String filename) {
        JGraphXAdapter<String, DefaultEdge> graphAdapter = new JGraphXAdapter<String, DefaultEdge>(g);
//        mxIGraphLayout layout = new mxCircleLayout(graphAdapter);
//        mxIGraphLayout layout = new mxCompactTreeLayout(graphAdapter);
//        mxIGraphLayout layout = new mxOrthogonalLayout(graphAdapter);
        mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter, SwingConstants.NORTH);
        layout.execute(graphAdapter.getDefaultParent());

        BufferedImage image =
                mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
        try {
            Path currentRelativePath = Paths.get("");
            Path completePath = Paths.get(currentRelativePath.toAbsolutePath().toString(), filename + ".png");
            File imgFile = new File(completePath.toUri());
            if (!imgFile.exists()) {
                imgFile.createNewFile();
            }
            ImageIO.write(image, "PNG", imgFile);
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

    public synchronized void simulate(final B agent, final ReactiveSystemOptions options) {
        AtomicInteger maximumTransitions = new AtomicInteger(options.getMaximumTransitions());
        AtomicInteger currentTransitionCount = new AtomicInteger(0);

        reactiveSystemListener.onReactiveSystemStarted();
        // while...
        // call appropriate listener methods

        //for each "available" agents not in visitedAgents
        // add here the "tactic" supplier: now is in-order
        Stream.generate(ReactionRuleSupplier.createInOrder(getReactionRules()))
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

    protected abstract B buildParametricReaction(final B agent, final BigraphMatch<?> match, ReactionRule<B> rule);

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
    private Class<B> getGenericTypeClass(int indexOfArgument) {
        try {
            String className = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[indexOfArgument].getTypeName();
            Class<?> clazz = Class.forName(className);
            return (Class<B>) clazz;
        } catch (Exception e) {
            throw new IllegalStateException("Class is not parametrized with a generic type!");
        }
    }


    public static class EmptyReactiveSystemListener implements ReactiveSystem.ReactiveSystemListener {

        @Override
        public void onReactiveSystemStarted() {

        }

        @Override
        public void onCheckingReactionRule(ReactionRule reactionRule) {

        }

        @Override
        public void onReactiveSystemFinished() {

        }

        @Override
        public void onUpdateReactionRuleApplies() {

        }
    }

    private Supplier<String> rSupplier = new Supplier<String>() {
        private int id = 0;

        @Override
        public String get() {
            return "r" + id++;
        }
    };
}
