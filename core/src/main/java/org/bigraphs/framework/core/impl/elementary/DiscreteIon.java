package org.bigraphs.framework.core.impl.elementary;

import org.bigraphs.framework.core.*;
import org.bigraphs.framework.core.*;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.NamedType;
import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.factory.AbstractBigraphFactory;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.eclipse.emf.ecore.EPackage;

import java.util.Objects;
import java.util.Set;

/**
 * This class represents a discrete ion.
 *
 * @author Dominik Grzelak
 */
public class DiscreteIon<S extends AbstractEcoreSignature<? extends Control<? extends NamedType<?>, ? extends FiniteOrdinal<?>>>>
        extends ElementaryBigraph<S> {
    private volatile PureBigraphBuilder<S> builder;

    public DiscreteIon(NamedType<?> name, Set<NamedType<?>> outerNames, S signature, EPackage bigraphMetamodel, AbstractBigraphFactory<S> factory) {
        super(null);
        builder = Objects.nonNull(bigraphMetamodel) ?
                (PureBigraphBuilder<S>) factory.createBigraphBuilder(signature, bigraphMetamodel) :
                (PureBigraphBuilder<S>) factory.createBigraphBuilder(signature);

        try {
            PureBigraphBuilder<S>.Hierarchy hierarchy = builder.createRoot().addChild(signature.getControlByName(name.stringValue()));
            outerNames.forEach(x -> {
                try {
                    hierarchy.linkToOuter(builder.createOuterName(x.stringValue()));
                } catch (TypeNotExistsException | InvalidConnectionException e) {
                    throw new RuntimeException(e);
                }
            });
            if (signature.getControlByName(name.stringValue()).getControlKind() != ControlStatus.ATOMIC)
                hierarchy.down().addSite();
        } catch (ControlIsAtomicException e) {
            throw new RuntimeException("Control shouldn't be atomic!");
        }
        bigraphDelegate = (Bigraph<S>) builder.createBigraph();
    }

    public DiscreteIon(NamedType<?> name, Set<NamedType<?>> outerNames, S signature, AbstractBigraphFactory<S> factory) {
        this(name, outerNames, signature, null, factory);
    }
}
