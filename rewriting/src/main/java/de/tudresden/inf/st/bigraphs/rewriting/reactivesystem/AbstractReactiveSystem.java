package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.rewriting.ReactionRule;
import de.tudresden.inf.st.bigraphs.rewriting.ReactiveSystem;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.predicates.ReactiveSystemPredicates;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

//TODO: add tactics/order/priorities for RR execution (here?): yes here we program our application.
//then we can see how this rule-style interactions with different simulation-styles and can select the best simulation workflow

/**
 * Abstract class of a bigraphical reactive system (BRS).
 *
 * @author Dominik Grzelak
 */
public abstract class AbstractReactiveSystem<B extends Bigraph<? extends Signature<?>>> implements ReactiveSystem<B> {
    private Logger logger = LoggerFactory.getLogger(AbstractReactiveSystem.class);

    protected B initialAgent;
    protected BiMap<String, ReactionRule<B>> reactionRules = HashBiMap.create();
    protected MutableList<ReactiveSystemPredicates<B>> predicates = Lists.mutable.empty();

    public AbstractReactiveSystem() {
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
    public synchronized boolean addReactionRule(ReactionRule<B> reactionRule) {
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

    public synchronized AbstractReactiveSystem<B> setAgent(B initialAgent) {
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
    public static class TransitionSystemBoundReactiveSystem<B extends Bigraph<? extends Signature<?>>> extends AbstractReactiveSystem<B> {
        private final AbstractReactiveSystem<B> reactiveSystem;
        private final ReactionGraph<B> reactionGraph;
        //TODO add predicate-witness bigraph map

        public TransitionSystemBoundReactiveSystem(ReactionGraph<B> reactionGraph, AbstractReactiveSystem<B> reactiveSystem) {
            this.reactiveSystem = reactiveSystem;
            this.reactionGraph = reactionGraph;
        }

        public ReactionGraph<B> getReactionGraph() {
            return reactionGraph;
        }
    }
}
