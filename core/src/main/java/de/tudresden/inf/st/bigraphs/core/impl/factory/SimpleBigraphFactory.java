package de.tudresden.inf.st.bigraphs.core.impl.factory;

import com.google.common.reflect.TypeToken;
import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.BigraphComposite;
import de.tudresden.inf.st.bigraphs.core.DefaultBigraphComposite;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.SignatureBuilder;

public class SimpleBigraphFactory<NT extends NamedType, FT extends FiniteOrdinal>
        extends AbstractBigraphFactory<DefaultDynamicSignature, NT, FT> {

    public SimpleBigraphFactory() {
        super.successorClass = new TypeToken<DefaultDynamicControl<NT, FT>>() {
        }.getType();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <SB extends SignatureBuilder> SB createSignatureBuilder() {
        return (SB) new DynamicSignatureBuilder<NT, FT>();
    }

    @Override
    public BigraphBuilder<DefaultDynamicSignature> createBigraphBuilder(Signature<?> signature) {
//        BigraphBuilder<DefaultDynamicSignature> builder = new BigraphBuilder<DefaultDynamicSignature>().useSignature(signature);
//        BigraphBuilder.start(DefaultDynamicSignature.class.cast(signature));
        return BigraphBuilder.start(DefaultDynamicSignature.class.cast(signature));
    }

    @Override
    public BigraphComposite<DefaultDynamicSignature> asBigraphOperator(Bigraph<DefaultDynamicSignature> outerBigraph) {
        return new DefaultBigraphComposite<>(outerBigraph);
    }

}
