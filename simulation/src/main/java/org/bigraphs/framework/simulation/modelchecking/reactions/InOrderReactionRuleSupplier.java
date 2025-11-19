/*
 * Copyright (c) 2019-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.simulation.modelchecking.reactions;

import java.util.Collection;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;

/**
 * Specific implementation of the {@link AbstractReactionRuleSupplier}.
 * <p>
 * A given set of reaction rule is returned in the order as they are provided.
 *
 * @author Dominik Grzelak
 */
public final class InOrderReactionRuleSupplier<B extends Bigraph<? extends Signature<?>>> extends AbstractReactionRuleSupplier<B> {
    private int currentCounter = 0;

    InOrderReactionRuleSupplier(Collection<ReactionRule<B>> availableRules) {
        super(availableRules);
    }

    @Override
    public ReactionRule<B> get() {
        if (currentCounter < availableRules.size()) {
            return availableRules.get(currentCounter++);
        }
        return null;
    }
}
