package org.bigraphs.framework.core;

import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.KindSignature;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.Diagnostician;

import java.util.function.Consumer;

/**
 * Interface for all Ecore-based signatures.
 * This interface is technology-specific, and not general as {@link Signature}.
 * <p>
 * It is similar to {@link EcoreBigraph}.
 *
 * @author Dominik Grzelak
 * @see Signature
 * @see EcoreBigraph
 */
public interface EcoreSignature extends EcoreBigraphExt {
    /**
     * Return the respective signature Ecore-based metamodel.
     *
     * @return the metamodel of the base signature specification
     * @see de.tudresden.inf.st.bigraphs.models.signatureBaseModel.SignatureBaseModelPackage
     */
    @Override
    EPackage getMetaModel();

    /**
     * Return the respective signature Ecore-based instance model.
     *
     * @return the signature instance model
     */
    @Override
    EObject getInstanceModel();

    //TODO move this to a validation class
    //TODO use EcoreBigraphExt interface instead of AbstractEcoreSignature: then we can store also bigraph classes
    /**
     * Keeps a list of validators for checking signature instance models.
     * If an instance model is invalid, a runtime exception will be thrown.
     */
    final ImmutableMap<Class<? extends EcoreSignature>, Consumer<EObject>> VALIDATORS = Maps.immutable.of(
            DefaultDynamicSignature.class, EcoreSignature::validateBSignature,
            KindSignature.class, EcoreSignature::validateBKindSignature
    );

    /**
     * Validate the Ecore-based dynamic signature instance model.
     *
     * @param bSignature the Ecore signature model
     * @return bSignature the same signature model if everything is fine
     * @throws RuntimeException if a duplicate control was found in the meta-model
     */
    static EObject validateBSignature(EObject bSignature) {
        if (!bSignature.eClass().getName().equalsIgnoreCase(BigraphMetaModelConstants.SignaturePackage.ECLASS_BDYNAMICSIGNATURE)) {
            throw new RuntimeException("Signature instance is not of EClass= " + BigraphMetaModelConstants.SignaturePackage.ECLASS_BDYNAMICSIGNATURE);
        }
        Diagnostic diagnostic = Diagnostician.INSTANCE.validate(bSignature);
        if (diagnostic.getSeverity() != Diagnostic.OK) {
            for (Diagnostic child : diagnostic.getChildren()) {
                if (child.getCode() == 13) { // duplicate control name
                    throw new RuntimeException("Duplicate control names found in signature definition.", child.getException());
                }
            }
        }

        return bSignature;
    }

    /**
     * Validate the Ecore-based kind signature instance model.
     *
     * @param bKindSignature the Ecore signature model
     * @return bKindSignature the same signature model if everything is fine
     * @throws RuntimeException if a duplicate control was found in the meta-model
     */
    static EObject validateBKindSignature(EObject bKindSignature) {
        if (!bKindSignature.eClass().getName().equalsIgnoreCase(BigraphMetaModelConstants.SignaturePackage.ECLASS_BKINDSIGNATURE)) {
            throw new RuntimeException("Signature instance is not of EClass= " + BigraphMetaModelConstants.SignaturePackage.ECLASS_BKINDSIGNATURE);
        }
        //TODO perform OCL-based checks
        Diagnostic diagnostic = Diagnostician.INSTANCE.validate(bKindSignature);
        if (diagnostic.getSeverity() != Diagnostic.OK) {
            throw new RuntimeException("The kind signature model is invalid", diagnostic.getException());
        }

        return bKindSignature;
    }
}
