package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.impl.elementary.Placings;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ElementaryBigraphTests {

    @Test
    void barren() {
        Placings.Barren b = Placings.barren();
        assertTrue(b.isPrime());
        assertEquals(1, b.getRoots().size());
        assertEquals(0, b.getSites().size());
        assertEquals(0, b.getOuterNames().size());
        assertEquals(0, b.getInnerNames().size());

        Placings.Barren b2 = Placings.barren();
        assertNotEquals(b, b2);

        assertTrue(b.isPrime());
        assertTrue(b2.isPrime());
    }

    @Test
    void join() {
        Placings.Join join1 = Placings.join();
        Placings.Join join2 = Placings.join();

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
        Placings.Merge merge = Placings.merge(3);
        assertEquals(1, merge.getRoots().size());
        assertEquals(3, merge.getSites().size());
    }
}
