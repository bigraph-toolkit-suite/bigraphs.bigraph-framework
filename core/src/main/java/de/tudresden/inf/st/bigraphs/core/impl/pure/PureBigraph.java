package de.tudresden.inf.st.bigraphs.core.impl.pure;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.collections.impl.factory.SortedSets;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is an Ecore-based model implementation of a pure bigraph.
 * A {@link PureBigraph} can be obtained by using a {@link PureBigraphBuilder}.
 * <p>
 * The class directly implements the {@link Bigraph} interface with {@link DefaultDynamicSignature} as type for the signature.
 * <p>
 * The class represents an immutable data structure providing also some operations (e.g., get parent of node).
 * The elements are stored separately in collections for easier access. The collections cannot be modified afterwards.
 *
 * @author Dominik Grzelak
 * @see BigraphBuilder
 */
public class PureBigraph implements Bigraph<DefaultDynamicSignature>, EcoreBigraph<DefaultDynamicSignature> {
    private final EPackage modelPackage;
    private final EObject bigraphInstanceModel;

    private final ImmutableSet<BigraphEntity.RootEntity> roots;
    private final ImmutableList<BigraphEntity.NodeEntity<DefaultDynamicControl>> nodes;
    private final MutableMap<EObject, BigraphEntity.NodeEntity<DefaultDynamicControl>> nodesMap = Maps.mutable.empty();
    private final MutableMap<BigraphEntity.NodeEntity<DefaultDynamicControl>, List<BigraphEntity.Port>> portMap = Maps.mutable.empty();
    private final MutableMap<BigraphEntity.Link, List<BigraphEntity<?>>> pointsOfLinkMap = Maps.mutable.empty();
    private final ImmutableSet<BigraphEntity.SiteEntity> sites;
    private final ImmutableSet<BigraphEntity.InnerName> innerNames;
    private final ImmutableSet<BigraphEntity.OuterName> outerNames;
    private final ImmutableSet<BigraphEntity.Edge> edges;
    private final DefaultDynamicSignature signature;

    public PureBigraph(BigraphBuilderSupport.InstanceParameter details) {
        this.modelPackage = details.getModelPackage();
        this.bigraphInstanceModel = details.getbBigraphObject();
        this.roots = Sets.immutable.<BigraphEntity.RootEntity>ofAll(details.getRoots());//Collections.unmodifiableSet(details.getRoots()); //roots;
        this.sites = Sets.immutable.ofAll(details.getSites()); //sites;
        this.nodes = Lists.immutable.ofAll(details.getNodes());//new ArrayList<>(Collections.unmodifiableSet(details.getNodes())); //nodes;
        this.nodesMap.putAll(this.nodes.stream().collect(Collectors.toMap(data -> data.getInstance(), data -> data)));
        this.outerNames = Sets.immutable.ofAll(details.getOuterNames()); //outerNames;
        this.innerNames = Sets.immutable.ofAll(details.getInnerNames()); //innerNames;
        this.edges = Sets.immutable.ofAll(details.getEdges()); //edges;
        this.signature = (DefaultDynamicSignature) details.getSignature(); //signature;
    }

    public EPackage getModelPackage() {
        return this.modelPackage;
    }

    @Override
    public EObject getModel() {
        return this.bigraphInstanceModel;
    }

    @Override
    public DefaultDynamicSignature getSignature() {
        return signature;
    }

    @Override
    public int getLevelOf(BigraphEntity<?> place) {
        if (BigraphEntityType.isRoot(place)) {
            return 0;
        }
        return getNodeDepth(place, 1);
    }

    private int getNodeDepth(BigraphEntity<?> data, int level) {
        BigraphEntity<?> parent = getParent(data);
        if (BigraphEntityType.isRoot(parent)) {
            return level;
        } else if (BigraphEntityType.isRoot(parent) && level == 0) {
            return 1;
        }
        return getNodeDepth(parent, level + 1);
    }

    @Override
    public List<BigraphEntity<?>> getOpenNeighborhoodOfVertex(BigraphEntity<?> node) {
        MutableList<BigraphEntity<?>> neighbors = Lists.mutable.empty();
        return neighborhoodHook(neighbors, node);
    }

    private List<BigraphEntity<?>> neighborhoodHook(List<BigraphEntity<?>> neighbors, BigraphEntity<?> node) {
        EObject instance = node.getInstance();
        // first check the children of the node
        EStructuralFeature chldRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
        if (chldRef != null) {
            EList<EObject> childs = (EList<EObject>) instance.eGet(chldRef);
            for (EObject each : childs) {
                addPlaceToList(neighbors, each);
            }
        }
        // second, the parent
        EStructuralFeature prntRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if (prntRef != null && instance.eGet(prntRef) != null) {
            final EObject each = (EObject) instance.eGet(prntRef);
            addPlaceToList(neighbors, each);
        }
        return neighbors;
    }

    /**
     * Convenient method that finds the corresponding 'node type' (e.g., root) of a given {@link EObject} instance and
     * adds it to the given list {@code list}.
     * <p>
     * Throws a runtime exception of the node couldn't be found.
     *
     * @param list the list
     * @param each node entity (e.g., root, node or site)
     */
    private void addPlaceToList(final List<BigraphEntity<?>> list, final EObject each) {
        if (isBNode(each)) {
            list.add(
                    nodesMap.get(each)
            );
        } else if (isBRoot(each)) {
            list.add(
                    getRoots().stream()
                            .filter(x -> x.getInstance().equals(each))
                            .findFirst().get()
            );
        } else if (isBSite(each)) {
            list.add(
                    getSites().stream()
                            .filter(x -> x.getInstance().equals(each))
                            .findFirst().get()
            );
        }
    }


    @Override
    public List<BigraphEntity.RootEntity> getRoots() {
        return this.roots.toList();
    }


    @Override
    public List<BigraphEntity.SiteEntity> getSites() {
        return this.sites.toList();
    }

    @Override
    public List<BigraphEntity.OuterName> getOuterNames() {
        return this.outerNames.toList();
    }

    @Override
    public List<BigraphEntity.InnerName> getInnerNames() {
        return this.innerNames.toList();
    }

    @Override
    public List<BigraphEntity<?>> getAllPlaces() {
//        return Lists.fixedSize.fromStream(Streams.<BigraphEntity<?>>concat((Stream) roots.stream(), (Stream) nodes.stream(), (Stream) sites.stream()));
        return Lists.fixedSize.<BigraphEntity<?>>ofAll((Iterable) getRoots())
                .withAll(getNodes())
                .withAll(getSites());
    }

    @Override
    public List<BigraphEntity.Link> getAllLinks() {
        return Lists.fixedSize.<BigraphEntity.Link>ofAll(getOuterNames()).withAll(getEdges());
    }

    @Override
    public List<BigraphEntity.Edge> getEdges() {
        return this.edges.toList();
    }

    @Override
    public BigraphEntity<?> getParent(BigraphEntity<?> node) {
        assert Objects.nonNull(node);
        EObject instance = node.getInstance();
        EStructuralFeature prntRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if ((prntRef) != null && Objects.nonNull(instance.eGet(prntRef))) {
            EObject each = (EObject) instance.eGet(prntRef);
            if (isBNode(each)) {
                //get control at instance level
                return Optional.ofNullable(nodesMap.get(each)).orElse(null);
            } else { //root
                return roots.stream().filter(x -> x.getInstance().equals(each)).findFirst().orElse(null);
            }
        }
        return null;
    }

    @Override
    public List<BigraphEntity.InnerName> getSiblingsOfInnerName(BigraphEntity.InnerName innerName) {
        if ((innerName) == null) return Collections.emptyList();
        BigraphEntity.Link linkOfPoint = getLinkOfPoint(innerName);
        if ((linkOfPoint) == null) return Collections.emptyList();
        return getPointsFromLink(linkOfPoint).stream().filter(BigraphEntityType::isInnerName)
                .filter(x -> !x.equals(innerName)).map(x -> (BigraphEntity.InnerName) x).collect(Collectors.toList());
    }

    public List<BigraphEntity<?>> getSiblingsOfNode(BigraphEntity<?> node) {
        if (BigraphEntityType.isRoot(node) || !isBPlace(node.getInstance())) return Collections.emptyList();
        BigraphEntity<?> parent = getParent(node);
        if ((parent) == null) return Collections.emptyList();
        List<BigraphEntity<?>> siblings = getChildrenOf(parent);
        siblings.remove(node);
        return siblings;
    }

    @Override
    @SuppressWarnings("unchecked")
    public BigraphEntity.NodeEntity<DefaultDynamicControl> getNodeOfPort(BigraphEntity.Port port) {
        if ((port) == null) return null;
        EObject instance = port.getInstance();
        EStructuralFeature nodeRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_NODE);
        if ((nodeRef) == null) return null;
        EObject nodeObject = (EObject) instance.eGet(nodeRef);
        Optional<BigraphEntity.NodeEntity<DefaultDynamicControl>> first =
                Optional.ofNullable(nodesMap.get(nodeObject));
        return first.orElse(null);
    }

    /**
     * Return all ports of a node. If the node's control has arity 0, then the list will always be empty.
     * If no link is attached to a port, the list will also be empty.
     * <p>
     * The list is ordered subject to the ports indices.
     *
     * @param node the node who's ports shall be returned
     * @return all ports of a node
     */
    @Override
    public List<BigraphEntity.Port> getPorts(BigraphEntity<?> node) {
        if (portMap.containsKey(node)) {
            return portMap.get(node);
        }
        if (!BigraphEntityType.isNode(node)) return Collections.emptyList();
        EObject instance = node.getInstance();
        EStructuralFeature portRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PORT);
        if (portRef == null) return Collections.emptyList();
        EList<EObject> portList = (EList<EObject>) instance.eGet(portRef);
        MutableSortedSet<BigraphEntity.Port> portsList = SortedSets.mutable.empty();
        for (EObject eachPort : portList) { // are ordered anyway
            BigraphEntity.Port port = BigraphEntity.create(eachPort, BigraphEntity.Port.class);
            EStructuralFeature indexAttr = eachPort.eClass().getEStructuralFeature(BigraphMetaModelConstants.ATTRIBUTE_INDEX);
            if (indexAttr != null) {
//                port.setIndex(portList.indexOf(eachPort));
                port.setIndex((int) eachPort.eGet(indexAttr));
                portsList.add(port);
            }
        }
        portMap.put((BigraphEntity.NodeEntity<DefaultDynamicControl>) node, portsList.toList());
        return portsList.toList();
    }

    @Override
    public <C extends Control<?, ?>> int getPortCount(BigraphEntity.NodeEntity<C> node) {
        if (!BigraphEntityType.isNode(node)) return 0;
        EObject instance = node.getInstance();
        EStructuralFeature portRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PORT);
        if ((portRef) == null) return 0;
        return ((EList<EObject>) instance.eGet(portRef)).size();
    }

    @Override
    public List<BigraphEntity<?>> getPointsFromLink(BigraphEntity.Link linkEntity) {
        if (pointsOfLinkMap.containsKey(linkEntity)) {
            return pointsOfLinkMap.get(linkEntity);
        }
        if (linkEntity == null) // || !isBLink(linkEntity.getInstance()))
            return Collections.emptyList();
        final EObject eObject = linkEntity.getInstance();
        final EStructuralFeature pointsRef = eObject.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_POINT);
        if ((pointsRef) == null) return Collections.emptyList();
        final EList<EObject> pointsObjects = (EList<EObject>) eObject.eGet(pointsRef);
        if ((pointsObjects) == null) return Collections.emptyList();

        final List<BigraphEntity<?>> result = new ArrayList<>();
        for (EObject eachObject : pointsObjects) {
            if (isBPort(eachObject)) {
                getNodes().stream()
                        .map(this::getPorts).flatMap(Collection::stream)
                        .filter(x -> x.getInstance().equals(eachObject))
                        .findFirst()
                        .ifPresent(result::add);
            } else if (isBInnerName(eachObject)) {
                getInnerNames().stream()
                        .filter(x -> x.getInstance().equals(eachObject))
                        .findFirst()
                        .ifPresent(result::add);
            }
        }
        pointsOfLinkMap.put(linkEntity, result);
        return result;
    }

    @Override
    public BigraphEntity.Link getLinkOfPoint(BigraphEntity<?> point) {
        if (!BigraphEntityType.isPointType(point)) return null;
        EObject eObject = point.getInstance();
        EStructuralFeature lnkRef = eObject.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
        if ((lnkRef) == null) return null;
        EObject linkObject = (EObject) eObject.eGet(lnkRef);
        if ((linkObject) == null) return null;
//        if (!isBLink(linkObject)) return null; //"owner" problem
        if (isBEdge(linkObject)) {
            Optional<BigraphEntity.Edge> first = getEdges().stream().filter(x -> x.getInstance().equals(linkObject)).findFirst();
            return first.orElse(null);
        } else {
            Optional<BigraphEntity.OuterName> first = getOuterNames().stream().filter(x -> x.getInstance().equals(linkObject)).findFirst();
            return first.orElse(null);
        }
    }

    @Override
    public List<BigraphEntity<?>> getChildrenOf(BigraphEntity<?> node) {
        EObject instance = node.getInstance();
        EStructuralFeature chldRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
//        Set<BigraphEntity> children = new LinkedHashSet<>();
        MutableSet<BigraphEntity<?>> children = Sets.mutable.empty();
        if (Objects.nonNull(chldRef)) {
            EList<EObject> childs = (EList<EObject>) instance.eGet(chldRef);
            for (EObject eachChild : childs) {
                if (isBNode(eachChild)) {//TODO set could be inefficient here for large bigraphs
                    Optional<BigraphEntity.NodeEntity<DefaultDynamicControl>> nodeEntity =
                            Optional.ofNullable(nodesMap.get(eachChild));
//                            nodes.stream().filter(x -> x.getInstance().equals(eachChild)).findFirst();
                    nodeEntity.ifPresent(children::add);
                } else if (isBSite(eachChild)) {
                    Optional<BigraphEntity.SiteEntity> nodeEntity =
                            sites.stream().filter(x -> x.getInstance().equals(eachChild)).findFirst();
                    nodeEntity.ifPresent(children::add);
                }
            }
        }
        return children.toList();
    }

    @Override
    public List<BigraphEntity.NodeEntity<DefaultDynamicControl>> getNodes() {
        return this.nodes.castToList();
    }

    @Override
    public BigraphEntity.RootEntity getTopLevelRoot(BigraphEntity<?> node) {
        EStructuralFeature prntRef = node.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if (node.getInstance().eGet(prntRef) != null) {
            return getTopLevelRoot(getParent(node));
        }
        return (BigraphEntity.RootEntity) node;
    }

    @Override
    public boolean isParentOf(BigraphEntity<?> node, BigraphEntity<?> possibleParent) {
        if ((node) == null || (possibleParent) == null) return false;
        if (node.equals(possibleParent)) return true;
        EStructuralFeature prntRef = node.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if ((prntRef) == null) return false;
        EObject parent = (EObject) node.getInstance().eGet(prntRef);
        if ((parent) == null) return false;
        if (isBRoot(parent) && !parent.equals(possibleParent.getInstance())) return false;
        if (parent.equals(possibleParent.getInstance())) {
            return true;
        } else if (!parent.equals(possibleParent.getInstance())) {
            Optional<BigraphEntity.NodeEntity<DefaultDynamicControl>> first = getNodes().stream().filter(x -> x.getInstance().equals(parent)).findFirst();
            return isParentOf(first.orElse(null), possibleParent);
        }
        return false;
    }

    @Override
    public <C extends Control<?, ?>> boolean areConnected
            (BigraphEntity.NodeEntity<C> place1, BigraphEntity.NodeEntity<C> place2) {
        if ((place1) == null || (place2) == null) return false;
        EStructuralFeature portsRef = place1.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PORT);
        if ((portsRef) == null) return false;
        EList<EObject> bPorts = (EList<EObject>) place1.getInstance().eGet(portsRef);
        for (EObject bPort : bPorts) {
            EStructuralFeature linkRef = bPort.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
            if ((linkRef) == null) return false;
            EObject linkObject = (EObject) bPort.eGet(linkRef);
            if ((linkObject) == null) continue;
            EStructuralFeature pointsRef = linkObject.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_POINT);
            if ((pointsRef) == null) continue;
            EList<EObject> bPoints = (EList<EObject>) linkObject.eGet(pointsRef);
            for (EObject bPoint : bPoints) {
                if (isBPort(bPoint)) {
                    EStructuralFeature nodeRef = bPoint.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_NODE);
                    assert nodeRef != null;
                    if (bPoint.eGet(nodeRef).equals(place2.getInstance())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
