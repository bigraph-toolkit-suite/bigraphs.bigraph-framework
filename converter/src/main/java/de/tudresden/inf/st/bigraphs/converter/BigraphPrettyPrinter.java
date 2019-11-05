package de.tudresden.inf.st.bigraphs.converter;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.rewriting.ReactiveSystem;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Dominik Grzelak
 */
public interface BigraphPrettyPrinter<B extends Bigraph<? extends Signature<?>>, R extends ReactiveSystem<B>>
        extends PrettyPrinter<R> {

    String toString(B bigraph);

    void toOutputStream(B bigraph, OutputStream outputStream) throws IOException;

    @Override
    default String toString(R system) {
        return toString(system.getAgent());
    }

    @Override
    default void toOutputStream(R system, OutputStream outputStream) throws IOException {
        toOutputStream(system.getAgent(), outputStream);
    }
}
