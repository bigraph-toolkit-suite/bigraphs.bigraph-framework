package de.tudresden.inf.st.bigraphs.converter;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.simulation.ReactiveSystem;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A generic pretty printer interface for bigraphical reactive systems.
 *
 * @param <R> type of the reactive system
 * @author Dominik Grzelak
 */
public interface ReactiveSystemPrettyPrinter<B extends Bigraph<? extends Signature<?>>, R extends ReactiveSystem> extends PrettyPrinter<B> {
    String toString(R system);

    void toOutputStream(R system, OutputStream outputStream) throws IOException;
}
