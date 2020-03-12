package de.tudresden.inf.st.bigraphs.converter.bigred;

import de.tudresden.inf.st.bigraphs.core.ControlKind;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import org.bigraph.model.Signature;
import org.bigraph.model.SignatureAdapter;
import org.bigraph.model.SimulationSpec;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.loaders.SignatureXMLLoader;
import org.bigraph.model.savers.SaveFailedException;
import org.bigraph.model.savers.SignatureXMLSaver;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pure;

/**
 * @author Dominik Grzelak
 */
public class BasicBigRedXmlWriteTests {

    @Test
    void write_test() throws SaveFailedException {
        DefaultDynamicSignature signature = createSignature();
        SignatureAdapter signatureAdapter = new SignatureAdapter(signature);

        PrintStream out = new PrintStream(System.out);

        SignatureXMLSaver sx = new SignatureXMLSaver();
        sx.setModel(signatureAdapter)
                .setOutputStream(out);
        sx.exportObject();
    }

    private static DefaultDynamicSignature createSignature() {
        DynamicSignatureBuilder defaultBuilder = pure().createSignatureBuilder();
        defaultBuilder
                .newControl().identifier(StringTypedName.of("Person")).arity(FiniteOrdinal.ofInteger(3)).kind(ControlKind.ATOMIC).assign()
                .newControl().identifier(StringTypedName.of("Room")).arity(FiniteOrdinal.ofInteger(2)).kind(ControlKind.PASSIVE).assign()
                .newControl().identifier(StringTypedName.of("User")).arity(FiniteOrdinal.ofInteger(1)).kind(ControlKind.ACTIVE).assign()
                .newControl().identifier(StringTypedName.of("Computer")).arity(FiniteOrdinal.ofInteger(0)).assign()
        ;

        return defaultBuilder.create();
    }
}
