package de.tudresden.inf.st.bigraphs.core.exceptions.builder;

/**
 * @author Dominik Grzelak
 */
public class TypeNotExistsException extends Exception {

    public TypeNotExistsException() {
        this("This bigraph type doesn't exists.");
    }

    public TypeNotExistsException(String message) {
        super(message);
    }
}
