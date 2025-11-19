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
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.NamedType;

/**
 * Abstract base class for a control within a signature.
 *
 * @param <NT> the named type of the control
 * @param <V>  the value domain represented as a finite ordinal
 * @author Dominik Grzelak
 */
public abstract class AbstractControl<NT extends NamedType<?>, V extends FiniteOrdinal<?>> implements Control<NT, V> {
    protected final NT name;
    protected final V arity;

    private int hashed = -1;

    public AbstractControl(NT name, V artiy) {
        this.name = name;
        this.arity = artiy;
    }

    @Override
    public V getArity() {
        return arity;
    }

    @Override
    public NT getNamedType() {
        return this.name;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "name=" + name.getValue() +
                ", arity=" + arity.getValue() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractControl)) return false;
        AbstractControl<?, ?> that = (AbstractControl<?, ?>) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(arity, that.arity);
    }

    @Override
    public int hashCode() {
        if (hashed == -1) {
            hashed = Objects.hash(name, arity);
        }
        return hashed;
    }
}
