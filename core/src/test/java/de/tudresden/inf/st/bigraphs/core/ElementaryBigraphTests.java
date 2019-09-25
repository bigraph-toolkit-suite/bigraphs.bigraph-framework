package de.tudresden.inf.st.bigraphs.core;

import com.google.common.collect.Lists;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.DiscreteIon;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Linkings;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Placings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ElementaryBigraphTests {
    private static PureBigraphFactory<StringTypedName, FiniteOrdinal<Integer>> factory = AbstractBigraphFactory.createPureBigraphFactory();

    @Test
    void barren() {
        Placings.Barren b = new Placings(factory).barren();
        assertNotNull(b.getModelPackage());
//        assertNotNull(b.getModel());
        assertTrue(b.isPrime());
        assertEquals(1, b.getRoots().size());
        assertEquals(0, b.getSites().size());
        assertEquals(0, b.getOuterNames().size());
        assertEquals(0, b.getInnerNames().size());

        Placings.Barren b2 = new Placings(factory).barren();
        assertNotEquals(b, b2);

        assertTrue(b.isPrime());
        assertTrue(b2.isPrime());
        assertTrue(b.isGround());
        assertTrue(b2.isGround());
    }

    @Test
    void join() {
        Placings.Join join1 = new Placings(factory).join();
        Placings.Join join2 = new Placings(factory).join();

        assertTrue(join1.isPrime());
        assertTrue(join2.isPrime());

        assertEquals(1, join1.getRoots().size());
        assertEquals(2, join1.getSites().size());
        assertEquals(0, join1.getOuterNames().size());
        assertEquals(0, join1.getInnerNames().size());
        assertNotEquals(join1, join2);
    }

    @Test
    void merge() {
        Placings.Merge merge = new Placings(factory).merge(3);
        assertEquals(1, merge.getRoots().size());
        assertEquals(3, merge.getSites().size());
    }

    @Test
    void permutations() {
        int MAX_N = 10;
        for (int i = 1; i < MAX_N; i++) {
            Placings<Signature>.Permutation permutation = new Placings<>(factory).permutation(i);
            assertEquals(i, permutation.getRoots().size());
            assertEquals(i, permutation.getSites().size());
            if (i == 1)
                assertTrue(permutation.isPrime());
            else
                assertFalse(permutation.isPrime());
            // no link graph
            assertEquals(0, permutation.getOuterFace().getValue().size());
            assertEquals(0, permutation.getInnerFace().getValue().size());
            assertEquals(0, permutation.getInnerNames().size());
            assertEquals(0, permutation.getOuterNames().size());

            // check that each site-root mapping has the same index
            for (int j = 0; j < i; j++) {
                assertEquals(
                        Lists.newArrayList(permutation.getRoots()).get(j).getIndex(),
                        Lists.newArrayList(permutation.getSites()).get(j).getIndex()
                );
            }
        }

        Placings.Symmetry symmetry11 = new Placings(factory).symmetry11();
        assertEquals(2, symmetry11.getRoots().size());
        assertEquals(2, symmetry11.getSites().size());
        assertEquals(0, symmetry11.getInnerNames().size());
        assertEquals(0, symmetry11.getOuterNames().size());
        ArrayList<BigraphEntity.RootEntity> roots = Lists.newArrayList(symmetry11.getRoots());
        ArrayList<BigraphEntity.SiteEntity> sites = Lists.newArrayList(symmetry11.getSites());
        // Checking symmetry of root and site indices respectively (reversed indices)
        assertEquals(0, roots.get(0).getIndex());
        assertEquals(1, sites.get(0).getIndex());
        assertEquals(1, roots.get(1).getIndex());
        assertEquals(0, sites.get(1).getIndex());
        assertFalse(symmetry11.isPrime());


        //identity barren: id_1
        Placings<Signature>.Identity1 identity1 = new Placings<>(factory).identity1();
        assertEquals(1, identity1.getRoots().size());
        assertEquals(1, identity1.getSites().size());
        assertEquals(0, identity1.getInnerNames().size());
        assertEquals(0, identity1.getOuterNames().size());
        assertEquals(
                Lists.newArrayList(identity1.getRoots()).get(0).getIndex(),
                Lists.newArrayList(identity1.getSites()).get(0).getIndex()
        );
        assertEquals(0, Lists.newArrayList(identity1.getRoots()).get(0).getIndex());
        assertEquals(0, Lists.newArrayList(identity1.getSites()).get(0).getIndex());
        assertTrue(identity1.isPrime());


    }

    @Test
    void ion_atom_molecule() {
        String nodeName = "K";
        StringTypedName controlName = StringTypedName.of(nodeName);
        int maxArityCount = 5;

        for (int arity = 0; arity < maxArityCount; arity++) {
            System.out.println("Create Discrete Ion " + nodeName + " with artiy " + arity);
            DynamicSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>> signatureBuilder =
                    factory.createSignatureBuilder();
            Signature signature = signatureBuilder
                    .newControl().identifier(controlName).arity(FiniteOrdinal.ofInteger(arity)).assign()
                    .create();

            Set<StringTypedName> outerNames = arity == 0 ? Collections.emptySet() : IntStream.range(0, arity).boxed()
                    .map(x -> StringTypedName.of("x" + x))
                    .collect(Collectors.toSet());

            int finalArity = arity;
            assertAll(() -> {
                DiscreteIon<DefaultDynamicSignature, StringTypedName, FiniteOrdinal<Integer>> discreteIon =
                        factory.createDiscreteIon(controlName, outerNames, (DefaultDynamicSignature) signature);
                discreteIon.getModelPackage();
                assertTrue(discreteIon.isDiscrete());
                assertTrue(discreteIon.isPrime());
                assertEquals(finalArity, discreteIon.getOuterNames().size());
                assertEquals(1, discreteIon.getSites().size());
                assertEquals(1, discreteIon.getNodes().size());
                assertEquals(1, discreteIon.getRoots().size());
                System.out.println("Make discrete atom from discrete ion: " + nodeName + "_x * 1");
                Bigraph<DefaultDynamicSignature> discreteAtom = factory.asBigraphOperator(discreteIon)
                        .compose(factory.createPlacings().barren())
                        .getOuterBigraph();

                assertEquals(0, discreteAtom.getSites().size());
                assertEquals(1, discreteAtom.getNodes().size());
                assertEquals(1, discreteAtom.getRoots().size());
                assertEquals(finalArity, discreteAtom.getOuterNames().size());
            });
        }
    }

    @Test
    @DisplayName("Testing that all placings can be built from symmetry, barren and join: Examples from Defintion 3.1 of the Milner book")
    void placings_operation() {
        Placings<DefaultDynamicSignature> placings = factory.createPlacings();

        Placings<DefaultDynamicSignature>.Merge merge_0 = placings.merge(0);
        Placings<DefaultDynamicSignature>.Barren barren = placings.barren();
        // are isomorph?
        assertEquals(merge_0.getRoots().size(), barren.getRoots().size());
        assertEquals(merge_0.getSites().size(), barren.getSites().size());

        Placings<DefaultDynamicSignature>.Join join = placings.join();
        BigraphComposite<DefaultDynamicSignature> joinOp = factory.asBigraphOperator(join);
        Placings<DefaultDynamicSignature>.Identity1 identity1 = placings.identity1();
        BigraphComposite<DefaultDynamicSignature> identiy1Op = factory.asBigraphOperator(identity1);
        assertAll(() -> {
            for (int n = 0; n < 5; n++) {
                System.out.println("merge_(" + (n + 1) + ") = join * (id_1 + merge_(" + n + "))");
                Placings<DefaultDynamicSignature>.Merge merge_nPlus1 = placings.merge(n + 1);

                Placings<DefaultDynamicSignature>.Merge merge_n = placings.merge(n);
                BigraphComposite<DefaultDynamicSignature> juxtaposed = identiy1Op.juxtapose(merge_n);

                BigraphComposite<DefaultDynamicSignature> result = joinOp.compose(juxtaposed);

                assertEquals(merge_nPlus1.getRoots().size(), result.getOuterBigraph().getRoots().size());
                assertEquals(merge_nPlus1.getSites().size(), result.getOuterBigraph().getSites().size());
            }
        });

        assertAll(() -> {
            System.out.println("id_1 * 1 = 1");
            BigraphComposite<DefaultDynamicSignature> isAlsoBarren = identiy1Op.compose(barren);
            assertEquals(1, isAlsoBarren.getOuterBigraph().getRoots().size());
            assertEquals(0, isAlsoBarren.getOuterBigraph().getSites().size());
            assertEquals(0, isAlsoBarren.getOuterBigraph().getOuterNames().size());
            assertEquals(0, isAlsoBarren.getOuterBigraph().getInnerNames().size());
            assertTrue(isAlsoBarren.getOuterBigraph().isPrime());

            System.out.println("merge_1 * 1 = 1");
            BigraphComposite isAlsoBarren2 = factory.asBigraphOperator(placings.merge(1)).compose(barren);
            assertTrue(isAlsoBarren2.getOuterBigraph().isPrime());
            assertEquals(1, isAlsoBarren2.getOuterBigraph().getRoots().size());
            assertEquals(0, isAlsoBarren2.getOuterBigraph().getSites().size());
            assertEquals(0, isAlsoBarren2.getOuterBigraph().getOuterNames().size());
            assertEquals(0, isAlsoBarren2.getOuterBigraph().getInnerNames().size());
        });
    }

    @Test
    @DisplayName("Testing link axioms: Theorem 3.6")
    void link_axions() {
        Linkings<DefaultDynamicSignature> linkings = factory.createLinkings();
        Linkings<DefaultDynamicSignature>.Identity identity_x = linkings.identity(StringTypedName.of("x"));
        Linkings<DefaultDynamicSignature>.IdentityEmpty identity_e = linkings.identity_e();
        Linkings<DefaultDynamicSignature>.Substitution substitution_x_to_x = linkings.substitution(StringTypedName.of("x"), StringTypedName.of("x"));
        Linkings<DefaultDynamicSignature>.Closure closure_x = linkings.closure(StringTypedName.of("x"));
        PureBigraphBuilder<DefaultDynamicSignature> bigraphBuilder = factory.createBigraphBuilder(factory.createSignatureBuilder().createSignature());
        bigraphBuilder.createOuterName("x");
        PureBigraph bx = bigraphBuilder.createBigraph();
        assertAll(() -> {
            System.out.println("x/x = id_x");
            assertEquals(substitution_x_to_x.getOuterNames().stream().map(BigraphEntity.OuterName::getName).findFirst(),
                    identity_x.getOuterNames().stream().map(BigraphEntity.OuterName::getName).findFirst());
            assertEquals(substitution_x_to_x.getInnerNames().stream().map(BigraphEntity.InnerName::getName).findFirst(),
                    identity_x.getInnerNames().stream().map(BigraphEntity.InnerName::getName).findFirst());
            assertEquals(substitution_x_to_x.getOuterNames().size() == 1, identity_x.getOuterNames().size() == 1);
            assertEquals(substitution_x_to_x.getInnerNames().size() == 1, identity_x.getInnerNames().size() == 1);
        });

        assertAll(() -> {
            System.out.println("/x * x = id_e");
            Bigraph<DefaultDynamicSignature> composed = factory.asBigraphOperator(closure_x).compose(bx).getOuterBigraph();
            assertEquals(composed.getOuterNames().stream().map(BigraphEntity.OuterName::getName).findFirst(),
                    identity_e.getOuterNames().stream().map(BigraphEntity.OuterName::getName).findFirst());
            assertEquals(composed.getInnerNames().stream().map(BigraphEntity.InnerName::getName).findFirst(),
                    identity_e.getInnerNames().stream().map(BigraphEntity.InnerName::getName).findFirst());
            assertEquals(composed.getOuterNames().size() == 1,
                    identity_e.getOuterNames().size() == 1);
            assertEquals(composed.getInnerNames().size() == 1,
                    identity_e.getInnerNames().size() == 1);
        });

        assertAll(() -> {
            System.out.println("/y * y/x = /x");
            Linkings<DefaultDynamicSignature>.Closure y = linkings.closure(StringTypedName.of("y"));
            Linkings<DefaultDynamicSignature>.Substitution yx = linkings.substitution(StringTypedName.of("y"), StringTypedName.of("x"));
            BigraphEntity.OuterName outerName_yx = yx.getOuterNames().stream().findFirst().get();
            BigraphEntity.InnerName innerName_yx = yx.getInnerNames().stream().findFirst().get();
            assertEquals(yx.getOuterNames().size(), 1);
            assertEquals(yx.getInnerNames().size(), 1);
            assertEquals(outerName_yx.getName(), "y");
            assertEquals(yx.getInnerNames().stream().findFirst().get().getName(), "x");
            assertEquals(yx.getPointsFromLink(outerName_yx).size(), 1);
            assertEquals(yx.getPointsFromLink(outerName_yx).stream().findFirst().get(), innerName_yx);
            assertEquals(y.getInnerNames().stream().findFirst().get().getName(), "y");
            Bigraph<DefaultDynamicSignature> composed = factory.asBigraphOperator(y).compose(yx).getOuterBigraph();
            Linkings<DefaultDynamicSignature>.Closure x = linkings.closure(StringTypedName.of("x"));
            assertEquals(composed.getInnerNames().size() == 1,
                    x.getInnerNames().size() == 1);
            assertEquals(composed.getOuterNames().size() == 0,
                    x.getOuterNames().size() == 0);
            assertEquals(composed.getInnerNames().stream().map(BigraphEntity.InnerName::getName).findFirst().get(),
                    x.getInnerNames().stream().map(BigraphEntity.InnerName::getName).findFirst().get());
            assertEquals(composed.getInnerNames().stream().map(BigraphEntity.InnerName::getName).findFirst().get(),
                    "x");
//            BigraphModelFileStore.exportAsInstanceModel((PureBigraph) composed, "./compose_test_2a",
//                    new FileOutputStream("./composedcomposed.xmi"));
        });

        assertAll(() -> {
            System.out.println("z/(Y and y) * (id_Y + y/X) = z/(Y and X)");
            StringTypedName[] Y0 = new StringTypedName[]{StringTypedName.of("y1"), StringTypedName.of("y2"), StringTypedName.of("y3")};
            StringTypedName[] X = new StringTypedName[]{StringTypedName.of("x1"), StringTypedName.of("x2"), StringTypedName.of("x3")};
            StringTypedName x = StringTypedName.of("x0");
            StringTypedName y = StringTypedName.of("y0");
            StringTypedName z = StringTypedName.of("z");
            List<StringTypedName> Y_and_y = new ArrayList<>(Arrays.asList(Y0));
            Y_and_y.add(y);
            List<StringTypedName> Y_and_X = new ArrayList<>(Arrays.asList(Y0));
            Y_and_X.addAll(Arrays.asList(X));

            // right-hand side of the equation
            Linkings<DefaultDynamicSignature>.Substitution z_over_XY = linkings.substitution(z, Y_and_X.toArray(new StringTypedName[Y_and_y.size()]));

            // parts of the left-hand of the equation
            Linkings<DefaultDynamicSignature>.Substitution z_over_Yy = linkings.substitution(z, Y_and_y.toArray(new StringTypedName[Y_and_y.size()]));
            Linkings<DefaultDynamicSignature>.Identity identity_Y = linkings.identity(Y0);
            assertEquals(identity_Y.getOuterNames().size(), identity_Y.getInnerNames().size());
            Linkings<DefaultDynamicSignature>.Substitution y_over_X = linkings.substitution(y, X);
            assertEquals(y_over_X.getOuterNames().size(), 1);
            assertEquals(y_over_X.getInnerNames().size(), 3);
            assertEquals(z_over_XY.getOuterNames().size(), 1);
            assertEquals(z_over_XY.getInnerNames().size(), Y_and_X.size());
            Bigraph<DefaultDynamicSignature> composedRight = factory.asBigraphOperator(identity_Y).juxtapose(y_over_X).getOuterBigraph();
            Bigraph<DefaultDynamicSignature> result = factory.asBigraphOperator(z_over_Yy).compose(composedRight).getOuterBigraph();
            assertEquals(result.getOuterNames().size(), z_over_XY.getOuterNames().size());
            assertEquals(result.getOuterNames().size(), 1);
            assertEquals(result.getOuterNames().stream().findFirst().get().getName(), z.stringValue());

            assertEquals(result.getInnerNames().size(), z_over_XY.getInnerNames().size());
            assertEquals(result.getInnerNames().size(), Y0.length + X.length);
            for (BigraphEntity.InnerName innerName : result.getInnerNames()) {
                if (!Y_and_X.contains(StringTypedName.of(innerName.getName()))) {
                    throw new Exception("inner name not contained in the initial name set X + Y");
                }
            }
//            BigraphModelFileStore.exportAsInstanceModel((PureBigraph) result, "compoy",
//                    new FileOutputStream("compoy.xmi"));
//            BigraphModelFileStore.exportAsInstanceModel(z_over_XY, "z_over_XY",
//                    new FileOutputStream("z_over_XY.xmi"));
        });
    }

    @Test
    @DisplayName("Testing place axioms: Theorem 3.6")
    void place_axioms() {
        Placings<DefaultDynamicSignature> placings = factory.createPlacings();
        Placings<DefaultDynamicSignature>.Join join = placings.join();
        Placings<DefaultDynamicSignature>.Barren barren = placings.barren();
        Placings<DefaultDynamicSignature>.Symmetry symmetry11 = placings.symmetry11();
        Placings<DefaultDynamicSignature>.Identity1 identity1 = placings.identity1();

        assertAll(() -> {
            System.out.println("join * y_(1,1) = join");
            Bigraph<DefaultDynamicSignature> resultIsJoin = factory.asBigraphOperator(join).compose(symmetry11).getOuterBigraph();
            assertEquals(1, resultIsJoin.getRoots().size());
            assertTrue(resultIsJoin.isPrime());
            assertFalse(resultIsJoin.isGround());
            assertEquals(2, resultIsJoin.getSites().size());
            assertEquals(0, Lists.newArrayList(resultIsJoin.getRoots()).get(0).getIndex());
//            assertEquals(1, Lists.newArrayList(resultIsJoin.getSites()).get(1).getIndex());
        });

        assertAll(() -> {
            System.out.println("join * (1 + id_1) = id_1");
            BigraphComposite b = factory.asBigraphOperator(barren).juxtapose(identity1);
            BigraphComposite isIdentity_1 = factory.asBigraphOperator(join).compose(b);

            assertEquals(1, isIdentity_1.getOuterBigraph().getRoots().size());
            assertEquals(1, isIdentity_1.getOuterBigraph().getSites().size());
            assertFalse(isIdentity_1.getOuterBigraph().isGround());
            assertTrue(isIdentity_1.getOuterBigraph().isPrime());
            assertEquals(0, isIdentity_1.getOuterBigraph().getInnerNames().size());
            assertEquals(0, isIdentity_1.getOuterBigraph().getOuterNames().size());
        });

        assertAll(() -> {
            System.out.println("join * (join + id_1) = join * (id_1 + join)");
            BigraphComposite<DefaultDynamicSignature> b1 = factory.asBigraphOperator(join).juxtapose(identity1);

            BigraphComposite<DefaultDynamicSignature> b2 = factory.asBigraphOperator(identity1).juxtapose(join);

            Bigraph<DefaultDynamicSignature> outerBigraph1 = factory.asBigraphOperator(join).compose(b1).getOuterBigraph();
            Bigraph<DefaultDynamicSignature> outerBigraph2 = factory.asBigraphOperator(join).compose(b2).getOuterBigraph();

            assertEquals(outerBigraph1.isGround(), outerBigraph2.isGround());
            assertEquals(outerBigraph1.getInnerNames().size(), outerBigraph2.getInnerNames().size());
            assertEquals(outerBigraph1.getOuterNames().size(), outerBigraph2.getOuterNames().size());
            assertEquals(outerBigraph1.getRoots().size(), outerBigraph2.getRoots().size());
            assertEquals(outerBigraph1.getSites().size(), outerBigraph2.getSites().size());
            assertEquals(outerBigraph1.getNodes().size(), outerBigraph2.getNodes().size());
            assertEquals(1, outerBigraph1.getRoots().size());
            assertEquals(3, outerBigraph1.getSites().size());

        });
    }

    @Test
    void linkings() {
        Linkings<Signature> linkings = new Linkings<>(factory);
        Linkings<Signature>.Closure x = linkings.closure(StringTypedName.of("x"));
        Linkings<Signature>.Substitution substitution = linkings.substitution(StringTypedName.of("y"),
                StringTypedName.of("x1"),
                StringTypedName.of("x2"),
                StringTypedName.of("x3")
        );
        assertFalse(x.isGround());
        assertFalse(substitution.isGround());
    }
}
