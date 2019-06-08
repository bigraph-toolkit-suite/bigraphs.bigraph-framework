package de.tudresden.inf.st.bigraphs.core.exceptions.builder;

/**
 * @author Dominik Grzelak
 */
public class LinkTypeNotExistsException extends TypeNotExistsException {

    public LinkTypeNotExistsException() {
        this("This bigraph link type doesn't exists.");
    }

    public LinkTypeNotExistsException(String message) {
        super(message);
    }
}
