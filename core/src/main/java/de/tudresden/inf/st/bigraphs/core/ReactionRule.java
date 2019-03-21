package de.tudresden.inf.st.bigraphs.core;

public interface ReactionRule<S extends Signature> extends BigraphicalStructure<S> {
    Bigraph<S> getRedex();

    Bigraph<S> getReactum();
}
