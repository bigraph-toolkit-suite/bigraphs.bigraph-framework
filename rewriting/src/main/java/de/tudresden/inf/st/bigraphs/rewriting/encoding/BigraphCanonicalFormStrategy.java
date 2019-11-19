package de.tudresden.inf.st.bigraphs.rewriting.encoding;

import de.tudresden.inf.st.bigraphs.core.Bigraph;

/**
 * @author Dominik Grzelak
 */
public abstract class BigraphCanonicalFormStrategy<B extends Bigraph<?>> {

    private BigraphCanonicalForm bigraphCanonicalForm;

    public BigraphCanonicalFormStrategy(BigraphCanonicalForm bigraphCanonicalForm) {
        this.bigraphCanonicalForm = bigraphCanonicalForm;
    }

    public abstract String compute(B bigraph);

    public BigraphCanonicalForm getBigraphCanonicalForm() {
        return bigraphCanonicalForm;
    }
}
