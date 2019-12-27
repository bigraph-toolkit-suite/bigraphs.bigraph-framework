package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import java.util.Collection;
import java.util.List;

/**
 * Delegator class for bigraphs (currently only suitable for pure bigraphs, e.g., no getParents() method available)
 *
 * @param <S> type of the signature
 * @author Dominik Grzelak
 */
public abstract class BigraphDelegator<S extends Signature> implements Bigraph<S> {

    protected Bigraph<S> bigraphDelegate;

    public BigraphDelegator(Bigraph<S> bigraphDelegate) {
        this.bigraphDelegate = bigraphDelegate;
    }

    protected <B extends Bigraph<S>> B getBigraphDelegate() {
        return (B) bigraphDelegate;
    }

    @Override
    public S getSignature() {
        return bigraphDelegate.getSignature();
    }

    @Override
    public Collection<BigraphEntity.RootEntity> getRoots() {
        return bigraphDelegate.getRoots();
    }

    @Override
    public Collection<BigraphEntity.SiteEntity> getSites() {
        return bigraphDelegate.getSites();
    }

    @Override
    public Collection<BigraphEntity.OuterName> getOuterNames() {
        return bigraphDelegate.getOuterNames();
    }

    @Override
    public Collection<BigraphEntity.InnerName> getInnerNames() {
        return bigraphDelegate.getInnerNames();
    }

    @Override
    public Collection<BigraphEntity.Edge> getEdges() {
        return bigraphDelegate.getEdges();
    }

    @Override
    public <C extends Control> Collection<BigraphEntity.NodeEntity<C>> getNodes() {
        return bigraphDelegate.getNodes();
    }

    @Override
    public Collection<BigraphEntity> getChildrenOf(BigraphEntity node) {
        return bigraphDelegate.getChildrenOf(node);
    }

    @Override
    public boolean areConnected(BigraphEntity.NodeEntity place1, BigraphEntity.NodeEntity place2) {
        return bigraphDelegate.areConnected(place1, place2);
    }

    @Override
    public BigraphEntity getParent(BigraphEntity node) {
        return bigraphDelegate.getParent(node);
    }

    @Override
    public BigraphEntity getLinkOfPoint(BigraphEntity point) {
        return bigraphDelegate.getLinkOfPoint(point);
    }

    @Override
    public Collection<BigraphEntity> getPointsFromLink(BigraphEntity linkEntity) {
        return bigraphDelegate.getPointsFromLink(linkEntity);
    }

    @Override
    public List<BigraphEntity> getOpenNeighborhoodOfVertex(BigraphEntity node) {
        return bigraphDelegate.getOpenNeighborhoodOfVertex(node);
    }

    @Override
    public BigraphEntity getTopLevelRoot(BigraphEntity node) {
        return bigraphDelegate.getTopLevelRoot(node);
    }

    @Override
    public boolean isParentOf(BigraphEntity node, BigraphEntity possibleParent) {
        return bigraphDelegate.isParentOf(node, possibleParent);
    }

    @Override
    public Collection<BigraphEntity.InnerName> getSiblingsOfInnerName(BigraphEntity.InnerName innerName) {
        return bigraphDelegate.getSiblingsOfInnerName(innerName);
    }

    @Override
    public <C extends Control> BigraphEntity.NodeEntity<C> getNodeOfPort(BigraphEntity.Port port) {
        return bigraphDelegate.getNodeOfPort(port);
    }

    @Override
    public Collection<BigraphEntity> getSiblingsOfNode(BigraphEntity node) {
        return bigraphDelegate.getSiblingsOfNode(node);
    }

    @Override
    public int getLevelOf(BigraphEntity place) {
        return bigraphDelegate.getLevelOf(place);
    }

    @Override
    public Collection<BigraphEntity> getAllPlaces() {
        return bigraphDelegate.getAllPlaces();
    }

    @Override
    public Collection<BigraphEntity.Port> getPorts(BigraphEntity node) {
        return bigraphDelegate.getPorts(node);
    }

    @Override
    public int getPortCount(BigraphEntity node) {
        return bigraphDelegate.getPortCount(node);
    }

//    @Override
//    public EPackage getModelPackage() {
//        return bigraphDelegate.getModelPackage();
//    }
//
//    @Override
//    public EObject getModel() {
//        return bigraphDelegate.getModel();
//    }
}
