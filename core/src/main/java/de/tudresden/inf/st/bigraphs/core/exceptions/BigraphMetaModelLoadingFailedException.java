package de.tudresden.inf.st.bigraphs.core.exceptions;

/**
 * @author Dominik Grzelak
 */
public class BigraphMetaModelLoadingFailedException extends RuntimeException {

    public BigraphMetaModelLoadingFailedException(Exception e) {
        super("Failed to load the base bigraph meta model. Maybe the *.jar is not included?" + e.toString());
        setStackTrace(e.getStackTrace());
    }
}
