package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.exceptions;

/**
 * @author Dominik Grzelak
 */
public class InvalidSimulationStrategy extends BigraphSimulationException {

    public InvalidSimulationStrategy() {
        super("The simulation strategy is not valid. Must not be null.");
    }
}
