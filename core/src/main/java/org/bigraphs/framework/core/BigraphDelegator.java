package org.bigraphs.framework.core;

import org.bigraphs.framework.core.impl.BigraphEntity;

import java.util.Collection;
import java.util.List;

/**
 * Delegator base class for bigraphs.
 * <p>
 * Currently supports only pure bigraphs.
 * <p>
 * Wraps a {@link Bigraph} instance and forwards all calls to it.
 * Subclasses can extend or adapt behavior by overriding selected
 * methods while reusing the delegate for the core implementation.
 *
 * @param <S> the signature type
 * @author Dominik Grzelak
 */
public abstract class BigraphDelegator<S extends Signature<?>> implements Bigraph<S> {

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
    public <C extends Control<?, ?>> Collection<BigraphEntity.NodeEntity<C>> getNodes() {
        return bigraphDelegate.getNodes();
    }

    @Override
    public Collection<BigraphEntity<?>> getChildrenOf(BigraphEntity<?> node) {
        return bigraphDelegate.getChildrenOf(node);
    }

    @Override
    public <C extends Control<?, ?>> boolean areConnected(BigraphEntity.NodeEntity<C> place1, BigraphEntity.NodeEntity<C> place2) {
        return bigraphDelegate.areConnected(place1, place2);
    }

    @Override
    public BigraphEntity<?> getParent(BigraphEntity<?> node) {
        return bigraphDelegate.getParent(node);
    }

    @Override
    public BigraphEntity.Link getLinkOfPoint(BigraphEntity<?> point) {
        return bigraphDelegate.getLinkOfPoint(point);
    }

    @Override
    public Collection<BigraphEntity<?>> getPointsFromLink(BigraphEntity.Link linkEntity) {
        return bigraphDelegate.getPointsFromLink(linkEntity);
    }

    @Override
    public List<BigraphEntity<?>> getOpenNeighborhoodOfVertex(BigraphEntity<?> node) {
        return bigraphDelegate.getOpenNeighborhoodOfVertex(node);
    }

    @Override
    public BigraphEntity.RootEntity getTopLevelRoot(BigraphEntity<?> node) {
        return bigraphDelegate.getTopLevelRoot(node);
    }

    @Override
    public boolean isParentOf(BigraphEntity<?> node, BigraphEntity<?> possibleParent) {
        return bigraphDelegate.isParentOf(node, possibleParent);
    }

    @Override
    public Collection<BigraphEntity.InnerName> getSiblingsOfInnerName(BigraphEntity.InnerName innerName) {
        return bigraphDelegate.getSiblingsOfInnerName(innerName);
    }

    @Override
    public <C extends Control<?, ?>> BigraphEntity.NodeEntity<C> getNodeOfPort(BigraphEntity.Port port) {
        return bigraphDelegate.getNodeOfPort(port);
    }

    @Override
    public Collection<BigraphEntity<?>> getSiblingsOfNode(BigraphEntity<?> node) {
        return bigraphDelegate.getSiblingsOfNode(node);
    }

    @Override
    public int getLevelOf(BigraphEntity<?> place) {
        return bigraphDelegate.getLevelOf(place);
    }

    @Override
    public Collection<BigraphEntity<?>> getAllPlaces() {
        return bigraphDelegate.getAllPlaces();
    }

    @Override
    public Collection<BigraphEntity.Link> getAllLinks() {
        return bigraphDelegate.getAllLinks();
    }

    @Override
    public Collection<BigraphEntity.Port> getPorts(BigraphEntity<?> node) {
        return bigraphDelegate.getPorts(node);
    }

    @Override
    public <C extends Control<?, ?>> int getPortCount(BigraphEntity.NodeEntity<C> node) {
        return bigraphDelegate.getPortCount(node);
    }

}
