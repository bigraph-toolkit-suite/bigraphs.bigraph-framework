package de.tudresden.inf.st.bigraphs.core.exceptions;

/**
 * Exception that is thrown with operations on controls where the arity must be considered, e.g., connecting
 * links to a node with no free ports.
 *
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
