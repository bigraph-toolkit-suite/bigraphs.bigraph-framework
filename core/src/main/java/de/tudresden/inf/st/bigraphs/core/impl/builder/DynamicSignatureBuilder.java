package de.tudresden.inf.st.bigraphs.core.impl.builder;

import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;

import java.util.Collections;
import java.util.Set;

/**
 * The default signature with "dynamic" controls, meaning that controls can be active, passive or atomic.
 *
 * @author Dominik Grzelak
 * @see DynamicControlBuilder
 */
public class DynamicSignatureBuilder
        extends SignatureBuilder<StringTypedName, FiniteOrdinal<Integer>, DynamicControlBuilder, DynamicSignatureBuilder> {

    @Override
    protected DynamicControlBuilder createControlBuilder() {
        return new DynamicControlBuilder();
    }

    public DynamicControlBuilder newControl(String name, int arity) {
        DynamicControlBuilder builder = createControlBuilder();
        builder.withControlListBuilder(this);
        return builder.identifier(StringTypedName.of(name)).arity(FiniteOrdinal.ofInteger(arity));
    }

    public DynamicSignatureBuilder addControl(String name, int arity) {
        DynamicControlBuilder builder = createControlBuilder();
        builder.withControlListBuilder(this);
        return builder.identifier(StringTypedName.of(name)).arity(FiniteOrdinal.ofInteger(arity)).assign();
    }

    /**
     * Creates a signature with the given controls.
     * <b>Note:</b> All previously assigned controls will be discarded.
     *
     * @param controls the controls to use for the signature
     * @return a signature with the given controls
     */
    @Override
    public DefaultDynamicSignature createWith(Iterable<? extends Control<StringTypedName, FiniteOrdinal<Integer>>> controls) {
        return new DefaultDynamicSignature((Set<DefaultDynamicControl>) controls);
    }

    /**
     * Creates an empty signature without controls.
     *
     * @return an empty signature of type {@link DefaultDynamicSignature}
     */
    @Override
    public DefaultDynamicSignature createEmpty() {
        return new DefaultDynamicSignature(Collections.emptySet());
    }

    @Override
    public DefaultDynamicSignature create() {
        return (DefaultDynamicSignature) super.create();
    }

    //    @Override
//    protected <S extends Signature> Class<S> getSignatureClass() {
//        return (Class<S>) DefaultDynamicSignature.class;
//    }

}
