package de.tudresden.inf.st.bigraphs.core.exceptions;

public class ControlIsAtomicException extends InvalidArityOfControlException {

    public ControlIsAtomicException() {
        super("Control is atomic");
    }
}
