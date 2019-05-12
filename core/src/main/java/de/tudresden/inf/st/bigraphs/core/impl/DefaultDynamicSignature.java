package de.tudresden.inf.st.bigraphs.core.impl;

import de.tudresden.inf.st.bigraphs.core.AbstractSignature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;

import java.util.Collections;
import java.util.Set;

public final class DefaultDynamicSignature extends AbstractSignature<DefaultDynamicControl<? extends NamedType, ? extends FiniteOrdinal>> {

    public DefaultDynamicSignature(Set<DefaultDynamicControl<? extends NamedType, ? extends FiniteOrdinal>> controls) {
        super(controls);
    }

}
