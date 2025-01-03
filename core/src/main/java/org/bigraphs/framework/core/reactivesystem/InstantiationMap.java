package org.bigraphs.framework.core.reactivesystem;

import org.bigraphs.framework.core.datatypes.FiniteOrdinal;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An instantiation map for a parametric reaction rule.
 *
 * @author Dominik Grzelak
 */
public class InstantiationMap {

    private Map<FiniteOrdinal<Integer>, FiniteOrdinal<Integer>> mappings;

    private InstantiationMap(Map<FiniteOrdinal<Integer>, FiniteOrdinal<Integer>> mappings) {
        this.mappings = mappings;
    }

    /**
     * Creates an empty instantiation map. The method calls the {@link InstantiationMap#create(int)} method
     * with <i>0</i> as argument. The user must supply values by using {@link InstantiationMap#map(int, int)} afterward.
     *
     * @return an empty instantiation map
     * @see InstantiationMap#create(int)
     */
    public static InstantiationMap create() {
        return InstantiationMap.create(0);
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

    public InstantiationMap map(int from, int to) {
        mappings.put(FiniteOrdinal.ofInteger(from), FiniteOrdinal.ofInteger(to));
        return this;
    }

    public FiniteOrdinal<Integer> get(int from) {
        return mappings.get(FiniteOrdinal.ofInteger(from));
    }

    public int domainSize() {
        return this.mappings.keySet().size();
    }

    public int coDomainSize() {
        return this.mappings.values().size();
    }

    public Map<FiniteOrdinal<Integer>, FiniteOrdinal<Integer>> getMappings() {
        return mappings;
    }

    public boolean isIdentity() {
        if (domainSize() == coDomainSize()) {
            return this.mappings.keySet().stream().allMatch(x -> Objects.equals(x.getValue(), this.mappings.get(x).getValue()));
        }
        return false;
    }
}
