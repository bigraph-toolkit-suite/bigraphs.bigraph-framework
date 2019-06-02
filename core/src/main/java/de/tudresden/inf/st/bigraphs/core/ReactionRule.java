package de.tudresden.inf.st.bigraphs.core;

public interface ReactionRule<S extends Signature> extends HasSignature<S> {
    Bigraph<S> getRedex();

    Bigraph<S> getReactum();

    boolean canReverse();

    InstantiationMap getInstantationMap();
}
