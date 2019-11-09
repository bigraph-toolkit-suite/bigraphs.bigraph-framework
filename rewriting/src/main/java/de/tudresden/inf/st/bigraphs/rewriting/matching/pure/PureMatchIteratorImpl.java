package de.tudresden.inf.st.bigraphs.rewriting.matching.pure;

import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.rewriting.matching.BigraphMatch;

import java.util.*;

//TODO: in a future version: find matches incrementally and not all at the start (if possible)
//  next() and hasNext() must be rewritten then
//  see also comment in PureBigraphMatchingEngine

/**
 * Iterator implementation for bigraph matching.
 * <p>
 * This iterator is created by the {@link PureBigraphMatcher} class.
 *
 * @param <B> type of the bigraph
 * @author Dominik Grzelak
 */
public class PureMatchIteratorImpl<B extends PureBigraph> implements Iterator<BigraphMatch<B>> {
    private int cursor = 0;
    private List<BigraphMatch<B>> matches = new ArrayList<>();

    private PureBigraphMatchingEngine<B> matchingEngine;

    PureMatchIteratorImpl(PureBigraphMatchingEngine<B> matchingEngine) {
        this.matchingEngine = matchingEngine;
        this.findMatches();
    }

    private void findMatches() {
        this.matchingEngine.beginMatch();
//        BigraphMatch[] bigraphMatches = this.matchingEngine.getMatches().toArray(new BigraphMatch[0]);
//        this.matches = Collections.unmodifiableList(Arrays.<BigraphMatch<B>>asList(bigraphMatches));
        if (this.matchingEngine.hasMatched()) {
            this.matchingEngine.createMatchResult();
        }
        this.matches = Collections.<BigraphMatch<B>>unmodifiableList(this.matchingEngine.getMatches());
    }

    @Override
    public boolean hasNext() {
        if (matches.size() == 0) return false;
        boolean tmp = cursor != matches.size();
//        if (tmp) {
//            cursor++;
//        }
        return tmp; // && nextMatch != null;
    }

    @Override
    public BigraphMatch<B> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return matches.get(cursor++);
    }
}
