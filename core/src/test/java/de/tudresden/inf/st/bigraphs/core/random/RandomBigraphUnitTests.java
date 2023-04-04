package de.tudresden.inf.st.bigraphs.core.random;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.alg.generators.PureBigraphGenerator;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.MutableBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphComposite;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*;

/**
 * @author Dominik Grzelak
 */
public class RandomBigraphUnitTests {
    private final static String TARGET_TEST_PATH = "src/test/resources/dump/exported-models/";

    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() {
        end();
    }

    @Test
    void name() {
        DefaultDynamicSignature exampleSignature = createExampleSignature();
        PureBigraph generated = new PureBigraphGenerator(exampleSignature).generate(1, 10, 1.f);
    }

    @Test
    void ecoremodel_composition() throws InvalidConnectionException, TypeNotExistsException, IOException, IncompatibleSignatureException, IncompatibleInterfaceException {
        DefaultDynamicSignature exampleSignature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builder = pureBuilder(exampleSignature);
        PureBigraphGenerator pureBigraphGenerator = pureRandomBuilder(exampleSignature);
        PureBigraph randomBigraph = pureBigraphGenerator.generate(1, 10, 0f);

        BigraphEntity.OuterName network = builder.createOuterName("network");
        BigraphEntity.InnerName login = builder.createInnerName("login");
        builder.createRoot()
                .addChild("User").linkToOuter(network)
                .addChild("User").linkToInner(login).down().addSite();
        PureBigraph left = builder.createBigraph();
        //TODO: add to docu: spawnNewOne() - important for ecore operations
        PureBigraphBuilder<DefaultDynamicSignature> builder2 = pureBuilder(exampleSignature);
//        PureBigraphBuilder<DefaultDynamicSignature> builder2 = builder.spawnNewOne();
        BigraphEntity.OuterName login2 = builder2.createOuterName("login");
        BigraphEntity.InnerName login2User = builder2.createInnerName("nextLogin");
        BigraphEntity.InnerName abc = builder2.createInnerName("abc");
        BigraphEntity.OuterName xyz = builder2.createOuterName("network");//xyz
        builder2.createRoot()
                .addChild("User").linkToOuter(login2)
                .addChild("User").linkToInner(login2User);
        builder2.connectInnerToOuterName(abc, xyz);

        PureBigraph right = builder2.createBigraph();


        BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) left, new FileOutputStream("src/test/resources/dump/exported-models/random_left_03_before.xmi"));
        BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) right, new FileOutputStream("src/test/resources/dump/exported-models/random_right_03_before.xmi"));
        PureBigraphComposite<DefaultDynamicSignature> comp = new PureBigraphComposite<>(left);
        Assertions.assertThrows(IncompatibleInterfaceException.class, () -> {
            BigraphComposite<DefaultDynamicSignature> result = comp.compose(right);
        });
//        BigraphComposite<DefaultDynamicSignature> result = comp.parallelProductV2(right);
//        BigraphComposite<DefaultDynamicSignature> result = comp.parallelProduct(right);
//        BigraphComposite<DefaultDynamicSignature> juxtapose = ((PureBigraphComposite) result).juxtapose(randomBigraph);
//        BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) result.getOuterBigraph(), new FileOutputStream("src/test/resources/dump/exported-models/random_comp_03_after.xmi"));
//        BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) juxtapose.getOuterBigraph(), new FileOutputStream("src/test/resources/dump/exported-models/random_juxta_03_after.xmi"));
//        BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) left, new FileOutputStream("src/test/resources/dump/exported-models/random_left_03_after.xmi"));
//        BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) right, new FileOutputStream("src/test/resources/dump/exported-models/random_right_03_after.xmi"));

//        Assertions.assertNotNull(result);
    }

    @Test
    void performance() throws IOException, CloneNotSupportedException, IncompatibleSignatureException, IncompatibleInterfaceException {
        DefaultDynamicSignature exampleSignature = createExampleSignature();
        List<Double> resultsTime = new ArrayList<Double>();
        List<Double> resultsTimeCloneOp = new ArrayList<Double>();

        for (int i = 1000; i < 10000; i += 1000) {
//        for (int i = 10; i < 11; i += 1) {
            PureBigraph generated = new PureBigraphGenerator(exampleSignature).generate(1, i, 0.5f, 0f, 1f);
            MutableBuilder<DefaultDynamicSignature> mutableBuilder = new MutableBuilder<>(exampleSignature, generated.getModelPackage(), generated.getModel());
            Integer o = (Integer) mutableBuilder.availableNodes().keySet()
                    .stream()
                    .map(x -> Integer.parseInt(((String) x).replace("v", "")))
                    .map(x -> (Integer) x)
                    .max(Comparator.comparing(Function.identity()))
                    .get();
            System.out.println(o);
            String key = "v" + o;
            BigraphEntity.NodeEntity nodeEntity = mutableBuilder.availableNodes().get(key);
            BigraphEntity newSite = mutableBuilder.createNewSite(0);
            mutableBuilder.setParentOfNode(newSite, nodeEntity); // this notifies the EContentAdapter
            mutableBuilder.availableSites().put(0, (BigraphEntity.SiteEntity) newSite);
            PureBigraph bigraph = mutableBuilder.createBigraph();
            Assertions.assertNotNull(bigraph);
            long tclone1 = System.nanoTime();
            EcoreBigraph.Stub clone = new EcoreBigraph.Stub(bigraph).clone();
            PureBigraph cloned = PureBigraphBuilder.create(exampleSignature, clone.getModelPackage(), clone.getModel()).createBigraph();
            long tclone2 = System.nanoTime();
            double secsClone = (tclone2 - tclone1) * 1.f / 1e+6;
            resultsTimeCloneOp.add(secsClone);
            Assertions.assertNotNull(cloned);
            PureBigraphComposite<DefaultDynamicSignature> comp = new PureBigraphComposite<>(bigraph);
//            BigraphFileModelManagement.exportAsInstanceModel(comp.getOuterBigraph(), new FileOutputStream(TARGET_TEST_PATH + "random_left_01.xmi"));
            long l = System.nanoTime();
            BigraphComposite<DefaultDynamicSignature> result = comp.compose(cloned);
//            BigraphComposite<DefaultDynamicSignature> result = comp.compose(cloned);
            long l2 = System.nanoTime();
//            double secs = (l2 - l) * 1.f / 1e+9;
            double millisecs = (l2 - l) * 1.f / 1e+6;
//            System.out.println("Time: " + secs + " / ns " + (l2 - l));
            resultsTime.add(millisecs);
        }
        System.out.println(resultsTime);
        double total = resultsTime.stream().reduce(0d, (a, b) -> a + b).doubleValue();
        System.out.println("Avg: " + (total / resultsTime.size() - 1));
        System.out.println(resultsTimeCloneOp);
//        BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) result, new FileOutputStream(TARGET_TEST_PATH + "random_compose_result.xmi"));
    }

    private <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();
        signatureBuilder
                .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Spool")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(0)).assign();

        return (S) signatureBuilder.create();
    }
}
