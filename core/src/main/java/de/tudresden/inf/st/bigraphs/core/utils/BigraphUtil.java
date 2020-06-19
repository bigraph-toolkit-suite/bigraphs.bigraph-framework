package de.tudresden.inf.st.bigraphs.core.utils;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.impl.EcoreBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;

/**
 * @author Dominik Grzelak
 */
public class BigraphUtil {

    public static EcoreBigraph copy(EcoreBigraph bigraph) throws CloneNotSupportedException {
        EcoreBigraph.Stub clone = new EcoreBigraph.Stub(bigraph).clone();
        return PureBigraphBuilder.create(((Bigraph<?>) bigraph).getSignature(), clone.getModelPackage(), clone.getModel()).createBigraph();
    }

    /**
     * This doesn't check if DiscreteIon/etc., only for simple elementary bigraphs such as a merge.
     *
     * @param bigraph the bigraph to check
     * @return {@code true}, if bigraph is an elementary one (except for Discrete Ion).
     */
    public static boolean isBigraphElementary(Bigraph<?> bigraph) {
        boolean isPlacing = isBigraphElementaryPlacing(bigraph);
        boolean isLinking = isBigraphElementaryLinking(bigraph);
        return isLinking || isPlacing;
    }

    public static boolean isBigraphElementaryLinking(Bigraph<?> bigraph) {
        return bigraph.getEdges().size() == 0 &&
                bigraph.getAllPlaces().size() == 0 &&
                (bigraph.getInnerNames().size() != 0 || bigraph.getOuterNames().size() != 0);
    }

    public static boolean isBigraphElementaryPlacing(Bigraph<?> bigraph) {
        return bigraph.getEdges().size() == 0 &&
                bigraph.getInnerNames().size() == 0 &&
                bigraph.getOuterNames().size() == 0 &&
                bigraph.getNodes().size() == 0 &&
                bigraph.getRoots().size() != 0;
    }
}
