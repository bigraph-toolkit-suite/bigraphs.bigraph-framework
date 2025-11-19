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

import java.util.Collection;
import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;

/**
 * This interface represents a result of a bigraph matching and is used by the {@code BigraphMatchingEngine} in the
 * simulation module.
 *
 * @param <B> the bigraph type of the match
 * @author Dominik Grzelak
 */
public interface BigraphMatch<B extends Bigraph<? extends Signature<?>>> {

    /**
     * The context of the match
     *
     * @return the context
     */
    B getContext();

    /**
     * Identity link graph for the composition of the context and the redex image (see {@link BigraphMatch#getRedexImage()}.
     *
     * @return the identity link graph for the context
     */
    <BPrime extends Bigraph<? extends Signature<?>>> BPrime getContextIdentity();

    /**
     * Returns the redex of the reaction rule.
     *
     * @return the redex of the reaction rule
     */
    B getRedex();

    /**
     * Get the identity link graph of the redex to build the <i>redex image</i>.
     *
     * @return the identity link graph of the redex
     */
    <BPrime extends Bigraph<? extends Signature<?>>> BPrime getRedexIdentity();

    /**
     * Returns the <i>redex image</i> - the juxtaposition of the redex and a suitable identity.
     *
     * @return the product of the redex and identity link graph
     */
    B getRedexImage();

    /**
     * Get all parameters of the reaction rules as a list
     *
     * @return the parameters of the reaction rule
     */
    Collection<B> getParameters();

    B getParam();

    boolean wasRewritten();

}
