package de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.simulation.exceptions;

/**
 * @author Dominik Grzelak
 */
public class ModelCheckerExecutorServiceNotProvided extends RuntimeException {

    public ModelCheckerExecutorServiceNotProvided() {
        super("No executor service for the model checker was provided. Use the service class loader.");
    }
}
