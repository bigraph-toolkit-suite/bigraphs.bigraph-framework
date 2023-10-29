package org.bigraphs.framework.core.factory;

import org.bigraphs.framework.core.Bigraph;

import java.lang.reflect.Type;

/**
 * @author Dominik Grzelak
 */
public interface BigraphFactoryElement {

    Type getSignatureType();

    Class<? extends Bigraph<?>> getBigraphClassType();

}
