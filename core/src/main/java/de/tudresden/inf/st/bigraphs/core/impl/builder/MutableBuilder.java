package de.tudresden.inf.st.bigraphs.core.impl.builder;

import de.tudresden.inf.st.bigraphs.core.BigraphBuilderSupport;
import de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.EMetaModelData;
import de.tudresden.inf.st.bigraphs.core.exceptions.BigraphMetaModelLoadingFailedException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidArityOfControlException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.Map;

//TODO: do not inherit from builder directly
//  make lightweight builder: references are acquired via eclass directly

/**
 * A generic mutable builder intended to be used for all bigraph types
 *
 * @param <S>
 */
public class MutableBuilder<S extends Signature> extends PureBigraphBuilder<S> {

    public MutableBuilder(S signature) throws BigraphMetaModelLoadingFailedException {
        super(signature);
    }

    public MutableBuilder(S signature, EMetaModelData metaModelData) throws BigraphMetaModelLoadingFailedException {
        super(signature, metaModelData);
    }

    @Override
    public EPackage getLoadedEPackage() {
        return loadedEPackage;
    }

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

    public void connectToEdge(BigraphEntity.NodeEntity<Control> node, BigraphEntity.Edge theLink) {
        super.connectToEdge(node, theLink);

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

    /**
     * Performs no checking at all, only if the node is already connected to the given outer name.
     *
     * @param node1
     * @param outerName
     */
    public void connectNodeToOuterName2(BigraphEntity.NodeEntity<Control> node1, BigraphEntity.OuterName outerName) {
        if (!isConnectedWithLink(node1, outerName.getInstance())) {
            // create a port
            EList<EObject> bPorts = (EList<EObject>) node1.getInstance().eGet(node1.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PORT));
            int index = bPorts.size();
            BigraphEntity.Port newPortWithIndex = (BigraphEntity.Port) createNewPortWithIndex(index);
            //add port to node
            EStructuralFeature portsRef = node1.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PORT);
            EList<EObject> portsList = (EList<EObject>) node1.getInstance().eGet(portsRef);
            portsList.add(newPortWithIndex.getInstance());
            //connect node to link
            EStructuralFeature lnkRef = newPortWithIndex.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
            newPortWithIndex.getInstance().eSet(lnkRef, outerName.getInstance());
        }
    }

    public EObject createInstanceModel(EPackage loadedEPackage,
                                          S signature,
                                          Map<Integer, BigraphEntity.RootEntity> availableRoots,
                                          Map<Integer, BigraphEntity.SiteEntity> availableSites,
                                          Map<String, BigraphEntity.NodeEntity> availableNodes,
                                          Map<String, BigraphEntity.InnerName> availableInnerNames,
                                          Map<String, BigraphEntity.OuterName> availableOuterNames,
                                          Map<String, BigraphEntity.Edge> availableEdges) {
        InstanceParameter meta = new InstanceParameter(loadedEPackage,
                signature, availableRoots, availableSites,
                availableNodes, availableInnerNames, availableOuterNames, availableEdges);
        return meta.getbBigraphObject();
    }

}