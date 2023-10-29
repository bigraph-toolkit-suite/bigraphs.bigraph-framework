package org.bigraphs.framework.converter;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A pretty printer interface for just bigraphs.
 *
 * @author Dominik Grzelak
 */
public interface BigraphPrettyPrinter<B extends Bigraph<? extends Signature<?>>> extends PrettyPrinter<B> {

    String toString(B bigraph);

    void toOutputStream(B bigraph, OutputStream outputStream) throws IOException;
}
