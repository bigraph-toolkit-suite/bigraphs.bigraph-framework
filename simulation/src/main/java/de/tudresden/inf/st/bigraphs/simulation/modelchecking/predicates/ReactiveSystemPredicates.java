package de.tudresden.inf.st.bigraphs.simulation.modelchecking.predicates;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;

import java.util.function.Predicate;

/**
 * Predicate interface for the reaction graph.
 * <p>
 * Allows to test if some conditions hold while executing a BRS.
 * The predicates itself are bigraphs.
 * <p>
 * Internally a bigraph matcher instance for the concrete bigraph kind must be created.
 *
 * @author Dominik Grzelak
 */
public abstract class ReactiveSystemPredicates<B extends Bigraph<? extends Signature<?>>> implements Predicate<B> {

    protected boolean negate = false;

    /**
     * Get the bigraph used in the predicate.
     * If the predicate is a conditional predicate (e.g., {@link AndPredicate}), the methid returns {@code null}.
     *
     * @return the underlying bigraph of the predicate, or {@code null} for conditional predicates.
     */
    public abstract B getBigraph();

    /**
     * This method is responsible to test the current state of a transition system of a BRS
     * with the predicate at hand (which is provided by the concrete subclass).
     *
     * @param agent the current state of a transition system of a BRS to test the predicate against
     * @return {@code true} if the predicate matches, otherwise {@code false}
     */
    @Override
    public abstract boolean test(B agent);

    public boolean isNegate() {
        return negate;
    }

    public void setNegate(boolean negate) {
        this.negate = negate;
    }
}
