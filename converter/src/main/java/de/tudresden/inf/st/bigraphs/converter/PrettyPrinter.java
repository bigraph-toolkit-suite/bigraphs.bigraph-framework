package de.tudresden.inf.st.bigraphs.converter;

import de.tudresden.inf.st.bigraphs.rewriting.ReactiveSystem;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A generic pretty printer interface for bigraphical reactive systems.
 *
 * @param <R> type of the reactive system
 * @author Dominik Grzelak
 */
public interface PrettyPrinter<R extends ReactiveSystem> {
    String toString(R system);

    void toOutputStream(R system, OutputStream outputStream) throws IOException;
}
