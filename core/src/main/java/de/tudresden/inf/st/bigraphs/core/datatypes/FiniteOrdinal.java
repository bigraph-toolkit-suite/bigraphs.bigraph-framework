package de.tudresden.inf.st.bigraphs.core.datatypes;

import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidOrdinalTypeException;

public class FiniteOrdinal<T extends Number> {
    private T value;

    private FiniteOrdinal(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    private static <T extends Number> FiniteOrdinal<T> of(T v, Class<T> classType) {
        if(!classType.isInstance(Integer.class) || !classType.isInstance(Long.class)) {
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
}
