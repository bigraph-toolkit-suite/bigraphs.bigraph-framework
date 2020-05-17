package de.tudresden.inf.st.bigraphs.simulation.matching.pure;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.ElementaryBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.simulation.matching.BigraphMatch;
import de.tudresden.inf.st.bigraphs.simulation.reactivesystem.AbstractSimpleReactiveSystem;

import java.util.ArrayList;
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
    private final Collection<Bigraph<DefaultDynamicSignature>> parameters;
    private final Bigraph<DefaultDynamicSignature> identity;
    private final Bigraph<DefaultDynamicSignature> contextIdentity;

    public PureBigraphParametricMatch(PureBigraph context,
                                      PureBigraph redex,
                                      PureBigraph redexImage,
                                      Collection<Bigraph<DefaultDynamicSignature>> parameters,
                                      Bigraph<DefaultDynamicSignature> identity,
                                      Bigraph<DefaultDynamicSignature> contextIdentity) {
        this.parameters = parameters;
        this.context = context;
        this.redexImage = redexImage;
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
        return identity;
    }
}
