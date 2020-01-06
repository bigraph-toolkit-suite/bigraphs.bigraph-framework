package de.tudresden.inf.st.bigraphs.simulation.reactivesystem.predicates;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;

import java.util.*;
import java.util.function.Predicate;

/**
 * A composite predicate which concatenates all given predicates with the <i>and</i> operator.
 *
 * @author Dominik Grzelak
 */
public class AndPredicate<B extends Bigraph<? extends Signature<?>>> extends ReactiveSystemPredicates<B> {

    private final List<ReactiveSystemPredicates<B>> predicates = new LinkedList<>();

    public AndPredicate(ReactiveSystemPredicates<B> predicateA, ReactiveSystemPredicates<B> predicateB) {
        this(predicateA, predicateB, false);
    }

    public AndPredicate(ReactiveSystemPredicates<B> predicateA, ReactiveSystemPredicates<B> predicateB, boolean negate) {
        predicates.add(predicateA);
        predicates.add(predicateB);
        super.negate = negate;
    }

    public AndPredicate(boolean negate, ReactiveSystemPredicates<B>... predicates) {
        super.negate = negate;
        this.predicates.addAll(Arrays.asList(predicates));
    }

    public AndPredicate(ReactiveSystemPredicates<B>... predicates) {
        this(false, predicates);
    }

    @Override
    public B getBigraph() {
        return null;
    }

    /**
     * Concatenates all given predicates with the <i>and</i> operator of the {@link ReactiveSystemPredicates} class and
     * evaluates them together.
     * <p>
     * Note: if the predicate set is empty, {@code true} will be returned also.
     *
     * @param agent the current state of a transition system of a BRS to test the predicate against
     * @return {@code true}, if the consolidated predicates evaluate to {@code true} (or predicate list is empty),
     * otherwise {@code false}.
     */
    @Override
    public boolean test(final B agent) {
        Optional<ReactiveSystemPredicates<B>> reduce = predicates.stream().reduce((bReactiveSystemPredicates, bReactiveSystemPredicates2) -> {
            Predicate<B> bPredicate = bReactiveSystemPredicates.isNegate() ? bReactiveSystemPredicates.negate() : bReactiveSystemPredicates;
            Predicate<B> bPredicate2 = bReactiveSystemPredicates2.isNegate() ? bReactiveSystemPredicates2.negate() : bReactiveSystemPredicates2;
            return (ReactiveSystemPredicates<B>) bPredicate.and(bPredicate2);
        });
        return reduce.map(bReactiveSystemPredicates -> bReactiveSystemPredicates.test(agent)).orElse(true);
    }
}
