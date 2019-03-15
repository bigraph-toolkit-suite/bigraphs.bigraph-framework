package de.tudresden.inf.st.bigraphs.core.impl.ecore;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.*;

//TODO immutable? besser, das erstellen wird separat erledigt, entweder über M2M und sowieso über ecore
//the signature etc are all ecore things
//kapselt das ecore model
//als decorator?

/**
 * Immutable bigraph data structure with some operations
 */
public class DynamicEcoreBigraph implements Bigraph<Signature> {
    private EPackage modelPackage; //TODO wirklich diese package?

    private Collection<BigraphEntity.RootEntity> roots = null;
    private Collection<BigraphEntity.SiteEntity> sites = null;
    private Collection<BigraphEntity.InnerName> innerNames = null;
    private Collection<BigraphEntity.OuterName> outerNames = null;
    private Collection<BigraphEntity.Edge> edges = null;
    private Collection<BigraphEntity.NodeEntity> nodes; //TODO: node set per tree??
    private Signature signature;

    //Fertig gebaute bigraph model
    //TODO see ecorebuilder....
    public DynamicEcoreBigraph(EPackage modelPackage, Signature signature, Collection<BigraphEntity.RootEntity> roots,
                               Collection<BigraphEntity.SiteEntity> sites,
                               Collection<BigraphEntity.NodeEntity> nodes,
                               Collection<BigraphEntity.InnerName> innerNames,
                               Collection<BigraphEntity.OuterName> outerNames,
                               Collection<BigraphEntity.Edge> edges) {
        this.modelPackage = modelPackage;
        this.roots = roots;
        this.sites = sites;
        this.nodes = nodes;
        this.outerNames = outerNames;
        this.innerNames = innerNames;
        this.edges = edges;
        this.signature = signature;
    }

    public EPackage getModelPackage() {
        return this.modelPackage;
    }

    @Override
    public Signature getSignature() {
        return signature;
    }

    @Override
    public Iterable<BigraphEntity.RootEntity> getRoots() {
        return this.roots;
    }

    @Override
    public Iterable<BigraphEntity.SiteEntity> getSites() {
        return this.sites;
    }

    @Override
    public Iterable<BigraphEntity.OuterName> getOuterNames() {
        return this.outerNames;
    }

    @Override
    public Iterable<BigraphEntity.InnerName> getInnerNames() {
        return this.innerNames;
    }

    @Override
    public Iterable<BigraphEntity.Edge> getEdges() {
        return this.edges;
    }

    @Override
    public Iterable<BigraphEntity.NodeEntity> getNodes() {
        return this.nodes;
    }

    @Override
    public boolean isGround() {
        return innerNames.size() == 0 && sites.size() == 0;
    }

    public int degreeOf(BigraphEntity.NodeEntity nodeEntity) {
        //get all edges
        EObject instance = nodeEntity.getInstance();
        int cnt = 0;
        EStructuralFeature portRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PORT);
        if (Objects.nonNull(portRef)) {
            EList<EObject> portList = (EList<EObject>) instance.eGet(portRef);
            cnt += portList.size();
        }
        EStructuralFeature chldRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
        if (Objects.nonNull(chldRef)) {
            EList<EObject> childs = (EList<EObject>) instance.eGet(chldRef);
            cnt += childs.size();
        }
        EStructuralFeature prntRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if (Objects.nonNull(prntRef) && Objects.nonNull(instance.eGet(prntRef))) {
            cnt++;
        }
        return cnt;
    }

    public List<BigraphEntity.NodeEntity> getChildren(BigraphEntity.NodeEntity node) {
        EObject instance = node.getInstance();
        EStructuralFeature chldRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
        if (Objects.nonNull(chldRef)) {
            EList<EObject> childs = (EList<EObject>) instance.eGet(chldRef);
            //create class ...
            //control can be acquired by the class name + signature
        }
        return null;
    }

    public Iterable<BigraphEntity.NodeEntity> getAllLeaves(int rootIdx) {
        List<BigraphEntity.NodeEntity> leaves = new ArrayList<>();
        for (BigraphEntity.NodeEntity node : nodes) {
            if (degreeOf(node) <= 1) {
                leaves.add(node);
            }
        }
        return leaves;
    }

    @Override
    public <T extends EObject> boolean areConnected(T place1, T place2) {
//        EList<BPort> bPorts = place1.getBPorts();
//        for (BPort bPort : bPorts) {
//            EList<BPoint> bPoints = bPort.getBLink().getBPoints();
//            for (BPoint bPoint : bPoints) {
//                if (bPoint instanceof BPort) {
//                    if (((BPort) bPoint).getBNode().equals(place2)) {
//                        System.out.println("connected");
//                        return true;
//                    }
//                }
//            }
//        }
        return false;
    }

    //    @Override
//    public boolean areConnected(BNode place1, BNode place2) {
//        EList<BPort> bPorts = place1.getBPorts();
//        for (BPort bPort : bPorts) {
//            EList<BPoint> bPoints = bPort.getBLink().getBPoints();
//            for (BPoint bPoint : bPoints) {
//                if (bPoint instanceof BPort) {
//                    if (((BPort) bPoint).getBNode().equals(place2)) {
//                        System.out.println("connected");
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }
}
