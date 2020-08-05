package de.tudresden.inf.st.bigraphs.simulation.matching;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;

import java.util.Collection;

/**
 * Interface for implementing a matching algorithm for a concrete bigraph kind (e.g., pure bigraphs).
 *
 * @author Dominik Grzelak
 */
public interface BigraphMatchingEngine<B extends Bigraph<? extends Signature<?>>> {

    Collection<? extends BigraphMatch<B>> getMatches();
}
