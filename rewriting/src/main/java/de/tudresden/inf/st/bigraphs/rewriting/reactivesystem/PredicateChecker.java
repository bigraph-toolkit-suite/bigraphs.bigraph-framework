package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Dominik Grzelak
 */
public class PredicateChecker<B extends Bigraph<? extends Signature<?>>> {

    private List<TransitionPredicates<B>> predicates = new ArrayList<>();
    /**
     * Storage for the results of each predicate check
     */
    private Map<TransitionPredicates<B>, Boolean> checked = new ConcurrentHashMap<>();

    public PredicateChecker(Collection<TransitionPredicates<B>> predicates) {
        this.predicates = new ArrayList<>(predicates);
    }


    public synchronized boolean checkAll(B agent) {
        checked.clear();
        for (TransitionPredicates<B> each : predicates) {
            checked.put(each, each.test(agent));
        }
        return checked.values().stream().anyMatch(x -> x);
    }

    public synchronized Map<TransitionPredicates<B>, Boolean> getChecked() {
        return checked;
    }

    public synchronized List<TransitionPredicates<B>> getPredicates() {
        return predicates;
    }
}
