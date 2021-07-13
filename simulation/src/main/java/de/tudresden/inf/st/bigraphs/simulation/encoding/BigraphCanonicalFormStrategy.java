package de.tudresden.inf.st.bigraphs.simulation.encoding;

import de.tudresden.inf.st.bigraphs.core.Bigraph;

/**
 * @author Dominik Grzelak
 */
public abstract class BigraphCanonicalFormStrategy<B extends Bigraph<?>> {

    private final BigraphCanonicalForm bigraphCanonicalForm;
    boolean printNodeIdentifiers = false;

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

    public abstract String compute(B bigraph);

    public BigraphCanonicalForm getBigraphCanonicalForm() {
        return bigraphCanonicalForm;
    }
}
