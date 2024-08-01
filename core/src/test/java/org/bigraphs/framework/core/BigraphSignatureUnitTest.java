package org.bigraphs.framework.core;

import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.SignatureNotConsistentException;
import org.bigraphs.framework.core.exceptions.builder.ControlNotExistsException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.factory.BigraphFactory;
import org.bigraphs.framework.core.impl.pure.KindBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.KindSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.signature.KindSignatureBuilder;
import org.bigraphs.framework.core.impl.elementary.DiscreteIon;
import org.bigraphs.framework.core.impl.elementary.Linkings;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.utils.BigraphUtil;
import org.bigraphs.framework.core.utils.emf.EMFUtils;
import org.bigraphs.model.signatureBaseModel.BControlStatus;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.bigraphs.framework.core.factory.BigraphFactory.createOrGetSignature;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BigraphSignatureUnitTest {

    @AfterEach
    void setUp() {

    }

    @Test
    void composeSignatureTest() {
        DefaultDynamicSignature sig1 = pureSignatureBuilder()
                .addControl("A", 1)
                .addControl("B", 1)
                .addControl("C", 1)
                .addControl("D", 1)
                .create();

        DefaultDynamicSignature sig2 = pureSignatureBuilder()
                .addControl("A", 2)
                .addControl("B", 2)
                .addControl("C", 2)
                .addControl("D", 2)
                .create();

        Assertions.assertThrows(SignatureNotConsistentException.class, () -> {
            BigraphUtil.composeSignatures(sig1, sig2);
        });

        DefaultDynamicSignature sig3 = pureSignatureBuilder()
                .addControl("E", 2)
                .addControl("F", 3)
                .addControl("G", 4)
                .create();

        DefaultDynamicSignature sigComp_1_3 = BigraphUtil.composeSignatures(sig1, sig3);
        assert sigComp_1_3.getControls().size() == 7;

        DefaultDynamicSignature sigMerge_1_2 = BigraphUtil.mergeSignatures(sig1, sig2, 0);
        assert sigMerge_1_2.getControls().size() == 4;

        sigMerge_1_2.getControls().forEach(c -> {
            assert c.getArity().getValue() == 1;
        });

        DefaultDynamicSignature sigMerge_1_2_right = BigraphUtil.mergeSignatures(sig1, sig2, 1);
        assert sigMerge_1_2_right.getControls().size() == 4;

        sigMerge_1_2_right.getControls().forEach(c -> {
            assert c.getArity().getValue() == 2;
        });

    }

    @Test
    void test_00() throws IncompatibleSignatureException, IncompatibleInterfaceException {
        DefaultDynamicSignature sig = pureSignatureBuilder()
                .newControl("K", 1).assign()
                .newControl("L", 1).assign()
                .create();
        DiscreteIon<DefaultDynamicSignature> K_x = pureDiscreteIon(sig, "K", "x");
        DiscreteIon<DefaultDynamicSignature> L_x = pureDiscreteIon(sig, "L", "x");
        Linkings<DefaultDynamicSignature>.Closure x = pureLinkings(sig).closure("x");
        BigraphComposite<DefaultDynamicSignature> G = ops(K_x).merge(L_x);
        ops(x).compose(G);

        Bigraph<DefaultDynamicSignature> F = ops(x).compose(G).getOuterBigraph();
        EObject instanceModel = F.getSignature().getInstanceModel();
        PureBigraphBuilder<DefaultDynamicSignature> newBuilder =
                pureBuilder(createOrGetSignature(instanceModel));
        // ... code of fluent builder API omitted ...
        PureBigraph bigraph = newBuilder.createBigraph(); //validation triggered

        Diagnostic diagnostic = Diagnostician.INSTANCE.validate(bigraph.getInstanceModel());
        if (diagnostic.getSeverity() != Diagnostic.OK) {
            System.out.println("ERROR in: " + diagnostic.getMessage());
            for (Diagnostic child : diagnostic.getChildren()) {
                System.out.println(" " + child.getMessage());
            }
        }

    }

    @Test
    void nameEqualsTest() {
        StringTypedName edge2A = StringTypedName.of("edge2");
        StringTypedName edge2B = StringTypedName.of("edge2");
        Assertions.assertEquals(edge2A, edge2B);
    }

    @Test
    @DisplayName("Create kind signature and test default behavior: All controls are active (non-atomic)")
    void create_instanceModelFromExtendedKindSignatureClass() throws IOException {
        KindSignatureBuilder ksb = kindSignatureBuilder();

        KindSignature signature = ksb.addControl("Room", 1)
                .addControl("Person", 2)
                .addControl("Computer", 2)
                .create();
        EPackage modelPackage = signature.getMetaModel();
        EObject model = signature.getInstanceModel();
        assert modelPackage != null;
        assert model != null;

        Map<String, EReference> allRefsBKindSignature = EMFUtils.findAllReferences2(model.eClass());
        EReference refBKindSorts = allRefsBKindSignature.get(BigraphMetaModelConstants.SignaturePackage.REFERENCE_BKINDPLACESORTS);
        EList<EObject> kindSorts = (EList<EObject>) model.eGet(refBKindSorts);
        for (EObject eachKindSort : kindSorts) {
            if (eachKindSort.eClass().getESuperTypes().get(0).getName().equalsIgnoreCase(BigraphMetaModelConstants.SignaturePackage.ECLASS_KINDSORTNONATOMIC)) {
                System.out.println("Active Node: " + eachKindSort);
                Map<String, EReference> allBKindSortRefs = EMFUtils.findAllReferences2(eachKindSort.eClass());
                EReference refKindSortChildren = allBKindSortRefs.get(BigraphMetaModelConstants.SignaturePackage.REFERENCE_BKINDSORTS);
                EList<EObject> kindSortChildren = (EList<EObject>) eachKindSort.eGet(refKindSortChildren);
                for (EObject eachChild : kindSortChildren) {
                    System.out.println("\t" + eachChild);
                }
            }
            if (eachKindSort.eClass().getESuperTypes().get(0).getName().equalsIgnoreCase(BigraphMetaModelConstants.SignaturePackage.ECLASS_BKINDSORTATOMIC)) {
                System.out.println("Passive Node: " + eachKindSort);
            }
        }
    }

    @Test
    @DisplayName("Create kind signature where place-sorting is specified")
    void create_instanceModelFromExtendedKindSignatureClass_2() throws ControlNotExistsException, IOException {
        KindSignatureBuilder ksb = kindSignatureBuilder();

        KindSignature signature = ksb.addControl("Room", 1)
                .addControl("Person", 2)
                .addControl("Computer", 2)
                .addActiveKindSort("Room", Lists.mutable.of("Person", "Computer"))
                .addPassiveKindSort("Person")
                .addPassiveKindSort("Computer")
                .create();
        EPackage modelPackage = signature.getMetaModel();
        EObject model = signature.getInstanceModel();
        assert modelPackage != null;
        assert model != null;

        Map<String, EReference> allRefsBKindSignature = EMFUtils.findAllReferences2(model.eClass());
        EReference refBKindSorts = allRefsBKindSignature.get(BigraphMetaModelConstants.SignaturePackage.REFERENCE_BKINDPLACESORTS);
        EList<EObject> kindSorts = (EList<EObject>) model.eGet(refBKindSorts);
        int cntRoom = 0;
        int cntAtomics = 0;
        for (EObject eachKindSort : kindSorts) {
            if (eachKindSort.eClass().getESuperTypes().get(0).getName().equalsIgnoreCase(BigraphMetaModelConstants.SignaturePackage.ECLASS_KINDSORTNONATOMIC)) {
                Map<String, EReference> allBKindSortRefs = EMFUtils.findAllReferences2(eachKindSort.eClass());
                EReference refKindSortChildren = allBKindSortRefs.get(BigraphMetaModelConstants.SignaturePackage.REFERENCE_BKINDSORTS);
                EList<EObject> kindSortChildren = (EList<EObject>) eachKindSort.eGet(refKindSortChildren);
                System.out.println("Active control: " + eachKindSort.eClass().getName());
                for (EObject eachChild : kindSortChildren) {
                    System.out.println("\t" + eachChild.eClass().getName());
                    cntRoom++;
                }
            }
            if (eachKindSort.eClass().getESuperTypes().get(0).getName().equalsIgnoreCase(BigraphMetaModelConstants.SignaturePackage.ECLASS_BKINDSORTATOMIC)) {
                System.out.println("Passive control: " + eachKindSort.eClass().getName());
                cntAtomics++;
            }
        }

        assert cntRoom == 2;
        assert cntAtomics == 2;

        BigraphFileModelManagement.Store.exportAsMetaModel(signature, System.out);
        BigraphFileModelManagement.Store.exportAsInstanceModel(signature, System.out);
    }

    @Test
    @DisplayName("Create a pureBuilder instance with a freshly created kind signature instance model")
    void createPureBuilderFromKindSignatureInstanceModel() {
        KindSignatureBuilder ksb = kindSignatureBuilder();

        KindSignature signature = ksb.addControl("Room", 1)
                .addControl("Person", 2)
                .addControl("Computer", 2)
                .create();
        EPackage modelPackage = signature.getMetaModel();
        EObject model = signature.getInstanceModel();
        assert modelPackage != null;
        assert model != null;

        KindBigraphBuilder ksBigraphBuilder = KindBigraphBuilder.create(model);
        assert ksBigraphBuilder.getMetaModel() != null;

        KindSignature kindSignatureRecreated = new KindSignature(model);
        assert kindSignatureRecreated.getControls().equals(signature.getControls());

        assert kindSignatureRecreated.getPlaceKindMap().get("Room").equals(signature.getPlaceKindMap().get("Room"));
        assert kindSignatureRecreated.getPlaceKindMap().equals(signature.getPlaceKindMap());

        KindBigraphBuilder kindBigraphBuilder = kindBuilder(signature);
        assert kindBigraphBuilder != null;

//        EPackage orGetMetaModel = BigraphFactory.createOrGetBigraphMetaModel(kindSignatureRecreated);
//        EPackage orGetMetaModel1 = BigraphFactory.createOrGetBigraphMetaModel(signature);
//        assert orGetMetaModel == orGetMetaModel1;
    }

    @Test
    @DisplayName("Create a pureBuilder instance with a freshly created dynamic signature instance model")
    void createPureBuilderFromDynamicSignatureInstanceModel() throws IOException {
        DynamicSignatureBuilder dynamicSigBuilder = pureSignatureBuilder();

        DefaultDynamicSignature signature = dynamicSigBuilder.addControl("Room", 1)
                .addControl("Person", 2, ControlStatus.ATOMIC)
                .addControl("Computer", 2)
                .create();
        EPackage modelPackage = signature.getMetaModel();
        EObject model = signature.getInstanceModel();
        assert modelPackage != null;
        assert model != null;

        System.out.println("Check if signature is already in the BigraphFactory Registry");
        AbstractEcoreSignature<? extends Control<?, ?>> orGetSignature = BigraphFactory.createOrGetSignature(model);
        assert orGetSignature.equals(signature);
        EPackage tmp1 = BigraphFactory.createOrGetSignatureMetaModel(signature);
        EPackage tmp2 = BigraphFactory.createOrGetSignatureMetaModel(orGetSignature);
        assert tmp1.equals(tmp2);
        assert tmp1.equals(modelPackage);
        assert tmp2.equals(modelPackage);

        PureBigraphBuilder<DefaultDynamicSignature> ksBigraphBuilder = PureBigraphBuilder.<DefaultDynamicSignature>create(model);
        assert ksBigraphBuilder.getMetaModel() != null;
        assert !ksBigraphBuilder.getMetaModel().equals(modelPackage); // because only the factory ensures that the sig-registry is asked

        DefaultDynamicSignature dynSignatureRecreated = new DefaultDynamicSignature(model);
        assert dynSignatureRecreated.getControls().equals(signature.getControls());


        EPackage orGetMetaModel1 = BigraphFactory.createOrGetBigraphMetaModel(signature);
        EPackage orGetMetaModel = BigraphFactory.createOrGetBigraphMetaModel(dynSignatureRecreated);
        assert orGetMetaModel == orGetMetaModel1;

        assert dynSignatureRecreated.getPlaceKindMap().get("Room").equals(signature.getPlaceKindMap().get("Room"));
        assert dynSignatureRecreated.getPlaceKindMap().get("Person").equals(signature.getPlaceKindMap().get("Person"));
        assert dynSignatureRecreated.getPlaceKindMap().get("Computer").equals(signature.getPlaceKindMap().get("Computer"));
        assert dynSignatureRecreated.getPlaceKindMap().size() != 0;
        assert signature.getPlaceKindMap().size() != 0;
        assert dynSignatureRecreated.getPlaceKindMap().equals(signature.getPlaceKindMap());

        BigraphFileModelManagement.Store.exportAsInstanceModel(dynSignatureRecreated, System.out);

    }

    //Feature: provide type-checking at compile time (because of type erasure)
    @DisplayName("Create a dynamic signature with atomic controls")
    @Test()
    @Order(1)
    public void createSignatures_Test() throws IOException {
        // Create signature with active controls
        DynamicSignatureBuilder defaultBuilder = new DynamicSignatureBuilder();
        defaultBuilder.newControl("Printer", 13).status(ControlStatus.ATOMIC).assign();
        DefaultDynamicSignature defaultSignature = defaultBuilder.create();

        BigraphFileModelManagement.Store.exportAsMetaModel(defaultSignature, System.out);
        BigraphFileModelManagement.Store.exportAsInstanceModel(defaultSignature, System.out);

        EPackage packageObject = defaultSignature.getMetaModel();
        EObject instanceObject = defaultSignature.getInstanceModel();
        EClass dynSigEClass = (EClass) packageObject.getEClassifier(BigraphMetaModelConstants.SignaturePackage.ECLASS_BDYNAMICSIGNATURE);
        Map<String, EReference> allRefs = EMFUtils.findAllReferences2(dynSigEClass);
        EReference eReferenceControls = allRefs.get(BigraphMetaModelConstants.SignaturePackage.REFERENCE_BCONTROLS);

        EClass bControlEClass = (EClass) packageObject.getEClassifier(BigraphMetaModelConstants.SignaturePackage.ECLASS_BCONTROL);
        EAttribute nameAttr = EMFUtils.findAttribute(bControlEClass, BigraphMetaModelConstants.SignaturePackage.ATTRIBUTE_NAME);
        EAttribute arityAttr = EMFUtils.findAttribute(bControlEClass, BigraphMetaModelConstants.SignaturePackage.ATTRIBUTE_ARITY);
        EAttribute statusAttr = EMFUtils.findAttribute(bControlEClass, BigraphMetaModelConstants.SignaturePackage.ATTRIBUTE_STATUS);
        assert nameAttr != null && arityAttr != null && statusAttr != null;
        EList<EObject> bControlsList = (EList<EObject>) instanceObject.eGet(eReferenceControls);
        EObject printerObject = bControlsList.get(0);
        assert printerObject != null;
        String o = (String) printerObject.eGet(nameAttr);
        assert o.equals("Printer");
        int o2 = (int) printerObject.eGet(arityAttr);
        assert o2 == 13;
        EEnumLiteral o3 = (EEnumLiteral) printerObject.eGet(statusAttr);
        assert o3.getLiteral().equals(BControlStatus.ATOMIC.getLiteral());

    }

    @DisplayName("Create a dynamic signatures with non-atomic controls")
    @Test()
    @Order(2)
    public void create_dynamicSignature() throws IOException {
        // Create signature with active controls
        // Create signature with dynamic controls
        DynamicSignatureBuilder dynamicBuilder = new DynamicSignatureBuilder();
        dynamicBuilder
                .newControl().arity(FiniteOrdinal.ofInteger(19)).identifier(StringTypedName.of("Spool")).status(ControlStatus.ACTIVE).assign()
                .newControl("Printer", 23).status(ControlStatus.PASSIVE).assign()
        ;
        DefaultDynamicSignature dynamicSignature = dynamicBuilder.create();


        BigraphFileModelManagement.Store.exportAsMetaModel(dynamicSignature, System.out);
        BigraphFileModelManagement.Store.exportAsInstanceModel(dynamicSignature, System.out);

        EPackage packageObject = dynamicSignature.getMetaModel();
        EObject instanceObject = dynamicSignature.getInstanceModel();
        EClass dynSigEClass = (EClass) packageObject.getEClassifier(BigraphMetaModelConstants.SignaturePackage.ECLASS_BDYNAMICSIGNATURE);
        Map<String, EReference> allRefs = EMFUtils.findAllReferences2(dynSigEClass);
        EReference eReferenceControls = allRefs.get(BigraphMetaModelConstants.SignaturePackage.REFERENCE_BCONTROLS);

        EClass bControlEClass = (EClass) packageObject.getEClassifier(BigraphMetaModelConstants.SignaturePackage.ECLASS_BCONTROL);
        EAttribute nameAttr = EMFUtils.findAttribute(bControlEClass, BigraphMetaModelConstants.SignaturePackage.ATTRIBUTE_NAME);
        EAttribute arityAttr = EMFUtils.findAttribute(bControlEClass, BigraphMetaModelConstants.SignaturePackage.ATTRIBUTE_ARITY);
        EAttribute statusAttr = EMFUtils.findAttribute(bControlEClass, BigraphMetaModelConstants.SignaturePackage.ATTRIBUTE_STATUS);
        assert nameAttr != null && arityAttr != null && statusAttr != null;
        EList<EObject> bControlsList = (EList<EObject>) instanceObject.eGet(eReferenceControls);
        int ctrlCnt = 0;
        for (EObject each : bControlsList) {
            if (each.eClass().getName().equals("Spool")) {
                String o = (String) each.eGet(nameAttr);
                assert o.equals("Spool");
                int o2 = (int) each.eGet(arityAttr);
                assert o2 == 19;
                EEnumLiteral o3 = (EEnumLiteral) each.eGet(statusAttr);
                assert o3.getLiteral().equals(BControlStatus.ACTIVE.getLiteral());
                ctrlCnt++;
            }
            if (each.eClass().getName().equals("Printer")) {
                String o = (String) each.eGet(nameAttr);
                assert o.equals("Printer");
                int o2 = (int) each.eGet(arityAttr);
                assert o2 == 23;
                EEnumLiteral o3 = (EEnumLiteral) each.eGet(statusAttr);
                assert o3.getLiteral().equals(BControlStatus.PASSIVE.getLiteral());
                ctrlCnt++;
            }
        }

        assert ctrlCnt == 2;
    }


    public static <C extends Control<?, ?>> Signature<C> createExampleSignature() {
        SignatureBuilder<StringTypedName, FiniteOrdinal<Integer>, ?, ?> defaultBuilder = new DynamicSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Spool")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign();

        return (Signature<C>) defaultBuilder.create();
    }

    @Test
    @Order(3)
    public void bigraphiclaSignatureAsTypeGraph_test() {
        Signature exampleSignature = createExampleSignature();
        Assertions.assertNotNull(exampleSignature);
        System.out.println(exampleSignature);

    }

    //for the dynamic emf feature: define ecore model at runtime (no code generation required)
    //Used for signatures: createNodeOfEClass dynamic subclasses of generated classes
    //code generation (i.e., signature extension) is based on the in-memory concept in EMF
    @Test
    @DisplayName("Load, Modify and Output an Ecore model")
    @Order(2)
    public void loadEcoreModelTest() throws URISyntaxException {
//        InputStream in = getClass().getResourceAsStream(BIGRAPH_BASE_MODEL);
//        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        try {
            EPackage metapackage = BigraphFileModelManagement.Load.internalBigraphMetaMetaModel();
            System.out.println("Model loaded");

//            EPackage metapackage = (EPackage) resource.getContents().get(0);
            // we can retrieve the class object for our entity class
            // by name. We can do the same for the Attribute.
            EClass entityClass = (EClass) metapackage.getEClassifier("BNode");
            System.out.println(entityClass);
//            ((BNode) entityClass).getBPorts(); // not possible
            EClass newControlClass = EMFUtils.createEClass("Spool");
            metapackage.getEClassifiers().add(newControlClass);
            EMFUtils.addSuperType(newControlClass, metapackage, entityClass.getName());

//            EMFUtils.writeEcoreFile(metapackage, "custom2", "http://www.example.com/", System.out);
        } catch (
                IOException e) {
            e.printStackTrace();
        }

    }
}
