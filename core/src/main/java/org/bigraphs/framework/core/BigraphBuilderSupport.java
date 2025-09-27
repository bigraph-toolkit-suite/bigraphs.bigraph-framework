package org.bigraphs.framework.core;

import org.bigraphs.framework.core.exceptions.SignatureValidationFailedException;
import org.bigraphs.framework.core.exceptions.builder.LinkTypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.signature.KindSignature;
import org.bigraphs.framework.core.validation.BModelValidationResult;
import org.bigraphs.framework.core.validation.InvalidModelResult;
import org.bigraphs.framework.core.validation.ValidModelResult;
import org.bigraphs.framework.core.validation.ValidatorNotFound;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Supplier;

/**
 * Abstract support class for concrete bigraph builder implementations.
 * <p>
 * Provides generic helper methods for constructing bigraphs.
 *
 * @author Dominik Grzelak
 */
public abstract class BigraphBuilderSupport<S extends Signature<? extends Control<?, ?>>>
        implements BigraphBuilder<S> {

    protected static final String DEFAULT_EDGE_PREFIX = "e";
    protected static final String DEFAULT_VERTEX_PREFIX = "v";

    public abstract EPackage getMetaModel();

    protected abstract EObject getInstanceModel();

    protected abstract Map<String, EClass> getAvailableEClasses();

    protected abstract Map<String, EReference> getAvailableEReferences();

    public abstract void closeInner(BigraphEntity.InnerName innerName, boolean keepIdleName) throws LinkTypeNotExistsException;

    /**
     * Executes various metamodel validators.
     * It returns the class of the respective signature instance object passed to this method
     * (e.g., {@link KindSignature}).
     *
     * @param signatureMetaModel the signature instance model
     * @return the class of the corresponding signature instance model
     * @throws SignatureValidationFailedException If the validation fails because the instance model does not conform to a metamodel or
     *                                            the respective signature class constructor could not be found.
     */
    protected static BModelValidationResult executeValidationChain(EObject signatureMetaModel) throws SignatureValidationFailedException {
        List<BModelValidationResult> results = EcoreSignature.VALIDATORS.keyValuesView().collect(x -> {
            try {
                x.getTwo().accept(signatureMetaModel);
                return new ValidModelResult(x.getOne());
            } catch (Exception e) {
                return new InvalidModelResult(e);
            }
        }).toList();
        if (results.size() == 0) {
            return new ValidatorNotFound();
        }
        if (results.stream().noneMatch(x -> x.getClass().equals(ValidModelResult.class))) {
            return results.stream().filter(x -> x.getClass().equals(InvalidModelResult.class)).findFirst().get();
        }
        return results.stream().filter(x -> x.getClass().equals(ValidModelResult.class)).findFirst().get();
    }

    //TODO move to BigraphFactory
    public static <S extends AbstractEcoreSignature<? extends Control<?, ?>>> S getSignatureFromMetaModel(EObject signatureInstanceModel)
            throws SignatureValidationFailedException {
        // perform all validations first
        BModelValidationResult validationResult = executeValidationChain(signatureInstanceModel);
        if (validationResult.isValid()) {
            Class<? extends EcoreSignature> clazz = ((ValidModelResult) validationResult).getModelClass();
            try {
                // Now create the respective signature object from the class info given above
                Constructor<S> ctor = (Constructor<S>) clazz.getConstructor(EObject.class);
                S sig = ctor.newInstance(signatureInstanceModel);
                assert sig.getInstanceModel().equals(signatureInstanceModel);
                assert sig.getControls() != null;
                return sig;
            } catch (Exception e) {
                throw new SignatureValidationFailedException(e);
            }
        }
        SignatureValidationFailedException e = new SignatureValidationFailedException(validationResult.getClass().toString());
        if (((InvalidModelResult) validationResult).getExceptions().size() > 0) {
            e.initCause(((InvalidModelResult) validationResult).getExceptions().get(0));
        }
        throw e;
    }

    protected void assertSortingIsEnsuredForControl(String controlName) {
        //TODO: use in all child() methods in subclass
    }

    protected void assertSortingIsEnsuredForControl(Control controlName) {
        this.assertSortingIsEnsuredForControl(controlName.getNamedType().stringValue());
    }

    protected boolean isOuter(EObject eObject) {
        return Objects.nonNull(eObject) && eObject.eClass().equals(getAvailableEClasses().get(BigraphMetaModelConstants.CLASS_OUTERNAME));
    }

    public boolean isRoot(EObject eObject) {
        return Objects.nonNull(eObject) && eObject.eClass().equals(getAvailableEClasses().get(BigraphMetaModelConstants.CLASS_ROOT));
    }

    public boolean isNode(EObject eObject) {
        return Objects.nonNull(eObject) && eObject.eClass().equals(getAvailableEClasses().get(BigraphMetaModelConstants.CLASS_NODE));
    }

    protected boolean isEdge(EObject eObject) {
        return Objects.nonNull(eObject) && eObject.eClass().equals(getAvailableEClasses().get(BigraphMetaModelConstants.CLASS_EDGE));
    }

    protected EObject createBBigraphContainer(Collection<BigraphEntity.RootEntity> roots,
                                              Collection<BigraphEntity.Edge> edges,
                                              Collection<BigraphEntity.InnerName> innerNames,
                                              Collection<BigraphEntity.OuterName> outerNames) {
//        assert availableEClasses.get(BigraphMetaModelConstants.CLASS_BIGRAPH) != null;
        EObject eObject = getMetaModel().getEFactoryInstance().create(getAvailableEClasses().get(BigraphMetaModelConstants.CLASS_BIGRAPH));

        roots.forEach(x -> {
            ((EList) eObject.eGet(getAvailableEReferences().get(BigraphMetaModelConstants.REFERENCE_BROOTS)))
                    .add(x.getInstance());
        });
        edges.forEach(x -> {
            ((EList) eObject.eGet(getAvailableEReferences().get(BigraphMetaModelConstants.REFERENCE_BEDGES)))
                    .add(x.getInstance());
        });
        innerNames.forEach(x -> {
            ((EList) eObject.eGet(getAvailableEReferences().get(BigraphMetaModelConstants.REFERENCE_BINNERNAMES)))
                    .add(x.getInstance());
        });
        outerNames.forEach(x -> {
            ((EList) eObject.eGet(getAvailableEReferences().get(BigraphMetaModelConstants.REFERENCE_BOUTERNAMES)))
                    .add(x.getInstance());
        });
        return eObject;
    }

    //TODO: add method "fromFile/Model/": args are instance (EObject) and meta-model (EPackage)
    // we need to reconstruct the internal object structure
    // do also validation against the meta-model: EcoreSignature provides such information

    //DTO?
    public class InstanceParameter {
        private EPackage modelPackage;
        private S signature;
        private Set<BigraphEntity.RootEntity> roots;
        private Set<BigraphEntity.SiteEntity> sites;
        private Set<BigraphEntity.InnerName> innerNames;
        private Set<BigraphEntity.OuterName> outerNames;
        private Set<BigraphEntity.Edge> edges;
        private Set<BigraphEntity.NodeEntity> nodes;
        private EObject bBigraphObject;

        public InstanceParameter(EPackage loadedEPackage,
                                 EObject instanceModel,
                                 S signature,
                                 Map<Integer, BigraphEntity.RootEntity> availableRoots,
                                 Map<Integer, BigraphEntity.SiteEntity> availableSites,
                                 Map<String, BigraphEntity.NodeEntity> availableNodes,
                                 Map<String, BigraphEntity.InnerName> availableInnerNames,
                                 Map<String, BigraphEntity.OuterName> availableOuterNames,
                                 Map<String, BigraphEntity.Edge> availableEdges
        ) {
            this.modelPackage = loadedEPackage;
            this.signature = signature;
            this.roots = new LinkedHashSet<>(availableRoots.values());
            this.edges = new LinkedHashSet<>(availableEdges.values());
            this.sites = new LinkedHashSet<>(availableSites.values());
            this.outerNames = new LinkedHashSet<>(availableOuterNames.values());
            this.innerNames = new LinkedHashSet<>(availableInnerNames.values());
            this.nodes = new LinkedHashSet<>(availableNodes.values());
            if ((instanceModel) == null)
                this.bBigraphObject = createBBigraphContainer(this.roots, this.edges, this.innerNames, this.outerNames);
            else
                this.bBigraphObject = instanceModel;
        }

        public InstanceParameter(EPackage loadedEPackage,
                                 S signature,
                                 Map<Integer, BigraphEntity.RootEntity> availableRoots,
                                 Map<Integer, BigraphEntity.SiteEntity> availableSites,
                                 Map<String, BigraphEntity.NodeEntity> availableNodes,
                                 Map<String, BigraphEntity.InnerName> availableInnerNames,
                                 Map<String, BigraphEntity.OuterName> availableOuterNames,
                                 Map<String, BigraphEntity.Edge> availableEdges
        ) {
            this(loadedEPackage, null, signature, availableRoots, availableSites, availableNodes,
                    availableInnerNames, availableOuterNames, availableEdges);
        }

        public Signature<? extends Control<?, ?>> getSignature() {
            return signature;
        }

        public EObject getbBigraphObject() {
            return bBigraphObject;
        }

        public EPackage getModelPackage() {
            return modelPackage;
        }

        public Set<BigraphEntity.RootEntity> getRoots() {
            return roots;
        }

        public Set<BigraphEntity.SiteEntity> getSites() {
            return sites;
        }

        public Set<BigraphEntity.InnerName> getInner() {
            return innerNames;
        }

        public Set<BigraphEntity.OuterName> getOuter() {
            return outerNames;
        }

        public Set<BigraphEntity.Edge> getEdges() {
            return edges;
        }

        public Set<BigraphEntity.NodeEntity> getNodes() {
            return nodes;
        }

    }

    protected static Supplier<String> createNameSupplier(final String prefix) {
        return new Supplier<String>() {
            private int id = 0;

            @Override
            public String get() {
                return prefix + id++;
            }
        };
    }

    protected static Supplier<Integer> createIndexSupplier() {
        return new Supplier<Integer>() {
            private int id = 0;

            @Override
            public Integer get() {
                return id++;
            }
        };
    }
}
