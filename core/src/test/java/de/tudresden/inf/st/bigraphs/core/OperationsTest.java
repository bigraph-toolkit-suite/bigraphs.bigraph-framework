package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.building.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.DynamicEcoreBigraph;
import de.tudresden.inf.st.bigraphs.core.factory.SimpleBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Placings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OperationsTest {

    private SimpleBigraphFactory<StringTypedName, FiniteOrdinal<Integer>> factory;

    @BeforeEach
    void setUp() {
        factory = new SimpleBigraphFactory<>();
    }

    @Test
    void compose_test() throws InvalidConnectionException, LinkTypeNotExistsException, IncompatibleSignatureException, IncompatibleInterfaceException {
        Signature<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> signature = createExampleSignature();
        BigraphBuilder<DefaultDynamicSignature> builderForF = factory.createBigraphBuilder(signature);
        BigraphBuilder<DefaultDynamicSignature> builderForG = factory.createBigraphBuilder(signature);

        BigraphEntity.OuterName jeff = builderForF.createOuterName("jeff");
        BigraphEntity.InnerName jeffG = builderForG.createInnerName("jeff");

        BigraphBuilder<DefaultDynamicSignature>.Hierarchy room =
                builderForF.newHierarchy(signature.getControlByName("Room"));
        room.addChild(signature.getControlByName("User")).connectNodeToOuterName(jeff).addChild(signature.getControlByName("Job"));
        builderForF.createRoot()
                .addHierarchyToParent(room);

        builderForG.createRoot()
                .addChild(signature.getControlByName("Job")).withNewHierarchy().addSite().goBack()
                .addChild(signature.getControlByName("User")).connectNodeToInnerName(jeffG);


        DynamicEcoreBigraph F = builderForF.createBigraph();
        DynamicEcoreBigraph G = builderForG.createBigraph();


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
        Placings.Merge mergeM = Placings.merge(m + 1);

        Placings.Join aJoin = Placings.join();
        Placings.Merge merge1 = Placings.merge(1); //id_1 = merge_1
        Placings.Merge mergeM0 = Placings.merge(m);

//        factory.asBigraphOperator(aJoin);

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
