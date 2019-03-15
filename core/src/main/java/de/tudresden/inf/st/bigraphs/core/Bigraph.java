package de.tudresden.inf.st.bigraphs.core;


import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.*;
import org.eclipse.emf.ecore.EObject;

import java.util.Collection;

//TODO think about the return types ...
//TODO: add isGround()
public interface Bigraph<S extends Signature> {
    /**
     * Get the respective signature of the current bigraph
     *
     * @return the signature of the bigraph
     */
    S getSignature();

    Iterable<BigraphEntity.RootEntity> getRoots();

    Iterable<BigraphEntity.SiteEntity> getSites();

    Iterable<BigraphEntity.OuterName> getOuterNames();

    Iterable<BigraphEntity.InnerName> getInnerNames();

    Iterable<BigraphEntity.Edge> getEdges();

    Iterable<BigraphEntity.NodeEntity> getNodes();

    boolean isGround();

    <T extends EObject> boolean areConnected(T place1, T place2);
}
