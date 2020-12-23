package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.operations.IncompatibleInterfaceException;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.utils.BigraphUtil;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Supplier;

/**
 * This class provides a collection of useful methods for implementations of {@link BigraphComposite}
 *
 * @author Dominik Grzelak
 */
public abstract class BigraphCompositeSupport<S extends Signature<? extends Control<?, ?>>> extends BigraphDelegator<S> implements BigraphComposite<S> {

    public BigraphCompositeSupport(Bigraph<S> bigraphDelegate) {
        super(bigraphDelegate);
    }

    protected void assertInterfaceCompatibleForCompose(Bigraph<S> outer, Bigraph<S> inner, boolean isCompose) throws IncompatibleInterfaceException {
        Set<FiniteOrdinal<Integer>> siteOrdinals = outer.getInnerFace().getKey();
        Set<FiniteOrdinal<Integer>> rootOrdinals = inner.getOuterFace().getKey();
        Set<StringTypedName> nameSetLeft = outer.getInnerFace().getValue();
        Set<StringTypedName> nameSetRight = inner.getOuterFace().getValue();
        boolean disjoint = Collections.disjoint(nameSetLeft, nameSetRight);
        if ((rootOrdinals.size() > 0 || siteOrdinals.size() > 0) && nameSetLeft.size() == 0 && nameSetRight.size() == 0)
            disjoint = false; // this is legit if they are only place graphs
//        if (isCompose && ((rootOrdinals.size() > 0 || siteOrdinals.size() > 0) && nameSetLeft.size() != nameSetRight.size())) {
//            disjoint = true;
//        }
        boolean disjoint2 = siteOrdinals.size() != rootOrdinals.size() || Collections.disjoint(siteOrdinals, rootOrdinals);
        if (siteOrdinals.size() == 0 && rootOrdinals.size() == 0) disjoint2 = false;
//        if (inner instanceof ElementaryBigraph || BigraphUtil.isBigraphElementary(inner)) {
//            if (!disjoint && isLinking(inner) && !disjoint2 && isPlacing(outer)) return;
//            if (!disjoint2 && isPlacing(inner)) return;
//        }
        if (outer instanceof ElementaryBigraph || BigraphUtil.isElementaryBigraph(outer)) {
            if (!disjoint && isLinking(outer) && !isCompose) {
                return;
            }
            if (!disjoint && isLinking(outer) && isCompose && nameSetLeft.size() == nameSetRight.size()) {
                return;
            }
            if (!disjoint2 && isPlacing(outer)) return;
        }
        if (disjoint || disjoint2) {
            throw new IncompatibleInterfaceException();
        }
    }

    protected boolean isLinking(Bigraph<S> bigraph) {
        return (BigraphUtil.isBigraphElementaryLinking(bigraph) ||
                (bigraph instanceof ElementaryBigraph && ((ElementaryBigraph<S>) bigraph).isLinking()));
    }

    protected boolean isPlacing(Bigraph<S> bigraph) {
        return (BigraphUtil.isBigraphElementaryPlacing(bigraph) ||
                (bigraph instanceof ElementaryBigraph && ((ElementaryBigraph<S>) bigraph).isPlacing()));
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

    public static class LinkComparator<T extends BigraphEntity<?>> implements Comparator<T> {

        @Override
        public int compare(T o1, T o2) {
            if (o1 instanceof BigraphEntity.Link && o2 instanceof BigraphEntity.Link) {
                int i = ((BigraphEntity.Link) o1).getName().compareTo(((BigraphEntity.Link) o2).getName());
                if (i == 0) {
                    return o1.getType().compareTo(o2.getType());
                }
                return i;
            } else if (o1 instanceof BigraphEntity.InnerName && o2 instanceof BigraphEntity.InnerName) {
                int i = ((BigraphEntity.InnerName) o1).getName().compareTo(((BigraphEntity.InnerName) o2).getName());
                if (i == 0) {
                    return o1.getType().compareTo(o2.getType());
                }
                return i;
            }
            return -1;
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

    protected Supplier<String> createNameSupplier(final String prefix, final int startingIndex) {
        return new Supplier<String>() {
            private int id = startingIndex;

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

    protected Supplier<Integer> createNameSupplier(final int startingIndex) {
        return new Supplier<Integer>() {
            private int id = startingIndex;

            @Override
            public Integer get() {
                return id++;
            }
        };
    }
}
