package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.ReactionRuleException;
import de.tudresden.inf.st.bigraphs.core.exceptions.SignatureNotFoundException;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;

/**
 * Represents an immutable data structure for all kinds of reaction rules.
 * <p>
 * The signature is inferred from the redex (since both redex and reactum must have the same signature,
 * otherwise an exception is thrown when instantiating a concrete reaction rule.
 *
 * @param <S> type of the signature
 */
public abstract class AbstractReactionRule<S extends Signature> implements ReactionRule<S> {
    protected S signature;
    protected Bigraph<S> redex;
    protected Bigraph<S> reactum;
    protected boolean canReverse = false;

    public AbstractReactionRule(Bigraph<S> redex, Bigraph<S> reactum) throws ReactionRuleException {
        this.redex = redex;
        this.reactum = reactum;
        assertSignaturesAreSame(this.redex.getSignature(), this.reactum.getSignature());
        this.signature = this.redex.getSignature();
    }

    protected void assertSignaturesAreSame(S signature1, S signature2) throws IncompatibleSignatureException {
        if (!signature1.equals(signature2)) {
            throw new IncompatibleSignatureException();
        }
    }

    protected boolean assertNoInvalidReactionRule() {
        //check same interface
        return true;
    }

    @Override
    public boolean canReverse() {
        return canReverse;
    }

    //    protected boolean assertNoSitesBelowRoot() {
//        for (BigraphEntity eachRoot : getRoots()) {
//            for (BigraphEntity each : getChildrenWithSites(eachRoot)) {
//                if (isSite(each.getInstance())) return false;
//            }
//        }
//        //size of outernames must match
//        //bigraph.getOuterNames().size()
//        return true;
//    }

    @Override
    public Bigraph<S> getRedex() {
        return redex;
    }

    @Override
    public Bigraph<S> getReactum() {
        return reactum;
    }

    @Override
    public S getSignature() {
        return signature;
    }
}
