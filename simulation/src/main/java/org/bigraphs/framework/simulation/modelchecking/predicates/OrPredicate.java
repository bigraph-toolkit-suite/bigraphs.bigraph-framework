package org.bigraphs.framework.simulation.modelchecking.predicates;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.reactivesystem.ReactiveSystemPredicate;
import org.bigraphs.framework.core.Signature;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A composite predicate which concatenates all given predicates with the <i>or</i> operator.
 *
 * @author Dominik Grzelak
 */
public class OrPredicate<B extends Bigraph<? extends Signature<?>>> extends ReactiveSystemPredicate<B> {

    private final List<ReactiveSystemPredicate<B>> predicates = new LinkedList<>();

    public OrPredicate(ReactiveSystemPredicate<B> predicateA, ReactiveSystemPredicate<B> predicateB) {
        this(predicateA, predicateB, false);
    }

    public OrPredicate(ReactiveSystemPredicate<B> predicateA, ReactiveSystemPredicate<B> predicateB, boolean negate) {
        predicates.add(predicateA);
        predicates.add(predicateB);
        super.negate = negate;
    }

    public OrPredicate(boolean negate, ReactiveSystemPredicate<B>... predicates) {
        super.negate = negate;
        this.predicates.addAll(Arrays.asList(predicates));
    }

    public OrPredicate(ReactiveSystemPredicate<B>... predicates) {
        this(false, predicates);
    }

    @Override
    public B getBigraph() {
        return null;
    }

    /**
     * Concatenates all given predicates with the <i>or</i> operator of the {@link ReactiveSystemPredicate} class and
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
        Optional<ReactiveSystemPredicate<B>> reduce = predicates.stream().reduce((bReactiveSystemPredicates, bReactiveSystemPredicates2) -> {
            Predicate<B> bPredicate = bReactiveSystemPredicates.isNegate() ? bReactiveSystemPredicates.negate() : bReactiveSystemPredicates;
            Predicate<B> bPredicate2 = bReactiveSystemPredicates2.isNegate() ? bReactiveSystemPredicates2.negate() : bReactiveSystemPredicates2;
            return (ReactiveSystemPredicate<B>) bPredicate.or(bPredicate2);
        });
        return reduce.map(bReactiveSystemPredicates -> bReactiveSystemPredicates.test(agent)).orElse(true);
    }
}
