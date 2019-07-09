package de.tudresden.inf.st.bigraphs.core.impl.ecore;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EPackageImpl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This Ecore-based model implementation of a bigraph is a immutable data structure providing also some operations on it.
 * Implements directly the {@link Bigraph} interface with a {@link DefaultDynamicSignature}.
 * <p>
 * The elements are stored separately in collections for easier access. The collections cannot modified afterwards.
 * <p>
 * A {@link PureBigraph} must be built using the {@link PureBigraphBuilder}.
 *
 * @author Dominik Grzelak
 * @see de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphBuilder
 */
public class PureBigraph implements Bigraph<DefaultDynamicSignature> {
    private EPackage modelPackage;

    private final Set<BigraphEntity.RootEntity> roots;
    private final Set<BigraphEntity.SiteEntity> sites;
    private final Set<BigraphEntity.InnerName> innerNames;
    private final Set<BigraphEntity.OuterName> outerNames;
    private final Set<BigraphEntity.Edge> edges;
    private final List<BigraphEntity.NodeEntity<DefaultDynamicControl>> nodes;
    private final Map<EObject, BigraphEntity.NodeEntity<DefaultDynamicControl>> nodesMap = new ConcurrentHashMap<>();
    private final DefaultDynamicSignature signature;

    public PureBigraph(PureBigraphBuilder.InstanceParameter details) {
        this.modelPackage = details.getModelPackage();
        this.roots = Collections.unmodifiableSet(details.getRoots()); //roots;
        this.sites = Collections.unmodifiableSet(details.getSites()); //sites;
        this.nodes = new ArrayList<>(Collections.unmodifiableSet(details.getNodes())); //nodes;
        this.nodesMap.putAll(this.nodes.stream().collect(Collectors.toMap(data -> data.getInstance(), data -> data)));
        this.outerNames = Collections.unmodifiableSet(details.getOuterNames()); //outerNames;
        this.innerNames = Collections.unmodifiableSet(details.getInnerNames()); //innerNames;
        this.edges = Collections.unmodifiableSet(details.getEdges()); //edges;
        this.signature = (DefaultDynamicSignature) details.getSignature(); //signature;
    }

    public EPackage getModelPackage() {
        return this.modelPackage;
    }

    @Override
    public DefaultDynamicSignature getSignature() {
        return signature;
    }

    @Override
    public int getLevelOf(BigraphEntity place) {
        if (BigraphEntityType.isRoot(place)) {
            return 0;
        }
        return getNodeDepth(place, 1);
    }

    private int getNodeDepth(BigraphEntity data, int level) {
        BigraphEntity parent = getParent(data);
        if (BigraphEntityType.isRoot(parent)) {
            return level;
        } else if (BigraphEntityType.isRoot(parent) && level == 0) {
            return 1;
        }
        return getNodeDepth(parent, level + 1);
    }

    @Override
    public List<BigraphEntity> getOpenNeighborhoodOfVertex(BigraphEntity node) {
        List<BigraphEntity> neighbors = new ArrayList<>();
        neighbors = neighborhoodHook(neighbors, node);
        return neighbors;
    }

    private List<BigraphEntity> neighborhoodHook(List<BigraphEntity> neighbors, BigraphEntity node) {
        EObject instance = node.getInstance();
        // first check the children of the node
        EStructuralFeature chldRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
        if (Objects.nonNull(chldRef)) {
            EList<EObject> childs = (EList<EObject>) instance.eGet(chldRef);
            for (EObject each : childs) {
                addPlaceToList(neighbors, each);
            }
        }
        // second, the parent
        EStructuralFeature prntRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if (Objects.nonNull(prntRef) && Objects.nonNull(instance.eGet(prntRef))) {
            final EObject each = (EObject) instance.eGet(prntRef);
            addPlaceToList(neighbors, each);
        }
        return neighbors;
    }

    private void addPlaceToList(final List<BigraphEntity> neighbors, final EObject each) {
        if (isBNode(each)) {
            neighbors.add(
                    nodesMap.get(each)
//                    getNodes().stream()
//                            .filter(x -> x.getInstance().equals(each))
//                            .findFirst().get()
            );
        } else if (isBRoot(each)) {
            neighbors.add(
                    getRoots().stream()
                            .filter(x -> x.getInstance().equals(each))
                            .findFirst().get()
            );
        } else if (isBSite(each)) {
            neighbors.add(
                    getSites().stream()
                            .filter(x -> x.getInstance().equals(each))
                            .findFirst().get()
            );
        }
    }


    @Override
    public Collection<BigraphEntity.RootEntity> getRoots() {
        return this.roots;
    }


    @Override
    public Collection<BigraphEntity.SiteEntity> getSites() {
        return this.sites;
    }

    @Override
    public Collection<BigraphEntity.OuterName> getOuterNames() {
        return this.outerNames;
    }

    @Override
    public Collection<BigraphEntity.InnerName> getInnerNames() {
        return this.innerNames;
    }

    @Override
    public Collection<BigraphEntity> getAllPlaces() {
        Collection<BigraphEntity> list = new ArrayList<>(roots);
        list.addAll(nodes);
        list.addAll(sites);
        return list;
    }

    @Override
    public Collection<BigraphEntity.Edge> getEdges() {
        return this.edges;
    }

    @Override
    public BigraphEntity getParent(BigraphEntity node) {
        EObject instance = node.getInstance();
        EStructuralFeature prntRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if (Objects.nonNull(prntRef) && Objects.nonNull(instance.eGet(prntRef))) {
            EObject each = (EObject) instance.eGet(prntRef);
            if (isBNode(each)) {
                //get control at instance level
                Optional<BigraphEntity.NodeEntity<DefaultDynamicControl>> nodeEntity =
                        Optional.ofNullable(nodesMap.get(each));
//                        nodes.stream().filter(x -> x.getInstance().equals(each)).findFirst();
                return nodeEntity.orElse(null);
            } else { //root
                Optional<BigraphEntity.RootEntity> rootEntity = roots.stream().filter(x -> x.getInstance().equals(each)).findFirst();
                return rootEntity.orElse(null);
            }
        }
        return null;
    }

    @Override
    public Collection<BigraphEntity.InnerName> getSiblingsOfInnerName(BigraphEntity.InnerName innerName) {
        if (Objects.isNull(innerName)) return Collections.emptyList();
        BigraphEntity linkOfPoint = getLinkOfPoint(innerName);
        if (Objects.isNull(linkOfPoint)) return Collections.emptyList();
        return getPointsFromLink(linkOfPoint).stream().filter(BigraphEntityType::isInnerName)
                .filter(x -> !x.equals(innerName)).map(x -> (BigraphEntity.InnerName) x).collect(Collectors.toList());
    }

    public Collection<BigraphEntity> getSiblingsOfNode(BigraphEntity node) {
        if (BigraphEntityType.isRoot(node) || !isBPlace(node.getInstance())) return Collections.emptyList();
        BigraphEntity parent = getParent(node);
        if (Objects.isNull(parent)) return Collections.emptyList();
        Collection<BigraphEntity> siblings = getChildrenOf(parent);
        return siblings.stream().filter(x -> !x.equals(node)).collect(Collectors.toList());
    }

    @Override
    public BigraphEntity.NodeEntity<DefaultDynamicControl> getNodeOfPort(BigraphEntity.Port port) {
        if (Objects.isNull(port)) return null;
        EObject instance = port.getInstance();
        EStructuralFeature nodeRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_NODE);
        if (Objects.isNull(nodeRef)) return null;
        EObject nodeObject = (EObject) instance.eGet(nodeRef);
        Optional<BigraphEntity.NodeEntity<DefaultDynamicControl>> first =
                Optional.ofNullable(nodesMap.get(nodeObject));
//                getNodes().stream().filter(x -> x.getInstance().equals(nodeObject)).findFirst();
        return first.orElse(null);
    }

    @Override
    public Collection<BigraphEntity.Port> getPorts(BigraphEntity node) {
        if (!BigraphEntityType.isNode(node)) return Collections.emptyList();
        EObject instance = node.getInstance();
        EStructuralFeature portRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PORT);
        EList<EObject> portList = (EList<EObject>) instance.eGet(portRef);
        List<BigraphEntity.Port> portsList = new LinkedList<>();
        for (EObject eachPort : portList) { // are ordered anyway
            //TODO: don't create new class everytime!
            BigraphEntity.Port port = BigraphEntity.create(eachPort, BigraphEntity.Port.class);
            port.setIndex(portList.indexOf(eachPort)); //eachPort.eGet(eachPort.eClass().getEStructuralFeature(BigraphMetaModelConstants.ATTRIBUTE_INDEX))
            portsList.add(port);
        }
        return portsList;
    }

    @Override
    public Collection<BigraphEntity> getPointsFromLink(BigraphEntity linkEntity) {
        if (Objects.isNull(linkEntity) || !isBLink(linkEntity.getInstance()))
            return Collections.emptyList();
        final EObject eObject = linkEntity.getInstance();
        final EStructuralFeature pointsRef = eObject.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_POINT);
        if (Objects.isNull(pointsRef)) return Collections.emptyList();
        final EList<EObject> pointsObjects = (EList<EObject>) eObject.eGet(pointsRef);
        if (Objects.isNull(pointsObjects)) return Collections.emptyList();

        final Collection<BigraphEntity> result = new ArrayList<>();
        for (EObject eachObject : pointsObjects) {
            if (isBPort(eachObject)) {
                Optional<BigraphEntity.Port> first = getNodes().stream()
                        .map(this::getPorts).flatMap(Collection::stream)
                        .filter(x -> x.getInstance().equals(eachObject))
                        .findFirst();
                first.ifPresent(result::add);
            } else if (isBInnerName(eachObject)) {
                Optional<BigraphEntity.InnerName> first = getInnerNames().stream().filter(x -> x.getInstance().equals(eachObject)).findFirst();
                first.ifPresent(result::add);
            }
        }
        return result;
    }

    @Override
    public BigraphEntity getLinkOfPoint(BigraphEntity point) {
        if (!BigraphEntityType.isPointType(point)) return null;
        EObject eObject = point.getInstance();
        EStructuralFeature lnkRef = eObject.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
        if (Objects.isNull(lnkRef)) return null;
        EObject linkObject = (EObject) eObject.eGet(lnkRef);
        if (Objects.isNull(linkObject)) return null;
        if (!isBLink(linkObject)) return null; //"owner" problem
        if (isBEdge(linkObject)) {
            Optional<BigraphEntity.Edge> first = getEdges().stream().filter(x -> x.getInstance().equals(linkObject)).findFirst();
            return first.orElse(null);
        } else {
            Optional<BigraphEntity.OuterName> first = getOuterNames().stream().filter(x -> x.getInstance().equals(linkObject)).findFirst();
            return first.orElse(null);
        }
    }

    @Override
    public Collection<BigraphEntity> getChildrenOf(BigraphEntity node) {
        EObject instance = node.getInstance();
        EStructuralFeature chldRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
        Set<BigraphEntity> children = new LinkedHashSet<>();
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
        return children;
    }

    @Override
    public Collection<BigraphEntity.NodeEntity<DefaultDynamicControl>> getNodes() {
        return this.nodes;
    }

    public BigraphEntity getTopLevelRoot(BigraphEntity node) {
        EStructuralFeature prntRef = node.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if (node.getInstance().eGet(prntRef) != null) {
            return getTopLevelRoot(BigraphEntity.create((EObject) node.getInstance().eGet(prntRef), BigraphEntity.RootEntity.class));
        }
        return node;
    }

//    public EObject getTopLevelRoot(EObject node) {
//        EStructuralFeature prntRef = node.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
//        if (node.eGet(prntRef) != null) {
//            return getTopLevelRoot((EObject) node.eGet(prntRef));
//        }
//        return node;
//    }

    @Override
    public boolean areConnected(BigraphEntity.NodeEntity place1, BigraphEntity.NodeEntity place2) {
        EStructuralFeature portsRef = place1.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PORT);
        if (Objects.isNull(portsRef)) return false;
        EList<EObject> bPorts = (EList<EObject>) place1.getInstance().eGet(portsRef);
        for (EObject bPort : bPorts) {
            EStructuralFeature linkRef = bPort.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
            if (Objects.isNull(linkRef)) return false;
            EObject linkObject = (EObject) bPort.eGet(linkRef);
            if (Objects.isNull(linkObject)) continue;
            EStructuralFeature pointsRef = linkObject.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_POINT);
            if (Objects.isNull(pointsRef)) continue;
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

    protected boolean isBPort(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_PORT);
    }

    protected boolean isBInnerName(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_INNERNAME);
    }

    protected boolean isBPoint(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_POINT);
    }

    protected boolean isBNode(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_NODE);
    }

    protected boolean isBSite(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_SITE);
    }

    protected boolean isBRoot(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_ROOT);
    }

    protected boolean isBLink(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_LINK);
    }

    protected boolean isBEdge(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_EDGE);
    }

    public boolean isBPlace(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_PLACE);
    }

    //works only for elements of the calling class
    protected boolean isOfEClass(EObject eObject, String eClassifier) {
        return eObject.eClass().getName().equals(((EPackageImpl) getModelPackage()).getEClassifierGen(eClassifier).getName()) ||
                eObject.eClass().equals(((EPackageImpl) getModelPackage()).getEClassifierGen(eClassifier)) ||
                eObject.eClass().getEAllSuperTypes().stream().map(ENamedElement::getName).collect(Collectors.toList()).contains(((EPackageImpl) getModelPackage()).getEClassifierGen(eClassifier).getName())
                || eObject.eClass().getEAllSuperTypes().contains(((EPackageImpl) getModelPackage()).getEClassifierGen(eClassifier));
    }
}
