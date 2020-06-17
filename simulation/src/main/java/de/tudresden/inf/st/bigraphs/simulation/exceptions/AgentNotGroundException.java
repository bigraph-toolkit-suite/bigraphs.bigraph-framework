package de.tudresden.inf.st.bigraphs.simulation.exceptions;

/**
 * @author Dominik Grzelak
 */
public class AgentNotGroundException extends BigraphSimulationException {

    public AgentNotGroundException() {
        super("The agent of the reactive system is not ground.");
    }
}
