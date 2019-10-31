package de.tudresden.inf.st.bigraphs.converter.bigmc;

import de.tudresden.inf.st.bigraphs.rewriting.ReactiveSystem;

import java.io.IOException;
import java.io.OutputStream;

public interface PrettyPrinter<T extends ReactiveSystem> {
    String toString(T system);

    void toOutputStream(T system, OutputStream outputStream) throws IOException;
}
