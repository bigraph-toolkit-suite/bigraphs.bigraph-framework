package de.tudresden.inf.st.bigraphs.rewriting.matching.pure;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.ElementaryBigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.PureBigraphComposite;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;
import de.tudresden.inf.st.bigraphs.rewriting.matching.BigraphMatch;

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
    private Bigraph<DefaultDynamicSignature> contextIdentity;

    public PureBigraphParametricMatch(PureBigraph context, PureBigraph redex,
                                      Collection<Bigraph> parameters, ElementaryBigraph identity, Bigraph<DefaultDynamicSignature> contextIdentity) {
        this.parameters = parameters;
        this.context = context;
        this.identity = identity;
        this.redex = redex;
        this.contextIdentity = contextIdentity;
        PureBigraphComposite<DefaultDynamicSignature> redexComposite = AbstractBigraphFactory.createPureBigraphFactory().asBigraphOperator(this.redex);
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
    public Bigraph<DefaultDynamicSignature> getContextIdentity() {
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