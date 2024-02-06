package org.bigraphs.framework.simulation.modelchecking.predicates;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.reactivesystem.ReactiveSystemPredicate;
import org.bigraphs.framework.core.Signature;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The task of the class is to evaluate a given set of predicates of class {@link ReactiveSystemPredicate}.
 * <p>
 * After evaluation, a map can be acquired to see which predicates evaluated to {@code true} or {@code false}.
 * <p>
 * This class is thread-safe.
 *
 * @author Dominik Grzelak
 */
public class PredicateChecker<B extends Bigraph<? extends Signature<?>>> {

    private final List<ReactiveSystemPredicate<B>> predicates;
    /**
     * Storage for the results of each predicate check
     */
    private final Map<ReactiveSystemPredicate<B>, Boolean> checked = new ConcurrentHashMap<>();

    public PredicateChecker(Collection<ReactiveSystemPredicate<B>> predicates) {
        this.predicates = new ArrayList<>(predicates);
    }


    /**
     * Checks all predicates passed via the constructor before.
     * Detailed evaluation results of each checked predicate can be acquired by the {@link PredicateChecker#getChecked()}
     * method afterwards.
     *
     * @param agent the bigraph agent to use for the predicate checking
     * @return {@code true}, if all predicates evaluated to {@code true}, otherwise {@code false}
     */
    public synchronized boolean checkAll(B agent) {
        checked.clear();
        for (ReactiveSystemPredicate<B> each : predicates) {
            if (each.isNegate()) {
                checked.put(each, each.negate().test(agent));
            } else {
                checked.put(each, each.test(agent));
            }
        }
        return checkIfAllChecksAreTrue(checked); //checked.values().stream().allMatch(x -> x);
    }

    private boolean checkIfAllChecksAreTrue(Map<ReactiveSystemPredicate<B>, Boolean> checks) {
        for (boolean b : checks.values()) {
            if (!b) return false;
        }
        return true;
    }

    /**
     * Get detailed information of the predicate evaluation after the method {@link PredicateChecker#checkAll(Bigraph)}
     * was called.
     *
     * @return a result map of the predicate evaluation
     */
    public synchronized Map<ReactiveSystemPredicate<B>, Boolean> getChecked() {
        return checked;
    }

    /**
     * Get the set of predicates with which the class was instantiated.
     *
     * @return set of predicates
     */
    public synchronized List<ReactiveSystemPredicate<B>> getPredicates() {
        return predicates;
    }
}
