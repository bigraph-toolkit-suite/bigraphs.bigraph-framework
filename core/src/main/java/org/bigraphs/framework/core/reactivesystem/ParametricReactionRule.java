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

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.exceptions.InvalidReactionRuleException;

/**
 * Concrete implementation of a parametric reaction rule.
 *
 * @param <B> type of the bigraph
 * @author Dominik Grzelak
 */
public class ParametricReactionRule<B extends Bigraph<? extends Signature<?>>> extends AbstractReactionRule<B> {

    public ParametricReactionRule(B redex, B reactum) throws InvalidReactionRuleException {
        super(redex, reactum);
    }

    public ParametricReactionRule(B redex, B reactum, InstantiationMap instantiationMap) throws InvalidReactionRuleException {
        super(redex, reactum, instantiationMap);
    }

    public ParametricReactionRule(B redex, B reactum, InstantiationMap instantiationMap, boolean isReversible) throws InvalidReactionRuleException {
        super(redex, reactum, instantiationMap, isReversible);
    }

    public ParametricReactionRule(B redex, B reactum, boolean isReversible) throws InvalidReactionRuleException {
        super(redex, reactum, isReversible);
    }

    // We need to relax this constraint because jLibBig handles this differently
    @Override
    public boolean isProperParametricRule() {
        return getRedex().isLean() && getReactum().isLean() && // both must be lean
                getRedex().getRoots().stream().allMatch(x -> getRedex().getChildrenOf(x).size() > 0); // only the redex must have no idle roots and names
    }

    // We need to relax this constraint because jLibBig handles this differently
    @Override
    public boolean isRedexSimple() {
        return
//                getRedex().getEdges().size() == 0 && // every link is open
                        getRedex().isGuarding() && //no site has a root as parent (+ no idle inner names)
                        getRedex().isMonomorphic(); // inner-injective

    }
}
