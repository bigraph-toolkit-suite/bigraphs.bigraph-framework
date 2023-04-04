package de.tudresden.inf.st.bigraphs.core.factory;

import de.tudresden.inf.st.bigraphs.core.Bigraph;

import java.lang.reflect.Type;

/**
 * @author Dominik Grzelak
 */
public interface BigraphFactoryElement {

    Type getSignatureType();

    Class<? extends Bigraph<?>> getBigraphClassType();

}
