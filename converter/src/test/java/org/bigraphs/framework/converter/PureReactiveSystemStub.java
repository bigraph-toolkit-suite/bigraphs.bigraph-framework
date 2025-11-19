/*
 * Copyright (c) 2021-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.converter;

import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.AbstractSimpleReactiveSystem;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.reactivesystem.ReactionRule;

public class PureReactiveSystemStub extends AbstractSimpleReactiveSystem<PureBigraph> {
    @Override
    public PureBigraph buildGroundReaction(PureBigraph agent, BigraphMatch<PureBigraph> match, ReactionRule<PureBigraph> rule) {
        return null;
    }

    @Override
    public PureBigraph buildParametricReaction(PureBigraph agent, BigraphMatch<PureBigraph> match, ReactionRule<PureBigraph> rule) {
        return null;
    }
}
