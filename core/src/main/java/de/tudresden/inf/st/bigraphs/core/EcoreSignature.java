package de.tudresden.inf.st.bigraphs.core;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.Diagnostician;

/**
 * @author Dominik Grzelak
 */
public interface EcoreSignature {
    /**
     * Return the respective signature Ecore-based metamodel.
     *
     * @return the metamodel of the base signature specification
     * @see de.tudresden.inf.st.bigraphs.models.signatureBaseModel.SignatureBaseModelPackage
     */
    EPackage getModelPackage();

    /**
     * Return the respective signature Ecore-based instance model.
     *
     * @return the signature instance model
     */
    EObject getModel();

    /**
     * Validate the Ecore-based signature meta-model.
     *
     * @param bSignature the Ecore signature model
     * @throws RuntimeException if a duplicate control was found in the meta-model
     */
    static void validateBSignature(EObject bSignature) {
        assert bSignature.eClass().getName().equalsIgnoreCase(BigraphMetaModelConstants.SignaturePackage.ECLASS_BDYNAMICSIGNATURE);
        Diagnostic diagnostic = Diagnostician.INSTANCE.validate(bSignature);
        if (diagnostic.getSeverity() != Diagnostic.OK) {
            for (Diagnostic child : diagnostic.getChildren()) {
                if (child.getCode() == 13) { // duplicate control name
                    throw new RuntimeException("Duplicate control names found in signature definition.", child.getException());
                }
            }
        }
    }

    static void validateBKindSignature(EObject bKindSignature) {
        assert bKindSignature.eClass().getName().equalsIgnoreCase(BigraphMetaModelConstants.SignaturePackage.ECLASS_BKINDSIGNATURE);
        //TODO perform OCL-based checks
        Diagnostic diagnostic = Diagnostician.INSTANCE.validate(bKindSignature);
        if (diagnostic.getSeverity() != Diagnostic.OK) {
            throw new RuntimeException("The kind signature model is invalid", diagnostic.getException());
        }
    }
}
