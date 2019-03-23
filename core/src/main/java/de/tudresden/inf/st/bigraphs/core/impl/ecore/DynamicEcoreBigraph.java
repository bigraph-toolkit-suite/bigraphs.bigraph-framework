package de.tudresden.inf.st.bigraphs.core.impl.ecore;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphBuilder;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.*;
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
public class DynamicEcoreBigraph implements Bigraph<Signature<DefaultDynamicControl<?, ?>>> {
    private EPackage modelPackage; //TODO wirklich diese package?

    private final Set<BigraphEntity.RootEntity> roots;
    private final Set<BigraphEntity.SiteEntity> sites;
    private final Set<BigraphEntity.InnerName> innerNames;
    private final Set<BigraphEntity.OuterName> outerNames;
    private final Set<BigraphEntity.Edge> edges;
    private final Set<BigraphEntity.NodeEntity<DefaultDynamicControl>> nodes;
    private final Signature<DefaultDynamicControl<?, ?>> signature;

    //Fertig gebaute bigraph model
    //TODO see ecorebuilder....
    public DynamicEcoreBigraph(BigraphBuilder.InstanceParameter details) {
        this.modelPackage = details.getModelPackage();
        this.roots = Collections.unmodifiableSet(details.getRoots()); //roots;
        this.sites = Collections.unmodifiableSet(details.getSites()); //sites;
        this.nodes = Collections.unmodifiableSet(details.getNodes()); //nodes;
        this.outerNames = Collections.unmodifiableSet(details.getOuterNames()); //outerNames;
        this.innerNames = Collections.unmodifiableSet(details.getInnerNames()); //innerNames;
        this.edges = Collections.unmodifiableSet(details.getEdges()); //edges;
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
    public Set<BigraphEntity> getChildrenOf(BigraphEntity node) {
        EObject instance = node.getInstance();
        EStructuralFeature chldRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
        Set<BigraphEntity> children = new LinkedHashSet<>();
        if (Objects.nonNull(chldRef)) {
            EList<EObject> childs = (EList<EObject>) instance.eGet(chldRef);
            for (EObject eachChild : childs) {
                if (isBNode(eachChild)) {//TODO set could be inefficient here for large bigraphs
                    Optional<BigraphEntity.NodeEntity<DefaultDynamicControl>> nodeEntity = nodes.stream().filter(x -> x.getInstance().equals(eachChild)).findFirst();
                    //control can be acquired by the class name + signature
//                    String controlName = eachChild.eClass().getName();
//                    Control control = getSignature().getControlByName(controlName);
//                    children.add(BigraphEntity.createNode(eachChild, control));
                    nodeEntity.ifPresent(children::add);
                }
//                else if (isSite(eachChild)) {
//                    children.add(BigraphEntity.create(eachChild, BigraphEntity.SiteEntity.class));
//                }
            }
        }
        return children;
    }

    @Override
    public Set<BigraphEntity.NodeEntity<DefaultDynamicControl>> getNodes() {
        return this.nodes;
    }

//    //FOR MATCHING
//    @Deprecated //in adapter
//    public Set<BigraphEntity> getNodesWithRoots() {
//        Set<BigraphEntity> allVertices = new LinkedHashSet<>();
//        allVertices.addAll(nodes);
//        allVertices.addAll(roots);
//        return allVertices;
//    }

    public BigraphEntity getTopLevelRoot(BigraphEntity node) {
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
    public boolean areConnected(BigraphEntity.NodeEntity place1, BigraphEntity.NodeEntity place2) {
        EStructuralFeature portsRef = place1.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PORT);
        if (Objects.isNull(portsRef)) return false;
        EList<EObject> bPorts = (EList<EObject>) place1.getInstance().eGet(portsRef);
        for (EObject bPort : bPorts) {
            EStructuralFeature linkRef = bPort.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
            if (Objects.isNull(linkRef)) return false;
            EObject linkObject = (EObject) bPort.eGet(linkRef);
            if (Objects.isNull(linkObject)) continue;
            EStructuralFeature pointsRef = linkObject.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_POINT);
            if (Objects.isNull(pointsRef)) continue;
            EList<EObject> bPoints = (EList<EObject>) linkObject.eGet(pointsRef);
            for (EObject bPoint : bPoints) {
                if (isBPort(bPoint)) {
                    EStructuralFeature nodeRef = bPoint.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_NODE);
                    assert nodeRef != null;
                    if (bPoint.eGet(nodeRef).equals(place2.getInstance())) {
                        System.out.println("connected");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected boolean isBPort(EObject eObject) {
        return eObject.eClass().getClassifierID() ==
                (((EPackageImpl) getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_PORT)).getClassifierID() ||
                eObject.eClass().getEAllSuperTypes().contains(((EPackageImpl) getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_PORT));
    }

    protected boolean isBNode(EObject eObject) {
        return eObject.eClass().equals(((EPackageImpl) getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_NODE)) ||
                eObject.eClass().getEAllSuperTypes().contains(((EPackageImpl) getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_NODE));
    }
}
