package de.tudresden.inf.st.bigraphs.core.exceptions;

public class BigraphMetaModelLoadingFailedException extends RuntimeException {

    public BigraphMetaModelLoadingFailedException() {
        super("Failed to load the base bigraph meta model. Maybe the *.jar is not included?");
    }
}
