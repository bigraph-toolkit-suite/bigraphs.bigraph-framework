package de.tudresden.inf.st.bigraphs.core.exceptions;

/**
 * Exception that is thrown when it is tried to nest a another node within an atomic node.
 *
 * @author Dominik Grzelak
 */
public class ControlIsAtomicException extends Exception {

    public ControlIsAtomicException() {
        super("Control is atomic");
    }
}
