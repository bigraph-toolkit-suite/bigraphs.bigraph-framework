package de.tudresden.inf.st.bigraphs.rewriting;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.rewriting.matching.AbstractBigraphMatcher;
import de.tudresden.inf.st.bigraphs.rewriting.matching.BigraphMatch;
import de.tudresden.inf.st.bigraphs.rewriting.matching.MatchIterable;
import de.tudresden.inf.st.bigraphs.rewriting.reactions.ReactionRuleSupplier;

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

//TODO: add tactics/order/priorities for RR execution (here?)

/**
 * @author Dominik Grzelak
 */
public abstract class AbstractReactiveSystem<S extends Signature, B extends Bigraph<S>> implements ReactiveSystem<S, B> {

    protected ReactiveSystemListener reactiveSystemListener;

    protected Set<ReactionRule<B>> reactionRules = new HashSet<>();
    protected Options options;
    protected AbstractBigraphMatcher<B> matcher;

    /**
     * keeps track of the visited agents where no reaction rules can be applied anymore
     */
    protected Map<B, Boolean> visitedAgents = new ConcurrentHashMap<>();

    public AbstractReactiveSystem() {
        onAttachListener(this);
        matcher = AbstractBigraphMatcher.create(getGenericTypeClass(1));
    }

    @Override
    public Collection<ReactionRule<B>> getReactionRules() {
        return reactionRules;
    }

    public boolean addReactionRule(ReactionRule<B> reactionRule) {
        return reactionRules.add(reactionRule);
    }

    //TODO: tasks for parallel jobs
    // https://stackify.com/java-thread-pools/
    // https://www.baeldung.com/java-executor-service-tutorial
    // https://github.com/pivovarit/parallel-collectors
    // https://www.baeldung.com/java-8-parallel-streams-custom-threadpool

    public void simulate(final B agent, final Options options) {
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
                    try {
                        MatchIterable match = matcher.match(agent, (B) eachRule.getRedex());
                        Iterator<BigraphMatch<?>> iterator = match.iterator();
                        while (iterator.hasNext()) {
                            BigraphMatch<?> next = iterator.next();
                            System.out.println("NEXT: " + next);
                            //create new rewriting and insert into map: <a,l,a'>
                            if (next.getParameters().size() == 0) {
                                buildGroundReaction(agent, next, eachRule);
                            } else {
                                buildParametricReaction(agent, next, eachRule);
                            }
                        }
                    } catch (IncompatibleSignatureException e) {
                        e.printStackTrace();
                    }
                });
        currentTransitionCount.incrementAndGet();


        //end
        reactiveSystemListener.onReactiveSystemFinished();
    }

    protected abstract void buildGroundReaction(final B agent, final BigraphMatch<?> match, ReactionRule<B> rule);

    protected abstract void buildParametricReaction(final B agent, final BigraphMatch<?> match, ReactionRule<B> rule);

    @Override
    public void setReactiveSystemListener(ReactiveSystemListener reactiveSystemListener) {
        this.reactiveSystemListener = reactiveSystemListener;
    }

    public Options getOptions() {
        return options;
    }

    private void onAttachListener(ReactiveSystem reactiveSystem) {
        if (reactiveSystem instanceof ReactiveSystem.ReactiveSystemListener) {
            reactiveSystem.setReactiveSystemListener((ReactiveSystemListener) this);
        } else {
            reactiveSystem.setReactiveSystemListener(DEFAULT_LISTENER);
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

    private final static ReactiveSystem.ReactiveSystemListener DEFAULT_LISTENER = new EmptyReactiveSystemListener();

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
}
