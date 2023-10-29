package org.bigraphs.framework.core.exceptions.builder;

/**
 * @author Dominik Grzelak
 */
public abstract class TypeNotExistsException extends Exception {

    public TypeNotExistsException() {
        this("This bigraph type doesn't exists.");
    }

    public TypeNotExistsException(String message) {
        super(message);
    }
}
