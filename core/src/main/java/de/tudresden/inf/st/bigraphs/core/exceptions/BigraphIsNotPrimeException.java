package de.tudresden.inf.st.bigraphs.core.exceptions;

/**
 * @author Dominik Grzelak
 */
public class BigraphIsNotPrimeException extends RuntimeException {
    public BigraphIsNotPrimeException() {
        super("Bigraph is not prime.");
    }
}
