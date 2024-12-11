package org.bigraphs.framework.simulation.matching.pure;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.reactivesystem.BigraphMatch;
import org.bigraphs.framework.core.reactivesystem.AbstractSimpleReactiveSystem;
import it.uniud.mads.jlibbig.core.std.Match;

import java.util.Collection;
import java.util.Collections;

/**
 * This class represents the result of a valid match.
 * It contains all required elements to perform the rewriting step later.
 *
 * @author Dominik Grzelak
 * @see AbstractSimpleReactiveSystem
 */
public class PureBigraphParametricMatch implements BigraphMatch<PureBigraph> {

    private PureBigraph context;
    private PureBigraph redex;
    private PureBigraph redexImage;
    private final Collection<PureBigraph> parameters;
    private Bigraph<DefaultDynamicSignature> redexIdentity;
    private Bigraph<DefaultDynamicSignature> contextIdentity;
    private PureBigraph paramWiring;
    private PureBigraph params;
    private final it.uniud.mads.jlibbig.core.std.Match jLibMatchResult;

    public PureBigraphParametricMatch(it.uniud.mads.jlibbig.core.std.Match jLibMatchResult,
                                      PureBigraph context,
                                      PureBigraph redex,
                                      PureBigraph redexImage,
                                      Bigraph<DefaultDynamicSignature> redexIdentity,
                                      PureBigraph paramWiring,
                                      Collection<PureBigraph> parameters) {
        // Everything is null exceptjLibMatchResult and redex because we have jLibBig bigraph objects
        // Until the bigraph is rewritten with the match result, they will be null.
        this.jLibMatchResult = jLibMatchResult;
        this.contextIdentity = null;
        this.redexIdentity = redexIdentity;
        this.redex = redex;
        this.context = context;
        this.redexImage = redexImage;
        this.paramWiring = paramWiring;
        this.parameters = parameters;
        this.params = null;
    }

    @Deprecated
    public PureBigraphParametricMatch(PureBigraph context,
                                      PureBigraph redex,
                                      PureBigraph redexImage,
                                      Collection<PureBigraph> parameters,
                                      Bigraph<DefaultDynamicSignature> redexIdentity,
                                      Bigraph<DefaultDynamicSignature> contextIdentity) {
        this.jLibMatchResult = null;
        this.parameters = parameters;
        this.context = context;
        this.redexImage = redexImage;
        this.redexIdentity = redexIdentity;
        this.redex = redex;
        this.contextIdentity = contextIdentity;
    }

    public Match getJLibMatchResult() {
        return jLibMatchResult;
    }

    @Override
    public Collection getParameters() {
        return Collections.unmodifiableCollection(parameters);
    }

    @Override
    public PureBigraph getParam() {
        return params;
    }


    public void setParam(PureBigraph param) {
        params = param;
    }

    @Override
    public boolean wasRewritten() {
        return context != null && params != null && redexImage != null;
    }

    @Override
    public PureBigraph getContext() {
        return context;
    }

    public void setContext(PureBigraph context) {
        this.context = context;
    }

    /**
     * <b>Note:</b> The return type is of class {@link Bigraph} with a {@link DefaultDynamicSignature}. We cannot cast
     * it to a pure bigraph because it may also be an elementary bigraph (in the form of a pure bigraph type)
     *
     * @return the identity link graph
     */
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

    public void setRedexImage(PureBigraph redexImage) {
        this.redexImage = redexImage;
    }

    @Override
    public Bigraph<DefaultDynamicSignature> getRedexIdentity() {
        return redexIdentity;
    }
}
