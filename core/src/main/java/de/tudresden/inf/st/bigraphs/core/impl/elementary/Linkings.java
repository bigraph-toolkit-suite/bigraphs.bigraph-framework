package de.tudresden.inf.st.bigraphs.core.impl.elementary;

import de.tudresden.inf.st.bigraphs.core.ElementaryBigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.MutableBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.SignatureBuilder;
import org.eclipse.emf.ecore.EPackage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

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

    public Linkings(AbstractBigraphFactory factory) {
//        AbstractBigraphFactory factory = new SimpleBigraphFactory<>();
        SignatureBuilder signatureBuilder = factory.createSignatureBuilder();
        emptySignature = (S) signatureBuilder.createSignature();
        mutableBuilder = BigraphBuilder.newMutableBuilder(emptySignature);
        loadedModelPacakge = mutableBuilder.getLoadedEPackage();
    }

    /**
     * @param signatureBuilder to create an empty signature of the appropriate type for working with
     *                         user-defined bigraphs of the same type created with the same factory
     */
    public Linkings(SignatureBuilder signatureBuilder) {
//        AbstractBigraphFactory factory = new SimpleBigraphFactory<>();
//        SignatureBuilder signatureBuilder = factory.createSignatureBuilder();
        emptySignature = (S) signatureBuilder.createSignature();
        mutableBuilder = BigraphBuilder.newMutableBuilder(emptySignature);
        loadedModelPacakge = mutableBuilder.getLoadedEPackage();
    }

    public class Closure extends ElementaryBigraph<S> {
        private final BigraphEntity.InnerName x;
        private final Collection<BigraphEntity.InnerName> innerNames = new ArrayList<>(1);

        Closure(NamedType<?> name) {
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
    }

    public class Substitution extends ElementaryBigraph<S> {
        private final Collection<BigraphEntity.OuterName> outerNames = new ArrayList<>(1);
        private final Collection<BigraphEntity.InnerName> innerNames;

        Substitution(NamedType<?> outerName, NamedType<?>... innerNames) {
            this.innerNames = new ArrayList<>(innerNames.length);
            BigraphEntity.OuterName newOuterName = (BigraphEntity.OuterName) mutableBuilder.createNewOuterName(outerName.stringValue());
            outerNames.add(newOuterName);
            for (int i = 0, n = innerNames.length; i < n; i++) {
                BigraphEntity.InnerName newInnerName = (BigraphEntity.InnerName) mutableBuilder.createNewInnerName(innerNames[i].stringValue());
                mutableBuilder.connectInnerToOuter(newInnerName, newOuterName);
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
    }
}
