package de.tudresden.inf.st.bigraphs.core.impl;

import de.tudresden.inf.st.bigraphs.core.AbstractSignature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;

public final class DefaultDynamicSignature extends AbstractSignature<DefaultDynamicControl<? extends NamedType, ? extends FiniteOrdinal>> {

    public DefaultDynamicSignature(Iterable<DefaultDynamicControl<? extends NamedType, ? extends FiniteOrdinal>> controls) {
        super(controls);
    }

    //    public DefaultDynamicSignature(Collection<DefaultDynamicControl> controls) {
//        super(controls);
//    }
}
