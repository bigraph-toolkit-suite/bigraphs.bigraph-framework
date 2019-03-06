package de.tudresden.inf.st.bigraphs.core.exceptions;

public class ControlIsAtomicException extends ArityMismatch {

    public ControlIsAtomicException() {
        super("Control is atomic");
    }
}
