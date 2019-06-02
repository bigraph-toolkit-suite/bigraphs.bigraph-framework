package de.tudresden.inf.st.bigraphs.core;

/**
 * Common interface for reaction rules.
 * <p>
 * A reaction rule is a data structure containing the redex, reactum and an instantiation map.
 *
 * @param <S>
 */
public interface ReactionRule<S extends Signature> extends HasSignature<S> {
    Bigraph<S> getRedex();

    Bigraph<S> getReactum();

    boolean isReversable();

    InstantiationMap getInstantationMap();
}
