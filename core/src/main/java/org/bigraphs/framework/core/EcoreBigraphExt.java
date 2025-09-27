package org.bigraphs.framework.core;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

/**
 * Technology-specific interface for <strong>EMF/Ecore-based</strong> bigraph objects (e.g., bigraphs or signatures).
 * <p>
 * Provides two basic methods:
 * <ul>
 *   <li>Access the metamodel ({@link EPackage})</li>
 *   <li>Access the instance model ({@link EObject})</li>
 * </ul>
 *
 * @author Dominik Grzelak
 * @see EcoreBigraph
 * @see EcoreSignature
 */
public interface EcoreBigraphExt {

    /**
     * Return the metamodel of a bigraph object or a signature object.
     * <p>
     * It is a metamodel that either extends the base bigraph metamodel or the base signature metamodel.
     *
     * @return the metamodel in Ecore format
     * @see org.bigraphs.model.bigraphBaseModel.BigraphBaseModelPackage
     * @see org.bigraphs.model.signatureBaseModel.SignatureBaseModelPackage
     */
    EPackage getMetaModel();

    /**
     * Return the Ecore-based instance model of a bigraph object or signature object.
     *
     * @return the instance model in Ecore format
     */
    EObject getInstanceModel();
}
