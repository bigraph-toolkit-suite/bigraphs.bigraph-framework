package de.tudresden.inf.st.bigraphs.core;

public interface BigraphicalStructure<S extends Signature> {
    /**
     * Get the respective signature of the current bigraph
     *
     * @return the signature of the bigraph
     */
    S getSignature();
}
