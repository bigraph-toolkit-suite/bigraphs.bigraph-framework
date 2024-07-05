package org.bigraphs.framework.core.reactivesystem;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.exceptions.*;
import org.bigraphs.framework.core.exceptions.*;

/**
 * This base class represents an immutable data structure for all kinds of reaction rules.
 * It contains the <i>redex</i>, <i>reactum</i> and an <i>instantiation map</i>.
 * <p>
 * The signature is inferred from the redex since both redex and reactum must have the same signature,
 * otherwise an exception is thrown when instantiating a concrete reaction rule.
 * <p>
 * Other checks are also performed checking the reaction rule for conformity: Interfaces and "simpleness".
 * <p>
 * A rule is said to be linear when they do not duplicate or delete parameters.
 *
 * @param <B> type of the bigraph
 * @author Dominik Grzelak
 */
public abstract class AbstractReactionRule<B extends Bigraph<? extends Signature<?>>> implements ReactionRule<B> {
    protected final Signature<?> signature;
    protected final B redex;
    protected final B reactum;
    protected boolean canReverse;
    protected InstantiationMap instantiationMap;
    protected ReactiveSystem<B> reactiveSystemAffili = null; // the "affiliation" to a specific reactive system
    protected String label;
    protected long priority = 0;

    protected TrackingMap trackingMap;

    public ReactiveSystemBoundReactionRule<B> withReactiveSystem(ReactiveSystem<B> reactiveSystem) {
        try {
            return new ReactiveSystemBoundReactionRule<>(reactiveSystem, AbstractReactionRule.this);
        } catch (InvalidReactionRuleException e) {
            return null;
        }
    }

    @Override
    public String getLabel() {
        return label;
    }

    public <T extends AbstractReactionRule<B>> T withLabel(String label) {
        this.label = label;
        return (T) this;
    }

    @Override
    public long getPriority() {
        return priority;
    }

    public <T extends AbstractReactionRule<B>> T withPriority(long priority) {
        this.priority = priority;
        return (T) this;
    }

    public AbstractReactionRule(final B redex, final B reactum) throws InvalidReactionRuleException {
        this(redex, reactum, InstantiationMap.create(redex.getSites().size()));
    }

    public AbstractReactionRule(final B redex, final B reactum, final InstantiationMap instantiationMap) throws InvalidReactionRuleException {
        this(redex, reactum, instantiationMap, false);
    }

    public AbstractReactionRule(final B redex, final B reactum, boolean isReversible) throws InvalidReactionRuleException {
        this(redex, reactum, InstantiationMap.create(redex.getSites().size()), isReversible);
    }

    public AbstractReactionRule(final B redex, final B reactum, final InstantiationMap instantiationMap, boolean isReversible) throws InvalidReactionRuleException {
        this.redex = redex;
        this.reactum = reactum;
        this.instantiationMap = instantiationMap;
        this.assertSignaturesAreSame(this.redex.getSignature(), this.reactum.getSignature());
        this.assertInterfaceDefinitionIsCorrect(this.redex, this.reactum);
        this.assertRedexIsSimple();
        if (isParametricRule()) {
            this.assertIsProperParametricRule();
            this.assertInstantiationMapIsWellDefined();
        }
        this.signature = this.redex.getSignature();
        this.canReverse = isReversible;
    }

    public AbstractReactionRule(ReactiveSystem delegate, AbstractReactionRule rule) throws InvalidReactionRuleException {
        this((B) rule.redex, (B) rule.reactum, rule.instantiationMap, rule.canReverse);
        this.reactiveSystemAffili = delegate;
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
    }

    protected void assertRedexIsSimple() throws RedexIsNotSimpleException {
        if (!isRedexSimple()) {
            throw new RedexIsNotSimpleException();
        }
    }

    protected void assertIsProperParametricRule() throws ParametricReactionRuleIsNotWellDefined {
        if (!isProperParametricRule()) {
            throw new ParametricReactionRuleIsNotWellDefined();
        }
    }

    protected void assertInstantiationMapIsWellDefined() throws InstantiationMapIsNotWellDefined {
        if (getInstantationMap().domainSize() != getReactum().getSites().size() &&
                getInstantationMap().coDomainSize() != getRedex().getSites().size()) {
            throw new InstantiationMapIsNotWellDefined();
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
    public boolean isReversible() {
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

    public static class ReactiveSystemBoundReactionRule<B extends Bigraph<? extends Signature<?>>> extends AbstractReactionRule<B> {

        private final ReactiveSystem<B> delegate;
        private final AbstractReactionRule<B> rule;

        /**
         * @param delegate must not be {@literal null}.
         * @param rule     must not be {@literal null}.
         */
        ReactiveSystemBoundReactionRule(ReactiveSystem<B> delegate, AbstractReactionRule<B> rule) throws InvalidReactionRuleException {
            super(delegate, rule);
            this.delegate = delegate;
            this.rule = rule;
        }

        public ReactiveSystem<B> getBoundedReactiveSystem() {
            return delegate;
        }

        public AbstractReactionRule<B> getRule() {
            return rule;
        }
    }

    public AbstractReactionRule<B> withTrackingMap(TrackingMap trackingMap) {
        this.trackingMap = trackingMap;
        return this;
    }

    public AbstractReactionRule<B> withInstantiationMap(InstantiationMap instantiationMap) {
        this.instantiationMap = instantiationMap;
        return this;
    }

    @Override
    public TrackingMap getTrackingMap() {
        return this.trackingMap;
    }
}
