package org.bigraphs.framework.simulation.matching;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.BigraphMatch;

import java.util.Iterator;

/**
 * Custom iterable implementation for matches of type {@link BigraphMatch}.
 *
 * @param <T> type of the bigraph within a {@link BigraphMatch} "container" holding the match result
 * @author Dominik Grzelak
 */
public class MatchIterable<T extends BigraphMatch<? extends Bigraph<?>>> implements Iterable<T> {

    private final Iterator<T> iterator;

    public MatchIterable(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    @Override
    public Iterator<T> iterator() {
        return iterator;
    }

}
