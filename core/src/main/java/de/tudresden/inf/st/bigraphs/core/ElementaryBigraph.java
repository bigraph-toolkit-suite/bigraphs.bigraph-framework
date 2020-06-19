package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.EcoreBigraph;
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
public abstract class ElementaryBigraph<S extends Signature<? extends Control<?, ?>>> extends BigraphDelegator<S> implements EcoreBigraph {

    public ElementaryBigraph(Bigraph<S> bigraphDelegate) {
        super(bigraphDelegate);
    }

    @Override
    public EPackage getModelPackage() {
        return ((EcoreBigraph) bigraphDelegate).getModelPackage();
    }

    @Override
    public EObject getModel() {
        return ((EcoreBigraph) bigraphDelegate).getModel();
    }

    @Override
    public Collection<BigraphEntity.RootEntity> getRoots() {
        return Collections.emptyList();
    }

    @Override
    public Collection<BigraphEntity.SiteEntity> getSites() {
        return Collections.emptyList();
    }

    @Override
    public Collection<BigraphEntity.OuterName> getOuterNames() {
        return Collections.emptyList();
    }

    @Override
    public Collection<BigraphEntity.InnerName> getInnerNames() {
        return Collections.emptyList();
    }

    @Override
    public Collection<BigraphEntity.Edge> getEdges() {
        return Collections.emptyList();
    }

    @Override
    public int getPortCount(BigraphEntity node) {
        return 0;
    }

    @Override
    public boolean isParentOf(BigraphEntity node, BigraphEntity possibleParent) {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public BigraphEntity getParent(BigraphEntity node) {
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
    public BigraphEntity getLinkOfPoint(BigraphEntity point) {
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
    public int getLevelOf(BigraphEntity place) {
        if (BigraphEntityType.isSite(place)) return 1;
        return 0;
    }

    @Override
    public Collection<BigraphEntity.InnerName> getSiblingsOfInnerName(BigraphEntity.InnerName innerName) {
        return Collections.emptyList();
    }

    @Override
    public Collection<BigraphEntity> getAllPlaces() {
        return Collections.EMPTY_LIST;
    }

    /**
     * Always returns {@code null}, since elementary bigraphs are node-free bigraphs
     *
     * @param port not considered
     * @param <C>  not considered
     * @return {@code null} is returned in every case
     */
    @Override
    public <C extends Control> BigraphEntity.NodeEntity<C> getNodeOfPort(BigraphEntity.Port port) {
        return null;
    }

    @Override
    public Collection<BigraphEntity> getPointsFromLink(BigraphEntity linkEntity) {
        if (Objects.isNull(linkEntity) || !isBLink(linkEntity.getInstance()))
            return Collections.EMPTY_LIST;
        final EObject eObject = linkEntity.getInstance();
        final EStructuralFeature pointsRef = eObject.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_POINT);
        if (Objects.isNull(pointsRef)) return Collections.EMPTY_LIST;
        final EList<EObject> pointsObjects = (EList<EObject>) eObject.eGet(pointsRef);
        if (Objects.isNull(pointsObjects)) return Collections.EMPTY_LIST;

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
    public <C extends Control> Collection<BigraphEntity.NodeEntity<C>> getNodes() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<BigraphEntity> getSiblingsOfNode(BigraphEntity node) {
        throw new RuntimeException("Not yet implemented! Elementary bigraph didn't implemented the method getSiblingsOfNode(BigraphEntity) yet.");
    }

    //TODO for discrete ion important!
    @Override
    public final Collection<BigraphEntity> getChildrenOf(BigraphEntity node) {
        return Collections.EMPTY_LIST;
    }


    @Override
    public Collection<BigraphEntity.Port> getPorts(BigraphEntity node) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public final boolean areConnected(BigraphEntity.NodeEntity place1, BigraphEntity.NodeEntity place2) {
        return false;
    }
}
