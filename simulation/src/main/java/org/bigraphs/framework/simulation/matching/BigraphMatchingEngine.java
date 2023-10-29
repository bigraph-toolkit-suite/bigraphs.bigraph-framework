package org.bigraphs.framework.simulation.matching;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.Signature;

import java.util.Collection;

/**
 * Interface for implementing a matching algorithm for a concrete bigraph kind (e.g., pure bigraphs).
 *
 * @author Dominik Grzelak
 */
public interface BigraphMatchingEngine<B extends Bigraph<? extends Signature<?>>> {

    Collection<? extends BigraphMatch<B>> getMatches();
}
