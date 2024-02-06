package org.bigraphs.framework.simulation.matching.pure;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

import java.util.*;

//TODO: in a future version: find matches incrementally and not all at the start
//  next() and hasNext() must be slightly rewritten then
// do not call find

/**
 * Iterator implementation for bigraph matching.
 * <p>
 * This iterator is created by the {@link PureBigraphMatcher} class.
 *
 * @author Dominik Grzelak
 */
public class PureMatchIteratorImpl implements Iterator<PureBigraphParametricMatch> {
    private int cursor = 0;
    private MutableList<PureBigraphParametricMatch> matches = Lists.mutable.empty();

    private PureBigraphMatchingEngine matchingEngine;

    PureMatchIteratorImpl(PureBigraphMatchingEngine matchingEngine) {
        this.matchingEngine = matchingEngine;
        this.findMatches();
    }

    private void findMatches() {
        this.matchingEngine.beginMatch();
        if (this.matchingEngine.hasMatched()) {
            this.matchingEngine.createMatchResult();
        }
        this.matches = Lists.mutable.ofAll(this.matchingEngine.getMatches());
    }

    @Override
    public boolean hasNext() {
        if (matches.isEmpty()) return false;
        return cursor != matches.size();
    }

    @Override
    public PureBigraphParametricMatch next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return matches.get(cursor++);
    }
}
