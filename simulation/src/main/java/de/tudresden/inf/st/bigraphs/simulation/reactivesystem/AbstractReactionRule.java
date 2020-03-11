package de.tudresden.inf.st.bigraphs.simulation.reactivesystem;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.exceptions.*;
import de.tudresden.inf.st.bigraphs.simulation.ReactionRule;

/**
 * This base class represents an immutable data structure for all kinds of reaction rules.
 * It contains the redex, reactum and an instantiation map.
 * <p>
 * The signature is inferred from the redex since both redex and reactum must have the same signature,
 * otherwise an exception is thrown when instantiating a concrete reaction rule.
 * <p>
 * Other checks are also performed checking the reaction rule for conformity: Interfaces and "simpleness".
 * <p>
 * A rule is said to be linear when they do not copy or delete parameters.
 *
 * @param <B> type of the bigraph
 * @author Dominik Grzelak
 */
public abstract class AbstractReactionRule<B extends Bigraph<? extends Signature<?>>> implements ReactionRule<B> {
    protected final Signature<?> signature;
    protected final B redex;
    protected final B reactum;
    protected boolean canReverse = false;
    protected final InstantiationMap instantiationMap;

    public AbstractReactionRule(final B redex, final B reactum, final InstantiationMap instantiationMap) throws InvalidReactionRuleException {
        this.redex = redex;
        this.reactum = reactum;
        this.instantiationMap = instantiationMap;
        this.assertSignaturesAreSame(this.redex.getSignature(), this.reactum.getSignature());
        this.assertInterfaceDefinitionIsCorrect(this.redex, this.reactum);
        this.assertRedexIsSimple();
        this.signature = this.redex.getSignature();
    }


    public AbstractReactionRule(final B redex, final B reactum) throws InvalidReactionRuleException {
        this(redex, reactum, InstantiationMap.create(redex.getSites().size()));
    }

    /**
     * Check if the interfaces of the redex and reactum conform to the following form: <br>
     * {@literal R = (R, R'), R: m -> J and R': m' -> J}.
     */
    private void assertInterfaceDefinitionIsCorrect(B redex, B reactum) throws NonConformReactionRuleInterfaces {
        // check if same interfaces
        if (!redex.getOuterFace().equals(reactum.getOuterFace())) {
            throw new NonConformReactionRuleInterfaces();
        }
//        if (redex.getSites().size() != reactum.getSites().size()) {
//            throw new NonConformReactionRuleInterfaces();
//        }
    }

    protected void assertRedexIsSimple() throws RedexIsNotSimpleException {
        if (!isRedexSimple()) {
            throw new RedexIsNotSimpleException();
        }
    }

    private void assertSignaturesAreSame(Signature signature1, Signature signature2) throws IncompatibleSignatureException {
        if (!signature1.equals(signature2)) {
            throw new IncompatibleSignatureException();
        }
    }

    @Override
    public InstantiationMap getInstantationMap() {
        return this.instantiationMap;
    }

    @Override
    public boolean isReversable() {
        return this.canReverse;
    }


    @Override
    public B getRedex() {
        return this.redex;
    }

    @Override
    public B getReactum() {
        return this.reactum;
    }

    public Signature<?> getSignature() {
        return this.signature;
    }
}
