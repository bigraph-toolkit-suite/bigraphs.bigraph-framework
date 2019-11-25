package de.tudresden.inf.st.bigraphs.core.impl;

import de.tudresden.inf.st.bigraphs.core.AbstractSignature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;

import java.util.Collections;
import java.util.Set;

public final class DefaultDynamicSignature extends AbstractSignature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> {

    public DefaultDynamicSignature(Set<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> controls) {
        super(controls);
    }

}
