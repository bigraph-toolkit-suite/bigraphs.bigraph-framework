package org.bigraphs.framework.core.exceptions;

public class AgentNotPrimeException extends ReactiveSystemException {

    public AgentNotPrimeException() {
        super("The agent of the reactive system is not prime.");
    }
}
