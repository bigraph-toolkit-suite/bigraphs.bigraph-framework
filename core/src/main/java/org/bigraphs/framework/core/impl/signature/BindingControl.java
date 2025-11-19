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
package org.bigraphs.framework.core.impl.signature;

import org.bigraphs.framework.core.AbstractControl;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.NamedType;

/**
 * <strong>Note:</strong> This class is not yet implemented!
 * <p>
 * A binding control for a binding signature for binding bigraphs.
 *
 * @param <NT> type of the label
 * @param <FO> type of the arity
 * @author Dominik Grzelak
 */
public class BindingControl<NT extends NamedType<?>, FO extends FiniteOrdinal<?>> extends AbstractControl<NT, FO> {

    protected BindingControl(NT name, FO arity) {
        super(name, arity);
    }

    public boolean isBindingControl() {
        return getArity().equals(FiniteOrdinal.ofInteger(0)) && getControlKind().equals(ControlStatus.PASSIVE);
    }

    @Override
    public ControlStatus getControlKind() {
        throw new RuntimeException("Not yet implemented");
    }
}
