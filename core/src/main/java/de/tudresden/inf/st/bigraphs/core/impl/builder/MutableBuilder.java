package de.tudresden.inf.st.bigraphs.core.impl.builder;

import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.exceptions.BigraphMetaModelLoadingFailedException;
import de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils;
import org.eclipse.emf.ecore.EObject;

import java.util.function.Supplier;

import static de.tudresden.inf.st.bigraphs.core.BigraphEntityType.*;

public class MutableBuilder<S extends Signature> extends BigraphBuilder<S> {

    public MutableBuilder(S signature, Supplier<String> nodeNameSupplier) throws BigraphMetaModelLoadingFailedException {
        super(signature, nodeNameSupplier);
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

}