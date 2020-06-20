package de.tudresden.inf.st.bigraphs.core.impl.elementary;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.EMetaModelData;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.MutableBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.SignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A placing is a node-free bigraph. The three elementary placing exist:
 * <ul>
 * <li>A barren root: 1</li>
 * <li><i>join</i></li>
 * <li><i>gamma</i><sub>m,n</sub></li>
 * </ul>
 * <p>
 * By that a special placing called {@literal merge_m: m -> 1} can be derived and is implemented here for
 * convenience. merge_0 = 1, merge_1 = id_1, merge_2 = join, hence, {@literal merge_{m+1} = join o (id_1 + merge_m)}.
 */
public class Placings<S extends Signature<? extends Control<?, ?>>> implements Serializable {
    private volatile S arbitrarySignature;
    private volatile MutableBuilder<S> mutableBuilder;
    private final EPackage loadedModelPackage;

    /**
     * @param signatureBuilder to create an empty signature of the appropriate type for working with
     *                         user-defined bigraphs of the same type created with the same factory
     */
    public Placings(SignatureBuilder signatureBuilder) {
        arbitrarySignature = (S) signatureBuilder.createEmpty();
        mutableBuilder = PureBigraphBuilder.newMutableBuilder(arbitrarySignature);
        loadedModelPackage = mutableBuilder.getLoadedEPackage();
    }

    public Placings(S signature) {
        this(signature, null);
    }

    public Placings(S signature, EMetaModelData metaModelData) {
        arbitrarySignature = signature;
        if (Objects.nonNull(metaModelData)) {
            mutableBuilder = PureBigraphBuilder.newMutableBuilder(arbitrarySignature, metaModelData);
        } else {
            mutableBuilder = PureBigraphBuilder.newMutableBuilder(arbitrarySignature);
        }
        loadedModelPackage = mutableBuilder.getLoadedEPackage();
    }

    public Placings<S>.Barren barren() {
        mutableBuilder.reset();
        return new Barren();
    }

    public Placings<S>.Identity1 identity1() {
        mutableBuilder.reset();
        return new Identity1();
    }

    public Symmetry symmetry11() {
        mutableBuilder.reset();
        return new Symmetry(2);
    }

    public Symmetry symmetry(int n) {
        mutableBuilder.reset();
        return new Symmetry(n);
    }

    public Placings<S>.Merge merge(int m) {
        mutableBuilder.reset();
        return new Merge(m);
    }

    public Placings<S>.Join join() {
        mutableBuilder.reset();
        return new Join();
    }

    /**
     * Create an "equally distributed permutation", similiar to an identity place graph.
     * Each site will be mapped exactly to one root where the indices match.
     *
     * @param n number of sites respectively roots mapped to each other {@literal n_i -> n_i, i in 1..n}
     * @return an "equally distributed permutation"
     */
    public Placings<S>.Permutation permutation(int n) {
        mutableBuilder.reset();
        return new Permutation(n);
    }

    public class Barren extends ElementaryBigraph<S> {
        private final BigraphEntity.RootEntity root;

        Barren() {
            super(null);
            root = (BigraphEntity.RootEntity) mutableBuilder.createNewRoot(0);

            metaModelPackage = EcoreUtil.copy(loadedModelPackage);
            instanceModel = mutableBuilder.createInstanceModel(metaModelPackage,
                    arbitrarySignature,
                    SortedMaps.mutable.of(0, root),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap());
        }

        @Override
        public S getSignature() {
            return arbitrarySignature;
        }

        @Override
        public final Collection<BigraphEntity.RootEntity> getRoots() {
            return Collections.singletonList(root);
        }

        @Override
        public Collection<BigraphEntity.SiteEntity> getSites() {
            return Collections.EMPTY_LIST;
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
        public List<BigraphEntity> getAllPlaces() {
            return super.getAllPlaces();
        }

        @Override
        public List<BigraphEntity> getSiblingsOfNode(BigraphEntity node) {
            return Collections.EMPTY_LIST;
        }

        @Override
        public Collection<BigraphEntity.InnerName> getSiblingsOfInnerName(BigraphEntity.InnerName innerName) {
            return Collections.EMPTY_LIST;
        }
    }

    public class Join extends ElementaryBigraph<S> {
        private final BigraphEntity.RootEntity root;
        private final ImmutableList<BigraphEntity.SiteEntity> sites;

        Join() {
            super(null);
            root = (BigraphEntity.RootEntity) mutableBuilder.createNewRoot(0);
            sites = Lists.immutable.of(
                    (BigraphEntity.SiteEntity) mutableBuilder.createNewSite(0),
                    (BigraphEntity.SiteEntity) mutableBuilder.createNewSite(1)
            );

            sites.forEach(siteEntity -> setParentOfNode(siteEntity, root));
            metaModelPackage = EcoreUtil.copy(loadedModelPackage);
            instanceModel = mutableBuilder.createInstanceModel(metaModelPackage,
                    arbitrarySignature,
                    SortedMaps.mutable.of(0, root),
                    SortedMaps.mutable.of(0, sites.get(0), 1, sites.get(1)),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap());
        }

        @Override
        public S getSignature() {
            return arbitrarySignature;
        }

        @Override
        public final Collection<BigraphEntity.RootEntity> getRoots() {
            return Collections.singletonList(root);
        }

        @Override
        public final Collection<BigraphEntity.SiteEntity> getSites() {
            return sites.castToList();
        }

        @Override
        public List<BigraphEntity> getSiblingsOfNode(BigraphEntity node) {
            if (BigraphEntityType.isSite(node) && sites.contains((BigraphEntity.SiteEntity) node)) {
                return sites.stream().filter(x -> !x.equals(node)).collect(Collectors.toList());
            }
            return Collections.emptyList();
        }

        @Override
        public Collection<BigraphEntity.InnerName> getSiblingsOfInnerName(BigraphEntity.InnerName innerName) {
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * A merge maps m sites to a single root where {@literal m  > 0}. Otherwise {@literal merge_0 = 1}.
     */
    public class Merge extends ElementaryBigraph<S> {
        private final BigraphEntity.RootEntity root;
        //        private final MutableList<BigraphEntity.SiteEntity> sites;
        private final MutableSortedMap<Integer, BigraphEntity.SiteEntity> sitesMap = SortedMaps.mutable.empty();

        Merge(final int m) {
            super(null);
            root = (BigraphEntity.RootEntity) mutableBuilder.createNewRoot(0);
            IntStream.range(0, m).forEach(value -> {
                BigraphEntity.SiteEntity newSite = (BigraphEntity.SiteEntity) mutableBuilder.createNewSite(value);
                setParentOfNode(newSite, root);
                sitesMap.put(value, newSite);
            });
            metaModelPackage = EcoreUtil.copy(loadedModelPackage);
            instanceModel = mutableBuilder.createInstanceModel(metaModelPackage,
                    arbitrarySignature,
                    SortedMaps.mutable.of(0, root),
                    sitesMap,
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap());

        }

        @Override
        public S getSignature() {
            return arbitrarySignature;
        }

        @Override
        public final Collection<BigraphEntity.RootEntity> getRoots() {
            return Collections.singletonList(root);
        }

        @Override
        public final Collection<BigraphEntity.SiteEntity> getSites() {
            return sitesMap.values();
        }

        @Override
        public List<BigraphEntity> getSiblingsOfNode(BigraphEntity node) {
            if (BigraphEntityType.isSite(node) && sitesMap.containsValue(node)) {
                return sitesMap.values().stream().filter(x -> !x.equals(node)).collect(Collectors.toList());
            }
            return Collections.emptyList();
        }

        @Override
        public Collection<BigraphEntity.InnerName> getSiblingsOfInnerName(BigraphEntity.InnerName innerName) {
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * Base class of a bijective placing.
     * <p>
     * This class will also create an identity place graph <i>id_n</i> ("equally distributed").
     * Each site will be mapped to one root where the indices are the same.
     */
    public class Permutation extends ElementaryBigraph<S> {
        private final MutableSortedMap<Integer, BigraphEntity.RootEntity> rootsMap = SortedMaps.mutable.empty();
        private final MutableSortedMap<Integer, BigraphEntity.SiteEntity> sitesMap = SortedMaps.mutable.empty();

        Permutation(int n) {
            super(null);
            for (int i = 0; i < n; i++) {
                BigraphEntity.RootEntity newRoot = (BigraphEntity.RootEntity) mutableBuilder.createNewRoot(i);
                BigraphEntity.SiteEntity newSite = (BigraphEntity.SiteEntity) mutableBuilder.createNewSite(i);
                setParentOfNode(newSite, newRoot);
                rootsMap.put(i, newRoot);
                sitesMap.put(i, newSite);
            }
            metaModelPackage = EcoreUtil.copy(loadedModelPackage);
            instanceModel = mutableBuilder.createInstanceModel(metaModelPackage,
                    arbitrarySignature, rootsMap, sitesMap,
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap());
        }

        @Override
        public Collection<BigraphEntity.RootEntity> getRoots() {
            return rootsMap.values();
        }

        @Override
        public Collection<BigraphEntity.SiteEntity> getSites() {
            return sitesMap.values();
        }

        @Override
        public S getSignature() {
            return arbitrarySignature;
        }

        @Override
        public List<BigraphEntity> getChildrenOf(BigraphEntity node) {
            if (!(node instanceof BigraphEntity.RootEntity)) return Collections.emptyList();
            return Lists.mutable.of(sitesMap.get(((BigraphEntity.RootEntity) node).getIndex()));
        }

        @Override
        public List<BigraphEntity> getSiblingsOfNode(BigraphEntity node) {
            return Collections.emptyList();
        }

        @Override
        public Collection<BigraphEntity.InnerName> getSiblingsOfInnerName(BigraphEntity.InnerName innerName) {
            return Collections.EMPTY_LIST;
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
        private final MutableSortedMap<Integer, BigraphEntity.RootEntity> rootsMap = SortedMaps.mutable.empty();
        private final MutableMap<Integer, BigraphEntity.SiteEntity> sitesMap = Maps.mutable.empty();

        Symmetry(int n) {
            super(0);
            for (int i = 0, j = (n - 1); i < n; i++, j--) {
                BigraphEntity.RootEntity newRoot = (BigraphEntity.RootEntity) mutableBuilder.createNewRoot(i);
                BigraphEntity.SiteEntity newSite = (BigraphEntity.SiteEntity) mutableBuilder.createNewSite(j);
                setParentOfNode(newSite, newRoot);
                rootsMap.put(i, newRoot);
                sitesMap.put(j, newSite);
            }
            metaModelPackage = EcoreUtil.copy(loadedModelPackage);
            instanceModel = mutableBuilder.createInstanceModel(metaModelPackage,
                    arbitrarySignature, rootsMap, sitesMap,
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap());
        }

        @Override
        public Collection<BigraphEntity.RootEntity> getRoots() {
            return rootsMap.values();
        }

        @Override
        public Collection<BigraphEntity.SiteEntity> getSites() {
            return sitesMap.values();
        }

        @Override
        public List<BigraphEntity> getChildrenOf(BigraphEntity node) {
            if (!(node instanceof BigraphEntity.RootEntity)) return Collections.emptyList();
            int n = sitesMap.size();
            int ix = Math.abs((n - 1) - ((BigraphEntity.RootEntity) node).getIndex());
            return Lists.mutable.of(sitesMap.get(ix));
        }

        @Override
        public S getSignature() {
            return arbitrarySignature;
        }
    }

    //TODO: move to BigraphUtils/EcoreUtils
    private void setParentOfNode(final BigraphEntity node, final BigraphEntity parent) {
        EStructuralFeature prntRef = node.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        node.getInstance().eSet(prntRef, parent.getInstance()); // child is automatically added to the parent according to the ecore model
    }

}
