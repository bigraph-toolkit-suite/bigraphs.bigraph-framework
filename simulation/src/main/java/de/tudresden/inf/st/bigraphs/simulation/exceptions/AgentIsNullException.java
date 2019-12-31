package de.tudresden.inf.st.bigraphs.simulation.exceptions;

/**
 * @author Dominik Grzelak
 */
public class AgentIsNullException extends BigraphSimulationException {

    public AgentIsNullException() {
        super("The agent of the reactive system is missing. Maybe it was not set. Must not be null");
    }
}
