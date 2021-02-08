package de.tudresden.inf.st.bigraphs.core.impl;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.*;

import java.io.IOException;
import java.util.*;

import static de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants.SignaturePackage.SORT_PREFIX;

/**
 * Concrete implementation of a pure (dynamic) signature, where controls can be assigned a {@code status =  (active, passive)},
 * and further place-sorts can be defined for each control.
 * <p>
 * This class generalizes the {@link DefaultDynamicSignature} class somewhat, because (place-sorted) kind signatures
 * are a generalisation of pure signatures.
 *
 * @author Dominik Grzelak
 */
public class KindSignature extends AbstractEcoreSignature<DefaultDynamicControl> implements IsPlaceSortable {

    protected MutableMap<String, KindSort> kindFunction;
    protected EFactory sigFactory;

    /**
     * Create a kind signature object for the given Ecore instance model.
     * The "extended" metamodel for kind signatures is stored in the member variable {@link AbstractEcoreSignature#sigPackage}.
     *
     * @param bKindSignature the instance model of a kind signature
     * @throws RuntimeException if the instance model is invalid (not conforming to the metamodel)
     * @see #getInstanceModel()
     * @see #getMetaModel()
     */
    public KindSignature(EObject bKindSignature) {
        super(EcoreSignature.validateBKindSignature(bKindSignature));
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
        // Re-create kind sort objects
        EReference eReferencePlaceSorts = allRefs.get(BigraphMetaModelConstants.SignaturePackage.REFERENCE_BKINDPLACESORTS);
        assert eReferencePlaceSorts != null;
        EList<EObject> availablePlaceSorts = (EList<EObject>) instanceModel.eGet(eReferencePlaceSorts);
        for (EObject eachPlaceSort : availablePlaceSorts) {
            String ctrlId = eachPlaceSort.eClass().getName();
            String rectifiedCtrlId = ctrlId.substring(0, ctrlId.lastIndexOf(SORT_PREFIX));
            if (EMFUtils.eClassHasSuperType(BigraphMetaModelConstants.SignaturePackage.ECLASS_KINDSORTNONATOMIC, eachPlaceSort.eClass())) {
                MutableList<DefaultDynamicControl> sorts = Lists.mutable.empty();
                Map<String, EReference> refsOfSort = EMFUtils.findAllReferences2(eachPlaceSort.eClass());
                EReference eReference = refsOfSort.get(BigraphMetaModelConstants.SignaturePackage.REFERENCE_BKINDSORTS);
                assert eReference != null;
                EList<EObject> bKindSorts = (EList<EObject>) eachPlaceSort.eGet(eReference);
                bKindSorts.forEach(x -> {
                    String rectifiedCtrlId2 = x.eClass().getName().substring(0, x.eClass().getName().lastIndexOf(SORT_PREFIX));
                    sorts.add(getControlByName(rectifiedCtrlId2));
                });
                kindFunction.put(rectifiedCtrlId, KindSort.create(getControlByName(rectifiedCtrlId), sorts));
            }
            if (EMFUtils.eClassHasSuperType(BigraphMetaModelConstants.SignaturePackage.ECLASS_BKINDSORTATOMIC, eachPlaceSort.eClass())) {
                kindFunction.put(rectifiedCtrlId, KindSort.create(getControlByName(rectifiedCtrlId), Lists.mutable.empty()));
            }
        }
    }

    /**
     * This constructor automatically assumes as the default that all controls are active, thus, the kind function
     * returns all available controls for the respective control.
     *
     * @param controls the controls of the kind signature
     */
    public KindSignature(Set<DefaultDynamicControl> controls) {
        this(controls, Collections.emptyList());
    }

    /**
     * This constructor creates a kind signature with the given controls and kind sorts.
     * If no kind sort is provided for a given control, the default behaviour kicks in.
     * That is, that control is active, thus, the kind function returns all available controls for the respective control.
     *
     * @param controls  the controls
     * @param kindSorts the kind sorts (must not be fully defined for all controls, has a default behaviour as described above)
     */
    public KindSignature(Set<DefaultDynamicControl> controls, Collection<KindSort> kindSorts) {
        super(controls);
        try {
            // (!) Important, because otherwise we might face a "A frozen model should not be modified" assertion exception:
            sigPackage = BigraphArtifacts.loadInternalSignatureMetaMetaModel();
            sigFactory = sigPackage.getEFactoryInstance();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        initKindFunction(kindSorts);

        MutableMap<String, EClass> bControlClassMap = Maps.mutable.of();
        MutableMap<String, EClass> bControlSortClassMap = Maps.mutable.empty();
        this.controls.forEach(c -> {
            String ctrlId = c.getNamedType().stringValue();
            EClass controlEClass = extendBControlEClass(ctrlId, sigPackage);
            bControlClassMap.put(ctrlId, controlEClass);
//            read also the kind function: if none is set for the control, status is active, and kind func. yields full control set
            if (Objects.nonNull(kindFunction.get(ctrlId)) &&
                    kindFunction.get(ctrlId).getKindsOfControl().size() > 0) { // is defined, and control has kinds assigned
                kindFunction.get(ctrlId).getKindsOfControl().forEach(x -> {
//                    c.setControlKind(Active) //TODO
                    EClass kindSortControlEClass = extendBKindSortCompositeEClass(ctrlId + SORT_PREFIX, sigPackage);
                    bControlSortClassMap.put(ctrlId + SORT_PREFIX, kindSortControlEClass);
                });
            } else if (Objects.nonNull(kindFunction.get(ctrlId)) &&
                    kindFunction.get(ctrlId).getKindsOfControl().size() == 0) { // defined, but control has no kinds assigned
//                c.setControlKind(Passive) //TODO
                EClass kindSortControlEClass = extendBKindSortLeafEClass(ctrlId + SORT_PREFIX, sigPackage);
                bControlSortClassMap.put(ctrlId + SORT_PREFIX, kindSortControlEClass);
            } else { // undefined: use default behaviour
                kindFunction.put(ctrlId, KindSort.create(c, Lists.mutable.ofAll(this.controls)));
//                c.setControlKind(Active) //TODO
                EClass kindSortControlEClass = extendBKindSortCompositeEClass(ctrlId + SORT_PREFIX, sigPackage);
                bControlSortClassMap.put(ctrlId + SORT_PREFIX, kindSortControlEClass);
            }
        });
        assert this.kindFunction.size() == this.controls.size();

        // Create the instance model now
        EClass kindSignatureEClass = (EClass) sigPackage.getEClassifier(BigraphMetaModelConstants.SignaturePackage.ECLASS_BKINDSIGNATURE);
        this.instanceModel = sigFactory.create(kindSignatureEClass);
        Map<String, EReference> allRefs = EMFUtils.findAllReferences2(kindSignatureEClass);
        // Create bControl classes and add them to bControls reference
        EReference eReferenceControls = allRefs.get(BigraphMetaModelConstants.SignaturePackage.REFERENCE_BCONTROLS);
        EReference eReferenceKindSorts = allRefs.get(BigraphMetaModelConstants.SignaturePackage.REFERENCE_BKINDPLACESORTS);
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
        });

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

        EcoreSignature.validateBKindSignature(instanceModel);
    }

    @Override
    public EPackage getMetaModel() {
        return this.sigPackage;
    }

    @Override
    public EObject getInstanceModel() {
        return this.instanceModel;
    }

    // ///////////////////////////////////////////
    // Some helper methods for Ecore-related stuff
    // ///////////////////////////////////////////

    protected void initKindFunction(Collection<KindSort> kindSorts) {
        if(kindFunction == null) {
            kindFunction = Maps.mutable.empty();
        }
        kindFunction.clear();
        kindSorts.forEach(eachSort -> {
            kindFunction.put(eachSort.getControl().getNamedType().stringValue(), eachSort);
        });
    }

    @Override
    public Map<String, KindSort> getPlaceKindMap() {
        return kindFunction;
    }
}
