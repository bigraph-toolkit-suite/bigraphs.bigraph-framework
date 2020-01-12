package de.tudresden.inf.st.bigraphs.core.documentation;

import de.tudresden.inf.st.bigraphs.core.BigraphArtifacts;
import de.tudresden.inf.st.bigraphs.core.BigraphComposite;
import de.tudresden.inf.st.bigraphs.core.ControlKind;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.EMetaModelData;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory;
import de.tudresden.inf.st.bigraphs.core.factory.PureBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Linkings;
import de.tudresden.inf.st.bigraphs.core.impl.elementary.Placings;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import org.junit.jupiter.api.Test;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.*;

/**
 * @author Dominik Grzelak
 */
public class GettingStartedGuide {

    @Test
    void name() throws InvalidConnectionException, IncompatibleSignatureException, IncompatibleInterfaceException {
        PureBigraphFactory pureFactory = pure();

        DefaultDynamicSignature signature = pureFactory.createSignatureBuilder()
                .newControl().identifier("User").arity(1).kind(ControlKind.ATOMIC).assign()
                .newControl(StringTypedName.of("Computer"), FiniteOrdinal.ofInteger(2)).assign()
                .create();

        PureBigraphBuilder<DefaultDynamicSignature> builder = pureFactory.createBigraphBuilder(signature);
        builder.createRoot()
                .addChild("User", "login").addChild("Computer", "login");
        PureBigraph bigraph = builder.createRoot()
                .addChild("User", "login").addChild("Computer", "login")
                .createBigraph();

        Placings<DefaultDynamicSignature> placings = pureFactory.createPlacings(signature);
        Placings<DefaultDynamicSignature>.Merge merge = placings.merge(2);
        Linkings<DefaultDynamicSignature> linkings = pureFactory.createLinkings(signature);
        Linkings<DefaultDynamicSignature>.Identity login = linkings.identity(StringTypedName.of("login"));

        BigraphComposite<DefaultDynamicSignature> composed = ops(merge).parallelProduct(login).compose(bigraph);
    }
}
