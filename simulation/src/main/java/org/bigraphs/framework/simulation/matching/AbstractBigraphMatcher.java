package org.bigraphs.framework.simulation.matching;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;
import org.bigraphs.framework.simulation.matching.pure.PureBigraphMatcher;

import java.lang.reflect.InvocationTargetException;

/**
 * This class is responsible for executing bigraph matching. A bigraph matcher consists of a bigraph
 * matching engine. A concrete matcher with the corresponding matching engine w.r.t. to the bigraph type. The correct one,
 * is created using the factory method {@link AbstractBigraphMatcher#create(Class)} by supplying the bigraph type as class.
 * <p>
 * The matcher needs an agent and redex to perform bigraph matching.
 * <p>
 * Matches are then returned via an iterator for easier access of the results The instances of the matches are of type
 * {@link BigraphMatch}.
 * The matching engine/iterator can later also access/override a so-called "custom constraint matching method"
 * (not yet implemented) to additionally specify some user-defined constraints (e.g., match attributes).
 * <p>
 * With other words: This class works like a factory to return the matches as iterables of class {@link BigraphMatch}.
 *
 * @param <B> type of the bigraph
 * @author Dominik Grzelak
 */
public abstract class AbstractBigraphMatcher<B extends Bigraph<? extends Signature<?>>> {
    protected B agent;
    protected B redex;
    protected ReactionRule<B> rule;
//    private Class<B> matcherClassType;

    //    @SuppressWarnings("unchecked")
    protected AbstractBigraphMatcher() {
//        this.matcherClassType = ((Class<B>) ((ParameterizedType) getClass()
//                .getGenericSuperclass()).getActualTypeArguments()[0]);
    }

    @SuppressWarnings("unchecked")
    public static <B extends Bigraph<? extends Signature<?>>> AbstractBigraphMatcher<B> create(Class<B> bigraphClass) {
        if (bigraphClass == PureBigraph.class) {
            try {
                return (AbstractBigraphMatcher<B>) Class.forName(PureBigraphMatcher.class.getCanonicalName()).getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Not Implemented Yet");
    }

    public abstract <M extends BigraphMatch<B>> MatchIterable<M> match(B agent, ReactionRule<B> rule);

    /**
     * Provide the matching engine for the specific bigraph type implemented by the sub class
     *
     * @return concrete bigraph matching engine
     */
    protected abstract BigraphMatchingEngine<B> instantiateEngine();

    /**
     * Returns the supplied agent passed via the {@link AbstractBigraphMatcher#match(Bigraph, ReactionRule)} method.
     *
     * @return the agent for the match
     */
    public B getAgent() {
        return agent;
    }

    /**
     * Returns the supplied redex passed via the {@link AbstractBigraphMatcher#match(Bigraph, ReactionRule)} method.
     *
     * @return the redex for the match
     */
    public B getRedex() {
        return redex;
    }

    //TODO: let user override a matchConstraints(...) method here
    // a minimal LTS can then not be possible under some circumstances when no RPO and IPO exist
}
