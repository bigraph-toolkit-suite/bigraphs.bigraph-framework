package de.tudresden.inf.st.bigraphs.matching;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.DynamicEcoreBigraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class DefaultParametricMatch implements Match<DynamicEcoreBigraph> {

    //TODO add bigraphbuilder/factory for composition
    private Collection<Bigraph> parameters = new ArrayList<>();
    private DynamicEcoreBigraph context;
    private DynamicEcoreBigraph identity;
    private DynamicEcoreBigraph redex;

    public DefaultParametricMatch(DynamicEcoreBigraph context, DynamicEcoreBigraph redex,
                                  Collection<Bigraph> parameters, DynamicEcoreBigraph identity) {
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
    public DynamicEcoreBigraph getContext() {
        return context;
    }

    @Override
    public DynamicEcoreBigraph getRedex() {
        return redex;
    }

    @Override
    public DynamicEcoreBigraph getRedexImage() {
        return null; //TODO make bigraph composition here
    }

    @Override
    public DynamicEcoreBigraph getRedexIdentity() {
        return identity;
    }
}
