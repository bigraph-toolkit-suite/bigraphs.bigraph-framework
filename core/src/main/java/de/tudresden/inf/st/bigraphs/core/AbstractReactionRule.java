package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.exceptions.*;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;

import java.util.Collection;

/**
 * This base class represents an immutable data structure for all kinds of reaction rules.
 * It contains the redex, reactum and an instantiation map.
 * <p>
 * The signature is inferred from the redex since both redex and reactum must have the same signature,
 * otherwise an exception is thrown when instantiating a concrete reaction rule.
 * <p>
 * Other checks are also performed checking the reaction rule for conformity: Interfaces and "simpleness".
 *
 * @param <S> type of the signature
 */
public abstract class AbstractReactionRule<S extends Signature> implements ReactionRule<S> {
    protected final S signature;
    protected final Bigraph<S> redex;
    protected final Bigraph<S> reactum;
    protected boolean canReverse = false;
    protected final InstantiationMap instantiationMap;

    public AbstractReactionRule(final Bigraph<S> redex, final Bigraph<S> reactum, final InstantiationMap instantiationMap) throws InvalidReactionRuleException {
        this.redex = redex;
        this.reactum = reactum;
        this.instantiationMap = instantiationMap;
        assertSignaturesAreSame(this.redex.getSignature(), this.reactum.getSignature());
        assertInterfaceDefintionIsCorrect(redex, reactum);
        assertParametricRedexIsSimple(this.redex);
        assertNoIdleOuterName(this.redex);
        this.signature = this.redex.getSignature();
    }


    public AbstractReactionRule(final Bigraph<S> redex, final Bigraph<S> reactum) throws InvalidReactionRuleException {
        this(redex, reactum, InstantiationMap.create(redex.getRoots().size()));//TODO check if this is correct concerning the instantiation map
    }

    private void assertSignaturesAreSame(S signature1, S signature2) throws IncompatibleSignatureException {
        if (!signature1.equals(signature2)) {
            throw new IncompatibleSignatureException();
        }
    }

    /**
     * Throws an exception if an outer name of a given redex is idle (i.e., not connected to a point of the redex).
     *
     * @param redex the outer names of the redex to check
     * @throws InvalidReactionRuleException if outer name is idle.
     */
    private void assertNoIdleOuterName(Bigraph<S> redex) throws InvalidReactionRuleException {
        boolean isIdle = false;
        for (BigraphEntity.OuterName eachOuter : redex.getOuterNames()) {
            // check for idle links (bigraphER doesn't allows it either
            if (redex.getPointsFromLink(eachOuter).size() == 0) {
                isIdle = true;
                break;
            }
        }
        if (isIdle) {
            throw new OuterNameIsIdleException();
        }
    }

    /**
     * Checks if a parametric redex is <i>simple</i>. A parametric redex is simple if:
     * <ul>
     * <li>open: every link is open</li>
     * <li>guarding: no site has a root as parent</li>
     * <li>inner-injective: no two sites are siblings</li>
     * </ul>
     * (see Milner book Def. 8.12, pp. 95
     *
     * @param redex the redex of the reaction rule to be checked
     */
    private void assertParametricRedexIsSimple(Bigraph<S> redex) throws RedexIsNotSimpleException {
        //"openness": all links are interfaces (edges, and outer names)
        //TODO must not be severe constraint, as we can internally model edges as outer names (in the matching as currently done)
        boolean isOpen = redex.getInnerNames().size() == 0 && redex.getEdges().size() == 0;

        //guarding: assertNoSitesBelowRoot
        boolean isGuarding = redex.getRoots().stream().map(redex::getChildrenOf).flatMap(Collection::stream)
                .noneMatch(BigraphEntityType::isSite);

        boolean areNotSiblings = false;
        for (BigraphEntity.SiteEntity eachSite : redex.getSites()) {
            if ((int) redex.getSiblingsOfNode(eachSite).stream().filter(BigraphEntityType::isSite).count() > 0) {
                areNotSiblings = true;
                break;
            }
        }

        if (!isOpen && !isGuarding && !areNotSiblings) {
            throw new RedexIsNotSimpleException();
        }
    }

    /**
     * Check if the interfaces of the redex and reactum conforms to the following form: <br/>
     * <i>R = (R, R'), R: m -> J and R': m' -> J</i>
     */
    private void assertInterfaceDefintionIsCorrect(Bigraph<S> redex, Bigraph<S> reactum) throws NoConformReactionRuleInterfaces {
        //check same interface
        if (!redex.getOuterFace().equals(reactum.getOuterFace())) {
            throw new NoConformReactionRuleInterfaces();
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
    public Bigraph<S> getRedex() {
        return this.redex;
    }

    @Override
    public Bigraph<S> getReactum() {
        return this.reactum;
    }

    @Override
    public S getSignature() {
        return this.signature;
    }
}
