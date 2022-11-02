package de.tudresden.inf.st.bigraphs.core;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

/**
 * Marker interface for Ecore-based bigraphical object (e.g., a bigraph or signature) that provides just two basic
 * methods to get the metamodel ({@link EPackage}), and the instance model ({@link EObject}).
 *
 * @author Dominik Grzelak
 */
public interface EcoreBigraphExt {

    /**
     * Return the metamodel of a bigraph (containing also signature information), or a signature object.
     * <p>
     * It is a metamodel that either extends the base bigraph metamodel or the base signature metamodel.
     *
     * @return the metamodel in Ecore
     * @see de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.BigraphBaseModelPackage
     */
    EPackage getModelPackage();

    /**
     * Return the Ecore-based instance model of a bigraph or signature object.
     *
     * @return the instance model in Ecore
     */
    EObject getModel();

    //TODO rename methods; integrate interface in EcoreSignature
//    /**
//     * Return the respective Ecore-based signature metamodel.
//     *
//     * @return the metamodel of the base signature specification
//     * @see de.tudresden.inf.st.bigraphs.models.signatureBaseModel.SignatureBaseModelPackage
//     */
//    EPackage getMetaModel();
//
//    /**
//     * Return the respective Ecore-based signature instance model.
//     *
//     * @return the signature instance model
//     */
//    EObject getInstanceModel();
}
