package de.tudresden.inf.st.bigraphs.core.impl.builder;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.exceptions.*;
import de.tudresden.inf.st.bigraphs.core.exceptions.building.*;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.DynamicEcoreBigraph;
import de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.com.google.common.collect.Lists;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.impl.EClassImpl;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

//TODO think about "ownership". Wenn z.B. BLink zurückgegeben wird, darf es nur wieder in den gleichen Bigraph aber nicht
//in andere (cf. jlibbig)

//TODO guided+nested builder: https://blog.crisp.se/2013/10/09/perlundholm/another-builder-pattern-for-java
//TODO: vereinfacht das bauen von ecore model - more fluent
//inside a root: offer only nodes of control types and sites to add at every level.
//can go into a node to add hierarchy (starthierarchy()/endHierarchy())
//offer finish method: than we are at the root level again to start from the beginning.
//-> builder here is kind of the multi-scale modeling approach from gassara.

/**
 * This bigraph builder offers a multi-scale approach for building bigraphs. From top to bottom.
 * Beginning with the outer and inner names. Create places and connect on the fly.
 * <p>
 * // * @param <C> the type of the control
 */
//<C extends Control<? extends NamedType<?>, ? extends FiniteOrdinal<?>>, S extends Signature<C>>
public class BigraphBuilder<S extends Signature> {

    protected EPackage loadedEPackage;

    private AtomicBoolean isCompleted = new AtomicBoolean(false);
    private S signature;

    /**
     * Complete map of all EClasses of the bigraph to be constructed
     */
    private final HashMap<String, EClass> availableEClasses = new HashMap<>();
    /**
     * Subset of {@code availableEClasses} that only contains the controls of the signature
     */
    private final HashMap<String, EClass> controlMap = new HashMap<>();
    /**
     * Map of all ecore references such as bChild and bPrnt
     */
    private final HashMap<String, EReference> availableReferences = new HashMap<>();

    // Create the VertexFactory so the generator can createNodeOfEClass vertices
    private Supplier<String> vertexNameSupplier;
//    private Supplier<String> rootNameSupplier;

    private final HashMap<String, BigraphEntity.Edge> availableEdges = new HashMap<>();
    private final HashMap<String, BigraphEntity.OuterName> availableOuterNames = new HashMap<>();
    private final HashMap<String, BigraphEntity.InnerName> availableInnerNames = new HashMap<>();
    private final HashMap<Integer, BigraphEntity.RootEntity> availableRoots = new HashMap<>();
    private final HashMap<Integer, BigraphEntity.SiteEntity> availableSites = new HashMap<>();
    private final HashMap<String, BigraphEntity.NodeEntity> availableNodes = new HashMap<>();

    //TODO new constructor with signature and namespace

    protected BigraphBuilder(S signature, Supplier<String> nodeNameSupplier) throws BigraphMetaModelLoadingFailedException {
        this.signature = signature;
        this.bigraphicalSignatureAsTypeGraph("SAMPLE"); //TODO provide URI (namespace) here
        this.vertexNameSupplier = nodeNameSupplier;
    }

    /**
     * (!) useSignature must be called afterwards
     */
    @Deprecated
    public BigraphBuilder() {
        this.vertexNameSupplier = new Supplier<String>() {
            private int id = 0;

            @Override
            public String get() {
                return "v" + id++;
            }
        };
    }

    @Deprecated
    public BigraphBuilder<S> useSignature(Signature signature) {
        this.signature = (S) signature;
        this.bigraphicalSignatureAsTypeGraph("SAMPLE"); //TODO
        return this;
    }

    //<C extends Control<?, ?>> Signature<C>
    public static <S extends Signature> BigraphBuilder<S> start(@NonNull S signature)
            throws BigraphMetaModelLoadingFailedException {
        return BigraphBuilder.start(signature,
                new Supplier<String>() {
                    private int id = 0;

                    @Override
                    public String get() {
                        return "v" + id++;
                    }
                });
    }

    public static <S extends Signature> MutableBuilder<S> newMutableBuilder(@NonNull S signature)
            throws BigraphMetaModelLoadingFailedException {
        return new MutableBuilder<>(signature, new Supplier<String>() {
            private int id = 0;

            @Override
            public String get() {
                return "v" + id++;
            }
        });
    }

    public static <S extends Signature> BigraphBuilder<S> start(@NonNull S signature,
                                                                Supplier<String> nodeNameSupplier)
            throws BigraphMetaModelLoadingFailedException {
        return new BigraphBuilder<>(signature, nodeNameSupplier);
    }


    //MOVE/change return type
    public Hierarchy createRoot() {
        BigraphEntity.RootEntity currentRoot = createRootEntity();
//        currentNode = currentRoot;
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
        BigraphEntity.NodeEntity<? extends Control> nodeEntity = BigraphEntity.createNode(childNode, control);
        Hierarchy hierarchy = new Hierarchy(nodeEntity);
        hierarchy.lastCreatedNode = (BigraphEntity.NodeEntity<Control<?, ?>>) nodeEntity;
        return hierarchy;
    }

    /**
     * A bigraph consists of many node hierarchy. This inner class represents one of these.
     * These hierarchies can be built independently and added to the bigraph later.
     */
    public class Hierarchy {
        final Hierarchy parentHierarchy;
        final BigraphEntity<Control<?, ?>> parent;

        final Set<BigraphEntity<Control<?, ?>>> child = new LinkedHashSet<>();
        BigraphEntity.NodeEntity<Control<?, ?>> lastCreatedNode;


        private Hierarchy(BigraphEntity<Control<?, ?>> parent) {
            this(parent, null);
        }

        private Hierarchy(BigraphEntity<Control<?, ?>> parent, Hierarchy parentHierarchy) {
            this.parentHierarchy = parentHierarchy;
            this.parent = parent;
        }

        public BigraphEntity getParent() {
            return parent;
        }

        public Hierarchy goBack() {
            return Objects.isNull(this.parentHierarchy) ? this : this.parentHierarchy;
        }

        /**
         * Creates a new hierarchy builder where the last created node is the parent of this new hierarchy.
         * <p>
         * One can go to the previous hierarchy by calling the {@link Hierarchy#goBack()} method.
         *
         * @return the new hierarchy
         */
        //CHECK if something was created...
        public Hierarchy withNewHierarchy() {
            return withNewHierarchyOn(getLastCreatedNode());
        }

        public Hierarchy addHierarchyToParent(Hierarchy thisOne) {
            assert thisOne.getParent() != null;
            addChildToParent(thisOne.getParent());
            return this;
        }


        /**
         * Creates a new dynamic hierarchy where the parent is the current one.
         *
         * @param entity
         * @return
         */
        private Hierarchy withNewHierarchyOn(BigraphEntity.NodeEntity<Control<?, ?>> entity) {
            if (!child.contains(entity)) {
                throw new RuntimeException("Not possible - A child must first be created");
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
        private BigraphEntity.NodeEntity<Control<?, ?>> createChild(Control<?, ?> control) {
            EObject childNode = createNodeOfEClass(control.getNamedType().stringValue(), control);
            BigraphEntity.NodeEntity<? extends Control> nodeEntity = BigraphEntity.createNode(childNode, control);
            updateLastCreatedNode((BigraphEntity.NodeEntity<Control<?, ?>>) nodeEntity);
            return getLastCreatedNode();
        }

        //BLEIBT - HELPER
        private boolean checkSameSignature(Control<?, ?> control) {
            return iterableToList(signature.getControls()).contains(control);
        }

        //MOVE
        //this implies: added to a parent (see lastCreatedNode)
        public Hierarchy addChild(Control<?, ?> control) {
            if (!checkSameSignature(control)) {
                //TODO debug output or something ...
                return this;
            }
            final BigraphEntity.NodeEntity<Control<?, ?>> child = createChild(control);
//            if (addToParent)
            addChildToParent(child);
//            ((EList) parent.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_CHILD))).add(child.getInstance());

//            EObject childNode = createNodeOfEClass(control.getNamedType().stringValue(), control);
//            updateLastCreatedNode(new BigraphEntity(child, control, BigraphEntityType.NODE));
            return this;
        }

        private void addChildToParent(final BigraphEntity node) {
            ((EList) parent.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_CHILD))).add(node.getInstance());
            child.add(node);
            if (node.getType().equals(BigraphEntityType.NODE)) {
                EAttribute name = EMFUtils.findAttribute(node.getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
                Object o = node.getInstance().eGet(name);
                availableNodes.put(String.valueOf(o), (BigraphEntity.NodeEntity) node);
            }
        }

        //MOVE, change return type
        public Hierarchy addSite() {
            final int ix = siteIdxSupplier.get();
            EObject eObject = createSiteOfEClass(ix);
//            EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClasses.get(BigraphMetaModelConstants.CLASS_SITE));
//            eObject.eSet(EMFUtils.findAttribute(eObject.eClass(), "index"), ix);
            BigraphEntity.SiteEntity siteEntity = BigraphEntity.create(eObject, BigraphEntity.SiteEntity.class);
//            ((EList) parent.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_CHILD))).add(eObject);
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
        public final Hierarchy connectByEdge(final Control<?, ?>... controls) throws InvalidArityOfControlException {
            List<BigraphEntity.NodeEntity<Control<?, ?>>> nodes = new ArrayList<>();
            for (int i = 0, n = controls.length; i < n; i++) {
                if (!checkSameSignature(controls[i])) return this; //TODO throw exception
            }
            for (int i = 0, n = controls.length; i < n; i++) {
                nodes.add(createChild(controls[i]));
            }

            BigraphBuilder.this.connectByEdge(nodes.toArray(new BigraphEntity.NodeEntity[nodes.size()]));
//            nodes.forEach(x -> child.add(x));
//            child.addAll(nodes.get(0));
//            child.addAll((Collection<? extends BigraphEntity<Control<?, ?>>>) nodes); // now its safe, after above
            nodes.forEach(this::addChildToParent);
            return this;
        }

        //To be used to call toe enclosing class' methods for passing the nodes as arguments
        public Collection<BigraphEntity<Control<?, ?>>> nodes() {
            return child;
        }


        public Hierarchy connectNodeToOuterName(BigraphEntity.OuterName outerName) throws LinkTypeNotExistsException, InvalidArityOfControlException {
            BigraphBuilder.this.connectNodeToOuterName(getLastCreatedNode(), outerName);
            return this;
        }

        //TODO
        public void connectNodesToInnerName(BigraphEntity.InnerName innerName, Control<?, ?>... controls) {
            throw new RuntimeException("not implemented yet"); //see below
        }

        public void connectNodeToInnerName(Control<?, ?> control, BigraphEntity.InnerName innerName) throws InvalidConnectionException, LinkTypeNotExistsException {
            addChild(control);
            connectNodeToInnerName(innerName);
        }

        public Hierarchy connectNodeToInnerName(BigraphEntity.InnerName innerName) throws LinkTypeNotExistsException, InvalidArityOfControlException, InvalidConnectionException {
            BigraphBuilder.this.connectNodeToInnerName(getLastCreatedNode(), innerName);
            return this;
        }

        public BigraphEntity.NodeEntity<Control<?, ?>> getLastCreatedNode() {
            return lastCreatedNode;
        }

        private void updateLastCreatedNode(BigraphEntity.NodeEntity<Control<?, ?>> eObject) {
            lastCreatedNode = eObject;
        }


    }

    protected EObject createSiteOfEClass(int index) {
        EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClasses.get(BigraphMetaModelConstants.CLASS_SITE));
        eObject.eSet(EMFUtils.findAttribute(eObject.eClass(), "index"), index);
        return eObject;
    }


    //BLEIBT
    private BigraphEntity.RootEntity createRootEntity() {
        final int ix = rootIdxSupplier.get();
        EObject eObject = this.createRootOfEClass(ix);
//        EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClasses.get(BigraphMetaModelConstants.CLASS_ROOT));
//        eObject.eSet(EMFUtils.findAttribute(eObject.eClass(), "index"), ix);//TODO über instance setzen
        BigraphEntity.RootEntity rootEntity = BigraphEntity.create(eObject, BigraphEntity.RootEntity.class);
        availableRoots.put(ix, rootEntity);
        return rootEntity;
    }

    protected EObject createRootOfEClass(int index) {
        EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClasses.get(BigraphMetaModelConstants.CLASS_ROOT));
        eObject.eSet(EMFUtils.findAttribute(eObject.eClass(), "index"), index);
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

    //BLEIBT

    /**
     * Returns the same outer name if it already exists under the same {@code name}
     *
     * @param name the name for the outer name
     * @return a new outer name or an existing one with the same name
     */
    public BigraphEntity.OuterName createOuterName(String name) {
        BigraphEntity.OuterName ecoreOuterName = availableOuterNames.get(name);
        if (ecoreOuterName == null) {
            EObject outername = loadedEPackage.getEFactoryInstance().create(availableEClasses.get(BigraphMetaModelConstants.CLASS_OUTERNAME));
            outername.eSet(EMFUtils.findAttribute(outername.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME), name);//TODO setName via outername instance
            ecoreOuterName = BigraphEntity.create(outername, BigraphEntity.OuterName.class);
            availableOuterNames.put(name, ecoreOuterName);
        }
        return ecoreOuterName;
    }

    //BLEIBT

    /**
     * Returns the same inner name if it already exists under the same {@code name}
     *
     * @param name the name for the inner name
     * @return a new inner name or an existing one with the same name
     */
    public BigraphEntity.InnerName createInnerName(String name) {
//        BigraphEntity.InnerName ecoreInnerName = new BigraphEntity.InnerName(availableInnerNames.get(name));
        BigraphEntity.InnerName ecoreInnerName = availableInnerNames.get(name); // = BigraphEntity.create(availableInnerNames.get(name), BigraphEntity.InnerName.class);
        if (ecoreInnerName == null) {
            EObject bInnerName = createInnerNameOfEClass(name);
//            EObject bInnerName = loadedEPackage.getEFactoryInstance().create(availableEClasses.get(BigraphMetaModelConstants.CLASS_INNERNAME));
//            bInnerName.eSet(EMFUtils.findAttribute(bInnerName.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME), name);
            try {
                ecoreInnerName = BigraphEntity.create(bInnerName, BigraphEntity.InnerName.class);
                ecoreInnerName.setInstance(bInnerName);
                availableInnerNames.put(name, ecoreInnerName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ecoreInnerName;
    }

    //BLEIBT
    //TODO change return type

    /**
     * no checks are done here... use {@link BigraphBuilder#isConnectedWithLink(de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity.NodeEntity, EObject)}
     *
     * @param node
     * @param edge
     * @see BigraphBuilder#isConnectedWithLink(de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity.NodeEntity, EObject)
     */
    protected void connectToEdge(BigraphEntity.NodeEntity<Control<?, ?>> node, BigraphEntity.Edge edge) {
//        EList<EObject> bPorts = (EList<EObject>) node.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_PORT));
        EList<EObject> bPorts = (EList<EObject>) node.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_PORT));
        Integer index = bPorts.size();
        //create port with index
        EObject portObject = createPortWithIndex(index);
        EReference linkReference = availableReferences.get(BigraphMetaModelConstants.REFERENCE_LINK);
        portObject.eSet(linkReference, edge.getInstance()); //add edge reference for port
        bPorts.add(portObject);
    }

    protected void connectToEdgeUsingIndex(BigraphEntity.NodeEntity<Control<?, ?>> node, BigraphEntity.Edge edge, int customPortIndex) {
//        EList<EObject> bPorts = (EList<EObject>) node.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_PORT));
        EList<EObject> bPorts = (EList<EObject>) node.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_PORT));
//        Integer index = bPorts.size();
        //create port with index
        EObject portObject = createPortWithIndex(customPortIndex);
        EReference linkReference = availableReferences.get(BigraphMetaModelConstants.REFERENCE_LINK);
        portObject.eSet(linkReference, edge.getInstance()); //add edge reference for port
        bPorts.add(portObject);
    }

    protected BigraphEntity.Edge connectByEdge(final BigraphEntity.NodeEntity<Control<?, ?>>... nodes) throws InvalidArityOfControlException {
        //get arity and check number of connections
        for (BigraphEntity.NodeEntity<Control<?, ?>> each : nodes) {
            checkIfNodeIsConnectable(each);
        }
        BigraphEntity.Edge edge = createEdgeOfEClass();
        for (BigraphEntity.NodeEntity<Control<?, ?>> each : nodes) {
            connectToEdge(each, edge);

        }
        return edge;
    }

    //BLEIBT
    //TODO change return type
    public void connectInnerToOuterName(BigraphEntity innerName, BigraphEntity outerName) throws InvalidConnectionException {
        //TODO: check if from the same set: the "owner" problem

        EObject edgeFromInnerName = getEdgeFromInnerName(innerName);
        if (edgeFromInnerName != null) throw new InnerNameConnectedToEdgeException();

        if (isInnerNameConnectedToOuterName(innerName, outerName)) return;
        if (isInnerNameConnectedToAnyOuterName(innerName)) throw new InnerNameConnectedToOuterNameException();

        EList<EObject> pointsOfOuterName = (EList<EObject>) outerName.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_POINT));
        pointsOfOuterName.add(innerName.getInstance());
//        outerName.getBPoints().add(innerName);
    }

    /**
     * Link a node with an inner name
     *
     * @param node1     the node
     * @param innerName the inner name
     * @throws LinkTypeNotExistsException     if the inner name doesn't exists (i.e., doesn't belong from this builder
     * @throws InvalidArityOfControlException if the control cannot connect anything (e.g., is atomic, or no open ports left)
     * @throws InvalidConnectionException     if the inner name is already connected to an outer name
     */
    public void connectNodeToInnerName(BigraphEntity.NodeEntity<Control<?, ?>> node1, BigraphEntity.InnerName innerName) throws LinkTypeNotExistsException, InvalidConnectionException {
        //check if outername exists
//        if (availableInnerNames.get(innerName.getInstance().eGet(EMFUtils.findAttribute(innerName.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME))) == null) {
        if (availableInnerNames.get(innerName.getName()) == null) { //  getInstance().eGet(EMFUtils.findAttribute(innerName.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME))) == null) {
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
            BigraphEntity.Edge edgeOfEClass = createEdgeOfEClass();
            if (Objects.nonNull(linkOfEdge) && linkOfEdge.eClass().equals(eClassEdge)) { // an edge
                edgeOfEClass.setInstance(linkOfEdge); // replace the instance of an edge
            }
            // and add it to the inner name
            innerName.getInstance().eSet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_LINK), edgeOfEClass.getInstance());
            connectToEdge(node1, edgeOfEClass);
        }
    }


    /**
     * Convenient method of {@link BigraphBuilder#connectInnerNames(BigraphEntity.InnerName, BigraphEntity.InnerName, boolean)}
     * which doesn't keep idle edges when inner names are already connected to a node. Meaning, that the last argument
     * defaults to {@code false}.
     *
     * @param ecoreInnerName1 an existing inner name
     * @param ecoreInnerName2 an existing inner name
     * @return the builder
     * @throws InvalidConnectionException
     */
    public BigraphBuilder<S> connectInnerNames(BigraphEntity.InnerName ecoreInnerName1, BigraphEntity.InnerName ecoreInnerName2) throws InvalidConnectionException {
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
    public BigraphBuilder<S> connectInnerNames(BigraphEntity.InnerName ecoreInnerName1, BigraphEntity.InnerName ecoreInnerName2, boolean keepIdleEdges) throws InvalidConnectionException {
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

    private void disconnectInnerNamesFromEdge(EObject edge) {
        if (Objects.isNull(edge) || !edge.eClass().equals(availableEClasses.get(BigraphMetaModelConstants.CLASS_EDGE)))
            return;
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

    //TODO: keep open port?? nicht benötigt, da keine kante auch kein port bedeutet (auch wenn das bei bigrapher grafisch so aussieht)
    //wird dann trotzdem nicht gezählt
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
//            bPoints1.clear(); //TODO
        }
    }

    //BLEIBT
    private boolean isInnerNameConnectedToAnyOuterName(BigraphEntity ecoreInnerName1) {
//        BLink link1 = ecoreInnerName1.getBLink();
        EClass eClassOuterName = availableEClasses.get(BigraphMetaModelConstants.CLASS_OUTERNAME);
        EObject link1 = (EObject) ecoreInnerName1.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_LINK));
        return !Objects.isNull(link1) && link1.eClass().equals(eClassOuterName);
    }

    //BLEIBT
    private boolean isInnerNameConnectedToOuterName(BigraphEntity ecoreInnerName1, BigraphEntity outerName) {
//        BLink link1 = ecoreInnerName1.getBLink();
//        return !Objects.isNull(link1) && link1.equals(outerName);
        EObject link1 = (EObject) ecoreInnerName1.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_LINK));
        return !Objects.isNull(link1) && link1.equals(outerName.getInstance());
    }

    //BLEIBT

    /**
     * This methods will return the connected edge of an inner name, <b>if</b> available.
     *
     * @param innerName the inner name
     * @return return the edge connected to the given inner name, otherwise {@code null}
     */
    @Nullable
    private EObject getEdgeFromInnerName(BigraphEntity innerName) {
//        BLink link1 = innerName.getBLink();
//        if (!Objects.isNull(link1) &&
//                link1.eClass().equals(BigraphBaseModelFactory.eINSTANCE.getBigraphBaseModelPackage().getBEdge()))
//            return (BEdge) link1;
//        return null;
        EClass eClassEdge = availableEClasses.get(BigraphMetaModelConstants.CLASS_EDGE);
        EObject link1 = (EObject) innerName.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_LINK));
        if (Objects.isNull(link1) || !link1.eClass().equals(eClassEdge)) return null;
        return link1;
    }

    //BLEIBT
    private boolean areInnerNamesConnectedByEdge(BigraphEntity.InnerName ecoreInnerName1, BigraphEntity.InnerName ecoreInnerName2) {
        EClass eClassInnerName = availableEClasses.get(BigraphMetaModelConstants.CLASS_INNERNAME);
//        EClass eClassEdge = availableEClasses.get(BigraphMetaModelConstants.CLASS_EDGE);

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
     * Check whether a node can be connected to something (i.e., an edge or an outer name which in turn also includes inner names).
     * <p>
     * The verification is based on the arity of the control and current connections made so far.
     *
     * @param node the node to check
     * @throws InvalidArityOfControlException if control is atomic or the current connections exceed the control's arity
     */
    private void checkIfNodeIsConnectable(BigraphEntity.NodeEntity<Control<?, ?>> node) throws InvalidArityOfControlException {
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
            throw new ControlIsAtomicException();
        if (numberOfConnections.compareTo(castedArityOfControl) >= 0)
            throw new ToManyConnections(); // numberOfConnections >= castedArityOfControl
    }

    //BLEIBT - HELPER
    protected void connectNodeToOuterName(BigraphEntity.NodeEntity<Control<?, ?>> node1, BigraphEntity.OuterName outerName) throws LinkTypeNotExistsException, InvalidArityOfControlException {
        //check if outername exists
        assertOuterNameExists(outerName);

        boolean connectedWithOuterName = isConnectedWithLink(node1, outerName.getInstance());
        if (!connectedWithOuterName) {
            //check arity of control
            checkIfNodeIsConnectable(node1);
            EObject instance1 = node1.getInstance();
            // connect procedure
            //Create ports
            EList<EObject> bPorts = (EList<EObject>) instance1.eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_PORT));
//            EList<EObject> bPorts = (EList<EObject>) instance1.eGet(factory.getBigraphBaseModelPackage().getBNode_BPorts());
            Integer index = bPorts.size(); // auf langer sicht andere methode finden, um den Index zu bekommen (unabhängig von der liste machen)
            EObject portObject = createPortWithIndex(index);
            bPorts.add(portObject); //.getInstance());
            portObject.eSet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_LINK), outerName.getInstance());
//            portObject.setBLink(outerName);
        }
    }

    //BLEIBT - HELPER
    protected EObject createPortWithIndex(final int index) {
        EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClasses.get(BigraphMetaModelConstants.CLASS_PORT));
        setIndexForEObject(eObject, index);
        return eObject;
    }

    private void setIndexForEObject(EObject eObject, final int index) {
        eObject.eSet(EMFUtils.findAttribute(eObject.eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX), index);
    }

    //BLEIBT - HELPER
    private boolean isConnectedWithLink(BigraphEntity.NodeEntity<Control<?, ?>> place1, @Nullable EObject aLink) {
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

    //BLEIBT - HELPER
    private boolean areNodesConnected(BigraphEntity.NodeEntity<Control<?, ?>> place1, BigraphEntity.NodeEntity<Control<?, ?>> place2) {
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

    //BLEIBT - HELPER
    protected EObject createNodeOfEClass(String name, @NonNull Control<?, ?> control) {
//        EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClasses.get(name));
//        EAttribute name1 = EMFUtils.findAttribute(eObject.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
//        eObject.eSet(name1, vertexNameSupplier.get());
//        return eObject;
        return this.createNodeOfEClass(name, control, vertexNameSupplier.get());
    }

    protected EObject createNodeOfEClass(String name, @NonNull Control<?, ?> control, String nodeIdentifier) {
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
        assert availableEClasses.get(BigraphMetaModelConstants.CLASS_EDGE) != null;
        final String name = edgeNameSupplier.get();
        EObject eObject = createEdgeOfEClass0(name);
        BigraphEntity.Edge edge = BigraphEntity.create(eObject, BigraphEntity.Edge.class);
        availableEdges.put(name, edge);
        return edge;
    }

    //TODO: reset values??
    public DynamicEcoreBigraph createBigraph() {
        InstanceParameter meta = new InstanceParameter(loadedEPackage, signature, availableRoots, availableSites,
                availableNodes, availableInnerNames, availableOuterNames, availableEdges);
        DynamicEcoreBigraph bigraph = new DynamicEcoreBigraph(meta);
        isCompleted.set(true);
        return bigraph;
    }

    //DTO?
    public class InstanceParameter {
        private EPackage modelPackage; //TODO wirklich diese package?
        private S signature;
        private Set<BigraphEntity.RootEntity> roots;
        private Set<BigraphEntity.SiteEntity> sites;
        private Set<BigraphEntity.InnerName> innerNames;
        private Set<BigraphEntity.OuterName> outerNames;
        private Set<BigraphEntity.Edge> edges;
        private Set<BigraphEntity.NodeEntity> nodes; //TODO: node set p

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
        }

        public Signature<Control<?, ?>> getSignature() {
            return signature;
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
    public void closeAllInnerNames() {
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
    }

    /**
     * Convenient method for {@link BigraphBuilder#closeInnerName(BigraphEntity.InnerName, boolean)}.
     * The last argument defaults to {@code false}.
     *
     * @param innerName an inner name
     * @throws LinkTypeNotExistsException
     */
    public void closeInnerName(BigraphEntity.InnerName innerName) throws LinkTypeNotExistsException {
        closeInnerName(innerName, false);
    }

    public void closeInnerName(BigraphEntity.InnerName innerName, boolean keepIdleName) throws LinkTypeNotExistsException {
//        EClass eClassOuterName = availableEClasses.get(BigraphMetaModelConstants.CLASS_OUTERNAME);
//        EClass eClassEdge = availableEClasses.get(BigraphMetaModelConstants.CLASS_EDGE);
//        EAttribute attribute = EMFUtils.findAttribute(innerName.getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
//        Object name = //innerName.getInstance().eGet(attribute);
//        assert name != null;
        //Owner check
//        EObject eObject = availableInnerNames.get(String.valueOf(name));
//        if (eObject == null) throw new InnerNameNotExistsException();
        if (availableInnerNames.get(innerName.getName()) == null ||
                !availableInnerNames.get(innerName.getName()).equals(innerName))
            throw new InnerNameNotExistsException();
        EObject blink = (EObject) innerName.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_LINK));
        if (blink != null && blink.eClass().equals(availableEClasses.get(BigraphMetaModelConstants.CLASS_OUTERNAME))) {
//            if (blink.eClass().equals(availableEClasses.get(BigraphMetaModelConstants.CLASS_OUTERNAME))) {
            EList<EObject> bPorts = (EList<EObject>) blink.eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_POINT));
            for (int i = 0, n = bPorts.size(); i < n; i++) {
                if (bPorts.get(i).equals(innerName.getInstance())) {
                    bPorts.remove(innerName.getInstance());
                    break;
                }
            }
        } else { // if (blink.eClass().equals(availableEClasses.get(BigraphMetaModelConstants.CLASS_EDGE))) {
            //remove the inner name from connected edge but leave the edge intact
            disconnectInnerNamesFromEdge(blink);
        }
//        }

        if (!keepIdleName)
            availableInnerNames.remove(innerName.getName());

    }

    //TODO: closeAllOuterNames()
    public void closeAllOuterNames() {
        throw new RuntimeException("not implemented yet");
    }

    public void closeOuterName(BigraphEntity.OuterName outerName) throws TypeNotExistsException {
//        innerName.getType().equals(BigraphEntityType.INNER_NAME)
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
     *
     * @param outername
     * @param keepIdleName
     * @throws TypeNotExistsException
     */
    public void closeOuterName(BigraphEntity.OuterName outername, boolean keepIdleName) throws TypeNotExistsException {
//        assert outername.getInstance() != null;
//        EAttribute attribute = EMFUtils.findAttribute(outername.getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
//        Object name = outername.getInstance().eGet(attribute);
//        assert name != null;
        assertOuterNameExists(outername);

        EList<EObject> bPoints = (EList<EObject>) outername.getInstance().eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_POINT));
        for (int i = bPoints.size() - 1; i >= 0; i--) {
//            if (bPoints.get(i).eClass().equals(availableEClasses.get(BigraphMetaModelConstants.CLASS_INNERNAME))) {
//                bPoints.remove(i);
//            } else
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

    //corresponds to the overridden generated code of the ecore-model library in eclipse
    private void updatePortIndices(EObject node) {
        if (!node.eClass().equals(availableEClasses.get(BigraphMetaModelConstants.CLASS_NODE))
                && !node.eClass().getESuperTypes().contains(availableEClasses.get(BigraphMetaModelConstants.CLASS_NODE)))
            return;//TODO check for indexable eclass super type
        EList<EObject> portList = (EList<EObject>) node.eGet(availableReferences.get(BigraphMetaModelConstants.REFERENCE_PORT));
        for (int i = portList.size() - 1; i >= 0; i--) {
            EObject eachPort = portList.get(i);
            setIndexForEObject(eachPort, i);
        }
    }

    private List<Control<?, ?>> iterableToList(Iterable<Control<?, ?>> controls) {
//        List<C> list = new ArrayList<>();
//        controls.iterator().forEachRemaining(list::add);
        return Lists.newArrayList(controls);
    }

    //TODO: important: change namespace etc.: since instance model will refer to this namespace later
    public void bigraphicalSignatureAsTypeGraph(String name) throws BigraphMetaModelLoadingFailedException {
//        EPackage loadedEPackage;
        try {
            loadedEPackage = BigraphArtifactHelper.loadInternalBigraphMetaModel();
            loadedEPackage.setNsPrefix("bigraphMetaModel_" + name);
            loadedEPackage.setName("bigraphMetaModel_" + name);
        } catch (IOException e) {
            throw new BigraphMetaModelLoadingFailedException(e);
        }

        Iterable<Control<?, ?>> controls = signature.getControls();
        StreamSupport.stream(controls.spliterator(), false)
                .forEach(x -> {
                    EClass entityClass = (EClass) loadedEPackage.getEClassifier("BNode");
                    String s = x.getNamedType().stringValue();
                    EClass newControlClass = EMFUtils.createEClass(s);
                    EMFUtils.addSuperType(newControlClass, loadedEPackage, entityClass.getName());
                    loadedEPackage.getEClassifiers().add(newControlClass);
                    controlMap.put(s, newControlClass);
                });
        Set<EReference> allrefs = new HashSet<>();
        EList<EObject> eObjects = loadedEPackage.eContents();
        for (EObject each : eObjects) {
            availableEClasses.put(((EClassImpl) each).getName(), (EClassImpl) each);
            allrefs.addAll(EMFUtils.findAllReferences((EClass) each));
        }
        allrefs.forEach(x -> availableReferences.put(x.getName(), x));
    }


    private Supplier<String> edgeNameSupplier = new Supplier<String>() {
        private int id = 0;

        @Override
        public String get() {
            return "e" + id++;
        }
    };

    private Supplier<Integer> rootIdxSupplier = new Supplier<Integer>() {
        private int id = 0;

        @Override
        public Integer get() {
            return id++;
        }
    };

    private Supplier<Integer> siteIdxSupplier = new Supplier<Integer>() {
        private int id = 0;

        @Override
        public Integer get() {
            return id++;
        }
    };
}
