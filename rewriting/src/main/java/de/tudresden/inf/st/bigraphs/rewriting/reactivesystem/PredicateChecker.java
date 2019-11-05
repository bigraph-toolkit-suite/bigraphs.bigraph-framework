package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.predicates.ReactiveSystemPredicates;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Dominik Grzelak
 */
public class PredicateChecker<B extends Bigraph<? extends Signature<?>>> {

    private List<ReactiveSystemPredicates<B>> predicates = new ArrayList<>();
    /**
     * Storage for the results of each predicate check
     */
    private Map<ReactiveSystemPredicates<B>, Boolean> checked = new ConcurrentHashMap<>();

    public PredicateChecker(Collection<ReactiveSystemPredicates<B>> predicates) {
        this.predicates = new ArrayList<>(predicates);
    }


    public synchronized boolean checkAll(B agent) {
        checked.clear();
        for (ReactiveSystemPredicates<B> each : predicates) {
            if (each.isNegate()) {
                checked.put(each, each.test(agent));
            } else {
                checked.put(each, each.negate().test(agent));
            }
        }
        return checked.values().stream().anyMatch(x -> x);
    }

    public synchronized Map<ReactiveSystemPredicates<B>, Boolean> getChecked() {
        return checked;
    }

    public synchronized List<ReactiveSystemPredicates<B>> getPredicates() {
        return predicates;
    }
}
