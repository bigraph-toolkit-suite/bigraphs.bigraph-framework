package de.tudresden.inf.st.bigraphs.core.utils;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.Objects;

/**
 * @author Dominik Grzelak
 */
public class BigraphUtil {

    public static <S extends Signature<? extends Control<?, ?>>> Bigraph<S> copyIfSame(Bigraph<S> g, Bigraph<S> f) {
        if (g.equals(f)) {
            try {
                EcoreBigraph.Stub clone = new EcoreBigraph.Stub((EcoreBigraph) f).clone();
                g = (Bigraph<S>) PureBigraphBuilder.create(g.getSignature(), clone.getModelPackage(), clone.getModel()).createBigraph();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException("Could not clone bigraph");
            }
        }
        return g;
    }

    public static <S extends Signature<? extends Control<?, ?>>> Bigraph<S> copy(Bigraph<S> f) {
        try {
            EcoreBigraph.Stub clone = new EcoreBigraph.Stub((EcoreBigraph) f).clone();
            return (Bigraph<S>) PureBigraphBuilder.create(f.getSignature(), clone.getModelPackage(), clone.getModel()).createBigraph();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Could not clone bigraph");
        }
    }

    public static EcoreBigraph copy(EcoreBigraph bigraph) throws CloneNotSupportedException {
        EcoreBigraph.Stub clone = new EcoreBigraph.Stub(bigraph).clone();
        return PureBigraphBuilder.create(((Bigraph<?>) bigraph).getSignature(), clone.getModelPackage(), clone.getModel()).createBigraph();
    }

    public static void setParentOfNode(final EObject node, final EObject parent) {
        EStructuralFeature prntRef = node.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        Objects.requireNonNull(prntRef);
        node.eSet(prntRef, parent); // child is automatically added to the parent according to the ecore model
    }

    /**
     * Basic checking method for simple elementary bigraphs such as a merge or closure.
     * This doesn't work for a <i>Discrete Ion</i> or a <i>molecule</i>, for instance.
     *
     * @param bigraph the bigraph to check
     * @return {@code true}, if bigraph is a simple elementary one (except for a discrete ion, for example).
     */
    public static boolean isElementaryBigraph(Bigraph<?> bigraph) {
        boolean isPlacing = isBigraphElementaryPlacing(bigraph);
        boolean isLinking = isBigraphElementaryLinking(bigraph);
        return isLinking || isPlacing;
    }

    public static boolean isBigraphElementaryLinking(Bigraph<?> bigraph) {
        return bigraph.getEdges().size() == 0 &&
                bigraph.getAllPlaces().size() == 0 &&
                (bigraph.getInnerNames().size() != 0 || bigraph.getOuterNames().size() != 0);
    }

    public static boolean isBigraphElementaryPlacing(Bigraph<?> bigraph) {
        return bigraph.getEdges().size() == 0 &&
                bigraph.getInnerNames().size() == 0 &&
                bigraph.getOuterNames().size() == 0 &&
                bigraph.getNodes().size() == 0 &&
                bigraph.getRoots().size() != 0;
    }
}
