package de.tudresden.inf.st.bigraphs.core.impl;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import org.checkerframework.checker.nullness.qual.*;
import de.tudresden.inf.st.bigraphs.core.*;

import java.util.Set;

public class BasicSignature extends AbstractSignature<DefaultControl<? extends NamedType, ? extends FiniteOrdinal>> {

    public BasicSignature(@NonNull Set<DefaultControl<? extends NamedType, ? extends FiniteOrdinal>> controls) {
        super(controls);
    }

}
