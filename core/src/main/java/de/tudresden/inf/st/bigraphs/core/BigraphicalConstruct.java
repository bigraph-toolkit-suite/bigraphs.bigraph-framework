package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;

import java.util.Collection;

public interface BigraphicalConstruct<S extends Signature> extends HasSignature<S> {

    /**
     * Prime bigraph has only one root and no inner names.
     *
     * @return true, if the bigraph is prime, otherwise false.
     */
    default boolean isPrime() {
        return getRoots().size() == 1 && getInnerNames().size() == 0;
    }

    //TODO
    default boolean isDiscrete() {
        return false;
    }

    Collection<BigraphEntity.RootEntity> getRoots();

    Collection<BigraphEntity.SiteEntity> getSites();

    Collection<BigraphEntity.OuterName> getOuterNames();

    Collection<BigraphEntity.InnerName> getInnerNames();
}
