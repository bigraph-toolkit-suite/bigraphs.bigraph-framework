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
 * Superinterface for all control representations.
 * <p>
 * A control is a {@link HasIdentifier} with label type {@code <NT>} and arity type {@code <T>}.
 *
 * @param <NT> the label type
 * @param <T>  the arity type
 * @author Dominik Grzelak
 */
public interface Control<NT extends NamedType, T extends FiniteOrdinal> extends HasIdentifier<NT> {

    /**
     * Returns the arity of the control
     *
     * @return the arity of type {@code T}
     */
    T getArity();

    /**
     * Returns the kind of the control.
     *
     * @return kind of the control
     */
    ControlStatus getControlKind();

}
