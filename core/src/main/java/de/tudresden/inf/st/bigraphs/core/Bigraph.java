package de.tudresden.inf.st.bigraphs.core;


import de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.*;
import org.eclipse.emf.ecore.EObject;

//TODO think about the return types ...
public interface Bigraph<S extends Signature> {
    /**
     * Get the respective signature of the current bigraph
     *
     * @return the signature of the bigraph
     */
    S getSignature();

    Iterable<? extends EObject> getRoots();

    Iterable<? extends EObject> getSites();

    Iterable<? extends EObject> getOuterNames();

    Iterable<? extends EObject> getInnerNames();

    <T extends EObject> boolean areConnected(T place1, T place2);
}
