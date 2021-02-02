package de.tudresden.inf.st.bigraphs.core.impl.builder;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Abstract base builder class for all types of signatures.
 *
 * @param <NT> type of the name representation
 * @param <FO> type of the finite ordinal representation
 * @param <C>  type of the control builder
 * @param <B>  type of the signature builder
 * @author Dominik Grzelak
 */
public abstract class SignatureBuilder<NT extends NamedType<?>,
        FO extends FiniteOrdinal<?>,
        C extends ControlBuilder<NT, FO, C>,
        B extends SignatureBuilder<?, ?, ?, ?>> {

    private Set<Control<NT, FO>> controls;

    public SignatureBuilder() {
        this.controls = new LinkedHashSet<>();
    }

//    @SuppressWarnings("unchecked")
//    private Class<C> getGenericTypeClass() {
//        try {
//            String className = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName();
//            Class<?> clazz = Class.forName(className);
//            return (Class<C>) clazz;
//        } catch (Exception e) {
//            throw new IllegalStateException("Class is not parametrized with generic type!!! Please use extends <> ");
//        }
//    }

    /**
     * Hook method to be implemented by subclasses for creating the corresponding control builder (i.e., only active or dynamic controls).
     *
     * @return the control builder
     */
    protected abstract C createControlBuilder();

    public C newControl() {
        C builder = createControlBuilder();
        builder.withControlListBuilder(this);
        return builder;
    }

    public C newControl(NT type, FO arity) {
        C builder = createControlBuilder();
        builder.withControlListBuilder(this);
        return builder.identifier(type).arity(arity);
    }

    public B addControl(Control<NT, FO> control) {
        controls.add(control);
        return self();
    }

    /**
     * Create a signature with the given controls.
     *
     * @param controls the controls to use for the signature
     * @return a signature with the given controls
     */
    public abstract Signature<?> createWith(Iterable<? extends Control<NT, FO>> controls);

    /**
     * Create the signature with the assigned controls so far.
     *
     * @return a signature
     */
    public Signature<?> create() {
        Signature<?> sig = createWith(getControls());
        if (sig instanceof AbstractEcoreSignature)
            BigraphFactory.createOrGetSignatureMetaModel((AbstractEcoreSignature<?>) sig);
        return sig;
    }

    /**
     * Creates an empty signature, meaning that the control set is empty.<br>
     * Needed for the interaction of elementary bigraphs and user-defined bigraphs.
     *
     * @return an empty signature of type {@literal <S>}.
     */
    public Signature<? extends Control<NT, FO>> createEmpty() {
        Signature<? extends Control<NT, FO>> sig = (Signature<? extends Control<NT, FO>>) createEmptyStub();
        if (sig instanceof AbstractEcoreSignature)
            BigraphFactory.createOrGetSignatureMetaModel((AbstractEcoreSignature<?>) sig);
        return sig;
    }

    /**
     * This method is not called by the user; it is called by {@link SignatureBuilder#createEmpty()}.
     */
    protected abstract Signature<? extends Control<NT, FO>> createEmptyStub();

    //    protected  abstract <S extends Signature> Class<S> getSignatureClass();
    @SuppressWarnings("unchecked")
    final B self() {
        return (B) this;
    }

    public Set<Control<NT, FO>> getControls() {
        return this.controls;
    }

}