/*
 * Copyright (c) 2019-2024 Bigraph Toolkit Suite Developers
 * Main Developer: Dominik Grzelak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bigraphs.framework.core.reactivesystem;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;

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
