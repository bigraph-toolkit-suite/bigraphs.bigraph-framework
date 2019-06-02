package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.exceptions.*;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Represents an immutable data structure for all kinds of reaction rules.
 * <p>
 * The signature is inferred from the redex (since both redex and reactum must have the same signature,
 * otherwise an exception is thrown when instantiating a concrete reaction rule.
 *
 * @param <S> type of the signature
 */
public abstract class AbstractReactionRule<S extends Signature> implements ReactionRule<S> {
    protected final S signature;
    protected final Bigraph<S> redex;
    protected final Bigraph<S> reactum;
    protected boolean canReverse = false;
    protected final InstantiationMap instantiationMap;

    public AbstractReactionRule(final Bigraph<S> redex, final Bigraph<S> reactum, final InstantiationMap instantiationMap) throws ReactionRuleException {
        this.redex = redex;
        this.reactum = reactum;
        this.instantiationMap = instantiationMap;
        assertSignaturesAreSame(this.redex.getSignature(), this.reactum.getSignature());
        assertInterfaceDefintionIsCorrect(redex, reactum);
        assertParametricRedexIsSimple(this.redex);
        this.signature = this.redex.getSignature();
    }


    public AbstractReactionRule(final Bigraph<S> redex, final Bigraph<S> reactum) throws ReactionRuleException {
        this(redex, reactum, InstantiationMap.create(redex.getRoots().size()));//TODO check if this is correct concerning the instantiation map
    }

    protected void assertSignaturesAreSame(S signature1, S signature2) throws IncompatibleSignatureException {
        if (!signature1.equals(signature2)) {
            throw new IncompatibleSignatureException();
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
    protected void assertParametricRedexIsSimple(Bigraph<S> redex) throws RedexIsNotSimpleException {
        //"openness": all links are interfaces (edges, and outer names)
        boolean isOpen = redex.getInnerNames().size() == 0 && redex.getEdges().size() == 0;
        //guarding: assertNoSitesBelowRoot
        boolean isGuarding = redex.getRoots().stream().map(redex::getChildrenOf).flatMap(Collection::stream)
                .noneMatch(BigraphEntityType::isSite);

        boolean areNotSiblings = false;
        for (BigraphEntity.SiteEntity eachSite : redex.getSites()) {
            if ((int) redex.getSiblings(eachSite).stream().filter(BigraphEntityType::isSite).count() > 0) {
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
    protected void assertInterfaceDefintionIsCorrect(Bigraph<S> redex, Bigraph<S> reactum) throws NoConformReactionRuleInterfaces {
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
    public boolean canReverse() {
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
