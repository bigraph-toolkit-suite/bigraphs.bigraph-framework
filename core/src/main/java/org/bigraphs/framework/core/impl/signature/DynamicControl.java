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

import java.util.Objects;
import org.bigraphs.framework.core.AbstractControl;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;

/**
 * Immutable dynamic control.
 * The status of a control can be explicitly specified; if none is provided, the control defaults to active.
 * <p>
 * Atomicity of a node:
 * <li>Atomic nodes are always empty.</li>
 * <li>Non-atomic nodes can be either active or passive.</li>
 *
 * @author Dominik Grzelak
 */
public class DynamicControl extends AbstractControl<StringTypedName, FiniteOrdinal<Integer>> {

    private final ControlStatus statusOfControl;

    private int hashed = -1;

    /**
     * Status will be set to {@link ControlStatus#ACTIVE}
     *
     * @param name  the label of the control
     * @param arity the arity of the control
     */
    protected DynamicControl(StringTypedName name, FiniteOrdinal<Integer> arity) {
        this(name, arity, ControlStatus.ACTIVE);
    }

    private DynamicControl(StringTypedName name, FiniteOrdinal<Integer> arity, ControlStatus statusOfControl) {
        super(name, arity);
        if ((statusOfControl) == null) {
            statusOfControl = ControlStatus.ACTIVE;
        }
        this.statusOfControl = statusOfControl;
    }

    public static DynamicControl createDynamicControl(StringTypedName name, FiniteOrdinal<Integer> arity,
                                                      ControlStatus kindOfControl) {
        return new DynamicControl(name, arity, kindOfControl);
    }

    @Override
    public ControlStatus getControlKind() {
        return statusOfControl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DynamicControl that)) return false;
        if (!super.equals(o)) return false;
        return statusOfControl == that.statusOfControl;
    }

    @Override
    public int hashCode() {
        if (hashed == -1) {
            hashed = Objects.hash(super.hashCode(), statusOfControl);
        }
        return hashed;
    }
}
