package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Supporting base class for concrete bigraph builder implementations.
 * <p>
 * Provides some generic methods for building bigraphs.
 *
 * @author Dominik Grzelak
 */
public abstract class BigraphBuilderSupport<S extends Signature> implements BigraphBuilder<S> {

//    public abstract <B extends Bigraph> B createInstance(S signature, EMetaModelData metaModelData);

    protected abstract EPackage getLoadedEPackage();

    protected abstract Map<String, EClass> getAvailableEClasses();

    protected abstract Map<String, EReference> getAvailableEReferences();

    protected EObject createBBigraphContainer(Collection<BigraphEntity.RootEntity> roots,
                                              Collection<BigraphEntity.Edge> edges,
                                              Collection<BigraphEntity.InnerName> innerNames,
                                              Collection<BigraphEntity.OuterName> outerNames) {
//        assert availableEClasses.get(BigraphMetaModelConstants.CLASS_BIGRAPH) != null;
        EObject eObject = getLoadedEPackage().getEFactoryInstance().create(getAvailableEClasses().get(BigraphMetaModelConstants.CLASS_BIGRAPH));

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
            this.bBigraphObject = createBBigraphContainer(this.roots, this.edges, this.innerNames, this.outerNames);
        }

        public Signature<Control<?, ?>> getSignature() {
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

        public Set<BigraphEntity.InnerName> getInnerNames() {
            return innerNames;
        }

        public Set<BigraphEntity.OuterName> getOuterNames() {
            return outerNames;
        }

        public Set<BigraphEntity.Edge> getEdges() {
            return edges;
        }

        public Set<BigraphEntity.NodeEntity> getNodes() {
            return nodes;
        }

    }
}