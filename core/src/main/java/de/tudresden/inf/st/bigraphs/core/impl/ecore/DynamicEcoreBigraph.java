package de.tudresden.inf.st.bigraphs.core.impl.ecore;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import java.util.Collection;

//TODO immutable? besser, das erstellen wird separat erledigt, entweder über M2M und sowieso über ecore
//the signature etc are all ecore things
//kapselt das ecore model
//als decorator?
public class DynamicEcoreBigraph implements Bigraph<Signature> {
    private EPackage modelPackage; //TODO wirklich diese package?

    private Collection<BigraphEntity.RootEntity> roots = null;
    private Collection<BigraphEntity.SiteEntity> sites = null;
    private Collection<BigraphEntity.InnerName> innerNames = null;
    private Collection<BigraphEntity.OuterName> outerNames = null;
    private Collection<BigraphEntity.Edge> edges = null;
    private Signature signature;

    //Fertig gebaute bigraph model
    //TODO see ecorebuilder....
    public DynamicEcoreBigraph(EPackage modelPackage, Signature signature, Collection<BigraphEntity.RootEntity> roots,
                               Collection<BigraphEntity.SiteEntity> sites,
                               Collection<BigraphEntity.InnerName> innerNames,
                               Collection<BigraphEntity.OuterName> outerNames,
                               Collection<BigraphEntity.Edge> edges) {
        this.modelPackage = modelPackage;
        this.roots = roots;
        this.sites = sites;
        this.outerNames = outerNames;
        this.innerNames = innerNames;
        this.edges = edges;
        this.signature = signature;
    }

    public EPackage getModelPackage() {
        return modelPackage;
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
