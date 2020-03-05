package de.tudresden.inf.st.bigraphs.simulation.reactivesystem;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.exceptions.OuterNameIsIdleException;
import de.tudresden.inf.st.bigraphs.core.exceptions.RedexIsNotSimpleException;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.simulation.ReactionRule;
import de.tudresden.inf.st.bigraphs.simulation.ReactiveSystem;
import de.tudresden.inf.st.bigraphs.simulation.matching.BigraphMatch;
import de.tudresden.inf.st.bigraphs.simulation.modelchecking.ReactionGraph;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.predicates.ReactiveSystemPredicates;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

//TODO: add tactics/order/priorities for RR execution (here?): yes here we program our application.
//then we can see how this rule-style interactions with different simulation-styles and can select the best simulation workflow

/**
 * Abstract class of a "nice and simple" bigraphical reactive system (BRS).
 * <p>
 * For all available bigraph types the corresponding reactive system implementation extends this class for
 * the implementation of their own specific nice and simple BRS.
 * <p>
 * The required composition instructions using the deconstructed match result (see {@link BigraphMatch}) to compute the
 * new agent, are  implemented by the subclasses. The methods {@code buildGroundReaction} and {@code buildParametricReaction}
 * are made abstract here to make this clear.
 *
 * @author Dominik Grzelak
 */
public abstract class AbstractSimpleReactiveSystem<B extends Bigraph<? extends Signature<?>>> implements ReactiveSystem<B> {
    private Logger logger = LoggerFactory.getLogger(AbstractSimpleReactiveSystem.class);

    protected B initialAgent;
    protected BiMap<String, ReactionRule<B>> reactionRules = HashBiMap.create();
    protected MutableList<ReactiveSystemPredicates<B>> predicates = Lists.mutable.empty();

    public AbstractSimpleReactiveSystem() {
    }

    @Override
    public abstract B buildGroundReaction(B agent, BigraphMatch<B> match, ReactionRule<B> rule);

    @Override
    public abstract B buildParametricReaction(B agent, BigraphMatch<B> match, ReactionRule<B> rule);

    /**
     * Checks if a parametric redex is <i>simple</i>. A parametric redex is simple if:
     * <ul>
     * <li>open: every link is open</li>
     * <li>guarding: no site has a root as parent</li>
     * <li>inner-injective: no two sites are siblings</li>
     * </ul>
     * (see Milner book Def. 8.12, pp. 95)
     * <p>
     * Note that the {@link AbstractReactionRule} class also checks if the redex is simple.
     *
     * @param reactionRule the reaction rule to be checked (redex is used)
     * @throws RedexIsNotSimpleException if the redex is not simple
     */
    protected void assertParametricRedexIsSimple(ReactionRule<B> reactionRule) throws RedexIsNotSimpleException {
        //"openness": all links are interfaces (edges, and outer names)
        //TODO must not be severe constraint, as we can internally model edges as outer names (in the matching as currently done)
//        boolean isOpen = redex.getInnerNames().size() == 0 && redex.getEdges().size() == 0;

        if (!reactionRule.isRedexSimple()) {
            throw new RedexIsNotSimpleException();
        }
    }

    /**
     * Throws an {@link InvalidReactionRuleException} if an outer name of a given redex is idle (i.e., not connected to a point of the redex).
     *
     * @param reactionRule reaction rule where the outer names of the redex are checked
     * @throws InvalidReactionRuleException if outer name is idle.
     */
    protected void assertNoIdleOuterName(ReactionRule<B> reactionRule) throws InvalidReactionRuleException {
        if (hasIdleOuterNames(reactionRule.getReactum())) {
            throw new OuterNameIsIdleException();
        }
    }

    protected boolean hasIdleOuterNames(B bigraph) {
        boolean isIdle = false;
        for (BigraphEntity.OuterName eachOuter : bigraph.getOuterNames()) {
            // check for idle links (bigraphER doesn't allows it either
            if (bigraph.getPointsFromLink(eachOuter).size() == 0) {
                isIdle = true;
                break;
            }
        }
        return isIdle;
    }

    @Override
    public synchronized Collection<ReactionRule<B>> getReactionRules() {
        return reactionRules.values();
    }

    @Override
    public synchronized BiMap<String, ReactionRule<B>> getReactionRulesMap() {
        return reactionRules;
    }

    @Override
    public synchronized List<ReactiveSystemPredicates<B>> getPredicates() {
        return predicates;
    }

    public synchronized void addPredicate(ReactiveSystemPredicates<B> predicate) {
        if (!predicates.contains(predicate)) {
            predicates.add(predicate);
        } else {
            logger.debug("Predicate {} was not added because it is already contained in the reactive system", predicate);
        }
    }

    @SuppressWarnings("unused")
    public synchronized boolean addReactionRule(ReactionRule<B> reactionRule) throws InvalidReactionRuleException {
        assertParametricRedexIsSimple(reactionRule);
        assertNoIdleOuterName(reactionRule); // redex is captured by "simple" constraint above
        if (!reactionRules.containsValue(reactionRule)) {
            reactionRules.put(rSupplier.get(), reactionRule);
            return true;
        }
        logger.debug("Reaction rule {} was not added because it is already contained in the reactive system", reactionRule);
        return false;
    }

    public synchronized Signature<?> getSignature() {
        assert initialAgent != null;
        return initialAgent.getSignature();
    }

    public synchronized B getAgent() {
        return initialAgent;
    }

    public synchronized AbstractSimpleReactiveSystem<B> setAgent(B initialAgent) {
        this.initialAgent = initialAgent;
        return this;
    }

    private Supplier<String> rSupplier = new Supplier<String>() {
        private int id = 0;

        @Override
        public String get() {
            return "r" + id++;
        }
    };

    /**
     * A bigraphical reactive system (BRS) bounded to a reaction graph.
     * This data object is usually generated as a result of a simulation process for evaluating the results later.
     * <p>
     * This class is thread-safe.
     *
     * @param <B> type of the bigraph
     */
    public static class TransitionSystemBoundSimpleReactiveSystem<B extends Bigraph<? extends Signature<?>>> extends AbstractSimpleReactiveSystem<B> {
        private final AbstractSimpleReactiveSystem<B> reactiveSystem;
        private final ReactionGraph<B> reactionGraph;
        //TODO add predicate-witness bigraph map

        public TransitionSystemBoundSimpleReactiveSystem(ReactionGraph<B> reactionGraph, AbstractSimpleReactiveSystem<B> reactiveSystem) {
            this.reactiveSystem = reactiveSystem;
            this.reactionGraph = reactionGraph;
        }

        public ReactionGraph<B> getReactionGraph() {
            return reactionGraph;
        }

        @Override
        public B buildGroundReaction(B agent, BigraphMatch<B> match, ReactionRule<B> rule) {
            return this.reactiveSystem.buildGroundReaction(agent, match, rule);
        }

        @Override
        public B buildParametricReaction(B agent, BigraphMatch<B> match, ReactionRule<B> rule) {
            return this.reactiveSystem.buildParametricReaction(agent, match, rule);
        }
    }
}
