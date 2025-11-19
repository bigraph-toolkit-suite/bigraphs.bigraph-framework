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
package org.bigraphs.framework.core.reactivesystem;

import com.google.common.collect.BiMap;
import java.util.Collection;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;

/**
 * Base interface for bigraphical reactive systems.
 * <p>
 * When a reactive system is executed based on a rule set then it synthesizes a labelled transition system which
 * represents the behavior of the bigraphical reactive system.
 * <p>
 * The interface offers methods to build the result of a reaction.
 *
 * @author Dominik Grzelak
 */
public interface ReactiveSystem<B extends Bigraph<? extends Signature<?>>> {

    /**
     * Return the labels of the transition system which are called reaction rules for BRS.
     *
     * @return the labels of the transition system
     */
    Collection<ReactionRule<B>> getReactionRules();

    BiMap<String, ReactionRule<B>> getReactionRulesMap();

    B getAgent();

    Signature getSignature();

    Collection<ReactiveSystemPredicate<B>> getPredicates();

    BiMap<String, ReactiveSystemPredicate<B>> getPredicateMap();

    B buildGroundReaction(final B agent, final BigraphMatch<B> match, final ReactionRule<B> rule);

    B buildParametricReaction(final B agent, final BigraphMatch<B> match, final ReactionRule<B> rule);

    /**
     * Checks whether the bigraphical reactive system is simple. A BRS is simple if all its reaction rules are so.
     *
     * @return {@code true} if the BRS is simple, otherwise {@code false}
     */
    default boolean isSimple() {
        return getReactionRules().stream().allMatch(ReactionRule::isRedexSimple);
    }

//    boolean addReactionRule(ReactionRule<B> reactionRule) throws InvalidReactionRuleException;
//    ReactiveSystem<B> setAgent(B initialAgent);
}
