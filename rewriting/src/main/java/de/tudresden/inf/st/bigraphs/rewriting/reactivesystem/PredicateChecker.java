package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.predicates.ReactiveSystemPredicates;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The task of the class is to evaluate a given set of predicates of class {@link ReactiveSystemPredicates}.
 * <p>
 * After evaluation, a map can be acquired to see which predicates evaluated to {@code true} or {@code false}.
 * <p>
 * This class is thread-safe.
 *
 * @author Dominik Grzelak
 */
public class PredicateChecker<B extends Bigraph<? extends Signature<?>>> {

    private final List<ReactiveSystemPredicates<B>> predicates;
    /**
     * Storage for the results of each predicate check
     */
    private final Map<ReactiveSystemPredicates<B>, Boolean> checked = new ConcurrentHashMap<>();

    public PredicateChecker(Collection<ReactiveSystemPredicates<B>> predicates) {
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
        for (ReactiveSystemPredicates<B> each : predicates) {
            if (each.isNegate()) {
                checked.put(each, each.negate().test(agent));
            } else {
                checked.put(each, each.test(agent));
            }
        }
        return checked.values().stream().allMatch(x -> x);
    }

    /**
     * Get detailed information of the predicate evaluation after the method {@link PredicateChecker#checkAll(Bigraph)}
     * was called.
     *
     * @return a result map of the predicate evaluation
     */
    public synchronized Map<ReactiveSystemPredicates<B>, Boolean> getChecked() {
        return checked;
    }

    /**
     * Get the set of predicates with which the class was instantiated.
     *
     * @return set of predicates
     */
    public synchronized List<ReactiveSystemPredicates<B>> getPredicates() {
        return predicates;
    }
}
