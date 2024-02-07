package org.bigraphs.framework.simulation.canonicalstring;

import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.elementary.Linkings;
import org.bigraphs.framework.core.impl.elementary.Placings;
import org.bigraphs.framework.simulation.encoding.BigraphCanonicalForm;
import org.apache.commons.io.FileUtils;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Sets;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Dominik Grzelak
 */
public class CanonicalElementaryBigraphsUnitTests {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/canonicform/elementary/";

    static BigraphCanonicalForm bfcs;
    static Placings<DefaultDynamicSignature> placings;
    static Linkings<DefaultDynamicSignature> linkings;

    @BeforeAll
    static void setUp() throws IOException {
        DefaultDynamicSignature EMPTY_SIG = pureSignatureBuilder().createEmpty();
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));

        bfcs = BigraphCanonicalForm.createInstance();
        placings = purePlacings(EMPTY_SIG);
        linkings = pureLinkings(EMPTY_SIG);
    }

    @Test
    @DisplayName("Barren (elementary placing)")
    void barren_test_01() {
        Placings<DefaultDynamicSignature>.Barren barren = placings.barren();

        String bfcs_1 = bfcs.bfcs(barren);
        System.out.println(bfcs_1);
        assertEquals("r0#", bfcs_1);
    }

    @Test
    @DisplayName("identity (elementary placing)")
    void identity_test_01() {
        Placings<DefaultDynamicSignature>.Identity1 identity1 = placings.identity1();
        String bfcs = CanonicalElementaryBigraphsUnitTests.bfcs.bfcs(identity1);
        System.out.println(bfcs);
        assertEquals("r0$0#", bfcs);
    }

    @Test
    @DisplayName("Join (elementary placing)")
    void join_test_01() {
        Placings<DefaultDynamicSignature>.Join join = placings.join();

        String bfcs_1 = bfcs.bfcs(join);
        System.out.println(bfcs_1);
        assertEquals("r0$01#", bfcs_1);
    }

    @Test
    @DisplayName("Merge_n where n = 2 (elementary placing)")
    void merge_test_01() {
        Placings<DefaultDynamicSignature>.Merge merge = placings.merge(2);
        String bfcs_1 = bfcs.bfcs(merge);
        System.out.println(bfcs_1);
        assertEquals("r0$01#", bfcs_1);
    }

    @Test
    @DisplayName("Merge_n with arbitrary n (elementary placing)")
    void merge_test_02() {
        Placings<DefaultDynamicSignature>.Merge merge3 = placings.merge(3);
        String bfcs_3 = bfcs.bfcs(merge3);
        System.out.println(bfcs_3);
        assertEquals("r0$012#", bfcs_3);

        Placings<DefaultDynamicSignature>.Merge merge4 = placings.merge(4);
        String bfcs_4 = bfcs.bfcs(merge4);
        System.out.println(bfcs_4);
        assertEquals("r0$0123#", bfcs_4);

        Placings<DefaultDynamicSignature>.Merge merge5 = placings.merge(5);
        String bfcs_5 = bfcs.bfcs(merge5);
        System.out.println(bfcs_5);
        assertEquals("r0$01234#", bfcs_5);
    }

    @Test
    @DisplayName("Merge_0 = 1 (elementary placing)")
    void merge_test_03() {
        Placings<DefaultDynamicSignature>.Merge merge0 = placings.merge(0);
        String bfcs_0 = bfcs.bfcs(merge0);
        System.out.println(bfcs_0);
        assertEquals("r0#", bfcs_0);

        Placings<DefaultDynamicSignature>.Barren barren = placings.barren();

        String bfcs_1 = bfcs.bfcs(barren);
        System.out.println(bfcs_1);
        assertEquals("r0#", bfcs_1);

        assertEquals(bfcs_0, bfcs_1);
    }

    @Test
    @DisplayName("Merge_2 = join (elementary placing)")
    void merge_test_04() {
        Placings<DefaultDynamicSignature>.Merge merge0 = placings.merge(2);
        String bfcs_0 = bfcs.bfcs(merge0);
        System.out.println(bfcs_0);
        assertEquals("r0$01#", bfcs_0);

        Placings<DefaultDynamicSignature>.Join join = placings.join();

        String bfcs_1 = bfcs.bfcs(join);
        System.out.println(bfcs_1);
        assertEquals("r0$01#", bfcs_1);

        assertEquals(bfcs_0, bfcs_1);
    }

    @Test
    void permutation_and_symmetry_test_01() {
        Placings<DefaultDynamicSignature>.Permutation perm4 = placings.permutation(4);
        String bfcsA = CanonicalElementaryBigraphsUnitTests.bfcs.bfcs(perm4);
        System.out.println(bfcsA);
        assertEquals("r0$0#r1$1#r2$2#r3$3#", bfcsA);

        Placings<DefaultDynamicSignature>.Symmetry symmetry1 = placings.symmetry(4);
        String bfcsSym1 = CanonicalElementaryBigraphsUnitTests.bfcs.bfcs(symmetry1);
        System.out.println(bfcsSym1);
        assertEquals("r0$3#r1$2#r2$1#r3$0#", bfcsSym1);

        Placings<DefaultDynamicSignature>.Symmetry symmetry = placings.symmetry11();
        String bfcsSym = CanonicalElementaryBigraphsUnitTests.bfcs.bfcs(symmetry);
        System.out.println(bfcsSym);
        assertEquals("r0$1#r1$0#", bfcsSym);

    }

    @Test
    @DisplayName("Single closure (elementary linking)")
    void closure_test_01() {
        Linkings<DefaultDynamicSignature>.Closure network = linkings.closure(StringTypedName.of("network"));
        String bfcs1 = bfcs.bfcs(network);
        System.out.println(bfcs1);
        assertEquals("network#", bfcs1);

        Linkings<DefaultDynamicSignature>.Closure x1 = linkings.closure(StringTypedName.of("x1"));
        String bfcs2 = bfcs.bfcs(x1);
        System.out.println(bfcs2);
        assertEquals("x1#", bfcs2);
    }

    @Test
    void closure_test_02() {
        Linkings<DefaultDynamicSignature>.Closure x1234 = linkings.closure(Sets.fixedSize.with(StringTypedName.of("x1"), StringTypedName.of("x2"), StringTypedName.of("x3"), StringTypedName.of("x4")));
        String bfcs1234 = bfcs.bfcs(x1234);
        System.out.println(bfcs1234);
        assertEquals("x1$x2$x3$x4#", bfcs1234);
    }

    @Test
    void substitution_test_01() {
        Linkings<DefaultDynamicSignature>.Substitution substitution = linkings.substitution(StringTypedName.of("y"), Lists.fixedSize.with(StringTypedName.of("x1"), StringTypedName.of("x2"), StringTypedName.of("x3"), StringTypedName.of("x4")));
        String bfcs1 = bfcs.bfcs(substitution);
        System.out.println(bfcs1);
        assertEquals("x1y$x2y$x3y$x4y#", bfcs1);
    }

    @Test
    @DisplayName("Identity link graph")
    void identity_linkgraph_test_01() {
        Linkings<DefaultDynamicSignature>.Identity identity0 = linkings.identity(StringTypedName.of("xbcd"));
        String b0 = bfcs.bfcs(identity0);
        System.out.println(b0);
        assertEquals("xbcdxbcd#", b0);

        Linkings<DefaultDynamicSignature>.Identity identity = linkings.identity(StringTypedName.of("x1"), StringTypedName.of("x2"), StringTypedName.of("x3"), StringTypedName.of("x4"));
        String b1 = bfcs.bfcs(identity);
        System.out.println(b1);
        assertEquals("x1x1$x2x2$x3x3$x4x4#", b1);
    }

    @Test
    void renaming_test_01() {

    }
}
