package de.tudresden.inf.st.bigraphs.visualization;

import java.io.File;
import java.io.IOException;

/**
 * Minimal common interface for all graphics exporters
 *
 * @param <T>
 * @author Dominik Grzelak
 */
public interface BigraphGraphicsExporter<T> {

    void toPNG(T t, File file) throws IOException;

    BigraphGraphicsExporter<T> with(GraphicalFeatureSupplier<?> supplier);
}
