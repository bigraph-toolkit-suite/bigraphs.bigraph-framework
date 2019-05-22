package de.tudresden.inf.st.bigraphs.core;


import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import org.eclipse.emf.ecore.EPackage;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface Bigraph<S extends Signature> extends HasSignature<S> {
    /**
     * Get the respective signature of the current bigraph
     *
     * @return the signature of the bigraph
     */
    S getSignature();

    default Map.Entry<Set<FiniteOrdinal<Integer>>, Set<StringTypedName>> getInnerFace() {
        return new AbstractMap.SimpleImmutableEntry<>(
                getSites().stream().map(x -> FiniteOrdinal.ofInteger(x.getIndex())).collect(Collectors.toSet()),
                getInnerNames().stream().map(x -> StringTypedName.of(x.getName())).collect(Collectors.toSet())
        );
    }

    default Map.Entry<Set<FiniteOrdinal<Integer>>, Set<StringTypedName>> getOuterFace() {
        return new AbstractMap.SimpleImmutableEntry<>(
                getRoots().stream().map(x -> FiniteOrdinal.ofInteger(x.getIndex())).collect(Collectors.toSet()),
                getOuterNames().stream().map(x -> StringTypedName.of(x.getName())).collect(Collectors.toSet())
        );
    }

    /**
     * Prime bigraph has only one root and no inner names.
     *
     * @return true, if the bigraph is prime, otherwise false.
     */
    default boolean isPrime() {
        return getRoots().size() == 1 && getInnerNames().size() == 0;
    }

    //TODO
    default boolean isDiscrete() {
        return false;
    }

    Collection<BigraphEntity.RootEntity> getRoots();

    Collection<BigraphEntity.SiteEntity> getSites();

    Collection<BigraphEntity.OuterName> getOuterNames();

    Collection<BigraphEntity.InnerName> getInnerNames();

    /**
     * Returns all places of the bigraph, i.e., roots, nodes and sites.
     *
     * @return all places of the bigraph
     */
    Collection<BigraphEntity> getAllPlaces();

    Collection<BigraphEntity.Edge> getEdges();

    <C extends Control> Collection<BigraphEntity.NodeEntity<C>> getNodes();

    Collection<BigraphEntity> getChildrenOf(BigraphEntity node);

    default boolean isGround() {
        return getInnerNames().size() == 0 && getSites().size() == 0;
    }

    /**
     * Get the parent of a bigraph's place. Passing a root as argument will
     * always return {@code null}.
     *
     * @param node a place of this bigraph
     * @return the parent of the given place, or {@code null}
     */
    BigraphEntity getParent(BigraphEntity node);

    BigraphEntity getLink(BigraphEntity node);

    Collection<BigraphEntity.Port> getPorts(BigraphEntity node);

    <C extends Control> BigraphEntity.NodeEntity<C> getNodeOfPort(BigraphEntity.Port port);

    /**
     * get all point entities (i.e., ports and inner names) from a link entity (edges and outer names)
     *
     * @param linkEntity
     * @return
     */
    Collection<BigraphEntity> getPointsFromLink(BigraphEntity linkEntity);

    boolean areConnected(BigraphEntity.NodeEntity place1, BigraphEntity.NodeEntity place2);

    EPackage getModelPackage();
}
