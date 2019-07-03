package de.tudresden.inf.st.bigraphs.core.exceptions;

/**
 * @author Dominik Grzelak
 */
public class BigraphIsNotGroundException extends RuntimeException {
    public BigraphIsNotGroundException() {
        super("Bigraph is not ground.");
    }
}
