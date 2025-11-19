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
package org.bigraphs.framework.core.datatypes;

import java.util.Objects;
import org.bigraphs.framework.core.exceptions.InvalidOrdinalTypeException;

/**
 * Data type for a finite ordinal which is used to represent the arity of a control
 * or a value in the interface of a bigraph.
 *
 * @param <T> type of the ordinal. Integer is currently supported.
 * @author Dominik Grzelak
 */
public class FiniteOrdinal<T extends Number> implements Comparable<FiniteOrdinal<T>> {
    private T value;

    private FiniteOrdinal(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    private static <T extends Number> FiniteOrdinal<T> of(T v, Class<T> classType) {
        if (!classType.isInstance(Integer.class) || !classType.isInstance(Long.class)) {
            throw new InvalidOrdinalTypeException();
        }
        return new FiniteOrdinal<>(v);
    }

    public static FiniteOrdinal<Integer> ofInteger(int v) {
        return new FiniteOrdinal<>(v);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FiniteOrdinal)) return false;
        FiniteOrdinal<?> that = (FiniteOrdinal<?>) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public int compareTo(FiniteOrdinal<T> o) {
        if (o.getValue() instanceof Long) {
            return Long.compare(this.getValue().longValue(), o.getValue().longValue());
        } else { // Integer
            return Integer.compare(this.getValue().intValue(), o.getValue().intValue());
        }
    }
}
