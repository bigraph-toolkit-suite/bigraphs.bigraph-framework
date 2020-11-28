package de.tudresden.inf.st.bigraphs.simulation.canonicalstring;

import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Linkings;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Placings;
import de.tudresden.inf.st.bigraphs.simulation.encoding.BigraphCanonicalForm;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pure;
import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pureSignatureBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Dominik Grzelak
 */
public class CanonicalElementaryBigraphUnitTests {
    private final static String TARGET_DUMP_PATH = "src/test/resources/dump/canonicform/elementary/";

    static PureBigraphFactory factory = pure();
    static DefaultDynamicSignature EMPTY_SIG = pureSignatureBuilder().createEmpty();
    static BigraphCanonicalForm bfcs;
    static Placings<DefaultDynamicSignature> placings;
    static Linkings<DefaultDynamicSignature> linkings;

    @BeforeAll
    static void setUp() throws IOException {
        File dump = new File(TARGET_DUMP_PATH);
        dump.mkdirs();
        FileUtils.cleanDirectory(new File(TARGET_DUMP_PATH));

        bfcs = BigraphCanonicalForm.createInstance();
        placings = factory.createPlacings(EMPTY_SIG);
        linkings = factory.createLinkings(EMPTY_SIG);
    }

    @Test
    @DisplayName("Barren (elementary placing)")
    void barren_test_01() {
        Placings<DefaultDynamicSignature>.Barren barren = placings.barren();

        String bfcs_1 = bfcs.bfcs(barren);
        System.out.println(bfcs_1);
        assertEquals("r0$0#", bfcs_1);
    }

    @Test
    @DisplayName("Merge_n where n = 2 (elementary placing)")
    void merge_test_01() {

    }

    @Test
    @DisplayName("Merge_n with arbitrary n (elementary placing)")
    void merge_test_02() {

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
    @DisplayName("Single closure (elementary linking)")
    void closure_test_01() {
    }

    @Test
    void closure_test_02() {
    }

    @Test
    void substitution_test_01() {

    }

    @Test
    void renaming_test_01() {

    }
}
