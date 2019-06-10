package de.tudresden.inf.st.bigraphs.core;

/**
 * Base interface for bigraphical reactive systems
 *
 * @author Dominik Grzelak
 */
public interface ReactiveSystem {

    /**
     * Checks whether the bigraphical reactive system is simple. A BRS is simple if all its reaction rules are so.
     *
     * @return {@code true} if the BRS is simple, otherwise {@code false}
     */
    boolean isSimple();
}
