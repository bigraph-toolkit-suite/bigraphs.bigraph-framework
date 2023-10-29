package org.bigraphs.framework.converter;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.ReactiveSystem;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A generic pretty printer interface for bigraphical reactive systems.
 *
 * @param <R> type of the reactive system
 * @author Dominik Grzelak
 */
public interface ReactiveSystemPrettyPrinter<B extends Bigraph<? extends Signature<?>>, R extends ReactiveSystem> extends PrettyPrinter<B> {
    /**
     * Returns the result of a reactive system encoding as string.
     *
     * @param system the reactive system being encoded
     * @return
     */
    String toString(R system);

    /**
     * Redirects the result of an encoding to an output stream.
     *
     * @param system       the reactive system being encoded
     * @param outputStream the output stream where the result shall be written to
     * @throws IOException because of the stream
     */
    void toOutputStream(R system, OutputStream outputStream) throws IOException;
}
