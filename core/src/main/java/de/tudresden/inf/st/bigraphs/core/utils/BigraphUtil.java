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
}
