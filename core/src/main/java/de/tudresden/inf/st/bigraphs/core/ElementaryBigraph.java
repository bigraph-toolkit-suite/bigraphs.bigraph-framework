package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.impl.EmptySignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;

import java.io.Serializable;
import java.util.Collection;

/**
 * Interface for the basic building blocks for all bigraphs.
 * <p>
 * With them other larger bigraphs can be built.
 */
public interface ElementaryBigraph extends BigraphicalConstruct<EmptySignature> {

}
