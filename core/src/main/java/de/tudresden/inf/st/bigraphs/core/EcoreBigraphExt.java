package de.tudresden.inf.st.bigraphs.core;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

/**
 * Marker interface for Ecore-based bigraphical object (e.g., a bigraph or signature) that provides just two basic
 * methods to get the metamodel ({@link EPackage}), and the instance model ({@link EObject}).
 * This interface is technology-specific.
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
     * @see de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.BigraphBaseModelPackage
     */
    EPackage getMetaModel();

    /**
     * Return the Ecore-based instance model of a bigraph object or signature object.
     *
     * @return the instance model in Ecore format
     */
    EObject getInstanceModel();
}
