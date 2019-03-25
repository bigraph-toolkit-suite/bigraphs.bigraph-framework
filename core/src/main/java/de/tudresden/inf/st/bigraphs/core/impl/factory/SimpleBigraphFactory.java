package de.tudresden.inf.st.bigraphs.core.impl.factory;

import de.tudresden.inf.st.bigraphs.core.BigraphCompositor;
import de.tudresden.inf.st.bigraphs.core.BigraphOperations;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.SignatureBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SimpleBigraphFactory extends AbstractBigraphFactory {

//<DynamicSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>>>
//    @Override
//    public DynamicSignatureBuilder createSignatureBuilder() {
//        return new DynamicSignatureBuilder<>();
//    }

    @Override
    public DynamicSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>> createSignatureBuilder() {
        return new DynamicSignatureBuilder<StringTypedName, FiniteOrdinal<Integer>>();
    }

    @Override
    public BigraphBuilder<DefaultDynamicControl<StringTypedName, FiniteOrdinal<Integer>>> createBigraphBuilder(@NonNull Signature signature) {
        return BigraphBuilder.start(signature);
    }

    @Override
    public BigraphOperations createBigraphOperations() {
        BigraphCompositor<DefaultDynamicSignature> defaultDynamicSignatureBigraphCompositor = new BigraphCompositor<>();
        defaultDynamicSignatureBigraphCompositor.setBuilder(new BigraphBuilder());
        return defaultDynamicSignatureBigraphCompositor;
    }
}
