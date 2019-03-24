package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.exceptions.IncompatibleSignatureException;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.ControlBuilder;

import java.lang.reflect.ParameterizedType;
import java.util.Collections;

//TODO builder instanzieren
public class BigraphCompositor<S extends Signature> implements BigraphOperations<S> {

    public BigraphCompositor() {
    }

    public static <S extends Signature> BigraphCompositor<S> create(Class<S> clazz) {
        try {
            return null; //clazz.newInstance();
//            new Class<C>. clazz;
//            String className = ((ParameterizedType) SignatureBuilder.class.getGenericSuperclass()).getActualTypeArguments()[0].getTypeName();
//            Class<?> clazz = Class.forName(className);
//            return (C) clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Class is not parametrized with generic type!!! Please use extends <> ");
        }
    }

    @Override
    public Bigraph<S> compose(Bigraph<S> left, Bigraph<S> right) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        assertSignaturesAreSame(left.getSignature(), right.getSignature());
        assertInterfaceCompatibleForCompose(left, right);


        return null;
    }

    protected void assertSignaturesAreSame(S signature1, S signature2) throws IncompatibleSignatureException {
        if (!signature1.equals(signature2)) {
            throw new IncompatibleSignatureException();
        }
    }

    protected void assertInterfaceCompatibleForCompose(Bigraph<S> left, Bigraph<S> right) throws IncompatibleInterfaceException {
        //xs: left: innerNames
        //ys: right: outerNames

        //check place graph
        int mLeft = left.getSites().size();
        int mRight = right.getRoots().size();
        int nLeft = left.getInnerNames().size();
        int nRight = right.getOuterNames().size();
        boolean disjoint = Collections.disjoint(left.getInnerNames(), right.getOuterNames());
        if (disjoint || mLeft != mRight) {
            throw new IncompatibleInterfaceException();
        }
    }
}
