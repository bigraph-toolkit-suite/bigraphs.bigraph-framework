package org.bigraphs.framework.core.impl.pure;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.Control;
import org.bigraphs.framework.core.EcoreBigraph;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.signature.KindSignature;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import java.util.Collection;
import java.util.List;

public class KindBigraph implements Bigraph<KindSignature>, EcoreBigraph<KindSignature> {

    @Override
    public KindSignature getSignature() {
        return null;
    }

    @Override
    public int getLevelOf(BigraphEntity<?> place) {
        return 0;
    }

    @Override
    public List<BigraphEntity<?>> getOpenNeighborhoodOfVertex(BigraphEntity<?> node) {
        return null;
    }

    @Override
    public Collection<BigraphEntity.RootEntity> getRoots() {
        return null;
    }

    @Override
    public Collection<BigraphEntity.SiteEntity> getSites() {
        return null;
    }

    @Override
    public Collection<BigraphEntity.OuterName> getOuterNames() {
        return null;
    }

    @Override
    public Collection<BigraphEntity.InnerName> getInnerNames() {
        return null;
    }

    @Override
    public Collection<BigraphEntity<?>> getAllPlaces() {
        return null;
    }

    @Override
    public Collection<BigraphEntity.Link> getAllLinks() {
        return null;
    }

    @Override
    public Collection<BigraphEntity.Edge> getEdges() {
        return null;
    }

    @Override
    public <C extends Control<?, ?>> Collection<BigraphEntity.NodeEntity<C>> getNodes() {
        return null;
    }

    @Override
    public BigraphEntity.RootEntity getTopLevelRoot(BigraphEntity<?> node) {
        return null;
    }

    @Override
    public boolean isParentOf(BigraphEntity<?> node, BigraphEntity<?> possibleParent) {
        return false;
    }

    @Override
    public Collection<BigraphEntity<?>> getChildrenOf(BigraphEntity<?> node) {
        return null;
    }

    @Override
    public BigraphEntity<?> getParent(BigraphEntity<?> node) {
        return null;
    }

    @Override
    public BigraphEntity.Link getLinkOfPoint(BigraphEntity<?> point) {
        return null;
    }

    @Override
    public Collection<BigraphEntity.Port> getPorts(BigraphEntity<?> node) {
        return null;
    }

    @Override
    public <C extends Control<?, ?>> int getPortCount(BigraphEntity.NodeEntity<C> node) {
        return 0;
    }

    @Override
    public <C extends Control<?, ?>> BigraphEntity.NodeEntity<C> getNodeOfPort(BigraphEntity.Port port) {
        return null;
    }

    @Override
    public Collection<BigraphEntity<?>> getSiblingsOfNode(BigraphEntity<?> node) {
        return null;
    }

    @Override
    public Collection<BigraphEntity.InnerName> getSiblingsOfInnerName(BigraphEntity.InnerName innerName) {
        return null;
    }

    @Override
    public Collection<BigraphEntity<?>> getPointsFromLink(BigraphEntity.Link linkEntity) {
        return null;
    }

    @Override
    public <C extends Control<?, ?>> boolean areConnected(BigraphEntity.NodeEntity<C> place1, BigraphEntity.NodeEntity<C> place2) {
        return false;
    }

    @Override
    public EPackage getMetaModel() {
        return null;
    }

    @Override
    public EObject getInstanceModel() {
        return null;
    }
}
