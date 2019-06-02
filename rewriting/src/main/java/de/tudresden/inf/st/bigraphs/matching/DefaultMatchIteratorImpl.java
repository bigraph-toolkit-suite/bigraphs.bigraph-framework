package de.tudresden.inf.st.bigraphs.matching;

import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;

import java.util.*;

//TODO: in a future version: find matches incrementally and not all at the start (if possible)
//  next() and hasNext() must be rewritten then
//  see also comment in BigraphMatchingEngine
public class DefaultMatchIteratorImpl implements Iterator<Match> {
    private int cursor = 0;
    private List<Match<PureBigraph>> matches = new ArrayList<>();

    private BigraphMatchingEngine<PureBigraph> matchingEngine;
//    private BigraphMatcher bigraphMatcher;
//    private Match nextMatch;

    DefaultMatchIteratorImpl(BigraphMatchingEngine<? extends PureBigraph> matchingEngine) throws IncompatibleSignatureException {
        this.matchingEngine = (BigraphMatchingEngine<PureBigraph>) matchingEngine;
        findMatches();
    }

    private void findMatches() {
        this.matchingEngine.beginMatch();
        matches = Collections.unmodifiableList(this.matchingEngine.getMatches());
    }

    @Override
    public boolean hasNext() {
        boolean tmp = cursor != matches.size();
//        if (tmp) {
//            cursor++;
//        }
        return tmp; // && nextMatch != null;
    }

    @Override
    public Match next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return matches.get(cursor++); //new MatchIterable(this);
    }
}
