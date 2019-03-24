package de.tudresden.inf.st.bigraphs.matching;

import java.util.Iterator;

/**
 * Custom "collection" impl for matches
 * return the appropriate iterator
 */
public class MatchIterable implements Iterable<Match> {

    @Override
    public Iterator<Match> iterator() {
        return new MatchIterator();
    }
}
