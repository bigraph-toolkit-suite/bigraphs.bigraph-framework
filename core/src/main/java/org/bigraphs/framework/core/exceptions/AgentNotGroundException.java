package org.bigraphs.framework.core.exceptions;

/**
 * @author Dominik Grzelak
 */
public class AgentNotGroundException extends ReactiveSystemException {

    public AgentNotGroundException() {
        super("The agent of the reactive system is not ground.");
    }
}
