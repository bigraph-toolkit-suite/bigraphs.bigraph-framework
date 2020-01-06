package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

/**
 * This class provides a collection of useful methods for implementations of {@link BigraphComposite}
 *
 * @author Dominik Grzelak
 */
public abstract class BigraphCompositeSupport<S extends Signature> extends BigraphDelegator<S> implements BigraphComposite<S> {

    public BigraphCompositeSupport(Bigraph<S> bigraphDelegate) {
        super(bigraphDelegate);
    }

    protected void assertInterfaceCompatibleForCompose(Bigraph<S> outer, Bigraph<S> inner) throws IncompatibleInterfaceException {
        Set<FiniteOrdinal<Integer>> siteOrdinals = outer.getInnerFace().getKey();
        Set<FiniteOrdinal<Integer>> rootOrdinals = inner.getOuterFace().getKey();
        Set<StringTypedName> nameSetLeft = outer.getInnerFace().getValue();
        Set<StringTypedName> nameSetRight = inner.getOuterFace().getValue();
        boolean disjoint = Collections.disjoint(nameSetLeft, nameSetRight);
        if ((rootOrdinals.size() > 0 || siteOrdinals.size() > 0) && nameSetLeft.size() == 0 && nameSetRight.size() == 0)
            disjoint = false; // this is legit if they are only place graphs
        boolean disjoint2 = siteOrdinals.size() != rootOrdinals.size() || Collections.disjoint(siteOrdinals, rootOrdinals);
        if (siteOrdinals.size() == 0 && rootOrdinals.size() == 0) disjoint2 = false;
        if (disjoint || disjoint2) {
            throw new IncompatibleInterfaceException();
        }
    }

    protected void assertInterfaceCompatibleForJuxtaposition(Bigraph<S> outer, Bigraph<S> inner) throws IncompatibleInterfaceException {
        Set<StringTypedName> innerNamesOuter = outer.getInnerFace().getValue();
        Set<StringTypedName> innerNamesInner = inner.getInnerFace().getValue();

        Set<StringTypedName> outerNamesOuter = outer.getOuterFace().getValue();
        Set<StringTypedName> outerNamesInner = inner.getOuterFace().getValue();

        boolean disjointInnerNames = Collections.disjoint(innerNamesOuter, innerNamesInner);
        boolean disjointOuterNames = Collections.disjoint(outerNamesOuter, outerNamesInner);
        if (!disjointInnerNames || !disjointOuterNames) {
            throw new IncompatibleInterfaceException("Common inner names or outer names ...");
        }
    }

    /**
     * Helper method to assign a parent to a node.
     *
     * @param node   the node
     * @param parent the parent
     */
    protected void setParentOfNode(final BigraphEntity node, final BigraphEntity parent) {
        EStructuralFeature prntRef = node.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        node.getInstance().eSet(prntRef, parent.getInstance()); // child is automatically added to the parent according to the ecore model
    }

    protected Supplier<String> createNameSupplier(final String prefix) {
        return new Supplier<String>() {
            private int id = 0;

            @Override
            public String get() {
                return prefix + id++;
            }
        };
    }

    protected Supplier<Integer> createNameSupplier() {
        return new Supplier<Integer>() {
            private int id = 0;

            @Override
            public Integer get() {
                return id++;
            }
        };
    }
}
