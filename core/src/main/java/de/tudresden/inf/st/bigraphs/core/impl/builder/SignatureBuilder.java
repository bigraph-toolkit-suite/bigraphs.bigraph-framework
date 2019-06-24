package de.tudresden.inf.st.bigraphs.core.impl.builder;

import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.Signature;

import java.util.LinkedHashSet;
import java.util.Set;


/**
 * Brdige pattern: abstraction is signature and implementor is controls or rather a strategy here?
 *
 * @param <NT>
 * @param <FO>
 * @param <C>
 * @param <B>
 */
public abstract class SignatureBuilder<NT extends NamedType, FO extends FiniteOrdinal, C extends ControlBuilder<NT, FO, C>, B extends SignatureBuilder> { //<C extends ControlBuilder, B extends SignatureBuilder<C, B>> {
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
    abstract C createControlBuilder();

    private static <C extends ControlBuilder> C createControlBuilder(Class<C> clazz) {
        try {
            return clazz.newInstance();
//            new Class<C>. clazz;
//            String className = ((ParameterizedType) SignatureBuilder.class.getGenericSuperclass()).getActualTypeArguments()[0].getTypeName();
//            Class<?> clazz = Class.forName(className);
//            return (C) clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Class is not parametrized with generic type!!! Please use extends <> ");
        }
    }

    public C newControl() {
        C builder = createControlBuilder();
        builder.withControlListBuilder(this);
        return builder;
    }

    public B addControl(Control<NT, FO> control) {
        controls.add(control);
        return self();
    }

    public abstract <S extends Signature> S createSignature(Iterable<? extends Control> controls); //, Class<S> type);

    /**
     * Creates an empty signature, meaning that the control set is empty.<br/>
     * Needed for the interaction of elementary bigraphs and user-defined bigraphs.
     *
     * @param <S>
     * @return an empty signature of type {@code S}.
     */
    public abstract <S extends Signature> S createSignature();

//    protected  abstract <S extends Signature> Class<S> getSignatureClass();

    @SuppressWarnings("unchecked")
    final B self() {
        return (B) this;
    }

    public Set<Control<NT, FO>> getControls() {
        return this.controls;
    }

    public <CT extends Control<NT, FO>, S extends Signature<CT>> S create() {
        return createSignature(getControls()); //, getSignatureClass());
    }
}