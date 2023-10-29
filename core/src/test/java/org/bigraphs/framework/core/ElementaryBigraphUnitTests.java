package org.bigraphs.framework.core;

import com.google.common.collect.Lists;
import org.bigraphs.framework.core.*;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.elementary.DiscreteIon;
import org.bigraphs.framework.core.impl.elementary.Linkings;
import org.bigraphs.framework.core.impl.elementary.Placings;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.FileOutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ElementaryBigraphUnitTests {

    @Test
    void barren() {
        DefaultDynamicSignature empty = pureSignatureBuilder().createEmpty();
        Placings<DefaultDynamicSignature> placings = purePlacings(empty);

        Placings<DefaultDynamicSignature>.Barren b = placings.barren();
        assertNotNull(b.getMetaModel());
        assertNotNull(b.getInstanceModel());
        assertTrue(b.isPrime());
        assertEquals(1, b.getRoots().size());
        assertEquals(0, b.getSites().size());
        assertEquals(0, b.getOuterNames().size());
        assertEquals(0, b.getInnerNames().size());

        Placings<DefaultDynamicSignature>.Barren b2 = placings.barren();
        assertNotNull(b2.getMetaModel());
        assertNotNull(b2.getInstanceModel());
        assertNotEquals(b, b2);

        assertTrue(b.isPrime());
        assertTrue(b2.isPrime());
        assertTrue(b.isGround());
        assertTrue(b2.isGround());
    }

    @Test
    void join() {
        DefaultDynamicSignature empty = pureSignatureBuilder().createEmpty();
        Placings<DefaultDynamicSignature> placings = purePlacings(empty);

        Placings<DefaultDynamicSignature>.Join join1 = placings.join();
        Placings<DefaultDynamicSignature>.Join join2 = placings.join();
        assertNotNull(join1.getMetaModel());
        assertNotNull(join1.getInstanceModel());
        assertNotNull(join2.getMetaModel());
        assertNotNull(join2.getInstanceModel());


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
        DefaultDynamicSignature empty = pureSignatureBuilder().createEmpty();
        Placings<DefaultDynamicSignature> placings = purePlacings(empty);

        Placings<DefaultDynamicSignature>.Merge merge = placings.merge(3);
        assertEquals(1, merge.getRoots().size());
        assertEquals(3, merge.getSites().size());
        assertNotNull(merge.getMetaModel());
        assertNotNull(merge.getInstanceModel());
    }

    @Test
    void permutations() {
        DefaultDynamicSignature empty = pureSignatureBuilder().createEmpty();
        Placings<DefaultDynamicSignature> placings = purePlacings(empty);

        int MAX_N = 10;
        for (int i = 1; i < MAX_N; i++) {
            Placings<DefaultDynamicSignature>.Permutation permutation = placings.permutation(i);
            assertNotNull(permutation.getMetaModel());
            assertNotNull(permutation.getInstanceModel());
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

        Placings<DefaultDynamicSignature>.Symmetry symmetry11 = placings.symmetry11();
        assertNotNull(symmetry11.getMetaModel());
        assertNotNull(symmetry11.getInstanceModel());
        assertEquals(2, symmetry11.getRoots().size());
        assertEquals(2, symmetry11.getSites().size());
        assertEquals(0, symmetry11.getInnerNames().size());
        assertEquals(0, symmetry11.getOuterNames().size());
        ArrayList<BigraphEntity.RootEntity> roots = Lists.newArrayList(symmetry11.getRoots());
        // Checking symmetry of root and site indices respectively (reversed indices)
        assertNotEquals(roots.get(0).getIndex(),
                ((BigraphEntity.SiteEntity) symmetry11.getChildrenOf(roots.get(0)).iterator().next()).getIndex());
        assertNotEquals(roots.get(1).getIndex(),
                ((BigraphEntity.SiteEntity) symmetry11.getChildrenOf(roots.get(1)).iterator().next()).getIndex());
        assertFalse(symmetry11.isPrime());


        //identity barren: id_1
        Placings<DefaultDynamicSignature>.Identity1 identity1 = placings.identity1();
        assertNotNull(identity1.getMetaModel());
        assertNotNull(identity1.getInstanceModel());
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
            DynamicSignatureBuilder signatureBuilder =
                    pureSignatureBuilder();
            Signature signature = signatureBuilder
                    .newControl().identifier(controlName).arity(FiniteOrdinal.ofInteger(arity)).assign()
                    .create();

            Set<StringTypedName> outerNames = arity == 0 ? Collections.emptySet() : IntStream.range(0, arity).boxed()
                    .map(x -> StringTypedName.of("x" + x))
                    .collect(Collectors.toSet());

            String[] outerNamesAsString = outerNames.stream().map(StringTypedName::getValue).toArray(String[]::new);

            int finalArity = arity;
            assertAll(() -> {
                DiscreteIon<DefaultDynamicSignature> discreteIon =
                        pureDiscreteIon((DefaultDynamicSignature) signature, controlName.getValue(), outerNamesAsString);
                Placings<DefaultDynamicSignature> placings = purePlacings(discreteIon.getSignature());
                assertNotNull(discreteIon.getMetaModel());
                assertNotNull(discreteIon.getInstanceModel());
                assertTrue(discreteIon.isDiscrete());
                assertTrue(discreteIon.isPrime());
                assertEquals(finalArity, discreteIon.getOuterNames().size());
                assertEquals(1, discreteIon.getSites().size());
                assertEquals(1, discreteIon.getNodes().size());
                assertEquals(1, discreteIon.getRoots().size());
                System.out.println("Make discrete atom from discrete ion: " + nodeName + "_x * 1");
                Bigraph<DefaultDynamicSignature> discreteAtom = ops(discreteIon)
                        .compose(placings.barren())
                        .getOuterBigraph();

                assertEquals(0, discreteAtom.getSites().size());
                assertEquals(1, discreteAtom.getNodes().size());
                assertEquals(1, discreteAtom.getRoots().size());
                assertEquals(finalArity, discreteAtom.getOuterNames().size());
            });
        }
    }

    @Test
    @DisplayName("Nesting test: build non-discrete molecules from discrete ones")
    void non_discrete_molecules() {
        DefaultDynamicSignature sig = pureSignatureBuilder().newControl("K", 3).assign().newControl("L", 2).assign().create();
        Linkings<DefaultDynamicSignature> linkingsFactory = pureLinkings(sig);

        DiscreteIon<DefaultDynamicSignature> K_xyz = pureDiscreteIon(sig, "K", "x", "y", "z");
        DiscreteIon<DefaultDynamicSignature> L_yz = pureDiscreteIon(sig, "L", "y", "z");

        Linkings<DefaultDynamicSignature>.Identity id_XY = linkingsFactory.identity(StringTypedName.of("y"), StringTypedName.of("z"));

        assertAll(() -> {
            BigraphComposite<DefaultDynamicSignature> a = ops(K_xyz).parallelProduct(id_XY);
//            BigraphFileModelManagement.exportAsInstanceModel(a.getOuterBigraph(), System.out);
            Bigraph<DefaultDynamicSignature> b = a.compose(L_yz).getOuterBigraph();
            BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) b, System.out);

            Bigraph<DefaultDynamicSignature> c = ops(K_xyz).nesting(L_yz).getOuterBigraph();
            BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) c, System.out);

            assertEquals(3, c.getOuterNames().size());
            assertEquals(3, b.getOuterNames().size());
            assertEquals(1, c.getSites().size());
            assertEquals(1, b.getSites().size());
            assertEquals(1, c.getRoots().size());
            assertEquals(1, b.getRoots().size());

            BigraphEntity.OuterName c_x = c.getOuterNames().stream().filter(q -> q.getName().equals("x")).findFirst().get();
            BigraphEntity.OuterName c_y = c.getOuterNames().stream().filter(q -> q.getName().equals("y")).findFirst().get();
            BigraphEntity.OuterName c_z = c.getOuterNames().stream().filter(q -> q.getName().equals("z")).findFirst().get();
            assertEquals(1, c.getPointsFromLink(c_x).size());
            assertEquals(2, c.getPointsFromLink(c_y).size());
            assertEquals(2, c.getPointsFromLink(c_z).size());

            BigraphEntity.OuterName b_x = b.getOuterNames().stream().filter(q -> q.getName().equals("x")).findFirst().get();
            BigraphEntity.OuterName b_y = b.getOuterNames().stream().filter(q -> q.getName().equals("y")).findFirst().get();
            BigraphEntity.OuterName b_z = b.getOuterNames().stream().filter(q -> q.getName().equals("z")).findFirst().get();
            assertEquals(1, b.getPointsFromLink(b_x).size());
            assertEquals(2, b.getPointsFromLink(b_y).size());
            assertEquals(2, b.getPointsFromLink(b_z).size());
        });
    }

    @Test
    void auto_infer_identity_in_composition() {
//        closure("x") * (a["x"] - barren())
        DefaultDynamicSignature sig = pureSignatureBuilder().newControl("K", 3).assign().newControl("L", 2).assign().create();
        Linkings<DefaultDynamicSignature> linkingsFactory = pureLinkings(sig);
        Placings<DefaultDynamicSignature> placingsFactory = purePlacings(sig);

        DiscreteIon<DefaultDynamicSignature> K_x = pureDiscreteIon(sig, "K", "x");
        Placings<DefaultDynamicSignature>.Merge merge = placingsFactory.merge(1);
        Linkings<DefaultDynamicSignature>.Closure x = linkingsFactory.closure(StringTypedName.of("x"));

//        assertAll(() -> {
//            Bigraph<DefaultDynamicSignature> r1 = ops(merge).compose(K_x).getOuterBigraph();
//            BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) K_x, System.out);
//            BigraphFileModelManagement.exportAsInstanceModel((EcoreBigraph) r1, System.out);
//        });


        assertAll(() -> {
            Bigraph<DefaultDynamicSignature> r = ops(x).compose(K_x).getOuterBigraph();
            BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) K_x, System.out);
            BigraphFileModelManagement.Store.exportAsInstanceModel((EcoreBigraph) r, System.out);
        });

    }

    @Test
    @DisplayName("Testing that all placings can be built from symmetry, barren and join: Examples from Defintion 3.1 of the Milner book")
    void placings_operation() {
        Placings placings = purePlacings(pureSignatureBuilder().createEmpty());

        Placings<DefaultDynamicSignature>.Merge merge_0 = placings.merge(0);
        Placings<DefaultDynamicSignature>.Barren barren = placings.barren();
        assertNotNull(merge_0.getMetaModel());
        assertNotNull(merge_0.getInstanceModel());
        assertNotNull(barren.getMetaModel());
        assertNotNull(barren.getInstanceModel());

        // are isomorph?
        assertEquals(merge_0.getRoots().size(), barren.getRoots().size());
        assertEquals(merge_0.getSites().size(), barren.getSites().size());

        Placings<DefaultDynamicSignature>.Join join = placings.join();
        BigraphComposite<DefaultDynamicSignature> joinOp = ops(join);
        Placings<DefaultDynamicSignature>.Identity1 identity1 = placings.identity1();
        BigraphComposite<DefaultDynamicSignature> identiy1Op = ops(identity1);
        assertAll(() -> {
            for (int n = 0; n < 5; n++) {
                System.out.println("merge_(" + (n + 1) + ") = join * (id_1 + merge_(" + n + "))");
                Placings<DefaultDynamicSignature>.Merge merge_nPlus1 = placings.merge(n + 1);

                Placings<DefaultDynamicSignature>.Merge merge_n = placings.merge(n);
                BigraphComposite<DefaultDynamicSignature> juxtaposed = identiy1Op.juxtapose(merge_n);

                BigraphComposite<DefaultDynamicSignature> result = joinOp.compose(juxtaposed);

                assertNotNull(((EcoreBigraph) result.getOuterBigraph()).getMetaModel());
                assertNotNull(((PureBigraph) result.getOuterBigraph()).getInstanceModel());
                assertEquals(merge_nPlus1.getRoots().size(), result.getOuterBigraph().getRoots().size());
                assertEquals(merge_nPlus1.getSites().size(), result.getOuterBigraph().getSites().size());
                assertEquals(((PureBigraph) result.getOuterBigraph()).getInstanceModel().eContents().size(), result.getOuterBigraph().getRoots().size());
                assertEquals(((PureBigraph) result.getOuterBigraph()).getInstanceModel().eContents().get(0).eContents().size(), result.getOuterBigraph().getSites().size());
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
            assertNotNull(((PureBigraph) isAlsoBarren.getOuterBigraph()).getMetaModel());
            assertNotNull(((PureBigraph) isAlsoBarren.getOuterBigraph()).getInstanceModel());

            System.out.println("merge_1 * 1 = 1");
            BigraphComposite<DefaultDynamicSignature> isAlsoBarren2 = ops(placings.merge(1)).compose(barren);
            assertNotNull(((PureBigraph) isAlsoBarren2.getOuterBigraph()).getMetaModel());
            assertNotNull(((PureBigraph) isAlsoBarren2.getOuterBigraph()).getInstanceModel());
            assertTrue(isAlsoBarren2.getOuterBigraph().isPrime());
            assertEquals(1, isAlsoBarren2.getOuterBigraph().getRoots().size());
            assertEquals(0, isAlsoBarren2.getOuterBigraph().getSites().size());
            assertEquals(0, isAlsoBarren2.getOuterBigraph().getOuterNames().size());
            assertEquals(0, isAlsoBarren2.getOuterBigraph().getInnerNames().size());
        });
    }

    @Test
    @DisplayName("Testing link axioms: Theorem 3.6")
    void link_axioms() {
        DefaultDynamicSignature empty = pureSignatureBuilder().createEmpty();
        Linkings<DefaultDynamicSignature> linkings = pureLinkings(empty);

        Linkings<DefaultDynamicSignature>.Identity identity_x = linkings.identity(StringTypedName.of("x"));
        Linkings<DefaultDynamicSignature>.IdentityEmpty identity_e = linkings.identity_e();
        Linkings<DefaultDynamicSignature>.Substitution substitution_x_to_x = linkings.substitution(StringTypedName.of("x"), StringTypedName.of("x"));
        Linkings<DefaultDynamicSignature>.Closure closure_x = linkings.closure(StringTypedName.of("x"));

        // Test if the Ecore-related meta-model and instance model are set
        assertNotNull(identity_x.getMetaModel());
        assertNotNull(identity_x.getInstanceModel());
        assertNotNull(identity_e.getMetaModel());
        assertNotNull(identity_e.getInstanceModel());
        assertNotNull(substitution_x_to_x.getMetaModel());
        assertNotNull(substitution_x_to_x.getInstanceModel());
        assertNotNull(closure_x.getMetaModel());
        assertNotNull(closure_x.getInstanceModel());

        PureBigraphBuilder<DefaultDynamicSignature> bigraphBuilder = pureBuilder(pureSignatureBuilder().createEmpty());
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
            Bigraph<DefaultDynamicSignature> composed = ops(closure_x).compose(bx).getOuterBigraph();
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
            Bigraph<DefaultDynamicSignature> composed = ops(y).compose(yx).getOuterBigraph();
            Linkings<DefaultDynamicSignature>.Closure x = linkings.closure(StringTypedName.of("x"));

            assertNotNull(((PureBigraph) composed).getMetaModel());
            assertNotNull(((PureBigraph) composed).getInstanceModel());

            assertEquals(composed.getInnerNames().size() == 1,
                    x.getInnerNames().size() == 1);
            assertEquals(composed.getOuterNames().size() == 0,
                    x.getOuterNames().size() == 0);
            assertEquals(composed.getInnerNames().stream().map(BigraphEntity.InnerName::getName).findFirst().get(),
                    x.getInnerNames().stream().map(BigraphEntity.InnerName::getName).findFirst().get());
            assertEquals(composed.getInnerNames().stream().map(BigraphEntity.InnerName::getName).findFirst().get(),
                    "x");
            BigraphFileModelManagement.Store.exportAsInstanceModel((PureBigraph) composed, new FileOutputStream("src/test/resources/dump/exported-models/composedcomposed.xmi"));
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
            assertNotNull(z_over_XY.getMetaModel());
            assertNotNull(z_over_XY.getInstanceModel());
            // parts of the left-hand of the equation
            Linkings<DefaultDynamicSignature>.Substitution z_over_Yy = linkings.substitution(z, Y_and_y.toArray(new StringTypedName[Y_and_y.size()]));
            Linkings<DefaultDynamicSignature>.Identity identity_Y = linkings.identity(Y0);
            assertEquals(identity_Y.getOuterNames().size(), identity_Y.getInnerNames().size());
            Linkings<DefaultDynamicSignature>.Substitution y_over_X = linkings.substitution(y, X);
            assertEquals(y_over_X.getOuterNames().size(), 1);
            assertEquals(y_over_X.getInnerNames().size(), 3);
            assertEquals(z_over_XY.getOuterNames().size(), 1);
            assertEquals(z_over_XY.getInnerNames().size(), Y_and_X.size());
            Bigraph<DefaultDynamicSignature> composedRight = ops(identity_Y).juxtapose(y_over_X).getOuterBigraph();
            Bigraph<DefaultDynamicSignature> result = ops(z_over_Yy).compose(composedRight).getOuterBigraph();
            assertNotNull(((PureBigraph) result).getMetaModel());
            assertNotNull(((PureBigraph) result).getInstanceModel());
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
        Placings placings = purePlacings(pureSignatureBuilder().createEmpty());

        Placings<DefaultDynamicSignature>.Join join = placings.join();
        Placings<DefaultDynamicSignature>.Barren barren = placings.barren();
        Placings<DefaultDynamicSignature>.Symmetry symmetry11 = placings.symmetry11();
        Placings<DefaultDynamicSignature>.Identity1 identity1 = placings.identity1();
        assertNotNull(join.getMetaModel());
        assertNotNull(join.getInstanceModel());
        assertNotNull(symmetry11.getMetaModel());
        assertNotNull(symmetry11.getInstanceModel());

        assertAll(() -> {
            System.out.println("join * y_(1,1) = join");
            Bigraph<DefaultDynamicSignature> resultIsJoin = ops(join).compose(symmetry11).getOuterBigraph();
            assertEquals(1, resultIsJoin.getRoots().size());
            assertTrue(resultIsJoin.isPrime());
            assertFalse(resultIsJoin.isGround());
            assertEquals(2, resultIsJoin.getSites().size());
            assertEquals(0, Lists.newArrayList(resultIsJoin.getRoots()).get(0).getIndex());
            assertNotNull(((PureBigraph) resultIsJoin).getMetaModel());
            assertNotNull(((PureBigraph) resultIsJoin).getInstanceModel());
//            assertEquals(1, Lists.newArrayList(resultIsJoin.getSites()).get(1).getIndex());
        });

        assertAll(() -> {
            System.out.println("join * (1 + id_1) = id_1");
            BigraphComposite b = ops(barren).juxtapose(identity1);
            BigraphComposite isIdentity_1 = ops(join).compose(b);

            assertEquals(1, isIdentity_1.getOuterBigraph().getRoots().size());
            assertEquals(1, isIdentity_1.getOuterBigraph().getSites().size());
            assertFalse(isIdentity_1.getOuterBigraph().isGround());
            assertTrue(isIdentity_1.getOuterBigraph().isPrime());
            assertEquals(0, isIdentity_1.getOuterBigraph().getInnerNames().size());
            assertEquals(0, isIdentity_1.getOuterBigraph().getOuterNames().size());
            assertNotNull(((PureBigraph) isIdentity_1.getOuterBigraph()).getMetaModel());
            assertNotNull(((PureBigraph) isIdentity_1.getOuterBigraph()).getInstanceModel());
        });

        assertAll(() -> {
            System.out.println("join * (join + id_1) = join * (id_1 + join)");
            BigraphComposite<DefaultDynamicSignature> b1 = ops(join).juxtapose(identity1);

            BigraphComposite<DefaultDynamicSignature> b2 = ops(identity1).juxtapose(join);

            Bigraph<DefaultDynamicSignature> outerBigraph1 = ops(join).compose(b1).getOuterBigraph();
            Bigraph<DefaultDynamicSignature> outerBigraph2 = ops(join).compose(b2).getOuterBigraph();

            assertEquals(outerBigraph1.isGround(), outerBigraph2.isGround());
            assertEquals(outerBigraph1.getInnerNames().size(), outerBigraph2.getInnerNames().size());
            assertEquals(outerBigraph1.getOuterNames().size(), outerBigraph2.getOuterNames().size());
            assertEquals(outerBigraph1.getRoots().size(), outerBigraph2.getRoots().size());
            assertEquals(outerBigraph1.getSites().size(), outerBigraph2.getSites().size());
            assertEquals(outerBigraph1.getNodes().size(), outerBigraph2.getNodes().size());
            assertEquals(1, outerBigraph1.getRoots().size());
            assertEquals(3, outerBigraph1.getSites().size());
            assertNotNull(((PureBigraph) outerBigraph1).getMetaModel());
            assertNotNull(((PureBigraph) outerBigraph1).getInstanceModel());
            assertNotNull(((PureBigraph) outerBigraph2).getMetaModel());
            assertNotNull(((PureBigraph) outerBigraph2).getInstanceModel());

        });
    }

    @Test
    void linkings() {
        DefaultDynamicSignature empty = pureSignatureBuilder().createEmpty();
        Linkings linkings = pureLinkings(empty);
        Linkings<DefaultDynamicSignature>.Closure x = linkings.closure(StringTypedName.of("x"));
        Linkings<DefaultDynamicSignature>.Substitution substitution = linkings.substitution(StringTypedName.of("y"),
                StringTypedName.of("x1"),
                StringTypedName.of("x2"),
                StringTypedName.of("x3")
        );
        assertNotNull(substitution.getMetaModel());
        Linkings linkings1 = pureLinkings(empty);
        assertNotNull(substitution.getInstanceModel());
        assertNotNull(x.getMetaModel());
        assertNotNull(x.getInstanceModel());
        assertFalse(x.isGround());
        assertFalse(substitution.isGround());
    }
}
