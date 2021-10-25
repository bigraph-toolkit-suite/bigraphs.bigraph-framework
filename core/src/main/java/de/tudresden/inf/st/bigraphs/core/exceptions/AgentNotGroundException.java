package de.tudresden.inf.st.bigraphs.core.exceptions;

/**
 * @author Dominik Grzelak
 */
public class AgentNotGroundException extends ReactiveSystemException {

    public AgentNotGroundException() {
        super("The agent of the reactive system is not ground.");
    }
}
