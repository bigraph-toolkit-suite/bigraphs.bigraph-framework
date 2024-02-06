package org.bigraphs.framework.core.datatypes;

import org.bigraphs.framework.core.exceptions.InvalidOrdinalTypeException;

import java.util.Objects;

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
