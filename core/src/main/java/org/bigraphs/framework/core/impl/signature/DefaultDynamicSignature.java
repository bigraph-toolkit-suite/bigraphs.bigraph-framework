package org.bigraphs.framework.core.impl.signature;

import org.bigraphs.framework.core.*;
import org.bigraphs.framework.core.*;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.utils.emf.EMFUtils;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.*;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.bigraphs.framework.core.BigraphMetaModelConstants.SignaturePackage.SORT_PREFIX;

/**
 * Concrete implementation of a pure (dynamic) signature, where controls can be assigned a {@code status =  (active, passive, atomic)}.
 *
 * @author Dominik Grzelak
 */
public final class DefaultDynamicSignature extends AbstractEcoreSignature<DefaultDynamicControl> implements IsPlaceSortable {

    protected MutableMap<String, KindSort> kindFunction;
    protected EFactory sigFactory;

    /**
     * Create a dynamic signature object for the given Ecore instance model.
     * The "extended" metamodel for dynamic signatures is stored in the member variable {@link AbstractEcoreSignature#sigPackage}.
     *
     * @param bSignature the instance model of a dynamic signature
     * @throws RuntimeException if the instance model is invalid (not conforming to the metamodel)
     * @see #getInstanceModel()
     * @see #getMetaModel()
     */
    public DefaultDynamicSignature(EObject bSignature) {
        super(EcoreSignature.validateBSignature(bSignature));
    }

    @Override
    protected void recreateControls() {
        if(kindFunction == null) {
            kindFunction = Maps.mutable.empty();
        }
        // Re-create control objects
        Map<String, EReference> allRefs = EMFUtils.findAllReferences2(instanceModel.eClass());
        EReference eReferenceControls = allRefs.get(BigraphMetaModelConstants.SignaturePackage.REFERENCE_BCONTROLS);
        assert eReferenceControls != null;
        EList<EObject> availableControls = (EList<EObject>) instanceModel.eGet(eReferenceControls);
        for (EObject eachControl : availableControls) {
            EAttribute nameAttr = EMFUtils.findAttribute(eachControl.eClass(), BigraphMetaModelConstants.SignaturePackage.ATTRIBUTE_NAME);
            EAttribute arityAttr = EMFUtils.findAttribute(eachControl.eClass(), BigraphMetaModelConstants.SignaturePackage.ATTRIBUTE_ARITY);
            EAttribute statusAttr = EMFUtils.findAttribute(eachControl.eClass(), BigraphMetaModelConstants.SignaturePackage.ATTRIBUTE_STATUS);
            assert nameAttr != null && arityAttr != null && statusAttr != null;
            String ctrlId = (String) eachControl.eGet(nameAttr);
            Integer ctrlArity = (Integer) eachControl.eGet(arityAttr);
            EEnumLiteral ctrlStatus = (EEnumLiteral) eachControl.eGet(statusAttr);

            DefaultDynamicControl defaultDynamicControl = DefaultDynamicControl.createDefaultDynamicControl(
                    StringTypedName.of(ctrlId),
                    FiniteOrdinal.ofInteger(ctrlArity),
                    ControlStatus.fromString(ctrlStatus.getLiteral())
            );
            controls.add(defaultDynamicControl);
        }
    }

    @Override
    protected void recreateSorts() {
        if(kindFunction == null) {
            kindFunction = Maps.mutable.empty();
        }
        Map<String, EReference> allRefs = EMFUtils.findAllReferences2(instanceModel.eClass());
        MutableList<DefaultDynamicControl> sorts = Lists.mutable.withAll(controls);
        // Re-create kind sort objects
        EReference eReferencePlaceSorts = allRefs.get(BigraphMetaModelConstants.SignaturePackage.REFERENCE_BKINDPLACESORTS);
        assert eReferencePlaceSorts != null;
        EList<EObject> availablePlaceSorts = (EList<EObject>) instanceModel.eGet(eReferencePlaceSorts);
        for (EObject eachPlaceSort : availablePlaceSorts) {
            String ctrlId = eachPlaceSort.eClass().getName();
            String rectifiedCtrlId = ctrlId.substring(0, ctrlId.lastIndexOf(SORT_PREFIX));
            if (EMFUtils.eClassHasSuperType(BigraphMetaModelConstants.SignaturePackage.ECLASS_KINDSORTNONATOMIC, eachPlaceSort.eClass())) {
                kindFunction.put(rectifiedCtrlId, KindSort.create(getControlByName(rectifiedCtrlId), sorts));
            }
            if (EMFUtils.eClassHasSuperType(BigraphMetaModelConstants.SignaturePackage.ECLASS_BKINDSORTATOMIC, eachPlaceSort.eClass())) {
                kindFunction.put(rectifiedCtrlId, KindSort.create(getControlByName(rectifiedCtrlId), Lists.mutable.empty()));
            }
        }
    }

    public DefaultDynamicSignature(Set<DefaultDynamicControl> controls) {
        super(controls);
        try {
            // (!) Important, because otherwise we might face a "A frozen model should not be modified" assertion exception:
            sigPackage = BigraphFileModelManagement.Load.internalSignatureMetaMetaModel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(kindFunction == null) {
            kindFunction = Maps.mutable.empty();
        }

        MutableMap<String, EClass> bControlClassMap = Maps.mutable.of();
        MutableMap<String, EClass> bControlSortClassMap = Maps.mutable.empty();
        for(DefaultDynamicControl c: this.controls) {
            String ctrlId = c.getNamedType().stringValue();
            EClass controlEClass = extendBControlEClass(ctrlId, sigPackage);
            bControlClassMap.put(ctrlId, controlEClass);
            if (c.getControlKind() == ControlStatus.ATOMIC) {
                kindFunction.put(ctrlId, KindSort.create(c, Lists.mutable.empty()));
                EClass kindSortControlEClass = extendBKindSortLeafEClass(ctrlId + SORT_PREFIX, sigPackage);
                bControlSortClassMap.put(ctrlId + SORT_PREFIX, kindSortControlEClass);
            } else { // Active or Passive
                kindFunction.put(ctrlId, KindSort.create(c, Lists.mutable.ofAll(this.controls)));
                EClass kindSortControlEClass = extendBKindSortCompositeEClass(ctrlId + SORT_PREFIX, sigPackage);
                bControlSortClassMap.put(ctrlId + SORT_PREFIX, kindSortControlEClass);
            }
        }
        assert this.kindFunction.size() == this.controls.size();

        // Create the instance model now
        sigFactory = sigPackage.getEFactoryInstance();
        EClass dynamicSignatureEClass = (EClass) sigPackage.getEClassifier(BigraphMetaModelConstants.SignaturePackage.ECLASS_BDYNAMICSIGNATURE);
        this.instanceModel = sigFactory.create(dynamicSignatureEClass);
        Map<String, EReference> allRefs = EMFUtils.findAllReferences2(dynamicSignatureEClass);
        // Create bControl classes and add them to bControls reference
        EReference eReferenceControls = allRefs.get(BigraphMetaModelConstants.SignaturePackage.REFERENCE_BCONTROLS);
        EReference eReferenceKindSorts = allRefs.get(BigraphMetaModelConstants.SignaturePackage.REFERENCE_BKINDPLACESORTS);
        assert eReferenceControls != null;
        MutableMap<String, EObject> kindSortInstanceMap = Maps.mutable.empty();
        for(DefaultDynamicControl eachCtrl: this.controls) {
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

            // Create concrete sort first and just add them to the bKindSorts reference of the instance BKindSignature
            EClass kindSorteClass = bControlSortClassMap.get(ctrlId + SORT_PREFIX);
            if (kindSorteClass.getESuperTypes().get(0).getName().equalsIgnoreCase(BigraphMetaModelConstants.SignaturePackage.ECLASS_KINDSORTNONATOMIC)) {
                EObject concreteKindSort = sigFactory.create(kindSorteClass);
                kindSortInstanceMap.put(ctrlId, concreteKindSort);
                EList<EObject> bKindSortsList = (EList<EObject>) this.instanceModel.eGet(eReferenceKindSorts);
                bKindSortsList.add(concreteKindSort);
            } else if (kindSorteClass.getESuperTypes().get(0).getName().equalsIgnoreCase(BigraphMetaModelConstants.SignaturePackage.ECLASS_BKINDSORTATOMIC)) {
                EObject concreteKindSort = sigFactory.create(kindSorteClass);
                kindSortInstanceMap.put(ctrlId, concreteKindSort);
                EList<EObject> bKindSortsList = (EList<EObject>) this.instanceModel.eGet(eReferenceKindSorts);
                bKindSortsList.add(concreteKindSort);
            }
        };

        // Update the reference for non-atomic kind sorts, this builds the kind function w.r.t. the Ecore model
        kindSortInstanceMap.forEach((key, value) -> {
            KindSort kindSort = kindFunction.get(key);
            Map<String, EReference> allRefsBKindSort = EMFUtils.findAllReferences2(value.eClass());
            EReference bKindSortChildrenReference = allRefsBKindSort.get(BigraphMetaModelConstants.SignaturePackage.REFERENCE_BKINDSORTS);
            kindSort.getKindsOfControl().forEach(eachKind -> {
                String ctrlKindId = eachKind.getNamedType().stringValue();
                EObject kindObject = kindSortInstanceMap.get(ctrlKindId);
                EList<EObject> kindSortChildrenList = (EList<EObject>) value.eGet(bKindSortChildrenReference);
                kindSortChildrenList.add(kindObject);
            });
        });

        EcoreSignature.validateBSignature(instanceModel);
    }

    @Override
    public EPackage getMetaModel() {
        return this.sigPackage;
    }

    @Override
    public EObject getInstanceModel() {
        return this.instanceModel;
    }

    @Override
    public Map<String, KindSort> getPlaceKindMap() {
        return kindFunction;
    }
}
