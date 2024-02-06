package org.bigraphs.framework.core;

import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.NamedType;

/**
 * Abstract class for all control builder implementations.
 * It is used by the {@link SignatureBuilder} class to create the individual control instances.
 *
 * @param <NT> type of the label of a control
 * @param <V>  type of the arity of a control
 * @param <B>  type of the control builder
 * @author Dominik Grzelak
 * @see SignatureBuilder
 */
public abstract class ControlBuilder<NT extends NamedType<?>, V extends FiniteOrdinal<?>, B extends ControlBuilder<NT, V, B>> {
    protected NT type;
    protected V arity;
    private SignatureBuilder<NT, V, B, ?> builder;

    public ControlBuilder() {

    }

    public B identifier(NT nt) {
        this.type = nt;
        return self();
    }

    public B arity(V arity) {
        this.arity = arity;
        return self();
    }

    public SignatureBuilder<NT, V, B, ?> assign() {
        this.builder.addControl(this.build());
        return this.builder;
    }


    protected abstract Control<NT, V> build();

    @SuppressWarnings("unchecked")
    protected final B self() {
        return (B) this;
    }

    public void withControlListBuilder(SignatureBuilder<NT, V, B, ?> cbSignatureBuilder) {
        this.builder = cbSignatureBuilder;
    }

    public NT getType() {
        return type;
    }

    public V getArity() {
        return arity;
    }
}
