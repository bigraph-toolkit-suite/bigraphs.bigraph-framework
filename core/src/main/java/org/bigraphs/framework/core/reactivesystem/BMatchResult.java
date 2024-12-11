package org.bigraphs.framework.core.reactivesystem;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;

/**
 * Generic interface for bigraph rewriting matching results.
 *
 * @param <B> type of the underlying bigraph
 * @author Dominik Grzelak
 */
public interface BMatchResult<B extends Bigraph<? extends Signature<?>>> {

    ReactionRule<B> getReactionRule();

    BigraphMatch<B> getMatch();

    /**
     * This stores the rewritten bigraph for reference
     * @return
     */
    B getBigraph();

    int getOccurrenceCount();

    /**
     * Get the canonical string of the agent for this match result
     * @return
     */
    String getCanonicalString();
}
