package de.tudresden.inf.st.bigraphs.core.impl.factory;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphBuilder;

//TODO extra class which provides functions to createNodeOfEClass a bigraph EMOF model with signature
public class EcoreBigraphFactory extends AbstractBigraphFactory {
    BigraphBuilder builder;

    @Override
    Bigraph createEmptyBigraph(Signature signature) {
//        builder = BigraphBuilder.start(signature);
        //TODO ecore model laden, signature erweitern und epackage in ecorebigraph einsetzen
//        new DynamicEcoreBigraph(epackage);
        return null;
    }

}
