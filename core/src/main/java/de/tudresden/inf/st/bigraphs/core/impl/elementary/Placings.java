package de.tudresden.inf.st.bigraphs.core.impl.elementary;

import de.tudresden.inf.st.bigraphs.core.ElementaryBigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.factory.SimpleBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.EmptySignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.MutableBuilder;

import java.io.Serializable;
import java.util.ArrayList;
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
public final class Placings implements Serializable {
    private volatile static SimpleBigraphFactory<StringTypedName, FiniteOrdinal<Integer>> factory = new SimpleBigraphFactory<>();
    private volatile static EmptySignature emptySignature = factory.createSignatureBuilder().createEmptySignature();
    private volatile static MutableBuilder<Signature> signatureMutableBuilder = BigraphBuilder.newMutableBuilder(emptySignature);

    public static Barren barren() {
        return new Barren();
    }

    public static Merge merge(int m) {
        return new Merge(m);
    }

    public static Join join() {
        return new Join();
    }

    public static class Barren implements ElementaryBigraph {
        private final BigraphEntity.RootEntity root;

        Barren() {
            root = (BigraphEntity.RootEntity) signatureMutableBuilder.createNewRoot(0);
        }

        @Override
        public EmptySignature getSignature() {
            return emptySignature;
        }

        @Override
        public Collection<BigraphEntity.RootEntity> getRoots() {
            return Collections.singletonList(root);
        }

        @Override
        public Collection<BigraphEntity.SiteEntity> getSites() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public Collection<BigraphEntity.OuterName> getOuterNames() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public Collection<BigraphEntity.InnerName> getInnerNames() {
            return Collections.EMPTY_LIST;
        }
    }

    public static class Join implements ElementaryBigraph {
        private final BigraphEntity.RootEntity root;
        private final Collection<BigraphEntity.SiteEntity> sites = new ArrayList<>(2);

        Join() {
            root = (BigraphEntity.RootEntity) signatureMutableBuilder.createNewRoot(0);
            sites.add((BigraphEntity.SiteEntity) signatureMutableBuilder.createNewSite(0));
            sites.add((BigraphEntity.SiteEntity) signatureMutableBuilder.createNewSite(1));
        }

        @Override
        public EmptySignature getSignature() {
            return emptySignature;
        }

        @Override
        public Collection<BigraphEntity.RootEntity> getRoots() {
            return Collections.singletonList(root);
        }

        @Override
        public Collection<BigraphEntity.SiteEntity> getSites() {
            return sites;
        }

        @Override
        public Collection<BigraphEntity.OuterName> getOuterNames() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public Collection<BigraphEntity.InnerName> getInnerNames() {
            return Collections.EMPTY_LIST;
        }
    }


    /**
     * A merge maps m sites to a single root where m > 0. Otherwise merge_0 = 1
     */
    public static class Merge implements ElementaryBigraph {
        private final BigraphEntity.RootEntity root;
        private final Collection<BigraphEntity.SiteEntity> sites;

        Merge(final int m) {
            sites = new ArrayList<>(m);
            root = (BigraphEntity.RootEntity) signatureMutableBuilder.createNewRoot(0);
            IntStream.range(0, m).forEach(value -> sites.add((BigraphEntity.SiteEntity) signatureMutableBuilder.createNewSite(value)));
        }

        @Override
        public EmptySignature getSignature() {
            return emptySignature;
        }

        @Override
        public Collection<BigraphEntity.RootEntity> getRoots() {
            return Collections.singletonList(root);
        }

        @Override
        public Collection<BigraphEntity.SiteEntity> getSites() {
            return sites;
        }

        @Override
        public Collection<BigraphEntity.OuterName> getOuterNames() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public Collection<BigraphEntity.InnerName> getInnerNames() {
            return Collections.EMPTY_LIST;
        }
    }

}
