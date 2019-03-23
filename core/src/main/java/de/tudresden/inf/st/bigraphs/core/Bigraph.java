package de.tudresden.inf.st.bigraphs.core;


import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.*;
import org.eclipse.emf.ecore.EObject;

import java.util.Collection;
import java.util.Set;

//TODO think about the return types ...
//TODO: add isGround()
public interface Bigraph<S extends Signature> extends BigraphicalStructure<S> {
    /**
     * Get the respective signature of the current bigraph
     *
     * @return the signature of the bigraph
     */
    S getSignature();

    Set<BigraphEntity.RootEntity> getRoots();

    Set<BigraphEntity.SiteEntity> getSites();

    Set<BigraphEntity.OuterName> getOuterNames();

    Set<BigraphEntity.InnerName> getInnerNames();

    Set<BigraphEntity.Edge> getEdges();

    Set<BigraphEntity.NodeEntity> getNodes();

    default boolean isGround() {
        return getInnerNames().size() == 0 && getSites().size() == 0;
    }

    default boolean isPrime() {
        return false;
    }

    default boolean isDiscrete() {
        return false;
    }

    <T extends EObject> boolean areConnected(T place1, T place2);
}
