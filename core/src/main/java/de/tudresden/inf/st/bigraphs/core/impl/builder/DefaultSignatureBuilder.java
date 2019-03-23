package de.tudresden.inf.st.bigraphs.core.impl.builder;

import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultSignature;

import java.util.Set;

//TODO nested builder: https://dzone.com/articles/nested-builder
//<NT extends NamedType, FO extends FiniteOrdinal, C extends ControlBuilder<NT, FO, ? extends ControlBuilder>, B extends SignatureBuilder>
public class DefaultSignatureBuilder<NT extends NamedType, FO extends FiniteOrdinal>
        extends SignatureBuilder<NT, FO, DefaultControlBuilder<NT, FO>, DefaultSignatureBuilder<NT, FO>> {

    public DefaultSignatureBuilder() {
        super();
    }

    @Override
    public DefaultControlBuilder<NT, FO> createControlBuilder() {
        return new DefaultControlBuilder<>();
    }

    @Override
    public <S extends Signature> S createSignature(Iterable<? extends Control> controls) {
//        Collections.emptyList().addAll(controls);
        return (S) new DefaultSignature((Set<DefaultControl<? extends NamedType, ? extends FiniteOrdinal>>) controls);
    }

//    @Override
//    protected <S extends Signature> Class<S> getSignatureClass() {
//        return (Class<S>) DefaultSignature.class;
//    }

}
