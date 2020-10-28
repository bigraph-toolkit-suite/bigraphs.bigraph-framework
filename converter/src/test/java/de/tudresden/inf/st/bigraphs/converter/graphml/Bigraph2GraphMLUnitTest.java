package de.tudresden.inf.st.bigraphs.converter.graphml;

import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Dominik Grzelak
 */
public class Bigraph2GraphMLUnitTest {
    private static final String DUMP_TARGET = "src/test/resources/dump/";

    @Test
    void bigraph_2_graphml_test_01() {
        DefaultDynamicSignature signature = createSignature();
        PureBigraph generate = BigraphFactory.pureRandomBuilder(signature).generate(1, 10, 0.5f, 1f, 1f);

        PureBigraph2GraphMLPrettyPrinter graphMLPrettyPrinter = new PureBigraph2GraphMLPrettyPrinter();

        String s = graphMLPrettyPrinter.toString(generate);
        assertNotNull(s);
        assertNotEquals("", s);
        assertAll(() -> {
            graphMLPrettyPrinter.toOutputStream(generate, System.out);
        });
    }

    private static DefaultDynamicSignature createSignature() {
        DynamicSignatureBuilder defaultBuilder = BigraphFactory.pureSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("A")).arity(FiniteOrdinal.ofInteger(4)).assign()
                .newControl().identifier(StringTypedName.of("B")).arity(FiniteOrdinal.ofInteger(3)).assign()
                .newControl().identifier(StringTypedName.of("C")).arity(FiniteOrdinal.ofInteger(2)).assign()
                .newControl().identifier(StringTypedName.of("D")).arity(FiniteOrdinal.ofInteger(1)).assign()
                .newControl().identifier(StringTypedName.of("E")).arity(FiniteOrdinal.ofInteger(0)).assign()
        ;
        return defaultBuilder.create();
    }
}
