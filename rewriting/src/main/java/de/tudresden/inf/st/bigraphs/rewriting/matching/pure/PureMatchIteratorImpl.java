package de.tudresden.inf.st.bigraphs.rewriting.matching.pure;

import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;
import de.tudresden.inf.st.bigraphs.rewriting.matching.BigraphMatch;

import java.util.*;

//TODO: in a future version: find matches incrementally and not all at the start (if possible)
//  next() and hasNext() must be rewritten then
//  see also comment in PureBigraphMatchingEngine
public class PureMatchIteratorImpl implements Iterator<BigraphMatch<?>> {
    private int cursor = 0;
    private List<BigraphMatch<PureBigraph>> matches = new ArrayList<>();

    private PureBigraphMatchingEngine<PureBigraph> matchingEngine;
//    private PureBigraphMatcher bigraphMatcher;
//    private BigraphMatch nextMatch;

    PureMatchIteratorImpl(PureBigraphMatchingEngine<? extends PureBigraph> matchingEngine) throws IncompatibleSignatureException {
        this.matchingEngine = (PureBigraphMatchingEngine<PureBigraph>) matchingEngine;
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
    public BigraphMatch<PureBigraph> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return matches.get(cursor++); //new MatchIterable(this);
    }
}
