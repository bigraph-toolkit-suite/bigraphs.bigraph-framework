package de.tudresden.inf.st.bigraphs.core.exceptions;

//TODO: node related...
public class ToManyConnections extends InvalidArityOfControlException {

    public ToManyConnections() {
        super("To many connections on this node");
    }
}
