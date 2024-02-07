package org.bigraphs.framework.core;

import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.elementary.Linkings;
import org.bigraphs.framework.core.impl.elementary.Placings;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.*;

/**
 * Interface for the basic building blocks for all Ecore-based bigraphs.
 * <p>
 * Elementary bigraphs are used to build larger and more complex bigraphs by composition.
 *
 * @param <S> type of the signature
 * @author Dominik Grzelak
 */
public abstract class ElementaryBigraph<S extends AbstractEcoreSignature<? extends Control<?, ?>>> extends BigraphDelegator<S> implements EcoreBigraph<S> {
    protected EPackage metaModelPackage;
    protected EObject instanceModel;

    public ElementaryBigraph(Bigraph<S> bigraphDelegate) {
        super(bigraphDelegate);
    }

    public boolean isPlacing() {
        return this instanceof Placings.Barren ||
                this instanceof Placings.Identity1 ||
                this instanceof Placings.Join ||
                this instanceof Placings.Merge ||
                this instanceof Placings.Permutation ||
                this instanceof Placings.Symmetry;
    }

    public boolean isLinking() {
        return this instanceof Linkings.Closure ||
                this instanceof Linkings.Identity ||
                this instanceof Linkings.IdentityEmpty ||
                this instanceof Linkings.Substitution;
    }

    @Override
    public EPackage getMetaModel() {
        if (Objects.nonNull(bigraphDelegate) && bigraphDelegate instanceof EcoreBigraph)
            return ((EcoreBigraph) bigraphDelegate).getMetaModel();
        return metaModelPackage;
    }

    @Override
    public EObject getInstanceModel() {
        if (Objects.nonNull(bigraphDelegate) && bigraphDelegate instanceof EcoreBigraph)
            return ((EcoreBigraph) bigraphDelegate).getInstanceModel();
        return instanceModel;
    }

    @Override
    public List<BigraphEntity<?>> getAllPlaces() {
        if (Objects.nonNull(bigraphDelegate)) return (List<BigraphEntity<?>>) bigraphDelegate.getAllPlaces();
        return Lists.fixedSize.<BigraphEntity<?>>ofAll((Iterable) getRoots()).withAll((Iterable) getSites());
    }

    @Override
    public Collection<BigraphEntity.Link> getAllLinks() {
        if (Objects.nonNull(bigraphDelegate)) return bigraphDelegate.getAllLinks();
        return Lists.fixedSize.<BigraphEntity.Link>ofAll(getOuterNames()).withAll(getEdges());
    }

    @Override
    public Collection<BigraphEntity.RootEntity> getRoots() {
        if (Objects.nonNull(bigraphDelegate)) return bigraphDelegate.getRoots();
        return Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C extends Control<?, ?>> Collection<BigraphEntity.NodeEntity<C>> getNodes() {
        if (Objects.nonNull(bigraphDelegate)) return bigraphDelegate.getNodes();
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<BigraphEntity.SiteEntity> getSites() {
        if (Objects.nonNull(bigraphDelegate)) return bigraphDelegate.getSites();
        return Collections.emptyList();
    }

    @Override
    public Collection<BigraphEntity.OuterName> getOuterNames() {
        if (Objects.nonNull(bigraphDelegate)) return bigraphDelegate.getOuterNames();
        return Collections.emptyList();
    }

    @Override
    public Collection<BigraphEntity.InnerName> getInnerNames() {
        if (Objects.nonNull(bigraphDelegate)) return bigraphDelegate.getInnerNames();
        return Collections.emptyList();
    }

    @Override
    public Collection<BigraphEntity.Edge> getEdges() {
        if (Objects.nonNull(bigraphDelegate)) return bigraphDelegate.getEdges();
        return Collections.emptyList();
    }

    @Override
    public <C extends Control<?, ?>> int getPortCount(BigraphEntity.NodeEntity<C> node) {
        if (Objects.nonNull(bigraphDelegate)) return bigraphDelegate.getPortCount(node);
        return 0;
    }

    @Override
    public boolean isParentOf(BigraphEntity<?> node, BigraphEntity<?> possibleParent) {
        if (Objects.nonNull(bigraphDelegate)) return bigraphDelegate.isParentOf(node, possibleParent);
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public BigraphEntity<?> getParent(BigraphEntity<?> node) {
        if (Objects.nonNull(bigraphDelegate)) return bigraphDelegate.getParent(node);
        EObject instance = node.getInstance();
        EStructuralFeature prntRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if (Objects.nonNull(prntRef) && Objects.nonNull(instance.eGet(prntRef))) {
            EObject each = (EObject) instance.eGet(prntRef);
            // can only be a root
            Optional<BigraphEntity.RootEntity> rootEntity = getRoots().stream().filter(x -> x.getInstance().equals(each)).findFirst();
            return rootEntity.orElse(null);
        }
        return null;
    }

    @Override
    public BigraphEntity.Link getLinkOfPoint(BigraphEntity<?> point) {
        if (Objects.nonNull(bigraphDelegate)) return bigraphDelegate.getLinkOfPoint(point);
        if (!BigraphEntityType.isPointType(point)) return null;
        EObject eObject = point.getInstance();
        EStructuralFeature lnkRef = eObject.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
        if (Objects.isNull(lnkRef)) return null;
        EObject linkObject = (EObject) eObject.eGet(lnkRef);
        if (Objects.isNull(linkObject)) return null;
        if (!isBLink(linkObject)) return null; //"owner" problem
//        assert isBLink(linkObject);
//        Optional<BigraphEntity> lnkEntity;
        if (isBEdge(linkObject)) {
            Optional<BigraphEntity.Edge> first = getEdges().stream().filter(x -> x.getInstance().equals(linkObject)).findFirst();
            return first.orElse(null);
        } else {
            Optional<BigraphEntity.OuterName> first = getOuterNames().stream().filter(x -> x.getInstance().equals(linkObject)).findFirst();
            return first.orElse(null);
        }
    }

    @Override
    public int getLevelOf(BigraphEntity<?> place) {
        if (Objects.nonNull(bigraphDelegate)) return bigraphDelegate.getLevelOf(place);
        if (BigraphEntityType.isSite(place)) return 1;
        return 0;
    }

    @Override
    public Collection<BigraphEntity.InnerName> getSiblingsOfInnerName(BigraphEntity.InnerName innerName) {
        if (Objects.nonNull(bigraphDelegate)) return bigraphDelegate.getSiblingsOfInnerName(innerName);
        throw new RuntimeException("Not yet implemented! Elementary bigraph didn't implemented the method getSiblingsOfInnerName() yet.");
    }

    /**
     * Always returns {@code null}, since elementary bigraphs are node-free bigraphs
     *
     * @param port not considered
     * @param <C>  not considered
     * @return {@code null} is returned in every case
     */
    @Override
    public <C extends Control<?, ?>> BigraphEntity.NodeEntity<C> getNodeOfPort(BigraphEntity.Port port) {
        if (Objects.nonNull(bigraphDelegate)) return bigraphDelegate.getNodeOfPort(port);
        throw new RuntimeException("Not yet implemented! Elementary bigraph didn't implemented the method getNodeOfPort() yet.");
    }

    @Override
    public Collection<BigraphEntity<?>> getPointsFromLink(BigraphEntity.Link linkEntity) {
        if (Objects.nonNull(bigraphDelegate)) return bigraphDelegate.getPointsFromLink(linkEntity);
        if (Objects.isNull(linkEntity) || !isBLink(linkEntity.getInstance()))
            return Collections.EMPTY_LIST;
        final EObject eObject = linkEntity.getInstance();
        final EStructuralFeature pointsRef = eObject.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_POINT);
        if (Objects.isNull(pointsRef)) return Collections.EMPTY_LIST;
        final EList<EObject> pointsObjects = (EList<EObject>) eObject.eGet(pointsRef);
        if (Objects.isNull(pointsObjects)) return Collections.EMPTY_LIST;

        final Collection<BigraphEntity<?>> result = new ArrayList<>();
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
    public List<BigraphEntity<?>> getSiblingsOfNode(BigraphEntity<?> node) {
        if (Objects.nonNull(bigraphDelegate)) return (List<BigraphEntity<?>>) bigraphDelegate.getSiblingsOfNode(node);
        throw new RuntimeException("Not yet implemented! Elementary bigraph didn't implemented the method getSiblingsOfNode(BigraphEntity) yet.");
    }

    @Override
    public List<BigraphEntity<?>> getChildrenOf(BigraphEntity<?> node) {
        if (Objects.nonNull(bigraphDelegate)) return (List<BigraphEntity<?>>) bigraphDelegate.getChildrenOf(node);
        return Collections.EMPTY_LIST;
    }


    @Override
    public Collection<BigraphEntity.Port> getPorts(BigraphEntity<?> node) {
        if (Objects.nonNull(bigraphDelegate)) return bigraphDelegate.getPorts(node);
        return Collections.EMPTY_LIST;
    }

    @Override
    public final <C extends Control<?, ?>> boolean areConnected(BigraphEntity.NodeEntity<C> place1, BigraphEntity.NodeEntity<C> place2) {
        if (Objects.nonNull(bigraphDelegate)) return bigraphDelegate.areConnected(place1, place2);
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public List<BigraphEntity<?>> getOpenNeighborhoodOfVertex(BigraphEntity<?> node) {
        if (Objects.nonNull(bigraphDelegate)) return bigraphDelegate.getOpenNeighborhoodOfVertex(node);
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public BigraphEntity.RootEntity getTopLevelRoot(BigraphEntity node) {
        if (Objects.nonNull(bigraphDelegate)) return bigraphDelegate.getTopLevelRoot(node);
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
