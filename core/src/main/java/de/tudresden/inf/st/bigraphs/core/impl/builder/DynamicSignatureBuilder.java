package de.tudresden.inf.st.bigraphs.core.impl.builder;


import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;

import java.util.Collections;
import java.util.Set;

public class DynamicSignatureBuilder<NT extends NamedType, FO extends FiniteOrdinal>
        extends SignatureBuilder<NT, FO, DynamicControlBuilder<NT, FO>, DynamicSignatureBuilder<NT, FO>> {

    @Override
    public DynamicControlBuilder<NT, FO> createControlBuilder() {
        return new DynamicControlBuilder<>();
    }


    @Override
    public <S extends Signature> S createSignature(Iterable<? extends Control> controls) {
//        return DefaultDynamicSignature.class.cast(new DefaultDynamicSignature(null));
        return (S) new DefaultDynamicSignature((Set<DefaultDynamicControl<? extends NamedType, ? extends FiniteOrdinal>>) controls);
    }

    @Override
    public DefaultDynamicSignature createSignature() {
        return new DefaultDynamicSignature(Collections.emptySet());
    }

//    @Override
//    protected <S extends Signature> Class<S> getSignatureClass() {
//        return (Class<S>) DefaultDynamicSignature.class;
//    }

}
