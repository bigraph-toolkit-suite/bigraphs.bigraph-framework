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

import java.util.Objects;
import java.util.Set;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;

/**
 * Generic super interface for a bigraph signature.
 * <p>
 * Technology-independent counterpart to {@link EcoreSignature}.
 *
 * @param <C> the control type
 * @author Dominik Grzelak
 */
public interface Signature<C extends Control<?, ?>> {

    /**
     * Get the controls of the signature.
     *
     * @return control set of the signature
     */
    Set<C> getControls();

    /**
     * Get the control by its string identifier
     *
     * @param name the identifier of the control
     * @return the corresponding control
     */
    default C getControlByName(String name) {
        for (C next1 : getControls()) {
            if (next1.getNamedType().stringValue().equals(name)) {
                return next1;
            }
        }
        return null;
    }

    default C getControl(String name, int arity) {
        for (C next1 : getControls()) {
            if (next1.getNamedType().stringValue().equals(name) &&
                    next1.getArity().getValue().intValue() == arity) {
                return next1;
            }
        }
        return null;
    }

    default C getControl(String name, int arity, ControlStatus controlStatus) {
        for (C next1 : getControls()) {
            if (next1.getNamedType().stringValue().equals(name) &&
                    next1.getArity().getValue().intValue() == arity &&
                    next1.getControlKind().equals(controlStatus)) {
                return next1;
            }
        }
        return null;
    }

    default FiniteOrdinal<?> getArity(String controlName) {
        C controlByName = getControlByName(controlName);
        if (Objects.nonNull(controlByName)) {
            return controlByName.getArity();
        }
        return null;
    }

    default FiniteOrdinal<?> getArity(C control) {
        if (getControls().contains(control)) {
            return control.getArity();
        }
        return null;
    }
}
