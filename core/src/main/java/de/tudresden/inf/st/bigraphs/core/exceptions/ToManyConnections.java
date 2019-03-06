package de.tudresden.inf.st.bigraphs.core.exceptions;

//TODO: node related...
public class ToManyConnections extends ArityMismatch {

    public ToManyConnections() {
        super("To many connections on this node");
    }
}
