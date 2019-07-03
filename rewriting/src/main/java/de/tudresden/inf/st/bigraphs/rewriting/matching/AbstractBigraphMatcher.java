package de.tudresden.inf.st.bigraphs.rewriting.matching;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;
import de.tudresden.inf.st.bigraphs.rewriting.matching.pure.PureBigraphMatcher;

import java.lang.reflect.ParameterizedType;

/**
 * This class is responsible for executing bigraph matching. A bigraph matcher consists of a bigraph
 * matching engine. A concrete matcher with the corresponding matching engine w.r.t. to the bigraph type. The correct one,
 * is created using the factory method {@link AbstractBigraphMatcher#create(Class)} by supplying the bigraph type as class.
 * <p>
 * The matcher needs an agent and redex to perform bigraph matching.
 * <p>
 * Matches are then returned via an iterator for easier access of the results The instances of the matches are of type
 * {@link BigraphMatch}.
 * For the iterator, the matching engine can get later also access a "custom constraints matching method"
 * (not yet implemented)
 * <p>
 * With other words: This class works like a factory to return the matches as iterables of class {@link BigraphMatch}.
 *
 * @param <B> type of the bigraph
 * @author Dominik Grzelak
 */
public abstract class AbstractBigraphMatcher<B extends Bigraph<?>> {
    protected B agent;
    protected B redex;
    private Class<B> matcherClassType;

    @SuppressWarnings("unchecked")
    protected AbstractBigraphMatcher() {
        this.matcherClassType = ((Class<B>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0]);
    }

    @SuppressWarnings("unchecked")
    public static <B extends Bigraph<?>> AbstractBigraphMatcher<B> create(Class<B> bigraphClass) {
        if (bigraphClass == PureBigraph.class) {
            try {
//            return (AbstractBigraphMatcher<B>) new PureBigraphMatcher();
                return (AbstractBigraphMatcher<B>) Class.forName(PureBigraphMatcher.class.getCanonicalName()).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Not Implemented Yet");
    }

    public abstract MatchIterable match(B agent, B redex);

    /**
     * Returns the supplied agent passed via the {@link AbstractBigraphMatcher#match(Bigraph, Bigraph)} method.
     *
     * @return the agent for the match
     */
    public B getAgent() {
        return agent;
    }

    /**
     * Returns the supplied redex passed via the {@link AbstractBigraphMatcher#match(Bigraph, Bigraph)} method.
     *
     * @return the redex for the match
     */
    public B getRedex() {
        return redex;
    }

    //TODO: let user override a matchConstraints(...) method here
}
