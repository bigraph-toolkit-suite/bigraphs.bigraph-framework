package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.factory.SimpleBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Linkings;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Placings;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

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
