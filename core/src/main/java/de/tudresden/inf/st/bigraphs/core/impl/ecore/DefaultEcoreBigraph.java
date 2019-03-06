package de.tudresden.inf.st.bigraphs.core.impl.ecore;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.model2.Root;
import de.tudresden.inf.st.bigraphs.model.BigraphBaseModel.*;
import org.eclipse.emf.common.util.EList;

//TODO immutable? besser, das erstellen wird separat erledigt, entweder über M2M und sowieso über ecore
//the signature etc are all ecore things
//kapselt das ecore model
//als decorator?
public class DefaultEcoreBigraph implements Bigraph<Signature> {
    private BigraphMetaModelPackage modelPackage; //TODO wirklich diese package?

    private Root root = null;

    //Fertig gebaute bigraph model
    //TODO see ecorebuilder....
    public DefaultEcoreBigraph(BigraphMetaModelPackage modelPackage) {
        this.modelPackage = modelPackage;
    }

    public Root getRoot() {
        return root;
    }

    public void setRoot(Root root) {
        this.root = root;
    }

    @Override
    public Signature getSignature() {
        return null;
    }

    @Override
    public Iterable<Root> getRoots() {
        return null;
    }

    @Override
    public Iterable<BSite> getSites() {
        return null;
    }

    @Override
    public Iterable<BOuterName> getOuterNames() {
        return null;
    }

    @Override
    public Iterable<BInnerName> getInnerNames() {
        return null;
    }

    @Override
    public <T extends BNode> boolean areConnected(T place1, T place2) {
        EList<BPort> bPorts = place1.getBPorts();
        for (BPort bPort : bPorts) {
            EList<BPoint> bPoints = bPort.getBLink().getBPoints();
            for (BPoint bPoint : bPoints) {
                if (bPoint instanceof BPort) {
                    if (((BPort) bPoint).getBNode().equals(place2)) {
                        System.out.println("connected");
                        return true;
                    }
                }
            }
        }
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
