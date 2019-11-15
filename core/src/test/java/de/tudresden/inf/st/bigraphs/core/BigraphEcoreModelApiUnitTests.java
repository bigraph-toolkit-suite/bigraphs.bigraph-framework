package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.BBigraph;
import de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.BRoot;
import de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.BigraphBaseModelFactory;
import org.junit.jupiter.api.Test;

/**
 * This test class uses the generated Ecore API of the {@link de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.BigraphBaseModelPackage}
 * to create some bigraphs. This is the direct approach without using the Bigraph Framework without further functionality.
 *
 * @author Dominik Grzelak
 */
public class BigraphEcoreModelApiUnitTests {

    @Test
    void apiestt() {
        BBigraph bBigraph = BigraphBaseModelFactory.eINSTANCE.createBBigraph();
        BRoot bRoot = BigraphBaseModelFactory.eINSTANCE.createBRoot();
        bBigraph.getBRoots().add(bRoot);
        System.out.println(bRoot.getBBigraph().hashCode());
        System.out.println(bBigraph.getBRoots().size());
        BBigraph bBigraph2 = BigraphBaseModelFactory.eINSTANCE.createBBigraph();
        bBigraph2.getBRoots().add(bRoot);

        System.out.println(bRoot.getBBigraph().hashCode());
        System.out.println(bBigraph.getBRoots().size());
        System.out.println(bBigraph2.getBRoots().size());
    }
}
