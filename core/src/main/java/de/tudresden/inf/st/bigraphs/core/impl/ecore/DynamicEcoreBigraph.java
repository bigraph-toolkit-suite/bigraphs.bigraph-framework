package de.tudresden.inf.st.bigraphs.core.impl.ecore;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.EcoreBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EPackageImpl;

import java.util.*;

//the signature etc are all ecore things
//kapselt das ecore model
//als decorator?

/**
 * Immutable bigraph data structure with some operations.
 * <p>
 * Ecore model of a bigraph. The elements are stored also separately in collections for easier access.
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
    public DynamicEcoreBigraph(
            EcoreBigraphBuilder.BigraphInstanceDetails details
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
    public Collection<BigraphEntity.Edge> getEdges() {
        return this.edges;
    }

    @Override
    public Collection<BigraphEntity.NodeEntity> getNodes() {
        return this.nodes;
    }

    //FOR MATCHING
    public Collection<BigraphEntity> getNodesWithRoots() {
        ArrayList<BigraphEntity> allVertices = new ArrayList<>();
        allVertices.addAll(nodes);
        allVertices.addAll(roots);
        return allVertices;
    }


    @Override
    public boolean isGround() {
        return innerNames.size() == 0 && sites.size() == 0;
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
