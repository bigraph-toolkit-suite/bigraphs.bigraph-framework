package de.tudresden.inf.st.bigraphs.core.impl.elementary;

import de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants;
import de.tudresden.inf.st.bigraphs.core.ElementaryBigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.MutableBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.SignatureBuilder;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.IntStream;

/**
 * "A placing is a bigraph m → n with no nodes".
 * Three kinds of placing exist:
 * <ul>
 * <li>A barren root: 1</li>
 * <li>Join</li>
 * <li>gamma_{m,n}</li>
 * </ul>
 * <p>
 * By that a special placing called merge_m: m -> 1 can be derived and is implemented here for
 * convenience. merge_0 = 1, merge_1 = id_1, merge_2 = join, hence, merge_{m+1} = join o (id_1 + merge_m).
 */
public class Placings<S extends Signature> implements Serializable {
    private volatile S emptySignature;
    private volatile MutableBuilder<S> mutableBuilder;
    private EPackage loadedModelPacakge;

    public Placings(AbstractBigraphFactory factory) {
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
    public Placings(SignatureBuilder signatureBuilder) {
//        AbstractBigraphFactory factory = new SimpleBigraphFactory<>();
//        SignatureBuilder signatureBuilder = factory.createSignatureBuilder();
        emptySignature = (S) signatureBuilder.createSignature();
        mutableBuilder = BigraphBuilder.newMutableBuilder(emptySignature);
        loadedModelPacakge = mutableBuilder.getLoadedEPackage();
    }

    public Placings<S>.Barren barren() {
        return new Barren();
    }

    public Placings<S>.Merge merge(int m) {
        return new Merge(m);
    }

    public Placings<S>.Join join() {
        return new Join();
    }

    public class Barren extends ElementaryBigraph {
        private final BigraphEntity.RootEntity root;

        Barren() {
            root = (BigraphEntity.RootEntity) mutableBuilder.createNewRoot(0);
        }

        @Override
        public S getSignature() {
            return emptySignature;
        }

        @Override
        public final Collection<BigraphEntity.RootEntity> getRoots() {
            return Collections.singletonList(root);
        }

        /**
         * Returns always {@code null} since a barren cannot have any child.
         *
         * @param node is not evaluated
         * @return always returns {@code null}
         */
        @Override
        public final BigraphEntity getParent(BigraphEntity node) {
            return null;
        }

        /**
         * Returns the single root of this barren.
         *
         * @return the barren's root
         */
        @Override
        public Collection<BigraphEntity> getAllPlaces() {
            return Collections.singletonList(root);
        }

        @Override
        public EPackage getModelPackage() {
            return loadedModelPacakge;
        }
    }

    public class Join extends ElementaryBigraph<S> {
        private final BigraphEntity.RootEntity root;
        private final Collection<BigraphEntity.SiteEntity> sites = new ArrayList<>(2);

        Join() {
            root = (BigraphEntity.RootEntity) mutableBuilder.createNewRoot(0);
            sites.add((BigraphEntity.SiteEntity) mutableBuilder.createNewSite(0));
            sites.add((BigraphEntity.SiteEntity) mutableBuilder.createNewSite(1));
            sites.forEach(siteEntity -> setParentOfNode(siteEntity, root));
        }

        @Override
        public S getSignature() {
            return emptySignature;
        }

        @Override
        public final Collection<BigraphEntity.RootEntity> getRoots() {
            return Collections.singletonList(root);
        }

        @Override
        public final Collection<BigraphEntity.SiteEntity> getSites() {
            return sites;
        }

        /**
         * Returns the single root and the two sites of this join.
         *
         * @return the join's root and two children
         */
        @Override
        public Collection<BigraphEntity> getAllPlaces() {
            Collection<BigraphEntity> list = new ArrayList<>();
            list.add(root);
            list.addAll(sites);
            return list;
        }

        @Override
        public EPackage getModelPackage() {
            return loadedModelPacakge;
        }
    }


    /**
     * A merge maps m sites to a single root where m > 0. Otherwise merge_0 = 1
     */
    public class Merge extends ElementaryBigraph<S> {
        private final BigraphEntity.RootEntity root;
        private final Collection<BigraphEntity.SiteEntity> sites;

        Merge(final int m) {
            sites = new ArrayList<>(m);
            root = (BigraphEntity.RootEntity) mutableBuilder.createNewRoot(0);
            IntStream.range(0, m).forEach(value -> sites.add((BigraphEntity.SiteEntity) mutableBuilder.createNewSite(value)));
            sites.forEach(siteEntity -> setParentOfNode(siteEntity, root));
        }

        @Override
        public S getSignature() {
            return emptySignature;
        }

        @Override
        public final Collection<BigraphEntity.RootEntity> getRoots() {
            return Collections.singletonList(root);
        }

        @Override
        public final Collection<BigraphEntity.SiteEntity> getSites() {
            return sites;
        }

        /**
         * Returns the single root and the {@code n} sites of this merge.
         *
         * @return the merge's root and {@code n} sites
         */
        @Override
        public Collection<BigraphEntity> getAllPlaces() {
            Collection<BigraphEntity> list = new ArrayList<>();
            list.add(root);
            list.addAll(sites);
            return list;
        }

        @Override
        public EPackage getModelPackage() {
            return loadedModelPacakge;
        }
    }

    private void setParentOfNode(final BigraphEntity node, final BigraphEntity parent) {
        EStructuralFeature prntRef = node.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        node.getInstance().eSet(prntRef, parent.getInstance()); // child is automatically added to the parent according to the ecore model
    }

}
