package de.tudresden.inf.st.bigraphs.core;

public interface HasSignature<S extends Signature> {
    /**
     * Get the corresponding signature of the underlying bigraph
     *
     * @return the signature of the bigraph
     */
    S getSignature();
}
