package de.tudresden.inf.st.bigraphs.core.datatypes;

import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidOrdinalTypeException;

import java.util.Objects;

/**
 * Data type for a finite ordinal which is used within an interface of a bigraph.
 *
 * @param <T> type of the ordinal. Either Integer or Long are supported
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

    //TODO erstmal nicht (keine generics erlauben (wg. EList capacity)
    @Deprecated
    public static FiniteOrdinal<Long> ofLong(long v) {
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
