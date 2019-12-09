package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.predicates;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;

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
public class OrPredicate<B extends Bigraph<? extends Signature<?>>> extends ReactiveSystemPredicates<B> {

    private final List<ReactiveSystemPredicates<B>> predicates = new LinkedList<>();

    public OrPredicate(ReactiveSystemPredicates<B> predicateA, ReactiveSystemPredicates<B> predicateB) {
        this(predicateA, predicateB, false);
    }

    public OrPredicate(ReactiveSystemPredicates<B> predicateA, ReactiveSystemPredicates<B> predicateB, boolean negate) {
        predicates.add(predicateA);
        predicates.add(predicateB);
        super.negate = negate;
    }

    public OrPredicate(boolean negate, ReactiveSystemPredicates<B>... predicates) {
        super.negate = negate;
        this.predicates.addAll(Arrays.asList(predicates));
    }

    public OrPredicate(ReactiveSystemPredicates<B>... predicates) {
        this(false, predicates);
    }

    /**
     * Concatenates all given predicates with the <i>or</i> operator of the {@link ReactiveSystemPredicates} class and
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
            return (ReactiveSystemPredicates<B>) bPredicate.or(bPredicate2);
        });
        return reduce.map(bReactiveSystemPredicates -> bReactiveSystemPredicates.test(agent)).orElse(true);
    }
}
