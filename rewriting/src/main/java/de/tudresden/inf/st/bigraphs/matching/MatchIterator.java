package de.tudresden.inf.st.bigraphs.matching;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//put the match engine here or provide one when instantiating
public class MatchIterator implements Iterator<Match> {

    List<Match> matches = new ArrayList<>();

    //Matching engine here


    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Match next() {
        return null;
    }
}
