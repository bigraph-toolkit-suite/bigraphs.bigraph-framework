package org.bigraphs.framework.core;

import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.NamedType;

import java.util.Objects;

/**
 * Abstract class representing a control of a signature.
 *
 * @param <NT>
 * @param <V>
 * @author Dominik Grzelak
 */
public abstract class AbstractControl<NT extends NamedType<?>, V extends FiniteOrdinal<?>> implements Control<NT, V> {
    protected final NT name;
    protected final V arity;

    int hashed = -1;

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
//        return Objects.hash(name, arity);
    }
}
