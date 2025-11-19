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

/**
 * A transition of a labelled transition system is a triple containing the source and target and the
 * label (i.e., the arrow itself which is a bigraph as well).
 *
 * @author Dominik Grzelak
 */
public class TransitionTriple<B extends Bigraph<? extends Signature<?>>> {
    private ReactiveSystem<B> transitionOwner;

    private B source;
    private B label;
    private B target;

    public TransitionTriple(ReactiveSystem<B> transitionOwner, B source, B label, B target) {
        this.transitionOwner = transitionOwner;
        this.source = source;
        this.label = label;
        this.target = target;
    }

    //TODO: isEngagedTransition
    public boolean isEngagedTransition() {
        throw new RuntimeException("Not implemented yet");
    }

    public ReactiveSystem<B> getTransitionOwner() {
        return transitionOwner;
    }

    public B getSource() {
        return source;
    }

    public B getLabel() {
        return label;
    }

    public B getTarget() {
        return target;
    }
}
