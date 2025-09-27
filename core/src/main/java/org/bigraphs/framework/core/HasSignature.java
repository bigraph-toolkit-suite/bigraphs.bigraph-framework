package org.bigraphs.framework.core;

/**
 * Interface for entities associated with a bigraph signature.
 *
 * @param <S> the signature type
 * @author Dominik Grzelak
 */
public interface HasSignature<S extends Signature<?>> {
    /**
     * Get the corresponding signature of the underlying bigraph
     *
     * @return the signature of the bigraph
     */
    S getSignature();
}
