package de.tudresden.inf.st.bigraphs.core;


import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;
import org.eclipse.emf.ecore.EPackage;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generic bigraph interface of all bigraph entities in this framework (for all pure bigraphs)
 * <p>
 * Important direct implementations are:
 * <ul>
 * <li>{@link PureBigraph}</li>
 * <li>{@link BigraphDelegator}</li>
 * <li>{@link ElementaryBigraph}</li>
 * </ul>
 *
 * @param <S> type of the signature
 * @author Dominik Grzelak
 */
public interface Bigraph<S extends Signature> extends HasSignature<S> {

    S getSignature();

    /**
     * Checks, if the bigraph is ground, that is, whether the inner interface is empty (no sites and no inner names).
     *
     * @return {@code true}, if the bigraph is ground, otherwise {@code false}
     */
    default boolean isGround() {
        return getInnerNames().size() == 0 && getSites().size() == 0;
    }

    /**
     * Checks, if the bigraph is prime.
     * A Prime bigraph has only one root and no inner names.
     *
     * @return true, if the bigraph is prime, otherwise false.
     */
    default boolean isPrime() {
        return getRoots().size() == 1 && getInnerNames().size() == 0;
    }

    /**
     * A concrete bigraph is epimorphic iff its place graph has no idle root and its link graph has no idle outer names.
     *
     * @return {@code true} if the bigraph is epimorphic, otherwise {@code false}
     */
    default boolean isEpimorphic() {
        boolean placeGraphIsEpi = getRoots().stream().allMatch(x -> getChildrenOf(x).size() > 0);
        boolean linkGraphIsEpi = getOuterNames().stream().allMatch(x -> getPointsFromLink(x).size() > 0);
        return placeGraphIsEpi && linkGraphIsEpi;
    }

    /**
     * A concrete bigraph is monomorphic iff no two sites are siblings and no two inner names are siblings.
     * With other words, every edge has at most one inner name.
     *
     * @return {@code true} if the bigraph is monomorphic, otherwise {@code false}
     */
    default boolean isMonomorphic() {
        // check that no two sites are siblings
        boolean noTwoSitesAreSiblings = getSites().stream().map(this::getSiblingsOfNode).allMatch(x -> x.stream().noneMatch(BigraphEntityType::isSite));

        // check that no two inner names are siblings
        for (BigraphEntity.InnerName eachInner : getInnerNames()) {
            final Collection<BigraphEntity> pointsFromLink = getPointsFromLink(getLinkOfPoint(eachInner));
            if (pointsFromLink.stream().filter(x -> !x.equals(eachInner)).anyMatch(BigraphEntityType::isInnerName)) {
                return false;
            }
        }

        return noTwoSitesAreSiblings;
    }

    //TODO
    default boolean isDiscrete() {
        return false;
    }

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

    /**
     * Returns the set of children of a given node (including sites). <br/>
     * If the node has no children, then an empty set is returned.
     *
     * @param node the node whose children should be returned
     * @return a set of children of the given node
     */
    Collection<BigraphEntity> getChildrenOf(BigraphEntity node);


    /**
     * Get the parent of a bigraph's place. Passing a root as argument will
     * always return {@code null}.
     *
     * @param node a place of this bigraph
     * @return the parent of the given place, or {@code null}
     */
    BigraphEntity getParent(BigraphEntity node);

    /**
     * Returns the link of a bigraph's point type.
     *
     * @param point a point of the bigraph
     * @return returns the link that connects the point a {@code null}
     */
    BigraphEntity getLinkOfPoint(BigraphEntity point);

    Collection<BigraphEntity.Port> getPorts(BigraphEntity node);

    <C extends Control> BigraphEntity.NodeEntity<C> getNodeOfPort(BigraphEntity.Port port);

    /**
     * Returns all siblings of the given node of the current bigraph. The node itself is not included.
     *
     * @param node the node whoms sibling should be returned
     * @return siblings of {@code node}
     */
    Collection<BigraphEntity> getSiblingsOfNode(BigraphEntity node);

    /**
     * get all point entities (i.e., ports and inner names) from a link entity (edges and outer names)
     *
     * @param linkEntity
     * @return
     */
    Collection<BigraphEntity> getPointsFromLink(BigraphEntity linkEntity);

    /**
     * Check if two nodes are connected to each other.
     * <p>
     * The method considers also indirect connection, meaning, it doesn't matter if they are connected by an edge
     * or outer name.
     *
     * @param place1 left node
     * @param place2 right node
     * @return true, if the two nodes are connected by an edge or outer name
     */
    boolean areConnected(BigraphEntity.NodeEntity place1, BigraphEntity.NodeEntity place2);

    EPackage getModelPackage();

    //    public BigraphEntity findByNodeName(Collection<BigraphEntity> list, String nodeName) {
//        return list.stream().filter(x -> BigraphEntityType.isNode(x)
//                && ((BigraphEntity.NodeEntity<Object>) x).getName().equals(nodeName))
//                .findFirst()
//                .get();
//    }
//
//    public BigraphEntity findRootByIndex(Collection<BigraphEntity.RootEntity> list, int index) {
//        return list.stream().filter(x -> x.getIndex() == index)
//                .findFirst()
//                .get();
//    }
}
