package de.tudresden.inf.st.bigraphs.core.impl.builder;

import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.Signature;

import java.util.ArrayList;
import java.util.Collection;

//TODO: überprüfen, ob controls nicht doppelt vorkommen

public abstract class SignatureBuilder<NT extends NamedType, FO extends FiniteOrdinal, C extends ControlBuilder, B extends SignatureBuilder> { //<C extends ControlBuilder, B extends SignatureBuilder<C, B>> {
    private Collection<Control<NT, FO>> controls;

    public SignatureBuilder() {
        this.controls = new ArrayList<>();
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
    public abstract C createControlBuilder();

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

//    public C newControl() {
//        C builder = createControlBuilder(); //createControlBuilder(getGenericTypeClass());//createControlBuilder(); //getGenericTypeClass());
//        builder.withControlListBuilder(this);
////                this.controls.add(control);
//        return builder;
//    }

    public C newControl() {
        C builder = createControlBuilder(); //createControlBuilder(getGenericTypeClass());//createControlBuilder(); //getGenericTypeClass());
        builder.withControlListBuilder(this);
//                this.controls.add(control);
        return builder;
    }

    public B addControl(Control<NT, FO> control) {
        controls.add(control);
        return self();
    }

//    public B begin() {
//        this.controls = new ArrayList<>();
//        return self();
//    }

    public abstract <S extends Signature> S createSignature(Iterable<? extends Control> controls); //, Class<S> type);

//    protected  abstract <S extends Signature> Class<S> getSignatureClass();

    @SuppressWarnings("unchecked")
    final B self() {
        return (B) this;
    }

    public Iterable<Control<NT, FO>> getControls() {
        return this.controls;
    }

    public <C extends Control<NT, FO>, S extends Signature<C>> S create() {
        return createSignature(getControls()); //, getSignatureClass());
    }
}