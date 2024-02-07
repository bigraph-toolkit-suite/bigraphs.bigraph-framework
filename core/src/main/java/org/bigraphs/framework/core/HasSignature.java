package org.bigraphs.framework.core;

/**
 * @param <S> type of the signature
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
