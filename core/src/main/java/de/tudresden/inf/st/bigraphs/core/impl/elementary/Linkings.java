package de.tudresden.inf.st.bigraphs.core.impl.elementary;

import de.tudresden.inf.st.bigraphs.core.ElementaryBigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.MutableBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.SignatureBuilder;
import org.eclipse.emf.ecore.EPackage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @param <S>
 * @author Dominik Grzelak
 */
public class Linkings<S extends Signature> implements Serializable {

    private volatile S emptySignature;
    private volatile MutableBuilder<S> mutableBuilder;
    private EPackage loadedModelPacakge;

    public Linkings<S>.Closure closure(NamedType<?> name) {
        return new Closure(name);
    }

    public Linkings<S>.Substitution substitution(NamedType<?> nameOuter, NamedType<?>... nameInner) {
        return new Substitution(nameOuter, nameInner);
    }

    public Linkings<S>.Identity identity(NamedType<?> name) {
        return new Identity(name);
    }

    /**
     * Creates an identity link graph from a given name set. Is also a renaming - a bijective substitution.
     *
     * @param nameSet name setl
     * @return the identity link graph created from the given name set
     */
    public Linkings<S>.Identity identity(NamedType<?>... nameSet) {
        return new Identity(nameSet);
    }

    /**
     * Empty identity link graph
     *
     * @return
     */
    public Linkings<S>.IdentityEmpty identity_e() {
        return new IdentityEmpty();
    }

    public Linkings(AbstractBigraphFactory factory) {
//        AbstractBigraphFactory factory = new PureBigraphFactory<>();
        SignatureBuilder signatureBuilder = factory.createSignatureBuilder();
        emptySignature = (S) signatureBuilder.createSignature();
        mutableBuilder = PureBigraphBuilder.newMutableBuilder(emptySignature);
        loadedModelPacakge = mutableBuilder.getLoadedEPackage();
    }

    /**
     * @param signatureBuilder to create an empty signature of the appropriate type for working with
     *                         user-defined bigraphs of the same type created with the same factory
     */
    public Linkings(SignatureBuilder signatureBuilder) {
//        AbstractBigraphFactory factory = new PureBigraphFactory<>();
//        SignatureBuilder signatureBuilder = factory.createSignatureBuilder();
        emptySignature = (S) signatureBuilder.createSignature();
        mutableBuilder = PureBigraphBuilder.newMutableBuilder(emptySignature);
        loadedModelPacakge = mutableBuilder.getLoadedEPackage();
    }

    public class IdentityEmpty extends ElementaryBigraph<S> {

        public IdentityEmpty() {
            super(null);
        }

        @Override
        public S getSignature() {
            return emptySignature;
        }

        @Override
        public EPackage getModelPackage() {
            return loadedModelPacakge;
        }
    }

    public class Identity extends Substitution {

        Identity(NamedType<?>... nameSet) {
            super(nameSet);
        }

        @Override
        public S getSignature() {
            return emptySignature;
        }

        @Override
        public EPackage getModelPackage() {
            return loadedModelPacakge;
        }

        @Override
        public Collection<BigraphEntity.InnerName> getSiblingsOfInnerName(BigraphEntity.InnerName innerName) {
            return Collections.emptyList();
        }
    }

    public class Closure extends ElementaryBigraph<S> {
        private final BigraphEntity.InnerName x;
        private final Collection<BigraphEntity.InnerName> innerNames = new ArrayList<>(1);

        Closure(NamedType<?> name) {
            super(null);
            x = (BigraphEntity.InnerName) mutableBuilder.createNewInnerName(name.stringValue());
            innerNames.add(x);
        }

        @Override
        public Collection<BigraphEntity.InnerName> getInnerNames() {
            return innerNames;
        }

        @Override
        public S getSignature() {
            return emptySignature;
        }

        @Override
        public EPackage getModelPackage() {
            return loadedModelPacakge;
        }

        /**
         * Always returns an empty list because a linking has no nodes.
         *
         * @param node argument is not considered
         * @return an empty list
         */
        @Override
        public Collection<BigraphEntity> getSiblingsOfNode(BigraphEntity node) {
            return Collections.EMPTY_LIST;
        }

    }

    public class Substitution extends ElementaryBigraph<S> {
        private final Collection<BigraphEntity.OuterName> outerNames;
        private final Collection<BigraphEntity.InnerName> innerNames;

        Substitution(NamedType<?> outerName, NamedType<?>... innerNames) {
            super(null);
            if (Objects.isNull(outerName) || innerNames.length == 0) {
                throw new RuntimeException("Substitution cannot be created because outer name or inner name is missing.");
            }
            this.innerNames = new ArrayList<>(innerNames.length);
            this.outerNames = new ArrayList<>(1);
            BigraphEntity.OuterName newOuterName = (BigraphEntity.OuterName) mutableBuilder.createNewOuterName(outerName.stringValue());
            outerNames.add(newOuterName);
            for (int i = 0, n = innerNames.length; i < n; i++) {
                BigraphEntity.InnerName newInnerName = (BigraphEntity.InnerName) mutableBuilder.createNewInnerName(innerNames[i].stringValue());
                mutableBuilder.connectInnerToOuter(newInnerName, newOuterName);
                this.innerNames.add(newInnerName);
            }
        }

        Substitution(final NamedType<?>... names) {
            super(null);
            if (names.length == 0) {
                throw new RuntimeException("Renaming (Substitution) cannot be created because names are missing.");
            }
            this.innerNames = new ArrayList<>(names.length);
            this.outerNames = new ArrayList<>(names.length);
            for (int i = 0, n = names.length; i < n; i++) {
                BigraphEntity.OuterName newOuterName = (BigraphEntity.OuterName) mutableBuilder.createNewOuterName(names[i].stringValue());
                BigraphEntity.InnerName newInnerName = (BigraphEntity.InnerName) mutableBuilder.createNewInnerName(names[i].stringValue());
                mutableBuilder.connectInnerToOuter(newInnerName, newOuterName);
                this.outerNames.add(newOuterName);
                this.innerNames.add(newInnerName);
            }
        }

        @Override
        public Collection<BigraphEntity.InnerName> getInnerNames() {
            return innerNames;
        }

        @Override
        public Collection<BigraphEntity.OuterName> getOuterNames() {
            return outerNames;
        }

        @Override
        public S getSignature() {
            return emptySignature;
        }

        @Override
        public EPackage getModelPackage() {
            return loadedModelPacakge;
        }

        /**
         * Always returns an empty list because a linking has no nodes.
         *
         * @param node argument is not considered
         * @return an empty list
         */
        @Override
        public Collection<BigraphEntity> getSiblingsOfNode(BigraphEntity node) {
            return Collections.EMPTY_LIST;
        }

        @Override
        public Collection<BigraphEntity.InnerName> getSiblingsOfInnerName(BigraphEntity.InnerName innerName) {
            return this.innerNames.stream().filter(x -> !x.equals(innerName)).collect(Collectors.toList());
        }
    }
}
