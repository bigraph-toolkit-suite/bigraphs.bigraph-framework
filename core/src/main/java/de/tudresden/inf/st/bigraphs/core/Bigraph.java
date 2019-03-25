package de.tudresden.inf.st.bigraphs.core;


import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import org.eclipse.emf.ecore.EPackage;

import java.util.Collection;

public interface Bigraph<S extends Signature> extends BigraphicalStructure<S> {
    /**
     * Get the respective signature of the current bigraph
     *
     * @return the signature of the bigraph
     */
    S getSignature();

    Collection<BigraphEntity.RootEntity> getRoots();

    Collection<BigraphEntity.SiteEntity> getSites();

    Collection<BigraphEntity.OuterName> getOuterNames();

    Collection<BigraphEntity.InnerName> getInnerNames();

    Collection<BigraphEntity.Edge> getEdges();

    <C extends Control> Collection<BigraphEntity.NodeEntity<C>> getNodes();

    BigraphEntity getParent(BigraphEntity node);
    //without sites
    Collection<BigraphEntity> getChildrenOf(BigraphEntity node);

    default boolean isGround() {
        return getInnerNames().size() == 0 && getSites().size() == 0;
    }

    default boolean isPrime() {
        return getRoots().size() == 1;
    }

    default boolean isDiscrete() {
        return false;
    }

    boolean areConnected(BigraphEntity.NodeEntity place1, BigraphEntity.NodeEntity place2);

    EPackage getModelPackage();
}
