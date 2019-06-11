package de.tudresden.inf.st.bigraphs.rewriting.matching;

import java.util.Iterator;

/**
 * Custom iterable implementation for matches of type {@link BigraphMatch}.
 *
 * @author Dominik Grzelak
 */
public class MatchIterable implements Iterable<BigraphMatch<?>> {

    private Iterator<BigraphMatch<?>> iterator;

    public MatchIterable(Iterator<BigraphMatch<?>> iterator) {
        this.iterator = iterator;
    }

    @Override
    public Iterator<BigraphMatch<?>> iterator() {
        return iterator;
    }

}
