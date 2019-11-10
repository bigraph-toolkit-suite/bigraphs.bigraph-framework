package de.tudresden.inf.st.bigraphs.core.impl.pure;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.EMetaModelData;
import de.tudresden.inf.st.bigraphs.core.exceptions.BigraphMetaModelLoadingFailedException;
import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidArityOfControlException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.*;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;
import de.tudresden.inf.st.bigraphs.core.impl.builder.MutableBuilder;
import de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.com.google.common.collect.Lists;
import org.eclipse.collections.api.block.function.primitive.IntToObjectFunction;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

/**
 * A concrete implementation of {@link BigraphBuilder} for <b>pure bigraphs</b>.
 * <p>
 * This bigraph builder offers a multi-scale approach for building bigraphs. From top to bottom.
 * Beginning with the outer and inner names. Create places and connect on the fly.
 *
 * @author Dominik Grzelak
 */
public class PureBigraphBuilder<S extends Signature> extends BigraphBuilderSupport<S> {
    private Logger logger = LoggerFactory.getLogger(PureBigraphBuilder.class);

    protected EPackage loadedEPackage;
    protected EObject loadedInstanceModel;
    private PureBigraph bigraph;

    private S signature;

    private boolean loadedFromFile = false;

    private Supplier<String> edgeNameSupplier = createNameSupplier(DEFAULT_EDGE_PREFIX);
    private Supplier<Integer> rootIdxSupplier = createIndexSupplier();
    private Supplier<Integer> siteIdxSupplier = createIndexSupplier();

    /**
     * Complete map of all EClasses of the bigraph to be constructed
     */
    private final Map<String, EClass> availableEClasses = new ConcurrentHashMap<>();
    /**
     * Subset of {@code availableEClasses} that only contains the controls of the signature
     */
    @Deprecated
    private final Map<String, EClass> controlMap = new ConcurrentHashMap<>();
    /**
     * Map of all ecore references such as bChild and bPrnt
     */
    private final Map<String, EReference> availableReferences = new ConcurrentHashMap<>();

    private Supplier<String> vertexNameSupplier;

    protected final Map<String, BigraphEntity.Edge> availableEdges = new ConcurrentHashMap<>();
    private final Map<String, BigraphEntity.OuterName> availableOuterNames = new ConcurrentHashMap<>();
    private final Map<String, BigraphEntity.InnerName> availableInnerNames = new ConcurrentHashMap<>();
    private final Map<Integer, BigraphEntity.RootEntity> availableRoots = new ConcurrentHashMap<>();
    private final Map<Integer, BigraphEntity.SiteEntity> availableSites = new ConcurrentHashMap<>();
    private final Map<String, BigraphEntity.NodeEntity> availableNodes = new ConcurrentHashMap<>();


    protected PureBigraphBuilder(S signature, EMetaModelData metaModelData) throws BigraphMetaModelLoadingFailedException {
        this.signature = signature;
        this.vertexNameSupplier = createNameSupplier(DEFAULT_VERTEX_PREFIX);
        this.bigraphicalSignatureAsTypeGraph(metaModelData);
    }

    protected PureBigraphBuilder(S signature, EPackage metaModel, EObject instanceModel) {
        this.signature = signature;
        this.vertexNameSupplier = createNameSupplier(DEFAULT_VERTEX_PREFIX);
        this.loadedEPackage = metaModel;
        this.initReferencesAndEClasses(false);
        this.loadedInstanceModel = instanceModel;
        // acquire all entities from the instance model and map them to our maps
        this.updateAllMaps();
        this.loadedFromFile = true;
    }

    protected PureBigraphBuilder(S signature, String metaModelFilePath, String instanceModelFilePath) throws BigraphMetaModelLoadingFailedException {
        this.signature = signature;
        this.vertexNameSupplier = createNameSupplier(DEFAULT_VERTEX_PREFIX);
        try {
            this.loadSignatureAsTypeGraph(metaModelFilePath);
            List<EObject> eObjects = BigraphArtifacts.loadBigraphInstanceModel(loadedEPackage, instanceModelFilePath);
            this.loadedInstanceModel = eObjects.get(0);
            // acquire all entities from the instance model and map them to our maps
            this.updateAllMaps();
            this.loadedFromFile = true;
        } catch (IOException e) {
            throw new BigraphMetaModelLoadingFailedException(e);
        }
    }

    protected PureBigraphBuilder(S signature) throws BigraphMetaModelLoadingFailedException {
        this(signature, new EMetaModelData.MetaModelDataBuilder()
                .setNsPrefix("bigraphMetaModel")
                .setName("SAMPLE")
                .create());
    }

    /**
     * Should not be directly called by the user. Instead use the {@link de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory}.
     *
     * @param instanceModelFilePath file path to the instance model
     * @return a configured builder with the bigraph instance loaded
     * @throws BigraphMetaModelLoadingFailedException when the model couldn't be loaded
     */
    public static <S extends Signature> PureBigraphBuilder<S> create(@NonNull S signature, String metaModelFilePath, String instanceModelFilePath) throws BigraphMetaModelLoadingFailedException {
        return new PureBigraphBuilder<>(signature, metaModelFilePath, instanceModelFilePath);
    }

    public static <S extends Signature> PureBigraphBuilder<S> create(@NonNull S signature, EPackage metaModel, EObject instanceModel) {
        return new PureBigraphBuilder<>(signature, metaModel, instanceModel);
    }

    /**
     * Should not be directly called by the user. Instead use the {@link de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory}.
     *
     * @param signature the signature for the builder
     * @param <S>       type of the signature
     * @return a pure bigraph builder with the given signature
     * @throws BigraphMetaModelLoadingFailedException when the model couldn't be loaded
     */
    public static <S extends Signature> PureBigraphBuilder<S> create(@NonNull S signature)
            throws BigraphMetaModelLoadingFailedException {
        return new PureBigraphBuilder<>(signature);
    }

    /**
     * Should not be directly called by the user. Instead use the {@link de.tudresden.inf.st.bigraphs.core.factory.AbstractBigraphFactory}.
     *
     * @param signature
     * @param metaModelData
     * @param <S>
     * @return
     * @throws BigraphMetaModelLoadingFailedException
     */
    public static <S extends Signature> PureBigraphBuilder<S> create(@NonNull S signature, EMetaModelData metaModelData)
            throws BigraphMetaModelLoadingFailedException {
        return new PureBigraphBuilder<>(signature, metaModelData);
    }

    public static <S extends Signature> MutableBuilder<S> newMutableBuilder(@NonNull S signature)
            throws BigraphMetaModelLoadingFailedException {
        return new MutableBuilder<>(signature);
    }

    @Override
    protected EPackage getLoadedEPackage() {
        return this.loadedEPackage;
    }

    @Override
    protected EObject getInstanceModel() {
        return loadedInstanceModel;
    }

    @Override
    protected Map<String, EClass> getAvailableEClasses() {
        return this.availableEClasses;
    }

    @Override
    protected Map<String, EReference> getAvailableEReferences() {
        return this.availableReferences;
    }

    /**
     * Method is used to update all necessary entity maps when a bigraph is loaded from the file system.
     */
    private void updateAllMaps() {
        loadedInstanceModel.eContents().stream()
                .forEach(each -> {
                    if (each.eClass().equals(availableEClasses.get(BigraphMetaModelConstants.CLASS_ROOT))) {
                        BigraphEntity.RootEntity rootEntity = BigraphEntity.create(each, BigraphEntity.RootEntity.class);
                        availableRoots.put(rootEntity.getIndex(), rootEntity);
                    }
                    if (each.eClass().equals(availableEClasses.get(BigraphMetaModelConstants.CLASS_EDGE))) {
                        BigraphEntity.Edge edge = BigraphEntity.create(each, BigraphEntity.Edge.class);
                        availableEdges.put(edge.getName(), edge);
                    }
                    if (each.eClass().equals(availableEClasses.get(BigraphMetaModelConstants.CLASS_OUTERNAME))) {
                        BigraphEntity.OuterName edge = BigraphEntity.create(each, BigraphEntity.OuterName.class);
                        availableOuterNames.put(edge.getName(), edge);
                    }
                    if (each.eClass().equals(availableEClasses.get(BigraphMetaModelConstants.CLASS_INNERNAME))) {
                        BigraphEntity.InnerName edge = BigraphEntity.create(each, BigraphEntity.InnerName.class);
                        availableInnerNames.put(edge.getName(), edge);
                    }
                    if (each.eClass().equals(availableEClasses.get(BigraphMetaModelConstants.CLASS_SITE))) {
                        BigraphEntity.SiteEntity edge = BigraphEntity.create(each, BigraphEntity.SiteEntity.class);
                        availableSites.put(edge.getIndex(), edge);
                    }
                });
        // find all node entities, starting from each found root in a recursive fashion
        availableRoots.values().forEach(eachRoot -> {
            EObject rootInstance = eachRoot.getInstance();
            recursiveUpdateNodeMap(rootInstance.eContents());
        });
    }

    /**
     * Used when the model was loaded from the file system. Then we also want to update our
     * {@code availableNodes} and {@code availableSites}. Roots are not considered here.
     *
     * @param objectList list of place nodes
     */
    private void recursiveUpdateNodeMap(List<EObject> objectList) {
        objectList.stream().forEach(each -> {
            if (each.eClass().getEAllSuperTypes().contains(availableEClasses.get(BigraphMetaModelConstants.CLASS_NODE))) {
                EAttribute nameAttr = EMFUtils.findAttribute(each.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
                String nameVal = (String) each.eGet(nameAttr);
                BigraphEntity.NodeEntity<? extends Control> node = BigraphEntity.createNode(each, signature.getControlByName(each.eClass().getName()));
                availableNodes.put(nameVal, node);
            } else if (each.eClass().equals(availableEClasses.get(BigraphMetaModelConstants.CLASS_SITE))) {
                BigraphEntity.SiteEntity siteEntity = BigraphEntity.create(each, BigraphEntity.SiteEntity.class);
                availableSites.put(siteEntity.getIndex(), siteEntity);
            }
            recursiveUpdateNodeMap(each.eContents());
        });
    }

    public Hierarchy createRoot() {
        BigraphEntity.RootEntity currentRoot = createRootEntity();
        return new Hierarchy(currentRoot);
    }

    /**
     * Creates a new independent hierarchy which can be added later.
     *
     * @param control the control of the parent for the new hierarchy
     * @return a new hierarchy for the current bigraph
     */
    public Hierarchy newHierarchy(Control control) {
        EObject childNode = createNodeOfEClass(control.getNamedType().stringValue(), control); // new Hierarchy()
        BigraphEntity.NodeEntity newParentNode = BigraphEntity.createNode(childNode, control);
        Hierarchy hierarchy = new Hierarchy(newParentNode);
        hierarchy.lastCreatedNode = (BigraphEntity.NodeEntity<Control>) newParentNode;
        return hierarchy;
    }

    public Hierarchy newHierarchy(String controlLabel) {
        return newHierarchy(signature.getControlByName(controlLabel));
    }

    /**
     * A bigraph consists of many node hierarchy. This inner class represents one of these.
     * These hierarchies can be built independently and added to the bigraph later.
     */
    public class Hierarchy implements NodeHierarchy {
        final Hierarchy parentHierarchy;
        final BigraphEntity<Control> parent;

        final Set<BigraphEntity<Control>> child = new LinkedHashSet<>();
        BigraphEntity.NodeEntity<Control> lastCreatedNode;


        private Hierarchy(BigraphEntity<Control> parent) {
            this(parent, null);
        }

        private Hierarchy(BigraphEntity<Control> parent, Hierarchy parentHierarchy) {
            this.parentHierarchy = parentHierarchy;
            this.parent = parent;
        }

        public BigraphEntity getParent() {
            return parent;
        }

        public Hierarchy goBack() {
            return Objects.isNull(this.parentHierarchy) ? this : this.parentHierarchy;
        }

        public Hierarchy top() {
            Hierarchy tmp = this.parentHierarchy;
            if (Objects.isNull(tmp)) return this;
            Hierarchy last = null;
            do {
                if (tmp != null) last = tmp;
                tmp = tmp.parentHierarchy;
            }
            while (tmp != null);
            return last;
        }

        //CHECK if something was created...
        public Hierarchy withNewHierarchy() throws ControlIsAtomicException {
            assertControlIsNonAtomic(getLastCreatedNode());
            return withNewHierarchyOn(getLastCreatedNode());
        }

        public Hierarchy addChild(Hierarchy thisOne) throws ControlIsAtomicException {
            assert thisOne.getParent() != null;
            assert BigraphEntityType.isNode(thisOne.getParent());
            assertControlIsNonAtomic(thisOne.getParent());
            // First, check if node is not already in the list or being put under a different name
            // the node itself and the name is searched
            // Ensures that the correct builder was used (because of the node name supplier)
            if (Objects.isNull(availableNodes.get(((BigraphEntity.NodeEntity) thisOne.getParent()).getName())) &&
                    availableNodes.values().stream().noneMatch(x -> x.equals(thisOne.getParent()))
            ) {
                addChildToParent(thisOne.getParent());
            }
            return this;
        }

        public Hierarchy addChild(String controlName) {
            return addChild(signature.getControlByName(controlName));
        }

        //this implies: added to a parent (see lastCreatedNode)
        public Hierarchy addChild(Control control) {
            assertControlIsNonAtomic(getParent());
            if (!checkSameSignature(control)) {
                logger.debug("Control {} couldn't be added because it isn't in the signature", control);
                return this;
            }
            final BigraphEntity.NodeEntity<Control> child = createChild(control);
            addChildToParent(child);
            return this;
        }

        public Hierarchy connectInnerNamesToNode(BigraphEntity.InnerName... innerNames) throws InvalidConnectionException, LinkTypeNotExistsException {
            PureBigraphBuilder.this.connectNodeToInnerNames(getLastCreatedNode(), innerNames);
            return this;
        }

        /**
         * Throws an exception if the given node has an atomic control
         *
         * @param bigraphEntity the bigraph node to check for atomicity
         */
        void assertControlIsNonAtomic(BigraphEntity bigraphEntity) {
            if (Objects.nonNull(bigraphEntity) &&
                    !BigraphEntityType.isRoot(bigraphEntity) &&
                    ControlKind.isAtomic(bigraphEntity.getControl())) {
                throw new ControlIsAtomicException();
            }
        }

        /**
         * Creates a new dynamic hierarchy where the parent is the current one.
         *
         * @param entity
         * @return
         */
        private Hierarchy withNewHierarchyOn(BigraphEntity.NodeEntity<Control> entity) {
            if (!child.contains(entity)) {
                throw new RuntimeException("withNewHierarchyOn(*) isn't possible - a child must first be created");
            }
            return new Hierarchy(entity, this);
        }

        /**
         * This method creates a bigraph node of control {@code control}.
         * <p>
         * Can create a dangling href when node is connected to an edge but not added to a parent.
         * If this is not intended the method {@link Hierarchy#addChildToParent(BigraphEntity)} must
         * be called.
         *
         * @param control the control
         * @return a freshly created object of control {@code control}
         */
        private BigraphEntity.NodeEntity<Control> createChild(Control control) {
            EObject childNode = createNodeOfEClass(control.getNamedType().stringValue(), control);
            BigraphEntity.NodeEntity<? extends Control> nodeEntity = BigraphEntity.createNode(childNode, control);
            updateLastCreatedNode((BigraphEntity.NodeEntity<Control>) nodeEntity);
            return getLastCreatedNode();
        }

        // support
        private boolean checkSameSignature(Control control) {
            return Lists.newArrayList(signature.getControls()).contains(control);
        }


        private void addChildToParent(final BigraphEntity node) {
            if (!BigraphEntityType.isPlaceType(node)) return;

            ((EList) parent.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_CHILD)))
                    .add(node.getInstance());
            child.add(node);


            if (BigraphEntityType.isNode(node)) {
                EAttribute name = EMFUtils.findAttribute(node.getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
                Object o = node.getInstance().eGet(name);
                availableNodes.put(String.valueOf(o), (BigraphEntity.NodeEntity) node);
            }
        }

        public Hierarchy addSite() {
            assertControlIsNonAtomic(getParent());
            final int ix = siteIdxSupplier.get();
            EObject eObject = createSiteOfEClass(ix);
            BigraphEntity.SiteEntity siteEntity = BigraphEntity.create(eObject, BigraphEntity.SiteEntity.class);
            addChildToParent(siteEntity);
            child.add(siteEntity);
            availableSites.put(ix, siteEntity);
            return this;
        }

        //TODO
        public final Hierarchy connectByEdge(final Hierarchy... hierarchies) {
            throw new RuntimeException("not implemented yet");
        }

        /**
         * Creates new child nodes and connects them with an edge
         *
         * @param controls the controls of the new nodes
         * @return the builder hierarchy
         * @throws InvalidArityOfControlException if nodes cannot be connected because of the control's arity
         */
//        @SafeVarargs
        public final Hierarchy connectByEdge(final Control... controls) throws InvalidArityOfControlException {
            List<BigraphEntity.NodeEntity<Control>> nodes = new ArrayList<>();
            for (int i = 0, n = controls.length; i < n; i++) {
                if (!checkSameSignature(controls[i])) return this; //TODO throw exception
            }
            for (int i = 0, n = controls.length; i < n; i++) {
                nodes.add(createChild(controls[i]));
            }

            PureBigraphBuilder.this.connectByEdge(nodes.toArray(new BigraphEntity.NodeEntity[nodes.size()]));
//            nodes.forEach(x -> child.add(x));
//            child.addAll(nodes.get(0));
//            child.addAll((Collection<? extends BigraphEntity<Control<?, ?>>>) nodes); // now its safe, after above
            nodes.forEach(this::addChildToParent);
            return this;
        }

        public final Hierarchy connectByEdge(final String... controls) throws InvalidArityOfControlException {
            return connectByEdge(
                    Arrays.stream(controls)
                            .map(x -> (Control<?, ?>) signature.getControlByName(x))
                            .toArray((IntToObjectFunction<Control[]>) Control[]::new)
            );
        }

        // To be used to call the enclosing class' methods for passing the nodes as arguments
        public Collection<BigraphEntity<Control>> nodes() {
            return child;
        }


        /**
         * Connect the previously created node to the given outer name.
         *
         * @param outerName the outer name to connect the node to
         * @return the current working node-hierarchy
         * @throws LinkTypeNotExistsException     the given outer name doesn't exist
         * @throws InvalidArityOfControlException link couldn't be established because of the control's arity
         */
        public Hierarchy connectNodeToOuterName(BigraphEntity.OuterName outerName) throws LinkTypeNotExistsException, InvalidConnectionException {
            PureBigraphBuilder.this.connectNodeToOuterName(getLastCreatedNode(), outerName);
            return this;
        }

        /**
         * Connect the previously created node to the given inner name.
         *
         * @param innerName the inner name to connect the node to
         * @return the current working node-hierarchy
         * @throws LinkTypeNotExistsException     the given inner name doesn't exist
         * @throws InvalidArityOfControlException link couldn't be established because of the control's arity
         */
        public Hierarchy connectNodeToInnerName(BigraphEntity.InnerName innerName) throws LinkTypeNotExistsException, InvalidConnectionException {
            PureBigraphBuilder.this.connectNodeToInnerName(getLastCreatedNode(), innerName);
            return this;
        }

        /**
         * Creates new nodes with the given controls and connects them to the inner name of the current bigraph.<br/>
         * Controls must not be <i>atomic</i>.
         *
         * @param innerName an existing inner name of the bigraph
         * @param controls  the nodes to be created under the given controls
         * @throws InvalidConnectionException link couldn't be established because of the control's arity
         * @throws LinkTypeNotExistsException the given inner name doesn't exist
         * @throws ControlIsAtomicException   if one of the given controls is atomic
         */
        public void connectNodesToInnerName(BigraphEntity.InnerName innerName, Control... controls) throws LinkTypeNotExistsException, InvalidConnectionException, ControlIsAtomicException {
            if (controls == null || controls.length == 0) return;
            for (Control each : controls) {
                connectNodeToInnerName(innerName, each);
            }
        }

        /**
         * Creates a new node with the given control and connects them to the inner name of the current bigraph.
         *
         * @param innerName an existing inner name of the bigraph
         * @param control   the node to be created under the given control
         * @throws InvalidConnectionException link couldn't be established because of the control's arity
         * @throws LinkTypeNotExistsException the given inner name doesn't exist
         */
        public void connectNodeToInnerName(BigraphEntity.InnerName innerName, Control control) throws InvalidConnectionException, LinkTypeNotExistsException, ControlIsAtomicException {
            addChild(control);
            connectNodeToInnerName(innerName);
        }


        public BigraphEntity.NodeEntity<Control> getLastCreatedNode() {
            return lastCreatedNode;
        }

        private void updateLastCreatedNode(BigraphEntity.NodeEntity<Control> eObject) {
            lastCreatedNode = eObject;
        }


    }

    protected EObject createSiteOfEClass(int index) {
        EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClasses.get(BigraphMetaModelConstants.CLASS_SITE));
        eObject.eSet(EMFUtils.findAttribute(eObject.eClass(), "index"), index);
        return eObject;
    }

    private BigraphEntity.RootEntity createRootEntity() {
        final int ix = rootIdxSupplier.get();
        EObject eObject = this.createRootOfEClass(ix);
        BigraphEntity.RootEntity rootEntity = BigraphEntity.create(eObject, BigraphEntity.RootEntity.class);
        availableRoots.put(ix, rootEntity);
        return rootEntity;
    }

    protected EObject createRootOfEClass(int index) {
        final EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClasses.get(BigraphMetaModelConstants.CLASS_ROOT));
        setIndexForEObject(eObject, index);
        return eObject;
    }

    protected EObject createInnerNameOfEClass(String name) {
        EObject bInnerName = loadedEPackage.getEFactoryInstance().create(availableEClasses.get(BigraphMetaModelConstants.CLASS_INNERNAME));
        bInnerName.eSet(EMFUtils.findAttribute(bInnerName.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME), name);
        return bInnerName;
    }

    protected EObject createOuterNameOfEClass(String name) {
        EObject bInnerName = loadedEPackage.getEFactoryInstance().create(availableEClasses.get(BigraphMetaModelConstants.CLASS_OUTERNAME));
        bInnerName.eSet(EMFUtils.findAttribute(bInnerName.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME), name);
        return bInnerName;
    }

    /**
     * Returns the same outer name if it already exists under the same {@code name}
     *
     * @param name the name for the outer name
     * @return a new outer name or an existing one with the same name
     */
    public BigraphEntity.OuterName createOuterName(final String name) {
        BigraphEntity.OuterName ecoreOuterName = availableOuterNames.get(name);
        if (ecoreOuterName == null) {
            final EObject outerNameObject = createOuterNameOfEClass(name);
            ecoreOuterName = BigraphEntity.create(outerNameObject, BigraphEntity.OuterName.class);
            availableOuterNames.put(name, ecoreOuterName);
        }
        return ecoreOuterName;
    }


    /**
     * Returns the same inner name if it already exists under the same {@code name}
     *
     * @param name the name for the inner name
     * @return a new inner name or an existing one with the same name
     */
    public BigraphEntity.InnerName createInnerName(String name) {
        BigraphEntity.InnerName ecoreInnerName = availableInnerNames.get(name);
        if (ecoreInnerName == null) {
            final EObject bInnerName = createInnerNameOfEClass(name);
            ecoreInnerName = BigraphEntity.create(bInnerName, BigraphEntity.InnerName.class);
            availableInnerNames.put(name, ecoreInnerName);
        }
        return ecoreInnerName;
    }

    /**
     * no checks are done here... use {@link PureBigraphBuilder#isConnectedWithLink(BigraphEntity.NodeEntity, EObject)}
     *
     * @param node
     * @param edge
     * @see PureBigraphBuilder#isConnectedWithLink(BigraphEntity.NodeEntity, EObject)
     */
    protected void connectToEdge(BigraphEntity.NodeEntity<Control> node, BigraphEntity.Edge edge) {
        EList<EObject> bPorts = (EList<EObject>) node.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_PORT));
        int index = bPorts.size();
        //create port with index
        EObject portObject = createPortWithIndex(index);
        EReference linkReference = availableReferences.get(BigraphMetaModelConstants.REFERENCE_LINK);
        portObject.eSet(linkReference, edge.getInstance()); //add edge reference for port
        bPorts.add(portObject);
    }

    protected void connectToLinkUsingIndex(BigraphEntity.NodeEntity<Control> node, BigraphEntity theLink, int customPortIndex) {
        EList<EObject> bPorts = (EList<EObject>) node.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_PORT));
        //create port with index
        EObject portObject = createPortWithIndex(customPortIndex);
        EReference linkReference = availableReferences.get(BigraphMetaModelConstants.REFERENCE_LINK);
        portObject.eSet(linkReference, theLink.getInstance()); //add edge reference for port
        bPorts.add(portObject);
    }

    protected BigraphEntity.Edge connectByEdge(final BigraphEntity.NodeEntity<Control>... nodes) throws InvalidArityOfControlException {
        //get arity and check number of connections
        for (BigraphEntity.NodeEntity<Control> each : nodes) {
            checkIfNodeIsConnectable(each);
        }
        BigraphEntity.Edge edge = createEdgeOfEClass();
        for (BigraphEntity.NodeEntity<Control> each : nodes) {
            connectToEdge(each, edge);

        }
        return edge;
    }


    /**
     * Connects an inner name to an outer name. The inner and outer name must be created with the same builder.
     * If they are already connected or doesn't belong to the builder than this builder is simply returned without
     * an error.
     *
     * @param innerName the inner name to be connected to the outer name
     * @param outerName the outer name
     * @return this builder instance
     * @throws InvalidConnectionException link couldn't be established because of already existing connection of the
     *                                    provided links
     */
    public PureBigraphBuilder<S> connectInnerToOuterName(BigraphEntity innerName, BigraphEntity outerName) throws InvalidConnectionException {
        // check if the inner name and outer name are coming from this builder
        if (availableInnerNames.values().stream().anyMatch(x -> x.equals(innerName)) &&
                availableOuterNames.values().stream().anyMatch(x -> x.equals(outerName))) {
            EObject edgeFromInnerName = getEdgeFromInnerName(innerName);
            if (edgeFromInnerName != null) throw new InnerNameConnectedToEdgeException();

            if (isInnerNameConnectedToOuterName(innerName, outerName)) return this;
            if (isInnerNameConnectedToAnyOuterName(innerName)) throw new InnerNameConnectedToOuterNameException();

            this.connectInnerToOuterName0(innerName, outerName);
        }
        return this;
    }

    /**
     * Ecore specific - only called by methods of this builder internally
     */
    protected void connectInnerToOuterName0(BigraphEntity innerName, BigraphEntity outerName) {
        EList<EObject> pointsOfOuterName = (EList<EObject>) outerName.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_POINT));
        pointsOfOuterName.add(innerName.getInstance());
    }

    /**
     * Link a node with an inner name by an edge
     *
     * @param node1     the node
     * @param innerName the inner name
     * @throws LinkTypeNotExistsException     if the inner name doesn't exists (i.e., doesn't belong from this builder
     * @throws InvalidArityOfControlException if the control cannot connect anything (e.g., is atomic, or no open ports left)
     * @throws InvalidConnectionException     if the inner name is already connected to an outer name
     */
    public void connectNodeToInnerName(BigraphEntity.NodeEntity<Control> node1, BigraphEntity.InnerName innerName) throws LinkTypeNotExistsException, InvalidConnectionException {
        //check if outername exists
        if (availableInnerNames.get(innerName.getName()) == null) {
            throw new InnerNameNotExistsException();
        }
        checkIfNodeIsConnectable(node1);
        if (isInnerNameConnectedToAnyOuterName(innerName)) throw new InnerNameConnectedToOuterNameException();

        //EDGE can connect many inner names: pick the specific edge of the given inner name
        //check if innerName has an edge (secure: inner name is not connected to an outer name here
        EObject linkOfEdge = (EObject) innerName.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_LINK));
        EClass eClassEdge = availableEClasses.get(BigraphMetaModelConstants.CLASS_EDGE);

        // check if node is connected with the possibly existing edge
        if (!isConnectedWithLink(node1, linkOfEdge)) {
            // did it failed because edge doesn't exists yet?
            // create an edge first
            BigraphEntity.Edge edgeOfEClass; // = createEdgeOfEClass();
            if (Objects.nonNull(linkOfEdge) && linkOfEdge.eClass().equals(eClassEdge)) { // an edge
                edgeOfEClass = createEdgeOfEClass(linkOfEdge);//.setInstance(linkOfEdge); // replace the instance of an edge
            } else {
                edgeOfEClass = createEdgeOfEClass();
            }
            // and add it to the inner name
            innerName.getInstance().eSet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_LINK), edgeOfEClass.getInstance());
            connectToEdge(node1, edgeOfEClass);
        }
    }

    /**
     * Link multiple inner names by an edge to one node
     * <p>
     * Old links of inner names (i.e., edges and outer names) are replaced by the freshly created edge.
     *
     * @param node1      the node
     * @param innerNames the inner name
     * @throws LinkTypeNotExistsException     if the inner name doesn't exists (i.e., doesn't belong from this builder
     * @throws InvalidArityOfControlException if the control cannot connect anything (e.g., is atomic, or no open ports left)
     * @throws InvalidConnectionException     if the inner name is already connected to an outer name
     */
    public void connectNodeToInnerNames(BigraphEntity.NodeEntity<Control> node1, BigraphEntity.InnerName... innerNames) throws LinkTypeNotExistsException, InvalidConnectionException {
        //check if outername exists
        if (innerNames.length == 0) return;
        checkIfNodeIsConnectable(node1);
        BigraphEntity.Edge newEdge = createEdgeOfEClass();
        connectToEdge(node1, newEdge);
        for (BigraphEntity.InnerName innerName : innerNames) {
            if (availableInnerNames.get(innerName.getName()) == null) {
                throw new InnerNameNotExistsException();
            }

            if (isInnerNameConnectedToAnyOuterName(innerName)) throw new InnerNameConnectedToOuterNameException();

            //EDGE can connect many inner names: pick the specific edge of the given inner name
            //check if innerName has an edge (secure: inner name is not connected to an outer name here
//            EObject linkOfInner = (EObject) innerName.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_LINK));

            // check if node is connected with the possibly existing edge
//            if (!isConnectedWithLink(node1, linkOfInner)) {
            //simply switch edge
//                if (Objects.nonNull(linkOfInner) && linkOfInner.eClass().equals(eClassEdge)) { // an edge
////                    newEdge.setInstance(linkOfInner); // replace the instance of an edge
//                    newEdge = createEdgeOfEClass(linkOfInner);
//                }
            // and add it to the inner name
            innerName.getInstance().eSet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_LINK), newEdge.getInstance());
//                if (!isConnectedWithLink(node1, newEdge)) {
//                    checkIfNodeIsConnectable(node1);
//                    connectToEdge(node1, newEdge);
//                }
//            }
        }
    }


    /**
     * Convenient method of {@link PureBigraphBuilder#connectInnerNames(BigraphEntity.InnerName, BigraphEntity.InnerName, boolean)}
     * which doesn't keep idle edges when inner names are already connected to a node. Meaning, that the last argument
     * defaults to {@code false}.
     *
     * @param ecoreInnerName1 an existing inner name
     * @param ecoreInnerName2 an existing inner name
     * @return the builder
     * @throws InvalidConnectionException
     */
    public PureBigraphBuilder<S> connectInnerNames(BigraphEntity.InnerName ecoreInnerName1, BigraphEntity.InnerName ecoreInnerName2) throws InvalidConnectionException {
        return connectInnerNames(ecoreInnerName1, ecoreInnerName2, false);
    }

    /**
     * Connects two existing inner names by an edge.
     * <p>
     * If they are already connected the class itself is returned.
     * <p>
     * The method will throw an exception if one of the given inner names
     * are already connected to an outer name.
     * <p>
     * If they are already connected to another edge from a node than the parameter {@code keepIdleEdges} decides whether
     * to keep the idle edges of the nodes when connecting the inner names or to remove them completely.
     *
     * @param ecoreInnerName1 an existing inner name
     * @param ecoreInnerName2 an existing inner name
     * @param keepIdleEdges   flag to decide whether idle edges should remain
     * @return the builder class
     * @throws InvalidConnectionException
     */
    public PureBigraphBuilder<S> connectInnerNames(BigraphEntity.InnerName ecoreInnerName1, BigraphEntity.InnerName ecoreInnerName2, boolean keepIdleEdges) throws InvalidConnectionException {
        assert ecoreInnerName1.getType().equals(BigraphEntityType.INNER_NAME);
        assert ecoreInnerName2.getType().equals(BigraphEntityType.INNER_NAME);
        // throw exception if an innername is already connected to an outername
        if (isInnerNameConnectedToAnyOuterName(ecoreInnerName1) || isInnerNameConnectedToAnyOuterName(ecoreInnerName2)) {
            throw new InnerNameConnectedToOuterNameException();
        }

        //are they already connected?
        if (areInnerNamesConnectedByEdge(ecoreInnerName1, ecoreInnerName2)) return this;

        // are they connected to any other edge? If so, should the "bond" be broken up?
        if (!keepIdleEdges) {
            disconnectNodesFromEdge(getEdgeFromInnerName(ecoreInnerName1));
            disconnectNodesFromEdge(getEdgeFromInnerName(ecoreInnerName2));
        } // otherwise idle edges will remain

        BigraphEntity.Edge edgeOfEClass = createEdgeOfEClass();
        EList<EObject> points = (EList<EObject>) edgeOfEClass.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_POINT));
        points.add(ecoreInnerName1.getInstance());
        points.add(ecoreInnerName2.getInstance());
        return this;
    }

    /**
     * Removes all inner names of an edge
     *
     * @param edge the edge who's inner names shall be removed
     */
    private void disconnectAllInnerNamesFromEdge(EObject edge) {
        if (isEdge(edge)) {
            EList<EObject> bPoints1 = (EList<EObject>) edge.eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_POINT));
            if (Objects.nonNull(bPoints1)) {
                Iterator<EObject> iterator = bPoints1.iterator();
                while (iterator.hasNext()) {
                    EObject x = iterator.next();
                    if (x.eClass().equals(availableEClasses.get(BigraphMetaModelConstants.CLASS_INNERNAME))) {
                        bPoints1.remove(x);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Removes the given inner name {@code objectToRemove} from {@code edge}.
     *
     * @param edge           the edge who's inner names shall be removed
     * @param objectToRemove the inner name to be removed from the edge
     */
    private void disconnectInnerNameFromEdge(EObject edge, EObject objectToRemove) {
        if (isEdge(edge)) {
            EList<EObject> bPoints1 = (EList<EObject>) edge.eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_POINT));
            if (Objects.nonNull(bPoints1)) {
                Iterator<EObject> iterator = bPoints1.iterator();
                while (iterator.hasNext()) {
                    EObject x = iterator.next();
                    if (x.equals(objectToRemove)) {
                        bPoints1.remove(x);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Method to disconnect all nodes from an edge. The corresponding node's port will also be removed. There are
     * no "idle" ports.
     *
     * @param edge the edge whose nodes should be removed
     */
    private void disconnectNodesFromEdge(EObject edge) {
        // only edge classes allowed ...
        if (Objects.isNull(edge) || !edge.eClass().equals(availableEClasses.get(BigraphMetaModelConstants.CLASS_EDGE)))
            return;

        EAttribute attribute = EMFUtils.findAttribute(edge.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
        Object name = edge.eGet(attribute);
        assert name != null;
        availableEdges.remove(String.valueOf(name));
        EList<EObject> bPoints1 = (EList<EObject>) edge.eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_POINT));
        if (Objects.nonNull(bPoints1)) {
            bPoints1.forEach(x -> {
                if (x.eClass().equals(availableEClasses.get(BigraphMetaModelConstants.CLASS_PORT))) {
                    EObject node = (EObject) x.eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_NODE));
                    EList<EObject> portList = (EList<EObject>) node.eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_PORT));
                    portList.remove(x);
                }
            });
        }
    }


    private boolean isInnerNameConnectedToAnyOuterName(BigraphEntity ecoreInnerName1) {
        EClass eClassOuterName = availableEClasses.get(BigraphMetaModelConstants.CLASS_OUTERNAME);
        EObject link1 = (EObject) ecoreInnerName1.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_LINK));
        return !Objects.isNull(link1) && link1.eClass().equals(eClassOuterName);
    }


    private boolean isInnerNameConnectedToOuterName(BigraphEntity ecoreInnerName1, BigraphEntity outerName) {
        EObject link1 = (EObject) ecoreInnerName1.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_LINK));
        return !Objects.isNull(link1) && link1.equals(outerName.getInstance());
    }

    /**
     * This methods will return the connected edge of an inner name, <b>if</b> available.
     *
     * @param innerName the inner name
     * @return return the edge connected to the given inner name, otherwise {@code null}
     */
    @Nullable
    private EObject getEdgeFromInnerName(BigraphEntity innerName) {
        EClass eClassEdge = availableEClasses.get(BigraphMetaModelConstants.CLASS_EDGE);
        EObject link1 = (EObject) innerName.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_LINK));
        if (Objects.isNull(link1) || !link1.eClass().equals(eClassEdge)) return null;
        return link1;
    }


    private boolean areInnerNamesConnectedByEdge(BigraphEntity.InnerName ecoreInnerName1, BigraphEntity.InnerName ecoreInnerName2) {
        EClass eClassInnerName = availableEClasses.get(BigraphMetaModelConstants.CLASS_INNERNAME);

        EObject link1 = getEdgeFromInnerName(ecoreInnerName1);
        EObject link2 = getEdgeFromInnerName(ecoreInnerName2);
        if (Objects.isNull(link1) || Objects.isNull(link2)) return false;

        //is an edge
        EList<EObject> bPoints1 = (EList<EObject>) link1.eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_POINT));
        EList<EObject> bPoints2 = (EList<EObject>) link2.eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_POINT));

        for (EObject eachPoint1 : bPoints1) {
            // look for possible inner name
            if (eachPoint1.eClass().equals(eClassInnerName) &&
                    ecoreInnerName2.getInstance().equals(eachPoint1)) {
                // reverse check...
                for (EObject eachPoint2 : bPoints2) {
                    if (eachPoint2.eClass().equals(eClassInnerName) &&
                            ecoreInnerName1.getInstance().equals(eachPoint2)) {
                        return true;
                    }
                }
            }
        }
        return false;

    }

    /**
     * Checks whether a node's ports can be connected to a link (i.e., an edge or an outer name which in turn also includes inner names).
     * <p>
     * The verification is based on the arity of the control and current connections made so far.
     *
     * @param node the node to check
     * @throws InvalidArityOfControlException if control is atomic or the current connections exceed the control's arity
     */
    public void checkIfNodeIsConnectable(BigraphEntity.NodeEntity<Control> node) throws InvalidArityOfControlException {
//        assert node.getType() ==
        EObject instance1 = node.getInstance();
//        EList<EObject> bPorts1 = (EList<EObject>) instance1.eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_PORT));
        EList<EObject> bPorts1 = (EList<EObject>) instance1.eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_PORT));
        Integer numberOfConnections = bPorts1.size();
        //check arity of control
        Number arityOfControl = node.getControl().getArity().getValue();
        Integer castedArityOfControl = numberOfConnections.getClass().cast(arityOfControl);
//        Integer value = (Integer) arity.getValue();
        castedArityOfControl = castedArityOfControl == null ? 0 : castedArityOfControl;
        if (castedArityOfControl == 0)
            throw new InvalidArityOfControlException();
        if (numberOfConnections.compareTo(castedArityOfControl) >= 0)
            throw new ToManyConnections(); // numberOfConnections >= castedArityOfControl
    }

    /**
     * Helper method
     *
     * @param node1
     * @param outerName
     * @throws LinkTypeNotExistsException
     * @throws InvalidArityOfControlException
     */
    protected void connectNodeToOuterName(BigraphEntity.NodeEntity<Control> node1, BigraphEntity.OuterName outerName) throws LinkTypeNotExistsException, InvalidArityOfControlException {
        assertOuterNameExists(outerName); //check if outername exists

        if (!isConnectedWithLink(node1, outerName.getInstance())) {
            //check arity of control
            checkIfNodeIsConnectable(node1);
            EObject instance1 = node1.getInstance();
            // connect procedure
            //Create ports
            EList<EObject> bPorts = (EList<EObject>) instance1.eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_PORT));
//            EList<EObject> bPorts = (EList<EObject>) instance1.eGet(factory.getBigraphBaseModelPackage().getBNode_BPorts());
            int index = bPorts.size(); // auf langer sicht andere methode finden, um den Index zu bekommen (unabhängig von der liste machen)
            EObject portObject = createPortWithIndex(index);
            bPorts.add(portObject); //.getInstance());
            portObject.eSet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_LINK), outerName.getInstance());
//            portObject.setBLink(outerName);
        }
    }

    /**
     * Helper method
     *
     * @param index
     * @return
     */
    protected EObject createPortWithIndex(final int index) {
        EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClasses.get(BigraphMetaModelConstants.CLASS_PORT));
        setIndexForEObject(eObject, index);
        return eObject;
    }

    /**
     * Helper method to set the index attribute of an {@link EObject} instance.
     *
     * @param eObject the instance
     * @param index   the value of the index to set
     */
    private void setIndexForEObject(EObject eObject, final int index) {
        eObject.eSet(EMFUtils.findAttribute(eObject.eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX), index);
    }

    /**
     * Helper method
     *
     * @param place1
     * @param aLink
     * @return
     */
    protected boolean isConnectedWithLink(BigraphEntity.NodeEntity<Control> place1, @Nullable EObject aLink) {
        if (Objects.isNull(aLink)) return false;
        EList<EObject> bPorts = (EList<EObject>) place1.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_PORT));
//        EList<EObject> bPorts = (EList<EObject>) place1.getInstance().eGet(factory.getBigraphBaseModelPackage().getBNode_BPorts());
//        EList<BPort> bPorts = place1.getBPorts();
        for (EObject bPort : bPorts) {
            //get link from port to a possible outername
            EObject link = (EObject) bPort.eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_LINK));
//            EObject link = (EObject) bPort.eGet(factory.getBigraphBaseModelPackage().getBPoint_BLink());
            if (link.eClass().equals(aLink.eClass())) {
                if (aLink.equals(link)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean areNodesConnected(BigraphEntity.NodeEntity<Control> place1, BigraphEntity.NodeEntity<Control> place2) {
        EClass eClassPort = availableEClasses.get(BigraphMetaModelConstants.CLASS_PORT);
        EList<EObject> bPorts = (EList<EObject>) place1.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_PORT));
//        EList<EObject> bPorts = (EList<EObject>) place1.getInstance().eGet(factory.getBigraphBaseModelPackage().getBNode_BPorts());
        for (EObject bPort : bPorts) {
            //get link from port to a possible outername
            EObject link = (EObject) bPort.eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_LINK));
//            EObject link = (EObject) bPort.eGet(factory.getBigraphBaseModelPackage().getBPoint_BLink());
            EList<EObject> bPortsRight = (EList<EObject>) link.eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_POINT));
//            EList<EObject> bPortsRight = (EList<EObject>) link.eGet(factory.getBigraphBaseModelPackage().getBLink_BPoints());
            for (EObject eachRight : bPortsRight) {
                if (eachRight.eClass().equals(eClassPort)) {
                    //Get reference from this port to the node
                    EObject node0 = (EObject) eachRight.eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_NODE));
//                    EObject node0 = (EObject) eachRight.eGet(factory.getBigraphBaseModelPackage().getBPort_BNode());
                    if (node0.equals(place2.getInstance())) return true;
                }
            }
        }
        return false;
    }

    protected EObject createNodeOfEClass(String name, @NonNull Control control) {
        return this.createNodeOfEClass(name, control, vertexNameSupplier.get());
    }

    protected EObject createNodeOfEClass(String name, @NonNull Control control, String nodeIdentifier) {
        EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClasses.get(name));
        EAttribute name1 = EMFUtils.findAttribute(eObject.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
        eObject.eSet(name1, nodeIdentifier);
        return eObject;
    }

    //BLEIBT - HELPER

    protected EObject createEdgeOfEClass0(String edgeName) {
        assert availableEClasses.get(BigraphMetaModelConstants.CLASS_EDGE) != null;
        EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClasses.get(BigraphMetaModelConstants.CLASS_EDGE));
        eObject.eSet(EMFUtils.findAttribute(eObject.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME), edgeName);
        return eObject;
    }

    /**
     * Creates an edge of EClass {@link de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.BEdge} and adds it to the
     * list of available edges {@code availableEdges}.
     *
     * @return the freshly created edge
     */
    protected BigraphEntity.Edge createEdgeOfEClass() {
        final String name = edgeNameSupplier.get();
        EObject eObject = createEdgeOfEClass0(name);
        return createEdgeOfEClass(eObject);
    }

    protected BigraphEntity.Edge createEdgeOfEClass(EObject eObject) {
        assert availableEClasses.get(BigraphMetaModelConstants.CLASS_EDGE) != null;
        BigraphEntity.Edge edge = BigraphEntity.create(eObject, BigraphEntity.Edge.class);
        EAttribute name = EMFUtils.findAttribute(eObject.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
        availableEdges.put((String) eObject.eGet(name), edge);
        return edge;
    }

    @SuppressWarnings("unchecked")
    public PureBigraph createBigraph() {
        if (Objects.isNull(bigraph)) {
            InstanceParameter meta;
            if (loadedFromFile && Objects.nonNull(loadedInstanceModel)) {
                meta = new InstanceParameter(loadedEPackage,
                        loadedInstanceModel,
                        signature, availableRoots, availableSites,
                        availableNodes, availableInnerNames, availableOuterNames, availableEdges);
            } else {
                meta = new InstanceParameter(loadedEPackage,
                        signature, availableRoots, availableSites,
                        availableNodes, availableInnerNames, availableOuterNames, availableEdges);
                loadedInstanceModel = meta.getbBigraphObject();
            }
            bigraph = new PureBigraph(meta);
        }
        return bigraph;
    }

    /**
     * Converts the current bigraph to a ground bigraph (i.e., no sites an no inner names).
     */
    public void makeGround() {
        // first, deal with the sites
        Iterator<Map.Entry<Integer, BigraphEntity.SiteEntity>> iterator = availableSites.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, BigraphEntity.SiteEntity> next = iterator.next();
            BigraphEntity.SiteEntity eachSite = next.getValue();

            // get parent
            EObject prnt = (EObject) eachSite.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_PARENT));
            assert prnt != null;
            // get all child
            EList<EObject> childs = (EList<EObject>) prnt.eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_CHILD));
            boolean remove = childs.remove(eachSite.getInstance());
            assert remove;
            iterator.remove();
        }

        // lastly, deal with the inner names
        closeAllInnerNames();
    }


    /**
     * Closes all inner names and doesn't keep idle edges and idle inner names.
     */
    public synchronized PureBigraphBuilder<S> closeAllInnerNames() {
        Iterator<Map.Entry<String, BigraphEntity.InnerName>> iterator = availableInnerNames.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, BigraphEntity.InnerName> next = iterator.next();
            try {
                // the inner name is not yet removed in the following method
                closeInnerName(next.getValue(), true);
                iterator.remove();
            } catch (LinkTypeNotExistsException ignored) { /* technically, this shouldn't happen*/
                throw new RuntimeException("This shouldn't happen. Please check the inner names map.");
            }
        }
        return this;
    }

    /**
     * Convenient method for {@link PureBigraphBuilder#closeInnerName(BigraphEntity.InnerName, boolean)}.
     * The last argument defaults to {@code false}.
     *
     * @param innerName an inner name
     * @throws LinkTypeNotExistsException
     */
    public void closeInnerName(BigraphEntity.InnerName innerName) throws LinkTypeNotExistsException {
        closeInnerName(innerName, false);
    }

    public void closeInnerNames(BigraphEntity.InnerName... innerName) throws LinkTypeNotExistsException {
        Arrays.stream(innerName).forEach(x -> {
            try {
                closeInnerName(x, false);
            } catch (LinkTypeNotExistsException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void closeInnerName(BigraphEntity.InnerName innerName, boolean keepIdleName) throws LinkTypeNotExistsException {
        if (availableInnerNames.get(innerName.getName()) == null ||
                !availableInnerNames.get(innerName.getName()).equals(innerName))
            throw new InnerNameNotExistsException();
        EObject blink = (EObject) innerName.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_LINK));
        // is connected to an outer name
        if (isOuterName(blink)) {
            EList<EObject> pointsOfLink = (EList<EObject>) blink.eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_POINT));
            for (int i = 0, n = pointsOfLink.size(); i < n; i++) {
                if (pointsOfLink.get(i).equals(innerName.getInstance())) {
                    pointsOfLink.remove(innerName.getInstance());
                    break;
                }
            }
        } else { // is connected to an edge
            // remove the inner name from connected edge but leave the edge intact
            disconnectInnerNameFromEdge(blink, innerName.getInstance());
        }

        if (!keepIdleName)
            availableInnerNames.remove(innerName.getName());
    }

    /**
     * Closes all outer names. See {@link PureBigraphBuilder#closeOuterName(BigraphEntity.OuterName)}.
     *
     * @throws TypeNotExistsException if the outer name doesn't exists
     */
    public synchronized PureBigraphBuilder<S> closeAllOuterNames() {
        Iterator<Map.Entry<String, BigraphEntity.OuterName>> iterator = availableOuterNames.entrySet().iterator();
        // don't replace the while loop with the iterator, because the outer name is removed too
        while (iterator.hasNext()) {
            try {
                closeOuterName(iterator.next().getValue(), false);
            } catch (TypeNotExistsException ignored) {
            }
        }
        return this;
    }

    /**
     * Closes the outer name by removing all connections and the outer name itself. This method doesn't allow
     * idle names.
     * Connected nodes over the same name will not be connected by an edge.
     *
     * @param outerName the outer name to remove
     * @throws TypeNotExistsException
     */
    public void closeOuterName(BigraphEntity.OuterName outerName) throws TypeNotExistsException {
        this.closeOuterName(outerName, false);
    }

    private void assertOuterNameExists(BigraphEntity.OuterName outername) throws OuterNameNotExistsException {
        if (availableOuterNames.get(outername.getName()) == null ||
                !availableOuterNames.get(outername.getName()).equals(outername)) {
            throw new OuterNameNotExistsException();
        }
    }

    /**
     * If an inner name is connected to an outer name, the inner name will remain (i.e., not be removed).
     * The port of a connected node is removed.
     *
     * @param outername    the outer name to remove
     * @param keepIdleName {@code true}, if the outer name shall be kept as an idle name; and {@code false} to remove
     * @throws TypeNotExistsException
     */
    public void closeOuterName(BigraphEntity.OuterName outername, boolean keepIdleName) throws TypeNotExistsException {
        assertOuterNameExists(outername);

        EList<EObject> bPoints = (EList<EObject>) outername.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_POINT));
        for (int i = bPoints.size() - 1; i >= 0; i--) {
            // the following is not really necessary
            if (bPoints.get(i).eClass().equals(availableEClasses.get(BigraphMetaModelConstants.CLASS_PORT))) {
                EObject port = bPoints.get(i);
                // get the corresponding node and remove the port
                EObject node = (EObject) port.eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_NODE));
                EList<EObject> portList = (EList<EObject>) node.eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_PORT));
                portList.remove(port);
                updatePortIndices(node);
            }
            bPoints.remove(i);
        }

        if (!keepIdleName)
            availableOuterNames.remove(outername.getName());
    }

    private void updatePortIndices(EObject node) {
        if (!node.eClass().equals(availableEClasses.get(BigraphMetaModelConstants.CLASS_NODE))
                && !node.eClass().getESuperTypes().contains(availableEClasses.get(BigraphMetaModelConstants.CLASS_NODE)))
            return;
        EList<EObject> portList = (EList<EObject>) node.eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_PORT));
        for (int i = portList.size() - 1; i >= 0; i--) {
            EObject eachPort = portList.get(i);
            setIndexForEObject(eachPort, i);
        }
    }

    public void loadSignatureAsTypeGraph(String metaModelFilePath) {
        try {
            loadedEPackage = BigraphArtifacts.loadBigraphMetaModel(metaModelFilePath);
        } catch (IOException e) {
            throw new BigraphMetaModelLoadingFailedException(e);
        }
        initReferencesAndEClasses(false);
    }

    private void initReferencesAndEClasses(boolean createNewNodesForMetaModel) {
        Iterable<Control<?, ?>> controls = signature.getControls();
        StreamSupport.stream(controls.spliterator(), false)
                .forEach(x -> {
                    EClass entityClass = (EClass) loadedEPackage.getEClassifier(BigraphMetaModelConstants.CLASS_NODE);
                    String s = x.getNamedType().stringValue();
                    if (createNewNodesForMetaModel) {
                        EClass newControlClass = EMFUtils.createEClass(s);
                        EMFUtils.addSuperType(newControlClass, loadedEPackage, entityClass.getName());
                        loadedEPackage.getEClassifiers().add(newControlClass);
                    }
                    controlMap.put(s, entityClass);
                });
        Set<EReference> allrefs = new HashSet<>();
        EList<EObject> eObjects = loadedEPackage.eContents();
        for (EObject each : eObjects) {
            availableEClasses.put(((EClassImpl) each).getName(), (EClassImpl) each);
            allrefs.addAll(EMFUtils.findAllReferences((EClass) each));
        }
        allrefs.forEach(x -> availableReferences.put(x.getName(), x));
    }

    private void bigraphicalSignatureAsTypeGraph(EMetaModelData modelData) throws BigraphMetaModelLoadingFailedException {
        try {
            loadedEPackage = BigraphArtifacts.loadInternalBigraphMetaModel();
            loadedEPackage.setNsPrefix(modelData.getNsPrefix());
            loadedEPackage.setNsURI(modelData.getNsUri());
            loadedEPackage.setName(modelData.getName());
        } catch (IOException e) {
            throw new BigraphMetaModelLoadingFailedException(e);
        }
        initReferencesAndEClasses(true);
    }

    /**
     * Clears all generated intermediate results of the bigraph's current construction inside the builder.
     * <br/>
     * <strong>Should not be called. Is made available for the {@link MutableBuilder}</strong>
     */
    protected void clearIntermediateResults() {
        this.availableInnerNames.clear();
        this.availableEdges.clear();
        this.availableOuterNames.clear();
        this.availableSites.clear();
        this.availableNodes.clear();
        this.availableRoots.clear();
        this.edgeNameSupplier = createNameSupplier(DEFAULT_EDGE_PREFIX);
        this.vertexNameSupplier = createNameSupplier(DEFAULT_VERTEX_PREFIX);
        this.rootIdxSupplier = createIndexSupplier();
        this.siteIdxSupplier = createIndexSupplier();
    }
}
