package de.tudresden.inf.st.bigraphs.core.impl;

import de.tudresden.inf.st.bigraphs.core.AbstractSignature;
import de.tudresden.inf.st.bigraphs.core.BigraphArtifacts;
import de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants;
import de.tudresden.inf.st.bigraphs.core.EcoreSignature;
import de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils;
import de.tudresden.inf.st.bigraphs.models.signatureBaseModel.BControl;
import de.tudresden.inf.st.bigraphs.models.signatureBaseModel.BKindSignature;
import org.eclipse.collections.api.bimap.MutableBiMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.BiMaps;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.*;

import java.io.IOException;
import java.util.*;

import static de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants.SignaturePackage.SORT_PREFIX;

public class KindSignature extends AbstractSignature<DefaultDynamicControl> {
    protected EObject instanceModel;
    protected MutableBiMap<String, KindSort> kindFunction = BiMaps.mutable.empty();
    protected EFactory sigFactory;
    protected EPackage sigPackage;

    /**
     * Create a kind signature object for the given Ecore instance model.
     * The "extended" metamodel for kind signatures is created ad-hoc and stored in the member variable {@code ePackage}.
     * <p>
     * If no kind sort is provided for a given control, the default behaviour kicks in.
     * That is, that control is active, thus, the kind function returns all available controls for the respective control.
     *
     * @param bKindSignature the instance model of a kind signature
     * @see #getModel()
     * @see #getModelPackage()
     */
    public KindSignature(EObject bKindSignature) {
        super();
        EcoreSignature.validateBKindSignature(bKindSignature);
        // extract and move everything to our internal structure
        // TODO re-generate the metamodel

        // Get all controls
//        for (BControl bControl : bKindSignature.getBControls()) {
//            dynamicSignatureBuilder = dynamicSignatureBuilder
//                    .newControl(bControl.getName(), bControl.getArity())
//                    .kind(ControlStatus.fromString(bControl.getStatus().getName())).assign();
        // init the kind functions (construct the kind sorts for the controls)

        // re-set the status based on the current sorts, if none was available use the default behavior
//        }
//        DynamicSignatureBuilder dynamicSignatureBuilder = new DynamicSignatureBuilder();
//        for (BControl bControl : bSignature.getBControls()) {
//            dynamicSignatureBuilder = dynamicSignatureBuilder
//                    .newControl(bControl.getName(), bControl.getArity())
//                    .kind(ControlStatus.fromString(bControl.getStatus().getName())).assign();
//        }
//        this.controls = dynamicSignatureBuilder.create().getControls();

        //TODO assert that kind function as size=control.size
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

        // Update the reference for non-atomic kind sorts, this builds the kind function w.r.t. Ecore model
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
    public EPackage getModelPackage() {
        return this.sigPackage;
    }

    @Override
    public EObject getModel() {
        return this.instanceModel;
    }

    // ///////////////////////////////////////////
    // Some helper methods for Ecore-related stuff
    // ///////////////////////////////////////////

    private void initKindFunction(Collection<KindSort> kindSorts) {
        kindFunction.clear();
        kindSorts.forEach(eachSort -> {
            kindFunction.put(eachSort.getControl().getNamedType().stringValue(), eachSort);
        });
    }

    private EClass extendBControlEClass(String controlName, EPackage sigPackage) {
        EClassifier eClassifier = sigPackage.getEClassifier(BigraphMetaModelConstants.SignaturePackage.ECLASS_BCONTROL);
        EClass controlClass = EMFUtils.createEClass(controlName);
        EMFUtils.addSuperType(controlClass, (EClass) eClassifier);
        sigPackage.getEClassifiers().add(controlClass);
        return controlClass;
    }

    private EClass extendBKindSortCompositeEClass(String controlName, EPackage sigPackage) {
        EClassifier eClassifier = sigPackage.getEClassifier(BigraphMetaModelConstants.SignaturePackage.ECLASS_KINDSORTNONATOMIC);
        EClass kindOfcontrolClass = EMFUtils.createEClass(controlName);
        EMFUtils.addSuperType(kindOfcontrolClass, (EClass) eClassifier);
        sigPackage.getEClassifiers().add(kindOfcontrolClass);
        return kindOfcontrolClass;
    }

    private EClass extendBKindSortLeafEClass(String controlName, EPackage sigPackage) {
        EClassifier eClassifier = sigPackage.getEClassifier(BigraphMetaModelConstants.SignaturePackage.ECLASS_BKINDSORTATOMIC);
        EClass kindOfControlClass = EMFUtils.createEClass(controlName);
        EMFUtils.addSuperType(kindOfControlClass, (EClass) eClassifier);
        sigPackage.getEClassifiers().add(kindOfControlClass);
        return kindOfControlClass;
    }
}
