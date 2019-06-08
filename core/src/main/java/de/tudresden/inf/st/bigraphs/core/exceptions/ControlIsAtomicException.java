package de.tudresden.inf.st.bigraphs.core.exceptions;

/**
 * @author Dominik Grzelak
 */
public class ControlIsAtomicException extends InvalidArityOfControlException {

    public ControlIsAtomicException() {
        super("Control is atomic");
    }
}
