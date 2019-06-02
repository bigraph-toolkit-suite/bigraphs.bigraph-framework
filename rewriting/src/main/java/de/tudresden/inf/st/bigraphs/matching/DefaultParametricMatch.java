package de.tudresden.inf.st.bigraphs.matching;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class DefaultParametricMatch implements Match<PureBigraph> {

    //TODO add bigraphbuilder/factory for composition
    private Collection<Bigraph> parameters = new ArrayList<>();
    private PureBigraph context;
    private PureBigraph identity;
    private PureBigraph redex;

    public DefaultParametricMatch(PureBigraph context, PureBigraph redex,
                                  Collection<Bigraph> parameters, PureBigraph identity) {
        this.parameters = parameters;
        this.context = context;
        this.identity = identity;
        this.redex = redex;
    }

    void assertCheckSites() {
        //TODO
    }

    @Override
    public Collection getParameter() {
        return Collections.unmodifiableCollection(parameters);
    }

    @Override
    public PureBigraph getContext() {
        return context;
    }

    @Override
    public PureBigraph getRedex() {
        return redex;
    }

    @Override
    public PureBigraph getRedexImage() {
        return null; //TODO make bigraph composition here
    }

    @Override
    public PureBigraph getRedexIdentity() {
        return identity;
    }
}
