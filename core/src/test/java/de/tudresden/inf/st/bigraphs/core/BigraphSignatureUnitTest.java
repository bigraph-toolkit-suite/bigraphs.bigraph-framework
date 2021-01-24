package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.ControlNotExistsException;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.KindSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.KindSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.SignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.kindSignatureBuilder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BigraphSignatureUnitTest {

    @BeforeAll
    static void init() {
//        createSignatures_Test();
    }

    @AfterEach
    void setUp() {

    }

    @Test
    void nameEqualsTest() {
        StringTypedName edge2A = StringTypedName.of("edge2");
        StringTypedName edge2B = StringTypedName.of("edge2");
        Assertions.assertEquals(edge2A, edge2B);
    }

    @Test
    void test_create_extendedKindSignature() throws IOException {

    }

    @Test
    @DisplayName("Create kind signature and test default behavior: All controls are active (non-atomic)")
    void create_instanceModelFromExtendedKindSignatureClass() throws IOException {
        KindSignatureBuilder ksb = kindSignatureBuilder();

        KindSignature signature = ksb.addControl("Room", 1)
                .addControl("Person", 2)
                .addControl("Computer", 2)
                .create();
        EPackage modelPackage = signature.getModelPackage();
        EObject model = signature.getModel();
        assert modelPackage != null;
        assert model != null;

        Map<String, EReference> allRefsBKindSignature = EMFUtils.findAllReferences2(model.eClass());
        EReference refBKindSorts = allRefsBKindSignature.get(BigraphMetaModelConstants.SignaturePackage.REFERENCE_BKINDPLACESORTS);
        EList<EObject> kindSorts = (EList<EObject>) model.eGet(refBKindSorts);
        for (EObject eachKindSort : kindSorts) {
            System.out.println(eachKindSort);
            if (eachKindSort.eClass().getESuperTypes().get(0).getName().equalsIgnoreCase(BigraphMetaModelConstants.SignaturePackage.ECLASS_KINDSORTNONATOMIC)) {
                Map<String, EReference> allBKindSortRefs = EMFUtils.findAllReferences2(eachKindSort.eClass());
                EReference refKindSortChildren = allBKindSortRefs.get(BigraphMetaModelConstants.SignaturePackage.REFERENCE_BKINDSORTS);
                EList<EObject> kindSortChildren = (EList<EObject>) eachKindSort.eGet(refKindSortChildren);
                for (EObject eachChild : kindSortChildren) {
                    System.out.println(eachChild);
                }
            }
            if (eachKindSort.eClass().getESuperTypes().get(0).getName().equalsIgnoreCase(BigraphMetaModelConstants.SignaturePackage.ECLASS_BKINDSORTATOMIC)) {

            }
        }
    }

    @Test
    @DisplayName("Create kind signature where place-sorting is specified")
    void create_instanceModelFromExtendedKindSignatureClass_2() throws ControlNotExistsException {
        KindSignatureBuilder ksb = kindSignatureBuilder();

        KindSignature signature = ksb.addControl("Room", 1)
                .addControl("Person", 2)
                .addControl("Computer", 2)
                .addActiveKindSort("Room", Lists.mutable.of("Person", "Computer"))
                .addPassiveKindSort("Person")
                .addPassiveKindSort("Computer")
                .create();
        EPackage modelPackage = signature.getModelPackage();
        EObject model = signature.getModel();
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
    }

    private EClass extendBControlEClass(String controlName, EPackage packageKindSig, EPackage dynamicSig) {
        EClassifier eClassifier = dynamicSig.getEClassifier(BigraphMetaModelConstants.SignaturePackage.ECLASS_BCONTROL);
        EClass controlClass = EMFUtils.createEClass(controlName);
        EMFUtils.addSuperType(controlClass, (EClass) eClassifier);
        packageKindSig.getEClassifiers().add(controlClass);
        return controlClass;
    }

    //Feature: provide type-checking at compile time (because of type erasure)
    @DisplayName("Create several signatures of different kinds (only atomic and dynamic ones)")
    @Test()
    @Order(1)
    public void createSignatures_Test() {
        // Create signature with active controls
        DynamicSignatureBuilder defaultBuilder = new DynamicSignatureBuilder();
        defaultBuilder.newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(1)).assign();
        DefaultDynamicSignature defaultSignature = defaultBuilder.create();
        // Create signature with dynamic controls
        DynamicSignatureBuilder dynamicBuilder = new DynamicSignatureBuilder();
        dynamicBuilder
                .newControl().arity(FiniteOrdinal.ofInteger(1)).identifier(StringTypedName.of("Spool")).assign();
        DefaultDynamicSignature controlSignature = dynamicBuilder.create();


//        DefaultDynamicControl<RandomNameType, FiniteOrdinal<Long>> test
//                = DefaultDynamicControl.createDefaultDynamicControl(RandomNameType.of(), FiniteOrdinal.ofLong(1), ControlKind.ACTIVE);
//
//        dynamicBuilder.addControl(test);


//                .newControl();
//                .identifier(StringTypedName.of(""))
//                .arity(FiniteOrdinal.ofInteger(1))
//                .assign()
//                .createNodeOfEClass();

//        DefaultSignatureBuilder<Integer> integerDefaultSignatureBuilder = new DefaultSignatureBuilder<>();
//        integerDefaultSignatureBuilder.begin()
//                .newControl().arity(FiniteOrdinal.ofInteger(1)).assign()
//                .newControl().arity(FiniteOrdinal.ofInteger(1)).assign();
//        Iterable<Control> controls = integerDefaultSignatureBuilder.getControls();
//        System.out.println(cont);

        //Create Controls
//        DefaultControl<Integer> printer = new DefaultControl<>(null, null);
//        printer = new DefaultControlBuilder<Integer>()
//                .arity(FiniteOrdinal.ofInteger(1))
//                .identifier(StringTypedName.of("printer"))
//                .createNodeOfEClass();
//
//
////        D cb = new DefaultControlBuilder();
//        DefaultDynamicControl<Integer> printerDyn = new DefaultDynamicControl<>(null, null);
//        printerDyn = new DynamicControlBuilder<Integer>()
//                .identifier(StringTypedName.of("printer"))
//                .arity(FiniteOrdinal.ofInteger(1))
//                .createNodeOfEClass();


        // Signature
//        SignatureBuilder<DefaultControl, ?> builder = new DefaultSignatureBuilder();
//        builder.withControls(controlCollection);

//        SignatureBuilder<DefaultControl, ?> builder = new DefaultSignatureBuilder();

//        SignatureBuilder.newBuilder();
//        SignatureBuilder<DefaultControl> controlCollectionBuilder =
//                SignatureBuilder.newBuilder()
//                .newControl(new DefaultControl(StringTypedName.of("Spool"), FiniteOrdinal.ofInteger(1)))
//                .newControl(new DefaultControl(StringTypedName.of("Printer"), FiniteOrdinal.ofInteger(1)));
//        Collection<DefaultControl> controlList0 = controlCollectionBuilder.createNodeOfEClass();
//
//        List<DefaultControl> controlList = new ArrayList<>();
//        controlList.add(new DefaultControl(StringTypedName.of("Spool"), FiniteOrdinal.ofInteger(1)));
//        controlList.add(new DefaultControl(StringTypedName.of("Printer"), FiniteOrdinal.ofInteger(1)));
//
//        Signature<DefaultControl> createNodeOfEClass = builder
//                .withControls(controlList0)
//                .createNodeOfEClass();
//
//
//        createNodeOfEClass.getControls().forEach(x -> {
//            System.out.println(x.getNamedType().getValue() + ":" + x.getArity().getValue());
//        });


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
            EPackage metapackage = BigraphArtifacts.loadInternalBigraphMetaMetaModel();
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
