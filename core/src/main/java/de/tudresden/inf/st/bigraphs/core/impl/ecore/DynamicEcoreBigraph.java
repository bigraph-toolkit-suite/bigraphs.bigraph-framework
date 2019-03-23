package de.tudresden.inf.st.bigraphs.core.impl.ecore;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphBuilder;
import org.eclipse.emf.ecore.*;

import java.util.*;

//the signature etc are all ecore things
//kapselt das ecore model
//als decorator?

/**
 * Immutable bigraph data structure with some operations.
 * <p>
 * Ecore model of a bigraph. The elements are stored also separately in collections for easier access.
 */
public class DynamicEcoreBigraph implements Bigraph<Signature<DefaultDynamicControl<?, ?>>> {
    private EPackage modelPackage; //TODO wirklich diese package?

    private Set<BigraphEntity.RootEntity> roots = null;
    private Set<BigraphEntity.SiteEntity> sites = null;
    private Set<BigraphEntity.InnerName> innerNames = null;
    private Set<BigraphEntity.OuterName> outerNames = null;
    private Set<BigraphEntity.Edge> edges = null;
    private Set<BigraphEntity.NodeEntity> nodes; //TODO: node set per tree??
    private Signature signature;

    //Fertig gebaute bigraph model
    //TODO see ecorebuilder....
    public DynamicEcoreBigraph(
            BigraphBuilder.BigraphInstanceDetails details
//            EPackage modelPackage, Signature signature, Collection<BigraphEntity.RootEntity> roots,
//                               Collection<BigraphEntity.SiteEntity> sites,
//                               Collection<BigraphEntity.NodeEntity> nodes,
//                               Collection<BigraphEntity.InnerName> innerNames,
//                               Collection<BigraphEntity.OuterName> outerNames,
//                               Collection<BigraphEntity.Edge> edges
    ) {
        this.modelPackage = details.getModelPackage();
        this.roots = details.getRoots(); //roots;
        this.sites = details.getSites(); //sites;
        this.nodes = details.getNodes(); //nodes;
        this.outerNames = details.getOuterNames(); //outerNames;
        this.innerNames = details.getInnerNames(); //innerNames;
        this.edges = details.getEdges(); //edges;
        this.signature = details.getSignature(); //signature;
    }

    public EPackage getModelPackage() {
        return this.modelPackage;
    }

    @Override
    public Signature getSignature() {
        return signature;
    }

    @Override
    public Set<BigraphEntity.RootEntity> getRoots() {
        return this.roots;
    }


    @Override
    public Set<BigraphEntity.SiteEntity> getSites() {
        return this.sites;
    }

    @Override
    public Set<BigraphEntity.OuterName> getOuterNames() {
        return this.outerNames;
    }

    @Override
    public Set<BigraphEntity.InnerName> getInnerNames() {
        return this.innerNames;
    }

    @Override
    public Set<BigraphEntity.Edge> getEdges() {
        return this.edges;
    }

    @Override
    public Set<BigraphEntity.NodeEntity> getNodes() {
        return this.nodes;
    }

    //FOR MATCHING
    @Deprecated //in adapter
    public Set<BigraphEntity> getNodesWithRoots() {
        Set<BigraphEntity> allVertices = new LinkedHashSet<>();
        allVertices.addAll(nodes);
        allVertices.addAll(roots);
        return allVertices;
    }


    @Override
    public boolean isGround() {
        return innerNames.size() == 0 && sites.size() == 0;
    }

    public BigraphEntity getTopLevelRoot(BigraphEntity node) {
        EPackage loadedEPackage = getModelPackage();
        EStructuralFeature prntRef = node.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if (node.getInstance().eGet(prntRef) != null) {
            return getTopLevelRoot(BigraphEntity.create((EObject) node.getInstance().eGet(prntRef), BigraphEntity.RootEntity.class));
        }
        return node;
    }

    public EObject getTopLevelRoot(EObject node) {
        EStructuralFeature prntRef = node.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if (node.eGet(prntRef) != null) {
            return getTopLevelRoot((EObject) node.eGet(prntRef));
        }
        return node;
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
