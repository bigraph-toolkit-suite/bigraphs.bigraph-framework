package de.tudresden.inf.st.bigraphs.core.impl.ecore;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.*;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

//TODO immutable? besser, das erstellen wird separat erledigt, entweder über M2M und sowieso über ecore
//the signature etc are all ecore things
//kapselt das ecore model
//als decorator?
public class DynamicEcoreBigraph implements Bigraph<Signature> {
    private BigraphBaseModelPackage modelPackage; //TODO wirklich diese package?

    private EObject root = null;

    //Fertig gebaute bigraph model
    //TODO see ecorebuilder....
    public DynamicEcoreBigraph(BigraphBaseModelPackage modelPackage) {
        this.modelPackage = modelPackage;
    }

    public EObject getRoot() {
        return root;
    }

    public void setRoot(EObject root) {
        this.root = root;
    }

    @Override
    public Signature getSignature() {
        return null;
    }

    @Override
    public Iterable<EObject> getRoots() {
        return null;
    }

    @Override
    public Iterable<EObject> getSites() {
        return null;
    }

    @Override
    public Iterable<EObject> getOuterNames() {
        return null;
    }

    @Override
    public Iterable<EObject> getInnerNames() {
        return null;
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
