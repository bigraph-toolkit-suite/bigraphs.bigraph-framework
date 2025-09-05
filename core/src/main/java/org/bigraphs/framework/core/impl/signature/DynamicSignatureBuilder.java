package org.bigraphs.framework.core.impl.signature;

import org.bigraphs.framework.core.AbstractEcoreSignature;
import org.bigraphs.framework.core.Control;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.SignatureBuilder;
import org.bigraphs.framework.core.datatypes.EMetaModelData;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.factory.BigraphFactory;

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

    public DynamicSignatureBuilder add(String name, int arity) {
        DynamicControlBuilder builder = createControlBuilder();
        builder.withControlListBuilder(this);
        return builder.identifier(StringTypedName.of(name)).arity(FiniteOrdinal.ofInteger(arity)).assign();
    }

    public DynamicSignatureBuilder add(String name, int arity, ControlStatus status) {
        DynamicControlBuilder builder = createControlBuilder();
        builder.withControlListBuilder(this);
        return builder.identifier(StringTypedName.of(name)).arity(FiniteOrdinal.ofInteger(arity)).status(status).assign();
    }

    /**
     * Creates a signature with the given controls.
     * <b>Note:</b> All previously assigned controls will be discarded.
     *
     * @param controls the controls to use for the signature
     * @return a signature with the given controls
     */
    @Override
    public DynamicSignature createWith(Iterable<? extends Control<StringTypedName, FiniteOrdinal<Integer>>> controls) {
        DynamicSignature sig = new DynamicSignature((Set<DynamicControl>) controls);
        BigraphFactory.createOrGetSignatureMetaModel((AbstractEcoreSignature<?>) sig);
        return sig;
    }

    @Override
    public DynamicSignature createEmpty() {
        return (DynamicSignature) super.createEmpty();
    }

    /**
     * Creates an empty signature without controls.
     *
     * @return an empty signature of type {@link DynamicSignature}
     */
    @Override
    protected DynamicSignature createEmptyStub() {
        return new DynamicSignature(Collections.EMPTY_SET);
    }

    @Override
    public DynamicSignature create() {
        return (DynamicSignature) super.create();
    }

    @Override
    public DynamicSignature create(EMetaModelData metaModelData) {
        return (DynamicSignature) super.create(metaModelData);
    }

    //    @Override
//    protected <S extends Signature> Class<S> getSignatureClass() {
//        return (Class<S>) DefaultDynamicSignature.class;
//    }

}
