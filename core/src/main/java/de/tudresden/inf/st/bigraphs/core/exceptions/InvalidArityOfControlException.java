package de.tudresden.inf.st.bigraphs.core.exceptions;

//TODO verständlicher machen

/**
 * @author Dominik Grzelak
 */
public class InvalidArityOfControlException extends InvalidConnectionException {

    public InvalidArityOfControlException() {
        super("Arity of control doesn't match the current node's arity.");
    }

    protected InvalidArityOfControlException(String message) {
        super(message);
    }
}
