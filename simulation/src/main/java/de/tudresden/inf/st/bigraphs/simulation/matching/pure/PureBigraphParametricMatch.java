package de.tudresden.inf.st.bigraphs.simulation.matching.pure;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.BigraphMatch;
import de.tudresden.inf.st.bigraphs.core.reactivesystem.AbstractSimpleReactiveSystem;
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

    private final PureBigraph context;
    private final PureBigraph redex;
    private final PureBigraph redexImage;
    private final Collection<PureBigraph> parameters;
    private final Bigraph<DefaultDynamicSignature> redexIdentity;
    private final Bigraph<DefaultDynamicSignature> contextIdentity;
    private PureBigraph paramWiring;

    private final it.uniud.mads.jlibbig.core.std.Match jLibMatchResult;

    public PureBigraphParametricMatch(it.uniud.mads.jlibbig.core.std.Match jLibMatchResult,
                                      PureBigraph context,
                                      PureBigraph redex,
                                      PureBigraph redexImage,
                                      Bigraph<DefaultDynamicSignature> redexIdentity,
                                      PureBigraph paramWiring,
                                      Collection<PureBigraph> parameters) {
        this.jLibMatchResult = jLibMatchResult;
        this.contextIdentity = null;
        this.redexIdentity = redexIdentity;
        this.redex = redex;
        this.context = context;
        this.redexImage = redexImage;
        this.paramWiring = paramWiring;
        this.parameters = parameters;
    }


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
    public PureBigraph getContext() {
        return context;
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

    @Override
    public Bigraph<DefaultDynamicSignature> getRedexIdentity() {
        return redexIdentity;
    }
}
