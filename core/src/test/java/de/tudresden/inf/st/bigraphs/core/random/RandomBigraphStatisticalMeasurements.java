package de.tudresden.inf.st.bigraphs.core.random;

import com.google.common.collect.Lists;
import com.google.common.graph.Traverser;
import de.tudresden.inf.st.bigraphs.core.BigraphFileModelManagement;
import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.alg.generators.PureBigraphGenerator;
import de.tudresden.inf.st.bigraphs.core.alg.generators.RandomBigraphGeneratorSupport;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pure;
import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pureSignatureBuilder;
import static java.util.stream.Collectors.groupingBy;

/**
 * This test class is used to compute some statistical measures such as node degree or node assortativity.
 *
 * @author Dominik Grzelak
 */
@Disabled
public class RandomBigraphStatisticalMeasurements {

    @Test
    void basic() throws IOException {
        DefaultDynamicSignature exampleSignature = createExampleSignature();
        PureBigraph generated = new PureBigraphGenerator(exampleSignature).generate(1, 20, 1.0f);

        BigraphFileModelManagement.Store.exportAsInstanceModel(generated, new FileOutputStream(new File("src/test/resources/dump/exported-models/randomly-generated.xmi")));

        //the richer get more rich... theorem proven
        for (BigraphEntity.RootEntity each : generated.getRoots()) {
            Traverser<BigraphEntity> stringTraverser = Traverser.forTree(x -> generated.getChildrenOf(x));
            Iterable<BigraphEntity> v0 = stringTraverser.depthFirstPostOrder(each);
            ArrayList<BigraphEntity> bigraphEntities = Lists.newArrayList(v0);
            System.out.println("Nodes for root " + each.getIndex() + "=" + bigraphEntities.size());
        }
    }

    @Test
    void stats() throws IOException {
//        Signature<Control<?, ?>> exampleSignature = createExampleSignature();

        List<Float> probs = new ArrayList<>(Arrays.asList(0.1f, 0.25f, 0.5f, 0.8f));
        List<Integer> nSize = new ArrayList<>(Arrays.asList(10, 100, 1000));

        StringBuilder sb = new StringBuilder();
        int nTimes = 10000;//maximum number of nSize
        for (Float p : probs) {
            for (Integer n : nSize) {

//        float p = 0.5f;


//                int n = 1000;
                List<double[]> collectedStats = IntStream.range(0, nTimes).boxed().map(i -> {
                    DefaultDynamicSignature exampleSignature = createRandomSignature(26, p);
//                    Collections.shuffle(exampleSignature.getControls());
                    PureBigraphGenerator generator = new PureBigraphGenerator(exampleSignature);
                    generator.generate(1, n + 1, p);
                    return generator.getStats();
                }).collect(Collectors.toList());
//        DoubleSummaryStatistics doubleSummaryStatistics = collectedStats
//                .stream()
//                .mapToDouble(x -> x[0])
//                .summaryStatistics();
//        long count = collectedStats.stream().mapToDouble(x -> x[0])
//                .filter(x -> x != 0)
//                .count();
                // collect the node count (having positive arity)
                List<Double> collect = collectedStats.stream()
                        .map(x -> x[0])
                        .collect(Collectors.toList());

                String q = collect.toString().replace("[", "").replace("]", "");
                sb.append(p).append(", ").append(n).append(", ").append(q).append("\r\n");
//        System.out.println(collect.toString());
//        System.out.println(count);
//        System.out.println(doubleSummaryStatistics);
            }
        }

//        if(new File(Paths.get("analysis.data")))
        Files.write(
                Paths.get("/home/dominik/Documents/PhD/Papers/Concept/RandomBigraphGeneration/analysis/analysis_ceil.data"),
                sb.toString().getBytes(),
                StandardOpenOption.CREATE);
    }

    @Test
    void degreeDistribution() throws IOException {

        List<Integer> tSize = new ArrayList<>();
        tSize.add(1);
        tSize.add(10);
        tSize.add(50);
        List<Integer> nSize = new ArrayList<>();
        nSize.add(100);
        nSize.add(1000);
        nSize.add(10000);

        StringBuilder sb = new StringBuilder();

        int nTimes = 10;
        for (Integer t : tSize) {
            for (Integer n : nSize) {

                List<Integer> collect = IntStream.range(0, nTimes).boxed().map(i -> {
                    DefaultDynamicSignature exampleSignature = createRandomSignature(26, 1.0f);
//                    Collections.shuffle(exampleSignature.getControls());
                    PureBigraphGenerator generator = new PureBigraphGenerator(exampleSignature);
                    PureBigraph generate = generator.generate(t, n + 1, 1.0f);
                    return generate.getAllPlaces().stream().map(x -> {
                        int a = generate.getChildrenOf(x).size();
                        int b = BigraphEntityType.isRoot(x) ? 0 : 1;
                        return a + b;
                    }).collect(Collectors.toList());
                })
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
                String q = collect.toString().replace("[", "").replace("]", "");
                sb.append(t).append(", ").append(n).append(", ").append(q).append("\r\n");
            }
        }

//        if(new File(Paths.get("analysis.data")))
        Files.write(
                Paths.get("/home/dominik/Documents/PhD/Papers/Concept/RandomBigraphGeneration/analysis/bnalysis_1.data"),
                sb.toString().getBytes(),
                StandardOpenOption.CREATE);
    }

    @Test
    void assortativeLinking() throws IOException {

        List<Integer> tSize = new ArrayList<>();
        tSize.add(1);
//        tSize.add(10);
//        tSize.add(50);
        List<Integer> nSize = new ArrayList<>();
//        nSize.add(100);
        nSize.add(1000);
//        nSize.add(10000);

        StringBuilder sb = new StringBuilder();

//        generator.setLinkStrategy(RandomBigraphGenerator.LinkStrategy.MAXIMAL_DEGREE_DISASSORTATIVE);
        int nTimes = 1;
        for (Integer t : tSize) {
            for (Integer n : nSize) {

                List<Integer> collect = IntStream.range(0, nTimes).boxed()
                        .map(i -> {
                            DefaultDynamicSignature exampleSignature = createRandomSignaturePortVariation(40, 1.0f);
//                    Collections.shuffle(exampleSignature.getControls());
                            PureBigraphGenerator generator = new PureBigraphGenerator(exampleSignature);
                            generator.setLinkStrategy(RandomBigraphGeneratorSupport.LinkStrategy.MAXIMAL_DEGREE_ASSORTATIVE);
                            PureBigraph generate = generator.generate(t, n + 1, 1.0f);
//                            try {
//                                String convert = GraphvizConverter.toPNG(generate,
//                                        true,
//                                        new File("src/test/resources/graphviz/generated_maxdegree.png")
//                                );
////                                System.out.println(convert);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }

                            final StringBuilder nodeDissa = new StringBuilder();
                            generate.getNodes().stream()
                                    .forEach(each -> {
//                                        if(each.getControl().getArity().getValue().intValue() > 13) {
//                                            System.out.println("error");
//                                        }

                                        int portCount = generate.getPorts(each).size();

                                        Integer reduce = generate.getPorts(each)
                                                .stream()
                                                .map(x -> generate.getLinkOfPoint(x))
                                                .map(x -> generate.getPointsFromLink(x))
                                                .flatMap(x -> x.stream())
                                                .filter(x -> BigraphEntityType.isPort(x))
                                                .map(x -> generate.getNodeOfPort((BigraphEntity.Port) x))
                                                .filter(x -> x != each)
                                                //a
//                                                .map(x -> x.getControl().getArity().getValue().intValue())
//                                                .map(x -> Math.abs(x - each.getControl().getArity().getValue().intValue()))
//                                              //b
                                                .map(x -> generate.getPorts(x).size())
                                                .map(x -> Math.abs(x - portCount))

                                                .reduce(0, Integer::sum);

                                        //a
//                                        int result = reduce / each.getControl().getArity().getValue().intValue();
                                        //b
                                        double result = -1; //reduce / q;
                                        if (portCount == 0) {
//                                            assert q != 0;
                                            result = 0.0;
                                        } else {
                                            result = (reduce * 1.0) / portCount;
                                        }

                                        nodeDissa.append(each.getName())
                                                .append(",")
                                                .append(each.getControl().getArity().getValue().intValue())
                                                .append(",")
                                                .append(portCount)
                                                .append(",")
                                                .append(result)
//                                        nodeDissa.append(each.getName()).append(",").append(generate.getPorts(each).size()).append(",").append(result)
                                                .append("\r\n");
                                    });

                            try {
                                Files.write(
//                                        Paths.get("/home/dominik/Documents/PhD/Papers/Concept/RandomBigraphGeneration/analysis/node_dissa.data"),
                                        Paths.get("/home/dominik/Documents/PhD/Papers/Concept/RandomBigraphGeneration/analysis/node_asso.data"),
                                        nodeDissa.toString().getBytes());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

//                            StringBuilder pa = new StringBuilder();
//                            generate.getNodes().stream()
//                                    .forEach(each -> {
//                                        pa.append(each.getName()).append(",")
////                                                .append(each.getControl().getArity().getValue().intValue())
//                                                .append(generate.getPorts(each).size())
//                                                .append(",").append(each.getControl().getArity().getValue().intValue())
////                                        nodeDissa.append(each.getName()).append(",").append(generate.getPorts(each).size()).append(",").append(result)
//                                                .append("\r\n");
//                                    });
//
//                            try {
//                                Files.write(
//                                        Paths.get("/home/dominik/Documents/PhD/Papers/Concept/RandomBigraphGeneration/analysis/ports-arity-ratio-asso.data"),
//                                        pa.toString().getBytes(),
//                                        StandardOpenOption.CREATE);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }

                            return generate.getAllPlaces().stream().map(x -> {
                                int a = generate.getChildrenOf(x).size();
                                int b = BigraphEntityType.isRoot(x) ? 0 : 1;
                                return a + b;
                            }).collect(Collectors.toList());
                        })
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
//                String q = collect.toString().replace("[", "").replace("]", "");
//                sb.append(t).append(", ").append(n).append(", ").append(q).append("\r\n");
            }
        }

//        Files.write(
//                Paths.get("/home/dominik/Documents/PhD/Papers/Concept/RandomBigraphGeneration/analysis/assortative_nalysis_1.data"),
//                sb.toString().getBytes(),
//                StandardOpenOption.CREATE);
    }

    private <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();
        signatureBuilder
                .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Spool")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Job")).arity(FiniteOrdinal.ofInteger(1)).assign();

        return (S) signatureBuilder.create();
    }

    private DefaultDynamicSignature createRandomSignature(int n, float probOfPositiveArity) {
        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();

        char[] chars = IntStream.rangeClosed('A', 'Z')
                .mapToObj(c -> "" + (char) c).collect(Collectors.joining()).toCharArray();

        int floorNum = (int) Math.ceil(n * probOfPositiveArity);
        for (int i = 0; i < floorNum; i++) {
            signatureBuilder = (DynamicSignatureBuilder) signatureBuilder.newControl().identifier(StringTypedName.of(String.valueOf(chars[i]))).arity(FiniteOrdinal.ofInteger(1)).assign();
        }
        for (int i = floorNum; i < n; i++) {
            signatureBuilder = (DynamicSignatureBuilder) signatureBuilder.newControl().identifier(StringTypedName.of(String.valueOf(chars[i]))).arity(FiniteOrdinal.ofInteger(0)).assign();
        }
        DefaultDynamicSignature s = signatureBuilder.create();
        ArrayList<DefaultDynamicControl> cs = new ArrayList<>(s.getControls());
        Collections.shuffle(cs);
        return signatureBuilder.createWith(new LinkedHashSet<>(cs));
    }

    private DefaultDynamicSignature createRandomSignaturePortVariation(int n, float probOfPositiveArity) {
        DynamicSignatureBuilder signatureBuilder = pureSignatureBuilder();

//        char[] chars = IntStream.rangeClosed('A', 'Z')
//                .mapToObj(c -> "" + (char) c).collect(Collectors.joining()).toCharArray();

//        SecureRandom rnd = new SecureRandom();
        Sequence sequence = new Sequence();
        int floorNum = (int) Math.ceil(n * probOfPositiveArity);
        for (int i = 0; i < floorNum; i++) {
//            int rand = rnd.nextInt(maxPorts);
//            rand = rand == 0 ? 1 : rand;
//            System.out.println(rand);
            String s = sequence.computeNext();
            signatureBuilder = (DynamicSignatureBuilder) signatureBuilder.newControl()
                    .identifier(StringTypedName.of(String.valueOf(s)))
                    .arity(FiniteOrdinal.ofInteger(i)).assign();
        }
        for (int i = floorNum; i < n; i++) {
            String s = sequence.computeNext();
            signatureBuilder = (DynamicSignatureBuilder) signatureBuilder
                    .newControl().identifier(StringTypedName.of(String.valueOf(s)))
                    .arity(FiniteOrdinal.ofInteger(0)).assign();
        }
        DefaultDynamicSignature s = signatureBuilder.create();
        ArrayList<DefaultDynamicControl> cs = new ArrayList<>(s.getControls());
        Collections.shuffle(cs);
        return signatureBuilder.createWith(new LinkedHashSet<>(cs));
    }
}
