package de.tudresden.inf.st.bigraphs.core;

public abstract class ReactionRule<S extends Signature> implements Bigraph<S>{

    //TODO Oder nur validae RR zulassen bei creation
    protected abstract boolean isValid();
}
