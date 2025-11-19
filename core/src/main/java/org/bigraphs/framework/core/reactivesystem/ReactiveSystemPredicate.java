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

import java.util.function.Predicate;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;

/**
 * Predicate interface for the reaction graph.
 * <p>
 * Allows to test if some conditions hold while executing a BRS.
 * The predicates itself are bigraphs.
 * <p>
 * Internally a bigraph matcher instance for the concrete bigraph kind must be created.
 *
 * @author Dominik Grzelak
 */
public abstract class ReactiveSystemPredicate<B extends Bigraph<? extends Signature<?>>> implements Predicate<B>, HasLabel {

    protected boolean negate = false;

    protected String label;

    /**
     * Get the bigraph used in the predicate.
     * If the predicate is a conditional predicate, the methid returns {@code null}.
     *
     * @return the underlying bigraph of the predicate, or {@code null} for conditional predicates.
     */
    public abstract B getBigraph();

    /**
     * This method is responsible to test the current state of a transition system of a BRS
     * with the predicate at hand (which is provided by the concrete subclass).
     *
     * @param agent the current state of a transition system of a BRS to test the predicate against
     * @return {@code true} if the predicate matches, otherwise {@code false}
     */
    @Override
    public abstract boolean test(B agent);

    public boolean isNegate() {
        return negate;
    }

    public void setNegate(boolean negate) {
        this.negate = negate;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public <T extends ReactiveSystemPredicate<B>> T withLabel(String label) {
        this.label = label;
        return (T) this;
    }
}
