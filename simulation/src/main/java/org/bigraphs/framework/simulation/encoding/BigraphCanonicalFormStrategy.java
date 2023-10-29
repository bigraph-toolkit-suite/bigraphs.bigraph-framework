package org.bigraphs.framework.simulation.encoding;

import org.bigraphs.framework.core.Bigraph;

/**
 * @author Dominik Grzelak
 */
public abstract class BigraphCanonicalFormStrategy<B extends Bigraph<?>> {

    private final BigraphCanonicalForm bigraphCanonicalForm;
    boolean printNodeIdentifiers = false;
    boolean rewriteOpenLinks = false;

    public BigraphCanonicalFormStrategy(BigraphCanonicalForm bigraphCanonicalForm) {
        this.bigraphCanonicalForm = bigraphCanonicalForm;
    }

    public boolean isPrintNodeIdentifiers() {
        return printNodeIdentifiers;
    }

    public BigraphCanonicalFormStrategy<B> setPrintNodeIdentifiers(boolean printNodeIdentifiers) {
        this.printNodeIdentifiers = printNodeIdentifiers;
        return this;
    }

    public boolean isRewriteOpenLinks() {
        return rewriteOpenLinks;
    }

    public BigraphCanonicalFormStrategy<B> setRewriteOpenLinks(boolean rewriteOpenLinks) {
        this.rewriteOpenLinks = rewriteOpenLinks;
        return this;
    }

    public abstract String compute(B bigraph);

    public BigraphCanonicalForm getBigraphCanonicalForm() {
        return bigraphCanonicalForm;
    }
}
