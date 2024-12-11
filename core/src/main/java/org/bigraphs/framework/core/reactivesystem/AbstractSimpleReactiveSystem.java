package org.bigraphs.framework.core.reactivesystem;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bigraphs.framework.core.*;
import org.bigraphs.framework.core.exceptions.*;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.AbstractEcoreSignature;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

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
    protected BiMap<String, ReactiveSystemPredicate<B>> predicateMap = HashBiMap.create();

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
    public void assertParametricRedexIsSimple(ReactionRule<B> reactionRule) throws RedexIsNotSimpleException {
        //"openness": all links are interfaces (edges, and outer names)
        //TODO must not be severe constraint, as we can internally model edges as outer names (in the matching as currently done)
//        boolean isOpen = redex.getInnerNames().size() == 0 && redex.getEdges().size() == 0;

        if (!reactionRule.isRedexSimple()) {
            throw new RedexIsNotSimpleException();
        }
    }

    public void assertAgentIsGround(B agent) throws AgentNotGroundException {
        if (!agent.isGround()) {
            throw new AgentNotGroundException();
        }
    }

    public void assertAgentIsPrime(B agent) throws AgentNotPrimeException {
        if (!agent.isPrime()) {
            throw new AgentNotPrimeException();
        }
    }

    /**
     * Throws an {@link InvalidReactionRuleException} if an outer name of a given redex is idle (i.e., not connected to a point of the redex).
     *
     * @param reactionRule reaction rule where the outer names of the redex are checked
     * @throws InvalidReactionRuleException if outer name is idle.
     */
    public void assertNoIdleOuterName(ReactionRule<B> reactionRule) throws InvalidReactionRuleException {
        if (hasIdleOuterNames(reactionRule.getRedex())) {
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

    protected void copyAttributes(B sourceBigraph, B targetBigraph) {
        MutableMap<String, Map<String, Object>> S = Maps.mutable.empty();
        // Iterate through all nodes and store in a map S: nodeId |-> Attributes
        sourceBigraph.getNodes().forEach(n -> {
            if (!n.getAttributes().isEmpty()) {
                S.put(n.getName(), n.getAttributes());
            }
        });
        // Iterate through keys in map S, for each id get the node in target and set attributes
        S.keySet().forEach(nodeId -> {
            Optional<BigraphEntity.NodeEntity<Control<?, ?>>> first = targetBigraph.getNodes().stream().filter(n -> n.getName().equals(nodeId)).findFirst();
            first.ifPresent(controlNodeEntity -> controlNodeEntity.setAttributes(S.get(nodeId)));
        });
    }

    @Override
    public synchronized Set<ReactionRule<B>> getReactionRules() {
        return reactionRules.values();
    }

    @Override
    public synchronized BiMap<String, ReactionRule<B>> getReactionRulesMap() {
        return reactionRules;
    }

    @Override
    public synchronized BiMap<String, ReactiveSystemPredicate<B>> getPredicateMap() {
        return predicateMap;
    }

    @Override
    public synchronized Set<ReactiveSystemPredicate<B>> getPredicates() {
        return predicateMap.values();
    }

    public synchronized void addPredicate(ReactiveSystemPredicate<B> predicate) {
        if (!predicateMap.containsValue(predicate)) {
            if (predicate.isDefined()) {
                String lbl = ((HasLabel) predicate).getLabel();
                lbl = predicateMap.containsKey(lbl) ? (lbl + rSupplier.get()) : lbl;
                predicateMap.put(lbl, predicate);
            } else {
                predicateMap.put(predSupplier.get(), predicate);
            }
        } else {
            logger.debug("Predicate {} was not added because it is already contained in the reactive system", predicate);
        }
    }

    @SuppressWarnings("unused")
    public synchronized boolean addReactionRule(ReactionRule<B> reactionRule) throws InvalidReactionRuleException {
        assertParametricRedexIsSimple(reactionRule);
        // assertNoIdleOuterName(reactionRule); // redex is captured by "simple" constraint above
        if (!reactionRules.containsValue(reactionRule)) {
            if (reactionRule.isDefined()) {
                String lbl = reactionRule.getLabel();
                lbl = reactionRules.containsKey(lbl) ? (lbl + rSupplier.get()) : lbl;
                reactionRules.put(lbl, reactionRule);
            } else {
                reactionRules.put(rSupplier.get(), reactionRule);
            }
//            reactionRule.isReversible()//TODO -> construct new one and set reversible false and recall method
            return true;
        }
        logger.debug("Reaction rule {} was not added because it is already contained in the reactive system", reactionRule);
        return false;
    }

    public synchronized AbstractEcoreSignature<?> getSignature() {
        assert initialAgent != null;
        assert initialAgent.getSignature() instanceof AbstractEcoreSignature;
        return (AbstractEcoreSignature<?>) initialAgent.getSignature();
    }

    public synchronized B getAgent() {
        return initialAgent;
    }

    public synchronized AbstractSimpleReactiveSystem<B> setAgent(B initialAgent) {
//        assertAgentIsGround(initialAgent);
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

    private Supplier<String> predSupplier = new Supplier<String>() {
        private int id = 0;

        @Override
        public String get() {
            return "p" + id++;
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
