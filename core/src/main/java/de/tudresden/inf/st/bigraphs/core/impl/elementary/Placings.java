package de.tudresden.inf.st.bigraphs.core.impl.elementary;

import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants;
import de.tudresden.inf.st.bigraphs.core.ElementaryBigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.MutableBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.SignatureBuilder;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
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

    @Deprecated
    public Placings(AbstractBigraphFactory factory) {
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
    public Placings(SignatureBuilder signatureBuilder) {
        emptySignature = (S) signatureBuilder.createSignature();
        mutableBuilder = PureBigraphBuilder.newMutableBuilder(emptySignature);
        loadedModelPacakge = mutableBuilder.getLoadedEPackage();
    }

    public Placings(S signature) {
        emptySignature = signature;
        mutableBuilder = PureBigraphBuilder.newMutableBuilder(emptySignature);
        loadedModelPacakge = mutableBuilder.getLoadedEPackage();
    }

    public Placings<S>.Barren barren() {
        return new Barren();
    }

    public Placings<S>.Identity1 identity1() {
        return new Identity1();
    }

    public Symmetry symmetry11() {
        return new Symmetry(2);
    }

    public Symmetry symmetry(int n) {
        return new Symmetry(n);
    }

    public Placings<S>.Merge merge(int m) {
        return new Merge(m);
    }

    public Placings<S>.Join join() {
        return new Join();
    }

    /**
     * Create an "equally distributed permutation", similiar to an identity place graph.
     * Each site will be mapped exactly to one root where the indices match.
     *
     * @param n number of sites respectively roots mapped to each other n_i -> n_i, i in 1..n
     * @return an "equally distributed permutation"
     */
    public Placings<S>.Permutation permutation(int n) {
        return new Permutation(n);
    }

    public class Barren extends ElementaryBigraph<S> {
        private final BigraphEntity.RootEntity root;

        Barren() {
            super(null);
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
        public Collection<BigraphEntity> getSiblingsOfNode(BigraphEntity node) {
            return Collections.EMPTY_LIST;
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
            super(null);
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
        public Collection<BigraphEntity> getSiblingsOfNode(BigraphEntity node) {
            if (BigraphEntityType.isSite(node) && sites.contains((BigraphEntity.SiteEntity) node)) {
                return sites.stream().filter(x -> !x.equals(node)).collect(Collectors.toList());
            }
            return Collections.emptyList();
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
            super(null);
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
        public Collection<BigraphEntity> getSiblingsOfNode(BigraphEntity node) {
            if (BigraphEntityType.isSite(node) && sites.contains((BigraphEntity.SiteEntity) node)) {
                return sites.stream().filter(x -> !x.equals(node)).collect(Collectors.toList());
            }
            return Collections.emptyList();
        }

        @Override
        public EPackage getModelPackage() {
            return loadedModelPacakge;
        }
    }

    /**
     * Base class of a bijective placing.
     * <p>
     * This class will also create an identity place graph <i>id_n</i> ("equally distributed").
     * Each site will be mapped to one root where the indices are the same.
     */
    public class Permutation extends ElementaryBigraph<S> {
        protected final Collection<BigraphEntity.RootEntity> roots;
        private final Collection<BigraphEntity.SiteEntity> sites;

        Permutation(int n) {
            super(null);
            roots = new ArrayList<>(n);
            sites = new ArrayList<>(n);

            for (int i = 0; i < n; i++) {
                BigraphEntity.RootEntity newRoot = (BigraphEntity.RootEntity) mutableBuilder.createNewRoot(i);
                BigraphEntity.SiteEntity newSite = (BigraphEntity.SiteEntity) mutableBuilder.createNewSite(i);
                setParentOfNode(newSite, newRoot);
                roots.add(newRoot);
                sites.add(newSite);
            }
        }

        @Override
        public Collection<BigraphEntity.RootEntity> getRoots() {
            return roots;
        }

        @Override
        public Collection<BigraphEntity.SiteEntity> getSites() {
            return sites;
        }

        @Override
        public S getSignature() {
            return emptySignature;
        }

        @Override
        public Collection<BigraphEntity> getSiblingsOfNode(BigraphEntity node) {
            return Collections.emptyList();
        }

        @Override
        public EPackage getModelPackage() {
            return loadedModelPacakge;
        }
    }

    /**
     * Identity of a barren. Is a permutation of size one.
     */
    public class Identity1 extends Permutation {

        Identity1() {
            super(1);
        }
    }

    /**
     * Symmetry placings
     */
    public class Symmetry extends Permutation {
        private final Collection<BigraphEntity.RootEntity> roots = new ArrayList<>(2);
        private final Collection<BigraphEntity.SiteEntity> sites = new ArrayList<>(2);

        Symmetry(int n) {
            super(0);

            for (int i = 0, j = 1; i < n; i++, j--) {
                BigraphEntity.RootEntity newRoot = (BigraphEntity.RootEntity) mutableBuilder.createNewRoot(i);
                BigraphEntity.SiteEntity newSite = (BigraphEntity.SiteEntity) mutableBuilder.createNewSite(j);
                setParentOfNode(newSite, newRoot);
                roots.add(newRoot);
                sites.add(newSite);
            }
        }

        @Override
        public Collection<BigraphEntity.RootEntity> getRoots() {
            return roots;
        }

        @Override
        public Collection<BigraphEntity.SiteEntity> getSites() {
            return sites;
        }

        @Override
        public Collection<BigraphEntity> getSiblingsOfNode(BigraphEntity node) {
            return Collections.emptyList();
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

    private void setParentOfNode(final BigraphEntity node, final BigraphEntity parent) {
        EStructuralFeature prntRef = node.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        node.getInstance().eSet(prntRef, parent.getInstance()); // child is automatically added to the parent according to the ecore model
    }

}
