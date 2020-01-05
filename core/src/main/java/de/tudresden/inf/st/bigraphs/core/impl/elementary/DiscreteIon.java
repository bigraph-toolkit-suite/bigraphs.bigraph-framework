package de.tudresden.inf.st.bigraphs.core.impl.elementary;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.ElementaryBigraph;
import de.tudresden.inf.st.bigraphs.core.Signature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;

import java.util.Collection;
import java.util.Set;

/**
 * @author Dominik Grzelak
 */
public class DiscreteIon<S extends Signature, NT extends NamedType, FT extends FiniteOrdinal> extends ElementaryBigraph<S> {
    private volatile PureBigraphBuilder<S> builder;

    public DiscreteIon(NT name, Set<NT> outerNames, S signature, AbstractBigraphFactory<S, NT, FT> factory) {
        super(null);
        builder = (PureBigraphBuilder<S>) factory.createBigraphBuilder(signature);

        try {
            PureBigraphBuilder<S>.Hierarchy hierarchy = builder.createRoot().addChild(signature.getControlByName(name.stringValue()));
            outerNames.forEach(x -> {
                try {
                    hierarchy.linkToOuter(builder.createOuterName(x.stringValue()));
                } catch (TypeNotExistsException | InvalidConnectionException e) {
                    throw new RuntimeException(e);
                }
            });
            hierarchy.down().addSite();
        } catch (ControlIsAtomicException e) {
            throw new RuntimeException("Control shouldn't be atomic!");
        }
        bigraphDelegate = (Bigraph<S>) builder.createBigraph();
    }

    @Override
    public Collection<BigraphEntity.RootEntity> getRoots() {
        return bigraphDelegate.getRoots();
    }

    @Override
    public Collection<BigraphEntity.SiteEntity> getSites() {
        return bigraphDelegate.getSites();
    }

    @Override
    public Collection<BigraphEntity.OuterName> getOuterNames() {
        return bigraphDelegate.getOuterNames();
    }

    @Override
    public <C extends Control> Collection<BigraphEntity.NodeEntity<C>> getNodes() {
        return bigraphDelegate.getNodes();
    }

    @Override
    public Collection<BigraphEntity.Port> getPorts(BigraphEntity node) {
        return bigraphDelegate.getPorts(node);
    }

    @Override
    public BigraphEntity getParent(BigraphEntity node) {
        return bigraphDelegate.getParent(node);
    }

    @Override
    public Collection<BigraphEntity> getPointsFromLink(BigraphEntity linkEntity) {
        return bigraphDelegate.getPointsFromLink(linkEntity);
    }
}
