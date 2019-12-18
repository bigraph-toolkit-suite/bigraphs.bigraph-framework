package de.tudresden.inf.st.bigraphs.rewriting.matching.pure;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.ElementaryBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.rewriting.matching.BigraphMatch;
import de.tudresden.inf.st.bigraphs.rewriting.reactivesystem.AbstractSimpleReactiveSystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Class represents the result of a match. Contains all necessary elements to perform the rewriting step later.
 *
 * @author Dominik Grzelak
 * @see AbstractSimpleReactiveSystem
 */
public class PureBigraphParametricMatch implements BigraphMatch<PureBigraph> {

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
