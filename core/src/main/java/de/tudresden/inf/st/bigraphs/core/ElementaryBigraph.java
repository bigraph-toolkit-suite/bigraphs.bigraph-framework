package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EPackageImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

/**
 * Interface for the basic building blocks for all bigraphs.
 * <p>
 * With them other larger bigraphs can be built.
 */
public abstract class ElementaryBigraph<S extends Signature> implements Bigraph<S> {

    @Override
    public Collection<BigraphEntity.RootEntity> getRoots() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<BigraphEntity.SiteEntity> getSites() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<BigraphEntity.OuterName> getOuterNames() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<BigraphEntity.InnerName> getInnerNames() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<BigraphEntity.Edge> getEdges() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public BigraphEntity getParent(BigraphEntity node) {
        EObject instance = node.getInstance();
        EStructuralFeature prntRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if (Objects.nonNull(prntRef) && Objects.nonNull(instance.eGet(prntRef))) {
            EObject each = (EObject) instance.eGet(prntRef);
            // can only be a root
            Optional<BigraphEntity.RootEntity> rootEntity = getRoots().stream().filter(x -> x.getInstance().equals(each)).findFirst();
            return rootEntity.orElse(null);
        }
        return null;
    }

    @Override
    public final BigraphEntity getLink(BigraphEntity node) {
        if (!BigraphEntityType.isPointType(node)) return null;
        EObject eObject = node.getInstance();
        EStructuralFeature lnkRef = eObject.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
        if (Objects.isNull(lnkRef)) return null;
        EObject linkObject = (EObject) eObject.eGet(lnkRef);
        if (Objects.isNull(linkObject)) return null;
        if (!isBLink(linkObject)) return null; //"owner" problem
//        assert isBLink(linkObject);
//        Optional<BigraphEntity> lnkEntity;
        if (isBEdge(linkObject)) {
            Optional<BigraphEntity.Edge> first = getEdges().stream().filter(x -> x.getInstance().equals(linkObject)).findFirst();
            return first.orElse(null);
        } else {
            Optional<BigraphEntity.OuterName> first = getOuterNames().stream().filter(x -> x.getInstance().equals(linkObject)).findFirst();
            return first.orElse(null);
        }
    }

    protected boolean isBLink(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_LINK);
    }

    protected boolean isBEdge(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_EDGE);
    }
    //works only for elements of the calling class
    protected boolean isOfEClass(EObject eObject, String eClassifier) {
        return eObject.eClass().equals(((EPackageImpl) getModelPackage()).getEClassifierGen(eClassifier)) ||
                eObject.eClass().getEAllSuperTypes().contains(((EPackageImpl) getModelPackage()).getEClassifierGen(eClassifier));
    }

    @Override
    public final <C extends Control> Collection<BigraphEntity.NodeEntity<C>> getNodes() {
        return Collections.EMPTY_LIST;
    }

    //TODO for discrete ion important!
    @Override
    public final Collection<BigraphEntity> getChildrenOf(BigraphEntity node) {
        return Collections.EMPTY_LIST;
    }


    @Override
    public final Collection<BigraphEntity.Port> getPorts(BigraphEntity node) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public final boolean areConnected(BigraphEntity.NodeEntity place1, BigraphEntity.NodeEntity place2) {
        return false;
    }
}
