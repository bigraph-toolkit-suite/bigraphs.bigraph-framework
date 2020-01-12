package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.impl.builder.SignatureBuilder;

public abstract class ControlBuilder<NT extends NamedType, V extends FiniteOrdinal, B extends ControlBuilder<NT, V, B>> {
    protected NT type;
    protected V arity;
    private SignatureBuilder<NT, V, B, ? extends SignatureBuilder> builder;

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

    public SignatureBuilder<NT, V, B, ? extends SignatureBuilder> assign() {
        this.builder.addControl(this.build());
        return this.builder;
    }


    protected abstract Control<NT, V> build();

    @SuppressWarnings("unchecked")
    public final B self() {
        return (B) this;
    }

    public void withControlListBuilder(SignatureBuilder<NT, V, B, ? extends SignatureBuilder> cbSignatureBuilder) {
        this.builder = cbSignatureBuilder;
    }

    public NT getType() {
        return type;
    }

    public V getArity() {
        return arity;
    }
}
