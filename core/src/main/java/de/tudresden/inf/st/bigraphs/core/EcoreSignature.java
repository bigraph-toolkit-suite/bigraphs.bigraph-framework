package de.tudresden.inf.st.bigraphs.core;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

/**
 * @author Dominik Grzelak
 */
public interface EcoreSignature {
    /**
     * Return the base signature meta-model.
     *
     * @return the metamodel of the base signature specification
     * @see de.tudresden.inf.st.bigraphs.models.signatureBaseModel.SignatureBaseModelPackage
     */
    EPackage getModelPackage();

    /**
     * Return the signature instance model (Ecore)
     *
     * @return the signature instance model
     */
    EObject getModel();
}
