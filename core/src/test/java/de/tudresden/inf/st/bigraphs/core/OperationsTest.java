package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.building.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Linkings;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Placings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class OperationsTest {

    private PureBigraphFactory<StringTypedName, FiniteOrdinal<Integer>> factory = AbstractBigraphFactory.createPureBigraphFactory();

    @BeforeEach
    void setUp() {
    }

    @Test
    void compose_test() throws InvalidConnectionException, LinkTypeNotExistsException, IncompatibleSignatureException, IncompatibleInterfaceException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        PureBigraphBuilder<DefaultDynamicSignature> builderForF = factory.createBigraphBuilder(signature);
        PureBigraphBuilder<DefaultDynamicSignature> builderForG = factory.createBigraphBuilder(signature);

        BigraphEntity.OuterName jeff = builderForF.createOuterName("jeff");
        BigraphEntity.InnerName jeffG = builderForG.createInnerName("jeff");

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy room =
                builderForF.newHierarchy(signature.getControlByName("Room"));
        room.addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff).addChild(signature.getControlByName("Job"));
        builderForF.createRoot()
                .addHierarchyToParent(room);

        builderForG.createRoot()
                .addChild(signature.getControlByName("Job")).withNewHierarchy().addSite().goBack()
                .addChild(signature.getControlByName("User")).connectNodeToInnerName(jeffG);


        PureBigraph F = builderForF.createBigraph();
        PureBigraph G = builderForG.createBigraph();


        BigraphComposite<DefaultDynamicSignature> compositor = factory.asBigraphOperator(G);
//        DefaultBigraphComposite<DefaultDynamicSignature> compositor = (DefaultBigraphComposite<DefaultDynamicSignature>) factory.asBigraphOperator(G);
        BigraphComposite<DefaultDynamicSignature> composedBigraph = compositor.compose(F);

        compositor.juxtapose(F);

//        Bigraph<DefaultDynamicSignature> outerBigraph = composedBigraph.getOuterBigraph();

//        BigraphEntity.NodeEntity<DefaultDynamicControl> next = F.getNodes().iterator().next();
//        BigraphEntity parent = F.getParent(next);
//        BigraphEntity parent0 = F.getParent(parent);
//        int index = ((BigraphEntity.RootEntity) parent0).getIndex();
//        System.out.println("index=" + index + " __ equals= " + parent0.equals(parent));
    }

    @Test
    void elementary_bigraph_composition_test() {
        //we want to check whether: merge_m+1 = join(id1 ⊗ merge_m).
        int m = 3;
        Placings<DefaultDynamicSignature> placings = factory.createPlacings();
        Placings<DefaultDynamicSignature>.Merge merge_MplusOne = placings.merge(m + 1);

        Placings<DefaultDynamicSignature>.Join aJoin = placings.join();
        Placings<DefaultDynamicSignature>.Merge merge_1 = placings.merge(1); //id_1 = merge_1
        Placings<DefaultDynamicSignature>.Merge merge_M = placings.merge(m);

        BigraphComposite<DefaultDynamicSignature> a = factory.asBigraphOperator(merge_1);
        BigraphComposite<DefaultDynamicSignature> b = factory.asBigraphOperator(aJoin);

        assertAll(() -> {
            Bigraph<DefaultDynamicSignature> outerBigraph = b.compose(a.juxtapose(merge_M).getOuterBigraph()).getOuterBigraph();
            assertEquals(merge_MplusOne.getRoots().size(), outerBigraph.getRoots().size());
            assertEquals(merge_MplusOne.getSites().size(), outerBigraph.getSites().size());
            assertEquals(merge_MplusOne.getOuterNames().size() == 0, outerBigraph.getOuterNames().size() == 0);
            assertEquals(merge_MplusOne.getOuterNames().size() == 0, outerBigraph.getInnerNames().size() == 0);

            // do the sites have the same and only one parent?
            BigraphEntity.RootEntity rootA = outerBigraph.getRoots().iterator().next();
            List<Boolean> collectA = outerBigraph.getSites().stream().map(x -> outerBigraph.getParent(x) == rootA).collect(Collectors.toList());
            BigraphEntity.RootEntity rootB = merge_MplusOne.getRoots().iterator().next();
            List<Boolean> collectB = merge_MplusOne.getSites().stream().map(x -> merge_MplusOne.getParent(x) == rootB).collect(Collectors.toList());

            assertEquals(collectA.stream().allMatch(y -> y), collectB.stream().allMatch(y -> y));
        });
    }

    @Test
    void elementary_bigraph_linkings_test() {
        Linkings<DefaultDynamicSignature> linkings = new Linkings<>(factory);
        Linkings<DefaultDynamicSignature>.Closure x1 = linkings.closure(StringTypedName.of("x"));
        final Linkings<DefaultDynamicSignature>.Closure x2 = linkings.closure(StringTypedName.of("x"));

        BigraphComposite<DefaultDynamicSignature> x1Op = factory.asBigraphOperator(x1);
        assertThrows(IncompatibleInterfaceException.class, () -> x1Op.juxtapose(x2));

        final Linkings<DefaultDynamicSignature>.Closure x3 = linkings.closure(StringTypedName.of("y"));
        assertAll(() -> {
            BigraphComposite<DefaultDynamicSignature> juxtapose = x1Op.juxtapose(x3);
            assertEquals(2, juxtapose.getOuterBigraph().getInnerNames().size());
            assertEquals(2, juxtapose.getOuterBigraph().getInnerFace().getValue().size());
        });


        assertAll(() -> {

            //a bigraph is composed with a closure resulting in a inner name rewriting of that bigraph
            Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
            PureBigraphBuilder<DefaultDynamicSignature> builderForG = factory.createBigraphBuilder(signature);
            BigraphEntity.InnerName zInner = builderForG.createInnerName("z");
            builderForG.createRoot().addChild(signature.getControlByName("User")).connectNodeToInnerName(zInner);
            PureBigraph simpleBigraph = builderForG.createBigraph();
            Linkings<DefaultDynamicSignature>.Substitution substitution = linkings.substitution(StringTypedName.of("z"), StringTypedName.of("y"));
            BigraphComposite<DefaultDynamicSignature> compose = factory.asBigraphOperator(simpleBigraph);
            BigraphComposite<DefaultDynamicSignature> compose1 = compose.compose(substitution);

            assertFalse(compose1.getOuterBigraph().isGround());
            assertEquals(1, compose1.getOuterBigraph().getInnerNames().size());
            // name was overwritten
            assertEquals("y", compose1.getOuterBigraph().getInnerNames().iterator().next().getName());
            assertEquals(0, compose1.getOuterBigraph().getOuterNames().size());
        });


        assertAll(() -> {
            Linkings<DefaultDynamicSignature>.Substitution a1 = linkings.substitution(StringTypedName.of("a"), StringTypedName.of("b"), StringTypedName.of("c"));
            Linkings<DefaultDynamicSignature>.Substitution a2 = linkings.substitution(StringTypedName.of("x"), StringTypedName.of("a"));
            Bigraph<DefaultDynamicSignature> outerBigraph = factory.asBigraphOperator(a2).compose(a1).getOuterBigraph();
            assertEquals(outerBigraph.getOuterNames().size(), 1);
            assertEquals(outerBigraph.getInnerNames().size(), 2);
            assertEquals(outerBigraph.getNodes().size(), 0);
            assertEquals(outerBigraph.getRoots().size(), 0);
            assertEquals(outerBigraph.getSites().size(), 0);
            assertFalse(outerBigraph.isGround());
        });
    }

    private <C extends Control<?, ?>, S extends Signature<C>> S createExampleSignature() {
        DynamicSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>> signatureBuilder = factory.createSignatureBuilder();
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
