package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;

import java.util.Objects;

public abstract class AbstractControl<NT extends NamedType<?>, V extends FiniteOrdinal<?>> implements Control<NT, V> {
    protected final NT name;
    protected final V arity;

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
        return Objects.hash(name, arity);
    }
}
