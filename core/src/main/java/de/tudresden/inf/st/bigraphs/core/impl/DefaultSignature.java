package de.tudresden.inf.st.bigraphs.core.impl;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import org.checkerframework.checker.nullness.qual.*;
import de.tudresden.inf.st.bigraphs.core.*;

public final class DefaultSignature extends AbstractSignature<DefaultControl<? extends NamedType, ? extends FiniteOrdinal>> {

    public DefaultSignature(@NonNull Iterable<DefaultControl<? extends NamedType, ? extends FiniteOrdinal>> controls) {
        super(controls);
    }

}
