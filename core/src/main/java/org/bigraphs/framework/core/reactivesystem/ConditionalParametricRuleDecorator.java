/*
 * Copyright (c) 2023-2024 Bigraph Toolkit Suite Developers
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

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;

public class ConditionalParametricRuleDecorator<B extends Bigraph<? extends Signature<?>>> extends ParametricReactionRule<B> {

    public interface RuleConditionMatcher<B extends Bigraph<? extends Signature<?>>> {
        boolean conditionIsSatisfied(B theAgent, ReactionRule<B> theRule, BigraphMatch<B> match);
    }

    private final ParametricReactionRule<B> reactionRule;

    private final RuleConditionMatcher<B> conditionMatcher;

    public ConditionalParametricRuleDecorator(ParametricReactionRule<B> reactionRule) throws InvalidReactionRuleException {
        this(reactionRule, (agent, rule, match) -> true);
    }

    public ConditionalParametricRuleDecorator(ParametricReactionRule<B> reactionRule, RuleConditionMatcher<B> conditionMatcher) throws InvalidReactionRuleException {
        super(reactionRule.getRedex(), reactionRule.getReactum(), reactionRule.getInstantationMap(), reactionRule.isReversible());
        this.reactionRule = reactionRule;
        this.conditionMatcher = conditionMatcher;
    }

    public boolean isMatchValid(B theAgent, ReactionRule<B> theRule, BigraphMatch<B> match) {
        return this.conditionMatcher.conditionIsSatisfied(theAgent, theRule, match);
    }

    /**
     * Returns the underlying reaction rule.
     *
     * @return the reaction rule of the conditional decorator
     */
    public ParametricReactionRule<B> getReactionRule() {
        return reactionRule;
    }

    public RuleConditionMatcher<B> getConditionMatcher() {
        return conditionMatcher;
    }
}
