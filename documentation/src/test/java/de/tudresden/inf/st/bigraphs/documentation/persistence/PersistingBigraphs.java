package de.tudresden.inf.st.bigraphs.documentation.persistence;

import de.tudresden.inf.st.bigraphs.core.BigraphFileModelManagement;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.EMetaModelData;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*;

public class PersistingBigraphs {

    public void code_sample_one() throws IOException {
        DefaultDynamicSignature signature = pureSignatureBuilder()
                .newControl().identifier(StringTypedName.of("Building")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("Laptop")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("Printer")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .create();

        createOrGetBigraphMetaModel(signature, EMetaModelData.builder()
                .setName("myMetaModel")
                .setNsPrefix("example")
                .setNsUri("http://example.org")
                .create());
        PureBigraphBuilder<DefaultDynamicSignature> bigraphBuilder = pureBuilder(
                signature
        );
        // do something with the bigraph builder ...
        PureBigraph bigraph = bigraphBuilder.createBigraph();

        // Export the metamodel
        BigraphFileModelManagement.Store.exportAsMetaModel(bigraph, new FileOutputStream(new File("meta-model.ecore")));
    }
}
