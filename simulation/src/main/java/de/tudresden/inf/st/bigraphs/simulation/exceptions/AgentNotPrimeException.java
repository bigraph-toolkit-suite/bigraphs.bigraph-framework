package de.tudresden.inf.st.bigraphs.simulation.exceptions;

public class AgentNotPrimeException extends BigraphSimulationException {

    public AgentNotPrimeException() {
        super("The agent of the reactive system is not prime.");
    }
}
