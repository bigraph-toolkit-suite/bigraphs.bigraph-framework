package de.tudresden.inf.st.bigraphs.matching;

import java.util.Iterator;

/**
 * Custom "collection" impl for matches
 * return the appropriate iterator
 */
public class MatchIterable implements Iterable<Match> {

    private DefaultMatchIteratorImpl iterator;

    public MatchIterable(DefaultMatchIteratorImpl iterator) {
        this.iterator = iterator;
    }

    @Override
    public Iterator<Match> iterator() {
        return iterator;
    }

//    @Override
//    public MatchIteratorImpl iterator() {
//        return this.iterator;
//    }
}
