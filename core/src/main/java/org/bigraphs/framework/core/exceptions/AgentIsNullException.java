package org.bigraphs.framework.core.exceptions;

/**
 * @author Dominik Grzelak
 */
public class AgentIsNullException extends ReactiveSystemException {

    public AgentIsNullException() {
        super("The agent of the reactive system is missing. Maybe it was not set. Must not be null");
    }
}
