package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DefaultSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URISyntaxException;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BigraphSignatureTest {

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

    //Feature: provide type-checking at compile time (because of type erasure)
    @DisplayName("Create several signatures of different kinds (only atomic and dynamic ones)")
    @Test()
    @Order(1)
    public void createSignatures_Test() {
        // Create signature with active controls
        DefaultSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>> defaultBuilder = new DefaultSignatureBuilder<>();
        defaultBuilder.newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(1)).assign();
        Signature<Control<StringTypedName, FiniteOrdinal<Integer>>> defaultSignature = defaultBuilder.create();
        // Create signature with dynamic controls
        DynamicSignatureBuilder<StringTypedName, FiniteOrdinal<Long>> dynamicBuilder = new DynamicSignatureBuilder<>();
        dynamicBuilder
                .newControl().arity(FiniteOrdinal.ofLong(1)).identifier(StringTypedName.of("Spool")).assign();
        Signature<Control<StringTypedName, FiniteOrdinal<Long>>> controlSignature = dynamicBuilder.create();


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
        DefaultSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>> defaultBuilder = new DefaultSignatureBuilder<>();
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
            EPackage metapackage = BigraphArtifacts.loadInternalBigraphMetaModel();
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
