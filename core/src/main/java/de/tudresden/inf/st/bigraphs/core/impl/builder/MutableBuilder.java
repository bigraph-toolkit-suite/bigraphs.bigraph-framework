package de.tudresden.inf.st.bigraphs.core.impl.builder;

import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.exceptions.BigraphMetaModelLoadingFailedException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidArityOfControlException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import java.util.Map;
import java.util.function.Supplier;

//TODO: do not inherit from builder directly
//  make lightweight builder: references are acquired via eclass directly

/**
 * A generic mutable builder intended to be used for all bigraph types
 *
 * @param <S>
 */
public class MutableBuilder<S extends Signature> extends PureBigraphBuilder<S> {

    public MutableBuilder(S signature, Supplier<String> nodeNameSupplier) throws BigraphMetaModelLoadingFailedException {
        super(signature, nodeNameSupplier);
    }

    public EPackage getLoadedEPackage() {
        return loadedEPackage;
    }

//    public void bigraphicalSignatureAsTypeGraph(String name) throws BigraphMetaModelLoadingFailedException {

    public BigraphEntity createNewNode(Control<?, ?> control, String nodeIdentifier) {
        EObject childNode = super.createNodeOfEClass(control.getNamedType().stringValue(), control, nodeIdentifier);
        return BigraphEntity.createNode(childNode, control);
    }

    public BigraphEntity createNewSite(int index) {
        EObject eObject = super.createSiteOfEClass(index);
        return BigraphEntity.create(eObject, BigraphEntity.SiteEntity.class);
    }

    public BigraphEntity createNewRoot(int index) {
        EObject eObject = super.createRootOfEClass(index);
        return BigraphEntity.create(eObject, BigraphEntity.RootEntity.class);
    }

    public BigraphEntity createNewInnerName(String name) {
        EObject innerNameOfEClass = super.createInnerNameOfEClass(name);
        return BigraphEntity.create(innerNameOfEClass, BigraphEntity.InnerName.class);
    }

    public BigraphEntity createNewOuterName(String name) {
        EObject outerNameOfEClass = super.createOuterNameOfEClass(name);
        return BigraphEntity.create(outerNameOfEClass, BigraphEntity.OuterName.class);
    }

    public BigraphEntity createNewEdge(String name) {
        EObject edgeOfEClass0 = super.createEdgeOfEClass0(name);
        return BigraphEntity.create(edgeOfEClass0, BigraphEntity.Edge.class);
    }

    public BigraphEntity createNewPortWithIndex(final int index) {
        EObject portWithIndex = super.createPortWithIndex(index);
        return BigraphEntity.create(portWithIndex, BigraphEntity.Port.class);
    }

    public void connectInnerToOuter(BigraphEntity.InnerName innerName, BigraphEntity.OuterName outerName) {
        super.connectInnerToOuterName0(innerName, outerName);
    }

    public void connectToLinkUsingIndex(BigraphEntity.NodeEntity<Control> node, BigraphEntity theLink, int customPortIndex) {
        super.connectToLinkUsingIndex(node, theLink, customPortIndex);
    }

    /**
     * Clears all generated intermediate results of the bigraph's current construction inside the builder.
     */
    public void reset() {
        super.clearIntermediateResults();
    }

    public Map<String, BigraphEntity.Edge> getCreatedEdges() {
        return super.availableEdges;
    }

    public void connectNodeToOuterName(BigraphEntity.NodeEntity<Control> node1, BigraphEntity.OuterName outerName) {
        try {
            super.connectNodeToOuterName(node1, outerName);
        } catch (LinkTypeNotExistsException | InvalidArityOfControlException e) {
            e.printStackTrace();
        }
    }

}