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
package org.bigraphs.framework.core.impl.signature;

import java.util.Set;
import org.bigraphs.framework.core.AbstractEcoreSignature;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.NamedType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

/**
 * <strong>Note:</strong> This class is not yet implemented!
 * <p>
 * Points can be bound in binding bigraphs and are represented by this signature type.
 * <p>
 * According to Milner, a binding implies that for a node it has arity 0 and it is passive.
 * <p>
 * Can only be used within binding bigraphs.
 * <p>
 * A binding signature K is a set of controls. For each K ∈ K it provides a pair of finite ordinals: the binding arity
 * arb(K) = h and the free arity arf(k) = k. We write ar(K) = arb(K) + arf (k).
 *
 * @author Dominik Grzelak
 */
public class BindingSignature extends AbstractEcoreSignature<BindingControl<? extends NamedType, ? extends FiniteOrdinal>> {

    public BindingSignature(Set<BindingControl<? extends NamedType, ? extends FiniteOrdinal>> controls) {
        super(controls);
    }

    public boolean isBindingControl(BindingControl<? extends NamedType, ? extends FiniteOrdinal> control) {
        if (!getControls().contains(control)) return false;
        BindingControl<? extends NamedType, ? extends FiniteOrdinal> controlByName = getControlByName(control.getNamedType().stringValue());
        return controlByName.isBindingControl();
    }

    @Override
    public EPackage getMetaModel() {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public EObject getInstanceModel() {
        throw new RuntimeException("Not implemented yet");
    }

}
