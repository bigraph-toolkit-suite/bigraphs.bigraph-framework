package de.tudresden.inf.st.bigraphs.core;

import com.google.common.collect.Lists;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.SimpleBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Linkings;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Placings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ElementaryBigraphTests {
    private static SimpleBigraphFactory<StringTypedName, FiniteOrdinal<Integer>> factory = new SimpleBigraphFactory<>();

    @Test
    void barren() {
        Placings.Barren b = new Placings(factory).barren();
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
