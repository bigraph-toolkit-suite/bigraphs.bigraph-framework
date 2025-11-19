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
package org.bigraphs.framework.core;

import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.NamedType;

/**
 * Abstract base class for all control builder implementations.
 * <p>
 * Used by {@link SignatureBuilder} to create individual control instances.
 *
 * @param <NT> the label type of a "control"
 * @param <V>  the arity type of a "control"
 * @param <B>  the concrete control builder type
 * @author Dominik Grzelak
 * @see SignatureBuilder
 */
public abstract class ControlBuilder<NT extends NamedType<?>, V extends FiniteOrdinal<?>, B extends ControlBuilder<NT, V, B>> {

    protected NT type;
    protected V arity;
    private SignatureBuilder<NT, V, B, ?> builder;

    public ControlBuilder() {

    }

    public B identifier(NT nt) {
        this.type = nt;
        return self();
    }

    public B arity(V arity) {
        this.arity = arity;
        return self();
    }

    public SignatureBuilder<NT, V, B, ?> assign() {
        this.builder.add(this.build());
        return this.builder;
    }


    protected abstract Control<NT, V> build();

    @SuppressWarnings("unchecked")
    protected final B self() {
        return (B) this;
    }

    public void withControlListBuilder(SignatureBuilder<NT, V, B, ?> cbSignatureBuilder) {
        this.builder = cbSignatureBuilder;
    }

    public NT getType() {
        return type;
    }

    public V getArity() {
        return arity;
    }
}
