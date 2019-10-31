package de.tudresden.inf.st.bigraphs.core;


import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * @return {@code true}, if the bigraph is prime, otherwise {@code false}.
     */
    default boolean isPrime() {
        return getRoots().size() == 1 && getInnerNames().size() == 0;
    }

    /**
     * A concrete bigraph is epi (epimorphic) iff its place graph has no idle root (i.e., no root is a barren) and
     * its link graph has no idle outer names.
     *
     * @return {@code true} if the bigraph is epi, otherwise {@code false}
     */
    default boolean isEpimorphic() {
        boolean placeGraphIsEpi = getRoots().stream().allMatch(x -> getChildrenOf(x).size() > 0);
        boolean linkGraphIsEpi = getOuterNames().stream().allMatch(x -> getPointsFromLink(x).size() > 0);
        return placeGraphIsEpi && linkGraphIsEpi;
    }

    /**
     * A concrete bigraph is mono (monomorphic) iff no two sites are siblings ("inner-injective") and no two inner names
     * are siblings. With other words, every edge has at most one inner name (= no two inner names are peers).
     *
     * @return {@code true} if the bigraph is mono, otherwise {@code false}
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

    /**
     * Returns the support of a bigraph. The support is a finite set comprising the nodes of the place graph and the edges
     * of the link graph of the current bigraph.
     * <p>
     * Only concrete bigraphs have a support. If this set is empty, the bigraph is considered as abstract.
     *
     * @return the support <i>|B|</i> of the bigraph <i>B</i>
     */
    default Collection<BigraphEntity> getSupport() {
        return Stream.concat(
                getNodes().stream(),
                getEdges().stream()).collect(Collectors.toList());
    }

    /**
     * A site is active if all its ancestors are also active.
     *
     * @param siteIndex the index of the site to check
     * @return {@code true} if the site is active, otherwise {@code false}
     */
    default boolean isActiveAtSite(int siteIndex) {
        Optional<BigraphEntity.SiteEntity> first = getSites().stream().filter(x -> x.getIndex() == siteIndex).findFirst();
        if (first.isPresent()) {
            BigraphEntity parent = getParent(first.get());
            while (Objects.nonNull(parent) && !BigraphEntityType.isRoot(parent)) {
                if (!ControlKind.isActive(parent.getControl())) {
                    return false;
                }
                parent = getParent(parent);
            }
            return true;
        }
        return false;
    }

    default boolean isActiveAtNode(BigraphEntity.NodeEntity node) {
        if (Objects.nonNull(node) && ControlKind.isActive(node.getControl())) {
            BigraphEntity parent = getParent(node);
            while (Objects.nonNull(parent) && !BigraphEntityType.isRoot(parent)) {
                if (!ControlKind.isActive(parent.getControl())) {
                    return false;
                }
                parent = getParent(parent);
            }
            return true;
        }
        return false;
    }

    /**
     * A bigraph is active if all its sites are active. See {@link Bigraph#isActiveAtSite(int)}.
     *
     * @return {@code true} if the bigraph is active.
     */
    default boolean isActive() {
        return getSites().stream().allMatch(x -> isActiveAtSite(x.getIndex()));
    }


    /**
     * Checks if the bigraph is guarding. A bigraph is guarding if no site has a root as parent and
     * no inner name is open.
     *
     * @return {@code true} if the bigraph is guarding, otherwise {@code false}
     */
    default boolean isGuarding() {
        return getRoots().stream()
                .map(this::getChildrenOf)
                .flatMap(Collection::stream)
                .noneMatch(BigraphEntityType::isSite) &&
                getInnerNames().size() == 0;
    }


    /**
     * A discrete bigraph has no edges and its link map is bijective (all names are distinct and every point is open).
     * <p>
     * See: 1.Jensen, O.H., Milner, R.: Bigraphs and mobile processes (revised). University of Cambridge Computer Laboratory (2004), p. 59.
     *
     * @return {@code true} if the bigraph is discrete, otherwise {@code false}
     */
    default boolean isDiscrete() {
        // check if no edges: ensures that no points are connected by an edge
        boolean hasNoEdges = getEdges().size() == 0;
        // check if no outer name is idle and at most one point is connected to it. No two points must be
        // peers.
        boolean noNameIsIdle = getOuterNames().stream().allMatch(x -> getPointsFromLink(x).size() == 1);

        //check that every point is open..
        boolean allPortsOpen = true;
        for (BigraphEntity.NodeEntity each : getNodes()) {
            if (each.getControl().getArity().getValue().longValue() >= 1) {
                Collection<BigraphEntity.Port> ports = getPorts(each);
                if (ports.size() == 0 ||
                        !ports.stream().allMatch(x -> BigraphEntityType.isOuterName(getLinkOfPoint(x)))) {
                    allPortsOpen = false;
                    break;
                }
            }
        }
        boolean allInnerOpen = getInnerNames().stream().allMatch(x -> BigraphEntityType.isOuterName(getLinkOfPoint(x)));

        return hasNoEdges && allPortsOpen && allInnerOpen && noNameIsIdle;
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

    //TODO rename method

    /**
     * Get the depth of a place entity.
     *
     * @param place the place who's depth should be computed
     * @return the depth of the given place entity of this bigraph
     */
    int getLevelOf(BigraphEntity place);

    /**
     * Gets the neighborhood of the given node of the place graph. The neighborhood is the set containing the its
     * children and its parent (without the node in question itself).
     *
     * @param node the nodes who's neighborhood should be returned
     * @return the neighborhood of the node of the place graph
     */
    List<BigraphEntity> getOpenNeighborhoodOfVertex(BigraphEntity node);

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

    BigraphEntity getTopLevelRoot(BigraphEntity node);

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

    /**
     * Return all ports of a node. If the node's control has arity 0, then the list will always be empty.
     * If no link is attached to a port, the list will also be empty.
     *
     * @param node the node who's ports shall be returned
     * @return all ports of a node
     */
    Collection<BigraphEntity.Port> getPorts(BigraphEntity node);

    /**
     * Get the number of "blocked/occupied" ports by links of a node of the place graph.
     * Check with the control's arity.
     *
     * @param node the node
     * @return the port count of the node which are already used by links
     */
    int getPortCount(BigraphEntity node);

    <C extends Control> BigraphEntity.NodeEntity<C> getNodeOfPort(BigraphEntity.Port port);

    /**
     * Returns all siblings of the given node of the current bigraph. The node itself is not included.
     *
     * @param node the node whoms sibling should be returned
     * @return siblings of {@code node}
     */
    Collection<BigraphEntity> getSiblingsOfNode(BigraphEntity node);

    /**
     * Returns all siblings of an inner name. The collection will not contain any port.
     *
     * @param innerName the inner name who's siblings should be returned
     * @return
     */
    Collection<BigraphEntity.InnerName> getSiblingsOfInnerName(BigraphEntity.InnerName innerName);

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

    EObject getModel();

}
