package de.tudresden.inf.st.bigraphs.core.impl.builder;

import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.exceptions.BigraphMetaModelLoadingFailedException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidArityOfControlException;
import de.tudresden.inf.st.bigraphs.core.exceptions.building.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

import java.util.function.Supplier;

import static de.tudresden.inf.st.bigraphs.core.BigraphEntityType.*;

//TODO: do not inherit from builder directly
//TODO make lightweight builder: references are acquired via eclass directly
public class MutableBuilder<S extends Signature> extends BigraphBuilder<S> {

    public MutableBuilder(S signature, Supplier<String> nodeNameSupplier) throws BigraphMetaModelLoadingFailedException {
        super(signature, nodeNameSupplier);
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

    public void connectToEdgeUsingIndex(BigraphEntity.NodeEntity<Control<?, ?>> node, BigraphEntity.Edge edge, int customPortIndex) {
        super.connectToEdgeUsingIndex(node, edge, customPortIndex);
    }

    public void connectNodeToOuterName(BigraphEntity.NodeEntity<Control<?, ?>> node1, BigraphEntity.OuterName outerName) {
        try {
            super.connectNodeToOuterName(node1, outerName);
        } catch (LinkTypeNotExistsException | InvalidArityOfControlException e) {
            e.printStackTrace();
        }
    }

}