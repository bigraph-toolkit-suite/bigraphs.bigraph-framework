package de.tudresden.inf.st.bigraphs.core.utils;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.Objects;
import java.util.stream.Stream;

import static de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory.pureSignatureBuilder;

/**
 * A collection of useful bigraph-related operations and queries.
 *
 * @author Dominik Grzelak
 */
public class BigraphUtil {

    public static <S extends AbstractEcoreSignature<? extends Control<?, ?>>> Bigraph<S> copyIfSame(Bigraph<S> g, Bigraph<S> f) {
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

    public static <S extends AbstractEcoreSignature<? extends Control<?, ?>>> Bigraph<S> copy(Bigraph<S> f) {
        try {
            EcoreBigraph.Stub clone = new EcoreBigraph.Stub((EcoreBigraph) f).clone();
            return (Bigraph<S>) PureBigraphBuilder.create(f.getSignature(), clone.getModelPackage(), clone.getModel()).createBigraph();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Could not clone bigraph");
        }
    }

    public static EcoreBigraph copy(EcoreBigraph bigraph) throws CloneNotSupportedException {
        EcoreBigraph.Stub clone = new EcoreBigraph.Stub(bigraph).clone();
        return PureBigraphBuilder.create((AbstractEcoreSignature) bigraph.getSignature(), clone.getModelPackage(), clone.getModel())
                .createBigraph();
    }

    public static void setParentOfNode(final BigraphEntity<?> node, final BigraphEntity<?> parent) {
        BigraphUtil.setParentOfNode(node.getInstance(), parent.getInstance());
    }

    public static void setParentOfNode(final EObject node, final EObject parent) {
        EStructuralFeature prntRef = node.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        Objects.requireNonNull(prntRef);
        node.eSet(prntRef, parent); // child is automatically added to the parent according to the ecore model
    }

    public static boolean isSomeParentOfNode(BigraphEntity<?> node, BigraphEntity<?> possibleParent, Bigraph<?> bigraph) {
        BigraphEntity<?> currentParent = node; //bigraph.getParent(node);
        while (currentParent != null) {
            currentParent = bigraph.getParent(currentParent);
            if (currentParent == possibleParent) {
                return true;
            }
        }
        return false;
    }

    // somewhat necessary, if bigrids are going to be used later in combination with other models

    /**
     * This method merges the two given signatures {@code left} and {@code right}.
     * A completely new instance will be created with a new underlying Ecore signature meta-model.
     * <p>
     * If either one of the given signatures is {@code null}, the other one is simply returned.
     *
     * @param left  a signature
     * @param right another signature to merge with {@code left}
     * @return a merged signature, or just {@code left} or {@code right}, if the other one is {@code null}
     * @throws RuntimeException if the underlying meta-model is invalid that is created in the merging process.
     */
    public static DefaultDynamicSignature mergeSignatures(DefaultDynamicSignature left, DefaultDynamicSignature right) {
        if (Objects.nonNull(left) && Objects.nonNull(right)) {
            DynamicSignatureBuilder sb = pureSignatureBuilder();
            Stream.concat(left.getControls().stream(), right.getControls().stream()).forEach(c -> {
                sb.newControl(c.getNamedType(), sb.newControl().getArity())
                        .status(c.getControlKind()).assign();
            });
            return sb.create();
        }
        return Objects.isNull(right) ? left : right;
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
