package de.tudresden.inf.st.bigraphs.rewriting.matching;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.ElementaryBigraph;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultBigraphComposite;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class PureBigraphParametricMatch implements BigraphMatch<PureBigraph> {

    //TODO add bigraphbuilder/factory for composition
    private Collection<Bigraph> parameters = new ArrayList<>();
    private PureBigraph context;
    private ElementaryBigraph identity;
    private PureBigraph redex;
    private PureBigraph redexImage;
    private PureBigraph contextIdentity;

    public PureBigraphParametricMatch(PureBigraph context, PureBigraph redex,
                                      Collection<Bigraph> parameters, ElementaryBigraph identity, PureBigraph contextIdentity) {
        this.parameters = parameters;
        this.context = context;
        this.identity = identity;
        this.redex = redex;
        this.contextIdentity = contextIdentity;
        DefaultBigraphComposite<DefaultDynamicSignature> redexComposite = AbstractBigraphFactory.createPureBigraphFactory().asBigraphOperator(this.redex);
        //TODO: calculate redexImage
    }

    @Override
    public Collection getParameters() {
        return Collections.unmodifiableCollection(parameters);
    }

    @Override
    public PureBigraph getContext() {
        return context;
    }

    @Override
    public PureBigraph getContextIdentity() {
        return contextIdentity;
    }

    @Override
    public PureBigraph getRedex() {
        return redex;
    }

    @Override
    public PureBigraph getRedexImage() {
        return redexImage;
    }

    @Override
    public ElementaryBigraph getRedexIdentity() {
        return identity;
    }
}
