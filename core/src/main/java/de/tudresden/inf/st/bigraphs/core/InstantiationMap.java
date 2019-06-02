package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An instantiation map for a parametric reaction rule.
 */
public class InstantiationMap {

    private Map<FiniteOrdinal<Integer>, FiniteOrdinal<Integer>> mappings = new ConcurrentHashMap<>();

    private InstantiationMap(Map<FiniteOrdinal<Integer>, FiniteOrdinal<Integer>> mappings) {
        this.mappings = mappings;
    }

    /**
     * Creates an identity-like (thus, linear) instantiation map for "n"-sites-to-roots mappings.
     * For example, 0->0, 1->1, 2->2, etc.
     *
     * @param n number of indices of the map
     * @return a linear instantiation map (i.e., identity map)
     */
    public static InstantiationMap create(int n) {
        if (n <= 0) return new InstantiationMap(Collections.emptyMap());
        Map<FiniteOrdinal<Integer>, FiniteOrdinal<Integer>> mappings0 = new ConcurrentHashMap<>();
        for (int i = 0; i < n; i++) {
            mappings0.put(FiniteOrdinal.ofInteger(i), FiniteOrdinal.ofInteger(i));
        }
        return new InstantiationMap(mappings0);
    }

    public void map(int from, int to) {
        mappings.put(FiniteOrdinal.ofInteger(from), FiniteOrdinal.ofInteger(to));
    }

    public FiniteOrdinal<Integer> get(int from) {
        return mappings.get(FiniteOrdinal.ofInteger(from));
    }
}
