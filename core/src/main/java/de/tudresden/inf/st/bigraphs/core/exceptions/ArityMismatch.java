package de.tudresden.inf.st.bigraphs.core.exceptions;

//TODO verständlicher machen
public class ArityMismatch extends Exception {

    public ArityMismatch() {
        super("Arity of control doesn't match the current node's arity.");
    }

    protected ArityMismatch(String message) {
        super(message);
    }
}
