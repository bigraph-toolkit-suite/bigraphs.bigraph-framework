package org.bigraphs.framework.core.exceptions;

/**
 * @author Dominik Grzelak
 */
public abstract class InvalidReactionRuleException extends Exception {

    public InvalidReactionRuleException() {
    }

    public InvalidReactionRuleException(String message) {
        super(message);
    }
}
