package org.bigraphs.framework.core.utils;

import org.bigraphs.framework.core.*;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.exceptions.IncompatibleSignatureException;
import org.bigraphs.framework.core.exceptions.SignatureNotConsistentException;
import org.bigraphs.framework.core.exceptions.operations.IncompatibleInterfaceException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DynamicControl;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.reactivesystem.InstantiationMap;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.bigraphs.framework.core.factory.BigraphFactory.ops;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

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
                g = (Bigraph<S>) PureBigraphBuilder.create(g.getSignature(), clone.getMetaModel(), clone.getInstanceModel()).create();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException("Could not clone bigraph");
            }
        }
        return g;
    }

    public static <S extends AbstractEcoreSignature<? extends Control<?, ?>>> Bigraph<S> copy(Bigraph<S> f) {
        try {
            EcoreBigraph.Stub clone = new EcoreBigraph.Stub((EcoreBigraph) f).clone();
            return (Bigraph<S>) PureBigraphBuilder.create(f.getSignature(), clone.getMetaModel(), clone.getInstanceModel()).create();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Could not clone bigraph");
        }
    }

    public static EcoreBigraph copy(EcoreBigraph bigraph) throws CloneNotSupportedException {
        EcoreBigraph.Stub clone = new EcoreBigraph.Stub(bigraph).clone();
        return PureBigraphBuilder.create((AbstractEcoreSignature) bigraph.getSignature(), clone.getMetaModel(), clone.getInstanceModel())
                .create();
    }

    public static PureBigraph toBigraph(EcoreBigraph.Stub<DynamicSignature> stub, DynamicSignature signature) {
        return PureBigraphBuilder.create(signature.getInstanceModel(), stub.getMetaModel(), stub.getInstanceModel()).create();
    }

    public static PureBigraph toBigraph(EPackage metaModel, EObject instanceModel, DynamicSignature signature) {
        return PureBigraphBuilder.create(signature.getInstanceModel(), metaModel, instanceModel).create();
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

    /**
     * Returns the common label of an bigraph entity (a node, root, outer name, ...).
     * Interface elements are prefixed such as "s" for sites, and "r" for roots.
     * <p>
     * Similar to {@link #getUniqueIdOfBigraphEntity(BigraphEntity)} but here for a node
     * the control label is used instead of its name id.
     *
     * @param x
     * @return
     */
    public static String getUniqueLabelOfBigraphEntity(BigraphEntity x) {
        String lbl = "";
        if (BigraphEntityType.isRoot(x)) {
            lbl = "r" + ((BigraphEntity.RootEntity) x).getIndex();
        } else if (BigraphEntityType.isSite(x)) {
            lbl = "s" + ((BigraphEntity.SiteEntity) x).getIndex();
        } else if (BigraphEntityType.isNode(x)) {
            lbl = ((BigraphEntity.NodeEntity) x).getControl().getNamedType().stringValue();
        } else if (BigraphEntityType.isLinkType(x)) {
            lbl = "o" + ((BigraphEntity.Link) x).getName();
        } else if (BigraphEntityType.isInnerName(x)) {
            lbl = "i" + ((BigraphEntity.InnerName) x).getName();
        } else if (BigraphEntityType.isPort(x)) {
            //TODO also add node label here
//            EMFUtils.findAttribute()
            lbl = "p" + ((BigraphEntity.Port) x).getIndex();
        }
        return lbl;
    }

    /**
     * Returns the unique id of an bigraph entity (a node, root, outer name, ...).
     * Interface elements are prefixed such as "s" for sites, and "r" for roots.
     * <p>
     * Similar to {@link #getUniqueLabelOfBigraphEntity(BigraphEntity)} but here for node
     * the name id is used instead of the control label.
     *
     * @param x
     * @return
     */
    public static String getUniqueIdOfBigraphEntity(BigraphEntity x) {
        String lbl = "";
        if (BigraphEntityType.isRoot(x)) {
            lbl = "r" + ((BigraphEntity.RootEntity) x).getIndex();
        } else if (BigraphEntityType.isSite(x)) {
            lbl = "s" + ((BigraphEntity.SiteEntity) x).getIndex();
        } else if (BigraphEntityType.isNode(x)) {
            lbl = ((BigraphEntity.NodeEntity) x).getName();
        } else if (BigraphEntityType.isLinkType(x)) {
            lbl = "o" + ((BigraphEntity.Link) x).getName();
        } else if (BigraphEntityType.isInnerName(x)) {
            lbl = "i" + ((BigraphEntity.InnerName) x).getName();
        } else if (BigraphEntityType.isPort(x)) {
            //TODO also add node label here
//            EMFUtils.findAttribute()
            lbl = "p" + ((BigraphEntity.Port) x).getIndex(); // id of node
        }
        return lbl;
    }

    /**
     * Each bigraph represents a "parameter" in a list.
     * The result of this method is the product of all parameters {@code discreteBigraphs} but each parameter is getting a new root index due to the
     * instantiation map.
     * The instantiation map maps the position of the initial bigraphs to a new one in the list.
     * The resulting bigraph has as many roots as parameters in the list.
     *
     * @param discreteBigraphs list of bigraphs (parameters)
     * @param instantiationMap an instantiation map
     * @return a bigraph containing all "parameters" in a new order. It has as many roots as the list size of {@code discreteBigraphs}.
     * @throws IncompatibleSignatureException
     * @throws IncompatibleInterfaceException
     */
    public static Bigraph reorderBigraphs(List<Bigraph> discreteBigraphs, InstantiationMap instantiationMap) throws IncompatibleSignatureException, IncompatibleInterfaceException {
        Bigraph d_Params;
        List<Bigraph> parameters = new ArrayList<>(discreteBigraphs);
        if (parameters.size() >= 2) {
            FiniteOrdinal<Integer> mu_ix = instantiationMap.get(0);
            BigraphComposite<?> d1 = ops((Bigraph) parameters.get(mu_ix.getValue()));
            for (int i = 1, n = parameters.size(); i < n; i++) {
                mu_ix = instantiationMap.get(i);
                d1 = d1.parallelProduct((Bigraph) parameters.get(mu_ix.getValue()));
            }
            d_Params = d1.getOuterBigraph();
        } else {
            d_Params = parameters.get(0);
        }
        return d_Params;
    }

    /**
     * This method merges the two given signatures {@code left} and {@code right}.
     * A completely new instance will be created with a new underlying Ecore signature metamodel.
     * <p>
     * If either one of the given signatures is {@code null}, the other one is simply returned.
     * <p>
     * Merging signatures does not require the two signatures to be consistent.
     * If both do not contain the same control labels, then merge is just like composition.
     * <p>
     * For same control labels, the left signature overrides the right one.
     *
     * @param left  a signature
     * @param right another signature to merge with {@code left}
     * @return a merged signature, or just {@code left} or {@code right}, if the other one is {@code null}
     * @throws RuntimeException if the underlying metamodel is invalid, that is created in the merging process.
     */
    public static DynamicSignature mergeSignatures(DynamicSignature left, DynamicSignature right) {
        return mergeSignatures(left, right, 0);
    }

    /**
     * This method merges the two given signatures {@code left} and {@code right}.
     * A completely new instance will be created with a new underlying Ecore signature metamodel.
     * <p>
     * If either one of the given signatures is {@code null}, the other one is simply returned.
     * <p>
     * Merging signatures does not require the two signatures to be consistent.
     * If both do not contain the same control labels, then merge is just like composition.
     * <p>
     * For same control labels,
     * the argument {@code leftOrRight} specifies whether the left signature overrides the right one:
     * 0 means the left signature has precedence, and 1 means the right signature has precedence.
     *
     * @param left        the first signature
     * @param right       the second signature to merge with the first one
     * @param leftOrRight whether to use the left or right signature if same controls found in both
     * @return the new merge signature
     */
    public static DynamicSignature mergeSignatures(DynamicSignature left, DynamicSignature right, int leftOrRight) {
        if (Objects.nonNull(left) && Objects.nonNull(right)) {
            Set<String> setLeft = left.getControls().stream().map(x -> x.getNamedType().stringValue()).collect(Collectors.toSet());
            Set<String> setRight = right.getControls().stream().map(x -> x.getNamedType().stringValue()).collect(Collectors.toSet());
            Set<String> intersectionSet = new HashSet<>(setLeft);
            intersectionSet.retainAll(setRight);
            DynamicSignatureBuilder sb = pureSignatureBuilder();
            Stream.concat(left.getControls().stream(), right.getControls().stream())
                    .filter(x -> !intersectionSet.contains(x.getNamedType().stringValue()))
                    .forEach(c -> {
                        sb.newControl(c.getNamedType(), c.getArity())
                                .status(c.getControlKind()).assign();
                    });
            // Special treatment for common control labels
            Stream<String> leftStream = left.getControls().stream().map(x -> x.getNamedType().stringValue());
            Stream<String> rightStream = right.getControls().stream().map(x -> x.getNamedType().stringValue());
            Stream<Map.Entry<String, String>> zippedLabels = zip(leftStream, rightStream);
            zippedLabels.forEach(pair -> {
                DynamicControl c = leftOrRight == 0 ? left.getControlByName(pair.getKey()) : right.getControlByName(pair.getKey());
                sb.newControl(c.getNamedType(), c.getArity())
                        .status(c.getControlKind()).assign();
            });
            return sb.create();
        }
        return Objects.isNull(right) ? left : right;
    }

    /**
     * This method composes the two given signatures {@code left} and {@code right}.
     * A completely new instance will be created with a new underlying Ecore signature metamodel.
     * <p>
     * If either one of the given signatures is {@code null}, the other one is simply returned.
     * <p>
     * Signatures are required to be consistent, i.e., both signatures have either different control labels, or
     * if they contain the same label, they must agree on the arity and status.
     *
     * @param left  a signature
     * @param right another signature to compose with {@code left}
     * @return a composed signature, or just {@code left} or {@code right}, if the other one is {@code null}
     * @throws RuntimeException if the signatures are not consistent
     */
    public static DynamicSignature composeSignatures(DynamicSignature left, DynamicSignature right) {
        assertSignaturesAreConsistent(left, right);
        if (Objects.nonNull(left) && Objects.nonNull(right)) {
            DynamicSignatureBuilder sb = pureSignatureBuilder();
            Stream.concat(left.getControls().stream(), right.getControls().stream()).forEach(c -> {
                sb.newControl(c.getNamedType(), c.getArity())
                        .status(c.getControlKind()).assign();
            });
            return sb.create();
        }
        return Objects.isNull(right) ? left : right;
    }

    public static Optional<DynamicSignature> composeSignatures(List<DynamicSignature> signatures) {
        return signatures.stream().reduce(BigraphUtil::composeSignatures);
    }

    public static Optional<DynamicSignature> leftMergeSignatures(List<DynamicSignature> signatures) {
        return signatures.stream().reduce(BigraphUtil::mergeSignatures);
    }

    /**
     * Throws a runtime exception if both signatures are not consistent
     *
     * @param left  the first signature
     * @param right the second signature
     */
    private static void assertSignaturesAreConsistent(DynamicSignature left, DynamicSignature right) {
        Set<String> setLeft = left.getControls().stream().map(x -> x.getNamedType().stringValue()).collect(Collectors.toSet());
        Set<String> setRight = right.getControls().stream().map(x -> x.getNamedType().stringValue()).collect(Collectors.toSet());
        Set<String> unionSet = new HashSet<>(setLeft);
        unionSet.addAll(setRight);
        if (unionSet.size() != (setLeft.size() + setRight.size())) {
            throw new SignatureNotConsistentException();
        }
    }


    public static <T, U> Stream<Map.Entry<T, U>> zip(Stream<T> stream1, Stream<U> stream2) {
        Iterator<T> iterator1 = stream1.iterator();
        Iterator<U> iterator2 = stream2.iterator();

        Stream.Builder<Map.Entry<T, U>> builder = Stream.builder();

        while (iterator1.hasNext() && iterator2.hasNext()) {
            builder.add(new AbstractMap.SimpleEntry<>(iterator1.next(), iterator2.next()));
        }

        return builder.build();
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
