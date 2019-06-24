package de.tudresden.inf.st.bigraphs.core.impl.builder;

import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;

public abstract class ControlBuilder<NT extends NamedType, V extends FiniteOrdinal, B extends ControlBuilder<NT, V, B>> {
    NT type;
    V arity;
    private SignatureBuilder<NT, V, B, ? extends SignatureBuilder> builder;

    ControlBuilder() {

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


    abstract Control<NT, V> build();

    @SuppressWarnings("unchecked")
    final B self() {
        return (B) this;
    }

    public void withControlListBuilder(SignatureBuilder<NT, V, B, ? extends SignatureBuilder> cbSignatureBuilder) {
        this.builder = cbSignatureBuilder;
    }
}
