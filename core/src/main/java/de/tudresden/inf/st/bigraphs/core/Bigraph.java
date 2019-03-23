package de.tudresden.inf.st.bigraphs.core;


import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.*;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.*;

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

    <C extends Control> Set<BigraphEntity.NodeEntity<C>> getNodes();
    //without sites
    Set<BigraphEntity> getChildrenOf(BigraphEntity node);

    default boolean isGround() {
        return getInnerNames().size() == 0 && getSites().size() == 0;
    }

    default boolean isPrime() {
        return false;
    }

    default boolean isDiscrete() {
        return false;
    }

    boolean areConnected(BigraphEntity.NodeEntity place1, BigraphEntity.NodeEntity place2);
}
