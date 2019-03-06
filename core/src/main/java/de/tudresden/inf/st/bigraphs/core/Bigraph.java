package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.model.BigraphBaseModel.*;

//TODO think about the return types ...
public interface Bigraph<S extends Signature> {
    /**
     * Get the respective signature of the current bigraph
     *
     * @return the signature of the bigraph
     */
    S getSignature();

    Iterable<? extends BRoot> getRoots();

    Iterable<? extends BSite> getSites();

    Iterable<? extends BOuterName> getOuterNames();

    Iterable<? extends BInnerName> getInnerNames();

    <T extends BNode> boolean areConnected(T place1, T place2);
}
