package de.tudresden.inf.st.bigraphs.core.impl.elementary;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import org.eclipse.emf.ecore.EPackage;

import java.util.Objects;
import java.util.Set;

/**
 * @author Dominik Grzelak
 */
public class DiscreteIon<S extends Signature<? extends Control<? extends NamedType<?>, ? extends FiniteOrdinal<?>>>>
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
            if (signature.getControlByName(name.stringValue()).getControlKind() != ControlKind.ATOMIC)
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
