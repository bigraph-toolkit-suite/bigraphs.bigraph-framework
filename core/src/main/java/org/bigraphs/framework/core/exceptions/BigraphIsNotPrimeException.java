package org.bigraphs.framework.core.exceptions;

/**
 * @author Dominik Grzelak
 */
public class BigraphIsNotPrimeException extends RuntimeException {
    public BigraphIsNotPrimeException() {
        super("Bigraph is not prime.");
    }
}
