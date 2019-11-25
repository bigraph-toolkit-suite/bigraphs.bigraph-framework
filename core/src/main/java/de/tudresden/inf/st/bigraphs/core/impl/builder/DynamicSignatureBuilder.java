package de.tudresden.inf.st.bigraphs.core.impl.builder;


import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;

import java.util.Collections;
import java.util.Set;

public class DynamicSignatureBuilder
        extends SignatureBuilder<StringTypedName, FiniteOrdinal<Integer>, DynamicControlBuilder<StringTypedName, FiniteOrdinal<Integer>>, DynamicSignatureBuilder> {

    @Override
    public DynamicControlBuilder<StringTypedName, FiniteOrdinal<Integer>> createControlBuilder() {
        return new DynamicControlBuilder<>();
    }


    @Override
    public DefaultDynamicSignature createSignature(Iterable<? extends Control> controls) {
//        return DefaultDynamicSignature.class.cast(new DefaultDynamicSignature(null));
        return new DefaultDynamicSignature((Set<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>>) controls);
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
