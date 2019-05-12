package de.tudresden.inf.st.bigraphs.core;


import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;
import org.eclipse.emf.ecore.EPackage;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface Bigraph<S extends Signature> extends BigraphicalConstruct<S> {
    /**
     * Get the respective signature of the current bigraph
     *
     * @return the signature of the bigraph
     */
    S getSignature();

    default Map.Entry<Set<FiniteOrdinal<Integer>>, Set<StringTypedName>> getInnerFace() {
        return new AbstractMap.SimpleImmutableEntry<>(
                getSites().stream().map(x -> FiniteOrdinal.ofInteger(x.getIndex())).collect(Collectors.toSet()),
                getInnerNames().stream().map(x -> StringTypedName.of(x.getName())).collect(Collectors.toSet())
        );
    }

    default Map.Entry<Set<FiniteOrdinal<Integer>>, Set<StringTypedName>> getOuterFace() {
        return new AbstractMap.SimpleImmutableEntry<>(
                getRoots().stream().map(x -> FiniteOrdinal.ofInteger(x.getIndex())).collect(Collectors.toSet()),
                getOuterNames().stream().map(x -> StringTypedName.of(x.getName())).collect(Collectors.toSet())
        );
    }

    Collection<BigraphEntity.Edge> getEdges();

    <C extends Control> Collection<BigraphEntity.NodeEntity<C>> getNodes();

    BigraphEntity getParent(BigraphEntity node);

    BigraphEntity getLink(BigraphEntity node);

    Collection<BigraphEntity.Port> getPorts(BigraphEntity node);

    //without sites
    Collection<BigraphEntity> getChildrenOf(BigraphEntity node);

    default boolean isGround() {
        return getInnerNames().size() == 0 && getSites().size() == 0;
    }

    boolean areConnected(BigraphEntity.NodeEntity place1, BigraphEntity.NodeEntity place2);

    EPackage getModelPackage();
}
