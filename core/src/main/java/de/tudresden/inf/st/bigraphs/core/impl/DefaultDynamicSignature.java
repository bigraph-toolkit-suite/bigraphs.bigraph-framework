package de.tudresden.inf.st.bigraphs.core.impl;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils;
import de.tudresden.inf.st.bigraphs.models.signatureBaseModel.BControl;
import de.tudresden.inf.st.bigraphs.models.signatureBaseModel.BDynamicSignature;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.*;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Concrete implementation of a pure (dynamic) signature, where controls can be assigned a {@code status =  (active, passive, atomic)}.
 *
 * @author Dominik Grzelak
 */
public final class DefaultDynamicSignature extends AbstractEcoreSignature<DefaultDynamicControl> {

    protected EFactory sigFactory;
    protected EPackage sigPackage;
    protected EObject instanceModel;

    public DefaultDynamicSignature(BDynamicSignature bSignature) { // TODO: change this to EObject
        super();
        //TODO: extend/re-create the metamodel here for the new approach, or maybe allow loading it
        EcoreSignature.validateBSignature(bSignature);
        instanceModel = bSignature;
        DynamicSignatureBuilder dynamicSignatureBuilder = new DynamicSignatureBuilder();
        for (BControl bControl : bSignature.getBControls()) {
            dynamicSignatureBuilder = dynamicSignatureBuilder
                    .newControl(bControl.getName(), bControl.getArity())
                    .status(ControlStatus.fromString(bControl.getStatus().getName())).assign();
        }
        this.controls = dynamicSignatureBuilder.create().getControls();
    }


    public DefaultDynamicSignature(Set<DefaultDynamicControl> controls) {
        super(controls);
        try {
            // (!) Important, because otherwise we might face a "A frozen model should not be modified" assertion exception:
            sigPackage = BigraphArtifacts.loadInternalSignatureMetaMetaModel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        MutableMap<String, EClass> bControlClassMap = Maps.mutable.of();
        this.controls.forEach(c -> {
            String ctrlId = c.getNamedType().stringValue();
            EClass controlEClass = extendBControlEClass(ctrlId, sigPackage);
            bControlClassMap.put(ctrlId, controlEClass);
        });

        sigFactory = sigPackage.getEFactoryInstance();
        // Create the instance model now
        EClass dynamicSignatureEClass = (EClass) sigPackage.getEClassifier(BigraphMetaModelConstants.SignaturePackage.ECLASS_BDYNAMICSIGNATURE);
        this.instanceModel = sigFactory.create(dynamicSignatureEClass);
        Map<String, EReference> allRefs = EMFUtils.findAllReferences2(dynamicSignatureEClass);
        // Create bControl classes and add them to bControls reference
        EReference eReferenceControls = allRefs.get(BigraphMetaModelConstants.SignaturePackage.REFERENCE_BCONTROLS);
        assert eReferenceControls != null;
        MutableMap<String, EObject> kindSortInstanceMap = Maps.mutable.empty();
        this.controls.forEach(eachCtrl -> {
            String ctrlId = eachCtrl.getNamedType().stringValue();
            EClass ctrlEClass = bControlClassMap.get(ctrlId);
            EObject concreteControlObject = sigFactory.create(ctrlEClass);
            // Set all attributes too
            EAttribute nameAttr = EMFUtils.findAttribute(ctrlEClass, BigraphMetaModelConstants.SignaturePackage.ATTRIBUTE_NAME);
            EAttribute arityAttr = EMFUtils.findAttribute(ctrlEClass, BigraphMetaModelConstants.SignaturePackage.ATTRIBUTE_ARITY);
            EAttribute statusAttr = EMFUtils.findAttribute(ctrlEClass, BigraphMetaModelConstants.SignaturePackage.ATTRIBUTE_STATUS);
            assert nameAttr != null && arityAttr != null && statusAttr != null;

            concreteControlObject.eSet(nameAttr, ctrlId);
            concreteControlObject.eSet(arityAttr, eachCtrl.getArity().getValue());
            EClassifier enumControlStatus = sigPackage.getEClassifier(BigraphMetaModelConstants.SignaturePackage.ECLASS_BCONTROLSTATUS);
            concreteControlObject.eSet(statusAttr, ((EEnum) enumControlStatus).getEEnumLiteral(eachCtrl.getControlKind().name()));

            // Add control to signature
            EList<EObject> bControlsList = (EList<EObject>) this.instanceModel.eGet(eReferenceControls);
            bControlsList.add(concreteControlObject);
        });
        EcoreSignature.validateBSignature(instanceModel);
    }

    @Override
    public EPackage getModelPackage() {
        return this.sigPackage;
    }

    @Override
    public EObject getModel() {
        return this.instanceModel;
    }

    private EClass extendBControlEClass(String controlName, EPackage sigPackage) {
        EClassifier eClassifier = sigPackage.getEClassifier(BigraphMetaModelConstants.SignaturePackage.ECLASS_BCONTROL);
        EClass controlClass = EMFUtils.createEClass(controlName);
        EMFUtils.addSuperType(controlClass, (EClass) eClassifier);
        sigPackage.getEClassifiers().add(controlClass);
        return controlClass;
    }
}
