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

import java.util.LinkedHashSet;
import java.util.Set;
import org.bigraphs.framework.core.datatypes.EMetaModelData;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.NamedType;
import org.bigraphs.framework.core.factory.BigraphFactory;

/**
 * Abstract base class for building instances of signatures.
 *
 * @param <NT> the name type
 * @param <FO> the finite ordinal type
 * @param <C>  the control builder type
 * @param <B>  the signature builder type
 * @author Dominik Grzelak
 */
public abstract class SignatureBuilder<NT extends NamedType<?>,
        FO extends FiniteOrdinal<?>,
        C extends ControlBuilder<NT, FO, C>,
        B extends SignatureBuilder<?, ?, ?, ?>> {

    private final Set<Control<NT, FO>> controls;

    public SignatureBuilder() {
        this.controls = new LinkedHashSet<>();
    }

    /**
     * Hook method to be implemented by subclasses for creating the corresponding control builder (i.e., only active or dynamic controls).
     *
     * @return the control builder
     */
    protected abstract C createControlBuilder();

    public C newControl() {
        C builder = createControlBuilder();
        builder.withControlListBuilder(this);
        return builder;
    }

    public C newControl(NT type, FO arity) {
        C builder = createControlBuilder();
        builder.withControlListBuilder(this);
        return builder.identifier(type).arity(arity);
    }

    public B add(Control<NT, FO> control) {
        controls.add(control);
        return self();
    }

    /**
     * Create a signature with the given controls.
     *
     * @param controls the controls to use for the signature
     * @return a signature with the given controls
     */
    public abstract Signature<?> createWith(Iterable<? extends Control<NT, FO>> controls);

    /**
     * Create the signature with the assigned controls so far.
     *
     * @return a signature
     */
    public Signature<?> create() {
        Signature<?> sig = createWith(getControls());
        if (sig instanceof AbstractEcoreSignature)
            BigraphFactory.createOrGetSignatureMetaModel((AbstractEcoreSignature<?>) sig);
        return sig;
    }

    public Signature<?> create(EMetaModelData metaModelData) {
        Signature<?> sig = createWith(getControls());
        if (sig instanceof AbstractEcoreSignature)
            BigraphFactory.createOrGetSignatureMetaModel((AbstractEcoreSignature<?>) sig, metaModelData);
        return sig;
    }

    /**
     * Creates an empty signature, meaning that the control set is empty.<br>
     * Needed for the interaction of elementary bigraphs and user-defined bigraphs.
     *
     * @return an empty signature of type {@literal <S>}.
     */
    public Signature<? extends Control<NT, FO>> createEmpty() {
        Signature<? extends Control<NT, FO>> sig = (Signature<? extends Control<NT, FO>>) createEmptyStub();
        if (sig instanceof AbstractEcoreSignature)
            BigraphFactory.createOrGetSignatureMetaModel((AbstractEcoreSignature<?>) sig);
        return sig;
    }

    /**
     * This method is not called by the user; it is called by {@link SignatureBuilder#createEmpty()}.
     */
    protected abstract Signature<? extends Control<NT, FO>> createEmptyStub();

    @SuppressWarnings("unchecked")
    protected final B self() {
        return (B) this;
    }

    public Set<Control<NT, FO>> getControls() {
        return this.controls;
    }
}
