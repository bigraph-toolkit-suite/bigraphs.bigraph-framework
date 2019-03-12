package de.tudresden.inf.st.bigraphs.core.impl.builder;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.exceptions.*;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.DynamicEcoreBigraph;
import de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.impl.EClassImpl;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import static de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants.BIGRAPH_BASE_MODEL;

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
 *
 * @param <C> the type of the control
 */
//<? extends NamedType<?>, ? extends FiniteOrdinal<?>>
public class EcoreBigraphBuilder<C extends Control<?, ?>> {

    private EPackage loadedEPackage;

    private boolean completed = false;
    private Signature<C> signature;

    //TODO: change from availableECLassMap to controlMap
    private final HashMap<String, EClass> controlMap = new HashMap<>();
    @Deprecated
    private final HashMap<String, EClass> availableEClassMap = new HashMap<>();
    @Deprecated
    private final HashMap<String, EReference> referenceMap = new HashMap<>();
    private final HashMap<String, EReference> cntmRefMap = new HashMap<>();

    // Create the VertexFactory so the generator can createNodeOfEClass vertices
    private Supplier<String> vertexNameSupplier;
//    private Supplier<String> rootNameSupplier;

    private final HashMap<String, EObject> availableEdges = new HashMap<>();
    private final HashMap<String, EObject> availableOuterNames = new HashMap<>();
    private final HashMap<String, EObject> availableInnerNames = new HashMap<>();
    private final HashMap<Integer, EObject> availableRoots = new HashMap<>(); //this is my "dynamic bigraph object" which can later be saved. //TODO probably a package??
    private final HashMap<Integer, EObject> availableSites = new HashMap<>(); //this is my "dynamic bigraph object" which can later be saved. //TODO probably a package??

    private EcoreBigraphBuilder(Signature<C> signature, Supplier<String> nodeNameSupplier) throws BigraphMetaModelLoadingFailedException {
        this.signature = signature;
        this.completed = false;
        this.bigraphicalSignatureAsTypeGraph();
        this.vertexNameSupplier = nodeNameSupplier;
    }

    public static <C extends Control<? extends NamedType<?>, ? extends FiniteOrdinal<?>>> EcoreBigraphBuilder start(@NonNull Signature<C> signature)
            throws BigraphMetaModelLoadingFailedException {
        return EcoreBigraphBuilder.start(signature,
                new Supplier<String>() {
                    private int id = 0;

                    @Override
                    public String get() {
                        return "v" + id++;
                    }
                });
    }

    public static <C extends Control<? extends NamedType<?>, ? extends FiniteOrdinal<?>>> EcoreBigraphBuilder start(@NonNull Signature<C> signature,
                                                                                                                    Supplier<String> nodeNameSupplier)
            throws BigraphMetaModelLoadingFailedException {
        return new EcoreBigraphBuilder<>(signature, nodeNameSupplier);
    }


    //MOVE/change return type
    public Hierarchy createRoot() {
        //TODO auf index achten! und hochsetzen
        EObject currentRoot = createRootOfEClass();
//        currentNode = currentRoot;
        return new Hierarchy(BigraphEntity.create(currentRoot, BigraphEntity.RootEntity.class));
    }


    //BLEIBT
    private EObject createRootOfEClass() {
        EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(BigraphMetaModelConstants.CLASS_ROOT));
        final int ix = rootIdxSupplier.get();
        eObject.eSet(EMFUtils.findAttribute(eObject.eClass(), "index"), ix);
        availableRoots.put(ix, eObject);
        return eObject;
    }

    public Hierarchy newHierarchy(C control) {
        EObject childNode = createNodeOfEClass(control.getNamedType().stringValue(), control); // new Hierarchy()
        BigraphEntity.NodeEntity<C> nodeEntity = BigraphEntity.createNode(childNode, control);
        Hierarchy hierarchy = new Hierarchy(nodeEntity);
        hierarchy.lastCreatedNode = nodeEntity;
        return hierarchy;
//            new EcoreBigraphBuilder.Hierarchy()
//        BigraphEntity.NodeEntity<C> child = createChild(control);
    }

    public class Hierarchy {
        final Hierarchy parentHierarchy;
        final BigraphEntity<C> parent;
        final Collection<BigraphEntity> childs = new ArrayList<>();
        BigraphEntity.NodeEntity<C> lastCreatedNode;

        /**
         * Creates a new independent hierarchy which can be added later.
         *
         * @param control the control for the hierarchies' parent
         * @return
         */


        private Hierarchy(BigraphEntity<C> parent) {
            this(parent, null);
        }

        private Hierarchy(BigraphEntity<C> parent, Hierarchy parentHierarchy) {
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
//            return new Hierarchy(getLastCreatedNode(), this);
        }

//        public Hierarchy withNewHierarchy(Hierarchy thisOne) {
//            return new Hierarchy(getLastCreatedNode(), thisOne);
////            return new Hierarchy(getLastCreatedNode(), this);
//        }

        public Hierarchy addHierarchyToParent(Hierarchy thisOne) {
            assert thisOne.getParent() != null;
            addChildToParent(thisOne.getParent());
            return this; //new Hierarchy(thisOne.getParent(), thisOne);
//            return new Hierarchy(getLastCreatedNode(), this);
        }


        /**
         * Creates a new dynamic hierarchy where the parent is the current one.
         *
         * @param entity
         * @return
         */
        private Hierarchy withNewHierarchyOn(BigraphEntity.NodeEntity<C> entity) {
            if (!childs.contains(entity)) {
                throw new RuntimeException("Not possible");
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
        private BigraphEntity.NodeEntity<C> createChild(C control) {
            EObject childNode = createNodeOfEClass(control.getNamedType().stringValue(), control);
            BigraphEntity.NodeEntity<C> nodeEntity = BigraphEntity.createNode(childNode, control);
            updateLastCreatedNode(nodeEntity); //new BigraphEntity.NodeEntity(childNode, control));
            return getLastCreatedNode();
        }

        //BLEIBT - HELPER
        private boolean checkSameSignature(C control) {
            return iterableToList(signature.getControls()).contains(control);
        }

        //MOVE
        //this implies: added to a parent (see lastCreatedNode)
        public Hierarchy addChild(C control) {
            if (!checkSameSignature(control)) {
                //TODO debug output or something ...
                return this;
            }
            final BigraphEntity.NodeEntity<C> child = createChild(control);
//            if (addToParent)
            addChildToParent(child);
//            ((EList) parent.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_CHILD))).add(child.getInstance());

//            EObject childNode = createNodeOfEClass(control.getNamedType().stringValue(), control);
//            updateLastCreatedNode(new BigraphEntity(child, control, BigraphEntityType.NODE));
            return this;
        }

        private void addChildToParent(final BigraphEntity node) {
            ((EList) parent.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_CHILD))).add(node.getInstance());
            childs.add(node);
        }

        //MOVE, change return type
        public Hierarchy addSite() {
            EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(BigraphMetaModelConstants.CLASS_SITE));
            final int ix = siteIdxSupplier.get();
            eObject.eSet(EMFUtils.findAttribute(eObject.eClass(), "index"), ix);
            ((EList) parent.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_CHILD))).add(eObject);
            BigraphEntity.SiteEntity siteEntity = BigraphEntity.create(eObject, BigraphEntity.SiteEntity.class);
            childs.add(siteEntity);
            availableSites.put(ix, eObject);
            return this;
        }

        /**
         * Creates new child nodes and connects them with an edge
         *
         * @param controls the controls of the new nodes
         * @return the builder hierarchy
         * @throws InvalidArityOfControlException if nodes cannot be connected because of the control's arity
         */
        @SafeVarargs
        public final Hierarchy connectByEdge(final C... controls) throws InvalidArityOfControlException {
            List<BigraphEntity.NodeEntity<C>> nodes = new ArrayList<>();
            for (int i = 0, n = controls.length; i < n; i++) {
                if (!checkSameSignature(controls[i])) return this; //TODO throw exception
            }
            for (int i = 0, n = controls.length; i < n; i++) {
                nodes.add(createChild(controls[i]));
            }

            EcoreBigraphBuilder.this.connectByEdge(nodes.toArray(new BigraphEntity.NodeEntity[nodes.size()]));
            childs.addAll(nodes); // now its safe, after above
            nodes.forEach(this::addChildToParent);
            return this;
        }

        //To be used to call toe enclosing class' methods for passing the nodes as arguments
        public Collection<BigraphEntity> nodes() {
            return childs;
        }


        public Hierarchy connectNodeToOuterName(BigraphEntity.OuterName outerName) throws LinkTypeNotExistsException, InvalidArityOfControlException {
            EcoreBigraphBuilder.this.connectNodeToOuterName(getLastCreatedNode(), outerName);
            return this;
        }

        public void connectNodeToInnerName(C control, BigraphEntity.InnerName innerName) throws InvalidConnectionException, LinkTypeNotExistsException {
            addChild(control);
            connectNodeToInnerName(innerName);
        }

        public Hierarchy connectNodeToInnerName(BigraphEntity.InnerName innerName) throws LinkTypeNotExistsException, InvalidArityOfControlException, InvalidConnectionException {
            EcoreBigraphBuilder.this.connectNodeToInnerName(getLastCreatedNode(), innerName);
            return this;
        }

        public BigraphEntity.NodeEntity<C> getLastCreatedNode() {
            return lastCreatedNode;
        }

        private void updateLastCreatedNode(BigraphEntity.NodeEntity<C> eObject) {
            lastCreatedNode = eObject;
        }


    }

    //BLEIBT

    /**
     * Returns the same outer name if it already exists under the same {@code name}
     *
     * @param name the name for the outer name
     * @return a new outer name or an existing one with the same name
     */
    public BigraphEntity.OuterName createOuterName(String name) {
        BigraphEntity.OuterName ecoreOuterName = BigraphEntity.create(availableOuterNames.get(name), BigraphEntity.OuterName.class);
//        BigraphEntity<C>.create (availableOuterNames.get(name), BigraphEntity.OuterName.class);
//        BigraphEntity.OuterName ecoreOuterName = new BigraphEntity.OuterName(availableOuterNames.get(name));
        if (ecoreOuterName.getInstance() == null) {
            EObject outername = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(BigraphMetaModelConstants.CLASS_OUTERNAME));
            outername.eSet(EMFUtils.findAttribute(outername.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME), name);
            ecoreOuterName.setInstance(outername);
//            ecoreOuterName = bOuterName; //new EcoreOuterName(outername).setName(name);
            availableOuterNames.put(name, ecoreOuterName.getInstance());
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
        BigraphEntity.InnerName ecoreInnerName = BigraphEntity.create(availableInnerNames.get(name), BigraphEntity.InnerName.class);
        if (ecoreInnerName.getInstance() == null) {
            EObject bInnerName = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(BigraphMetaModelConstants.CLASS_INNERNAME));
            bInnerName.eSet(EMFUtils.findAttribute(bInnerName.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME), name);
            ecoreInnerName.setInstance(bInnerName);
            availableInnerNames.put(name, bInnerName);
        }
//        newPackage.getEClassifiers().add(ecoreOuterName.getInstance().eClass());
        return ecoreInnerName;
    }

    //BLEIBT
    //TODO change return type

    /**
     * no checks are done here... use {@link EcoreBigraphBuilder#isConnectedWithLink(de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity.NodeEntity, EObject)}
     *
     * @param node
     * @param edge
     * @see EcoreBigraphBuilder#isConnectedWithLink(de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity.NodeEntity, EObject)
     */
    private void connectToEdge(BigraphEntity.NodeEntity<C> node, EObject edge) {
//        EList<EObject> bPorts = (EList<EObject>) node.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_PORT));
        EList<EObject> bPorts = (EList<EObject>) node.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_PORT));
        Integer index = bPorts.size();
        //create port with index
        EObject portObject = createPortWithIndex(index);
//        portObject.setBLink(edge);
        EReference linkReference = referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK);
        portObject.eSet(linkReference, edge); //add edge reference for port
        bPorts.add(portObject);
    }

    @Nullable
    private EObject connectByEdge(final BigraphEntity.NodeEntity<C>... nodes) throws InvalidArityOfControlException {
        //get arity and check number of connections
        for (BigraphEntity.NodeEntity<C> each : nodes) {
            checkIfNodeIsConnectable(each);
        }
        EObject edge = createEdgeOfEClass();
        for (BigraphEntity.NodeEntity<C> each : nodes) {
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

        EList<EObject> pointsOfOuterName = (EList<EObject>) outerName.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_POINT));
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
    public void connectNodeToInnerName(BigraphEntity.NodeEntity node1, BigraphEntity.InnerName innerName) throws LinkTypeNotExistsException, InvalidConnectionException {
        //check if outername exists
        if (availableInnerNames.get(innerName.getInstance().eGet(EMFUtils.findAttribute(innerName.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME))) == null) {
            throw new InnerNameNotExistsException();
        }
        checkIfNodeIsConnectable(node1);
        if (isInnerNameConnectedToAnyOuterName(innerName)) throw new InnerNameConnectedToOuterNameException();

        //EDGE can connect many inner names: pick the specific edge of the given inner name
        //check if innerName has an edge (secure: inner name is not connected to an outer name here
        EObject linkOfEdge = (EObject) innerName.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK));
        EClass eClassEdge = availableEClassMap.get(BigraphMetaModelConstants.CLASS_EDGE);

        // check if node is connected with the possibly existing edge
        if (!isConnectedWithLink(node1, linkOfEdge)) {
            // did it failed because edge doesn't exists yet?
            if (Objects.isNull(linkOfEdge) || !linkOfEdge.eClass().equals(eClassEdge)) { // no edge ... //TODO the second check isn't needed - cannot be an outer name
                // create an edge first
                linkOfEdge = createEdgeOfEClass();
            }
            // and add it to the inner name
            innerName.getInstance().eSet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK), linkOfEdge);
            connectToEdge(node1, linkOfEdge);
        }
    }


    /**
     * Convenient method of {@link EcoreBigraphBuilder#connectInnerNames(BigraphEntity.InnerName, BigraphEntity.InnerName, boolean)}
     * which doesn't keep idle edges when inner names are already connected to a node. Meaning, that the last argument
     * defaults to {@code false}.
     *
     * @param ecoreInnerName1 an existing inner name
     * @param ecoreInnerName2 an existing inner name
     * @return the builder
     * @throws InvalidConnectionException
     */
    public EcoreBigraphBuilder<C> connectInnerNames(BigraphEntity.InnerName ecoreInnerName1, BigraphEntity.InnerName ecoreInnerName2) throws InvalidConnectionException {
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
    public EcoreBigraphBuilder<C> connectInnerNames(BigraphEntity.InnerName ecoreInnerName1, BigraphEntity.InnerName ecoreInnerName2, boolean keepIdleEdges) throws InvalidConnectionException {
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

        EObject edgeOfEClass = createEdgeOfEClass();
        EList<EObject> points = (EList<EObject>) edgeOfEClass.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_POINT));
        points.add(ecoreInnerName1.getInstance());
        points.add(ecoreInnerName2.getInstance());
        return this;
    }

    private void disconnectInnerNamesFromEdge(EObject edge) {
        if (Objects.isNull(edge) || !edge.eClass().equals(availableEClassMap.get(BigraphMetaModelConstants.CLASS_EDGE)))
            return;
        EList<EObject> bPoints1 = (EList<EObject>) edge.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_POINT));
        if (Objects.nonNull(bPoints1)) {
            Iterator<EObject> iterator = bPoints1.iterator();
            while (iterator.hasNext()) {
                EObject x = iterator.next();
                if (x.eClass().equals(availableEClassMap.get(BigraphMetaModelConstants.CLASS_INNERNAME))) {
                    bPoints1.remove(x);
                    break;
                }
            }
        }
    }

    private void disconnectNodesFromEdge(EObject edge) {
        // only edge classes allowed ...
        if (Objects.isNull(edge) || !edge.eClass().equals(availableEClassMap.get(BigraphMetaModelConstants.CLASS_EDGE)))
            return;

        EAttribute attribute = EMFUtils.findAttribute(edge.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
        Object name = edge.eGet(attribute);
        assert name != null;
        availableEdges.remove(String.valueOf(name));
        EList<EObject> bPoints1 = (EList<EObject>) edge.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_POINT));
        if (Objects.nonNull(bPoints1)) {
            bPoints1.forEach(x -> {
                if (x.eClass().equals(availableEClassMap.get(BigraphMetaModelConstants.CLASS_PORT))) {
                    EObject node = (EObject) x.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_NODE));
                    EList<EObject> portList = (EList<EObject>) node.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_PORT));
                    portList.remove(x);
                }
            });
//            bPoints1.clear(); //TODO
        }
    }

    //BLEIBT
    private boolean isInnerNameConnectedToAnyOuterName(BigraphEntity ecoreInnerName1) {
//        BLink link1 = ecoreInnerName1.getBLink();
        EClass eClassOuterName = availableEClassMap.get(BigraphMetaModelConstants.CLASS_OUTERNAME);
        EObject link1 = (EObject) ecoreInnerName1.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK));
        return !Objects.isNull(link1) && link1.eClass().equals(eClassOuterName);
    }

    //BLEIBT
    private boolean isInnerNameConnectedToOuterName(BigraphEntity ecoreInnerName1, BigraphEntity outerName) {
//        BLink link1 = ecoreInnerName1.getBLink();
//        return !Objects.isNull(link1) && link1.equals(outerName);
        EObject link1 = (EObject) ecoreInnerName1.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK));
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
        EClass eClassEdge = availableEClassMap.get(BigraphMetaModelConstants.CLASS_EDGE);
        EObject link1 = (EObject) innerName.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK));
        if (Objects.isNull(link1) || !link1.eClass().equals(eClassEdge)) return null;
        return link1;
    }

    //BLEIBT
    private boolean areInnerNamesConnectedByEdge(BigraphEntity.InnerName ecoreInnerName1, BigraphEntity.InnerName ecoreInnerName2) {
        EClass eClassInnerName = availableEClassMap.get(BigraphMetaModelConstants.CLASS_INNERNAME);
//        EClass eClassEdge = availableEClassMap.get(BigraphMetaModelConstants.CLASS_EDGE);

        EObject link1 = getEdgeFromInnerName(ecoreInnerName1);
        EObject link2 = getEdgeFromInnerName(ecoreInnerName2);
        if (Objects.isNull(link1) || Objects.isNull(link2)) return false;

        //is an edge
        EList<EObject> bPoints1 = (EList<EObject>) link1.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_POINT));
        EList<EObject> bPoints2 = (EList<EObject>) link2.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_POINT));

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
    private void checkIfNodeIsConnectable(BigraphEntity.NodeEntity<C> node) throws InvalidArityOfControlException {
//        assert node.getType() ==
        EObject instance1 = node.getInstance();
//        EList<EObject> bPorts1 = (EList<EObject>) instance1.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_PORT));
        EList<EObject> bPorts1 = (EList<EObject>) instance1.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_PORT));
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
    private void connectNodeToOuterName(BigraphEntity.NodeEntity<C> node1, BigraphEntity.OuterName outerName) throws LinkTypeNotExistsException, InvalidArityOfControlException {
        //check if outername exists
        assert outerName.getType().equals(BigraphEntityType.OUTER_NAME);
        if (!outerName.getInstance().eClass().equals(availableEClassMap.get(BigraphMetaModelConstants.CLASS_OUTERNAME)) ||
                availableOuterNames.get(outerName.getInstance().eGet(EMFUtils.findAttribute(outerName.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME))) == null) {
            throw new OuterNameNotExistsException();
        }


        boolean connectedWithOuterName = isConnectedWithLink(node1, outerName.getInstance());
        if (!connectedWithOuterName) {
            //check arity of control
            checkIfNodeIsConnectable(node1);
            EObject instance1 = node1.getInstance();
            // connect procedure
            //Create ports
            EList<EObject> bPorts = (EList<EObject>) instance1.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_PORT));
//            EList<EObject> bPorts = (EList<EObject>) instance1.eGet(factory.getBigraphBaseModelPackage().getBNode_BPorts());
            Integer index = bPorts.size(); // auf langer sicht andere methode finden, um den Index zu bekommen (unabhängig von der liste machen)
            EObject portObject = createPortWithIndex(index);
            bPorts.add(portObject); //.getInstance());
            portObject.eSet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK), outerName.getInstance());
//            portObject.setBLink(outerName);
        }
    }

    //BLEIBT - HELPER
    private EObject createPortWithIndex(final int index) {
        EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(BigraphMetaModelConstants.CLASS_PORT));
        eObject.eSet(EMFUtils.findAttribute(eObject.eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX), index);
        return eObject;
    }

    //BLEIBT - HELPER
    private boolean isConnectedWithLink(BigraphEntity.NodeEntity<C> place1, @Nullable EObject aLink) {
        if (Objects.isNull(aLink)) return false;
        EList<EObject> bPorts = (EList<EObject>) place1.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_PORT));
//        EList<EObject> bPorts = (EList<EObject>) place1.getInstance().eGet(factory.getBigraphBaseModelPackage().getBNode_BPorts());
//        EList<BPort> bPorts = place1.getBPorts();
        for (EObject bPort : bPorts) {
            //get link from port to a possible outername
            EObject link = (EObject) bPort.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK));
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
    private boolean areNodesConnected(BigraphEntity.NodeEntity<C> place1, BigraphEntity.NodeEntity<C> place2) {
        EClass eClassPort = availableEClassMap.get(BigraphMetaModelConstants.CLASS_PORT);
        EList<EObject> bPorts = (EList<EObject>) place1.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_PORT));
//        EList<EObject> bPorts = (EList<EObject>) place1.getInstance().eGet(factory.getBigraphBaseModelPackage().getBNode_BPorts());
        for (EObject bPort : bPorts) {
            //get link from port to a possible outername
            EObject link = (EObject) bPort.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK));
//            EObject link = (EObject) bPort.eGet(factory.getBigraphBaseModelPackage().getBPoint_BLink());
            EList<EObject> bPortsRight = (EList<EObject>) link.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_POINT));
//            EList<EObject> bPortsRight = (EList<EObject>) link.eGet(factory.getBigraphBaseModelPackage().getBLink_BPoints());
            for (EObject eachRight : bPortsRight) {
                if (eachRight.eClass().equals(eClassPort)) {
                    //Get reference from this port to the node
                    EObject node0 = (EObject) eachRight.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_NODE));
//                    EObject node0 = (EObject) eachRight.eGet(factory.getBigraphBaseModelPackage().getBPort_BNode());
                    if (node0.equals(place2.getInstance())) return true;
                }
            }
        }
        return false;
    }

    //BLEIBT - HELPER
    private EObject createNodeOfEClass(String name, @NonNull C control) {
        EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(name));
        EAttribute name1 = EMFUtils.findAttribute(eObject.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
        eObject.eSet(name1, vertexNameSupplier.get());
        return eObject;
    }

    //BLEIBT - HELPER

    /**
     * Creates an edge of EClass {@link de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.BEdge} and adds it to the
     * list of available edges {@code availableEdges}.
     *
     * @return the freshly created edge
     */
    @NonNull
    private EObject createEdgeOfEClass() {
        assert availableEClassMap.get(BigraphMetaModelConstants.CLASS_EDGE) != null;
        EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(BigraphMetaModelConstants.CLASS_EDGE));
        final String name = edgeNameSupplier.get();
        eObject.eSet(EMFUtils.findAttribute(eObject.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME), name);
        availableEdges.put(name, eObject);
        return eObject;
    }

    public DynamicEcoreBigraph createBigraph() {
        DynamicEcoreBigraph bigraph = new DynamicEcoreBigraph(null);
//        bigraph.setRoot(currentRoot);
        return bigraph;
    }


    /**
     * Converts the current bigraph to a ground bigraph (i.e., no sites an no inner names).
     */
    public void makeGround() {
        // first deal with the sites
        Iterator<Map.Entry<Integer, EObject>> iterator = availableSites.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, EObject> next = iterator.next();
            EObject eachSite = next.getValue();

            // get parent
            EObject prnt = (EObject) eachSite.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_PARENT));
            assert prnt != null;
            // get all childs
            EList<EObject> childs = (EList<EObject>) prnt.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_CHILD));
            boolean remove = childs.remove(eachSite);
            assert remove;
            iterator.remove();
        }

        // deal with the inner names
        closeAllInnerNames();
    }


    /**
     * Closes all inner names and doesn't keep idle edges and idle inner names.
     */
    public void closeAllInnerNames() {
        for (Map.Entry<String, EObject> next : availableInnerNames.entrySet()) {
            try {
                // the inner name is also removed from the above map in the following method
                closeInnerName(BigraphEntity.create(next.getValue(), BigraphEntity.InnerName.class));
            } catch (LinkTypeNotExistsException ignored) { /* technically, this shouldn't happen*/ }
        }
    }

    /**
     * Convenient method for {@link EcoreBigraphBuilder#closeInnerName(BigraphEntity, boolean)}.
     * The last argument defaults to {@code false}.
     *
     * @param innerName an inner name
     * @throws LinkTypeNotExistsException
     */
    public void closeInnerName(BigraphEntity innerName) throws LinkTypeNotExistsException {
        closeInnerName(innerName, false);
    }

    public void closeInnerName(BigraphEntity innerName, boolean keepIdleNames) throws LinkTypeNotExistsException {
//        EClass eClassOuterName = availableEClassMap.get(BigraphMetaModelConstants.CLASS_OUTERNAME);
//        EClass eClassEdge = availableEClassMap.get(BigraphMetaModelConstants.CLASS_EDGE);
        EAttribute attribute = EMFUtils.findAttribute(innerName.getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
        Object name = innerName.getInstance().eGet(attribute);
//        boolean entryFound = false;
        EObject eObject = availableInnerNames.get(String.valueOf(name));
        if (eObject == null) throw new InnerNameNotExistsException();
        EObject blink = (EObject) eObject.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK));
        if (blink == null) return;

        if (blink.eClass().equals(availableEClassMap.get(BigraphMetaModelConstants.CLASS_OUTERNAME))) {
            EList<EObject> bPorts = (EList<EObject>) blink.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_POINT));
            for (int i = 0, n = bPorts.size(); i < n; i++) {
                if (bPorts.get(i).equals(innerName.getInstance())) {
                    bPorts.remove(innerName.getInstance());
                    break;
                }
            }
        } else { // if (blink.eClass().equals(availableEClassMap.get(BigraphMetaModelConstants.CLASS_EDGE))) {
            //remove the inner name from connected edge but leave the edge intact
            disconnectInnerNamesFromEdge(blink);
        }

        if (!keepIdleNames)
            availableInnerNames.remove(String.valueOf(name));

    }

    public void closeOuterName(BigraphEntity innerName) {
//        innerName.getType().equals(BigraphEntityType.INNER_NAME)
        throw new NotImplementedException();
    }


    //BLEIBT
    // cannot be instantiated from outside
    //container for everything for the user ...
    //TODO: subclasses? node, inner, outer


    //BLEIBT
    private List<C> iterableToList(Iterable<C> controls) {
        List<C> list = new ArrayList<>();
        controls.iterator().forEachRemaining(list::add);
        return list;
    }

    //BLEIBT
    public void WRITE_DEBUG() throws IOException {
        try {
            List<EObject> allresources = new ArrayList<>();
//            allresources.add(currentRoot.getInstance());
            availableRoots.forEach((s, x) -> allresources.add(x));
            availableOuterNames.forEach((s, x) -> allresources.add(x));
            availableInnerNames.forEach((s, x) -> allresources.add(x));
            availableEdges.forEach((s, x) -> allresources.add(x));
//            allresources.add(newPackage);
//            allresources.addAll();
            EMFUtils.writeDynamicInstanceModel(allresources, "bookStore", System.out);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    //BLEIBT
    //TODO: important: change namespace etc.: since instance model will refer to this namespace later
    //Wenn ich schon ein signatur modell habe, dann muss ich nur noch die controls extrahieren (subclasses of BNode)
    public void bigraphicalSignatureAsTypeGraph() throws BigraphMetaModelLoadingFailedException {
//        EPackage loadedEPackage;
        try {
            loadedEPackage = EMFUtils.loadEcoreModel(BIGRAPH_BASE_MODEL);
        } catch (IOException e) {
            throw new BigraphMetaModelLoadingFailedException();
        }

//        EList<EClassifier> eClassifiers = loadedEPackage.getEClassifiers();
        Iterable<C> controls = signature.getControls();
        StreamSupport.stream(controls.spliterator(), false)
                .forEach(x -> {
                    EClass entityClass = (EClass) loadedEPackage.getEClassifier("BNode");
//                    EClass entityClass = (EClass) bPackage.getEClassifier("BNode");
                    String s = x.getNamedType().stringValue();
                    EClass newControlClass = EMFUtils.createEClass(s);
                    EMFUtils.addSuperType(newControlClass, loadedEPackage, entityClass.getName());
//                    EMFUtils.addSuperType(newControlClass, bPackage, entityClass.getName());
                    loadedEPackage.getEClassifiers().add(newControlClass);
//                    bPackage.getEClassifiers().add(newControlClass);
//                    factory.getBigraphBaseModelPackage().getEClassifiers().add(newControlClass);
                    controlMap.put(s, newControlClass);
                    availableEClassMap.put(s, newControlClass);
                });
        List<EReference> allrefs = new ArrayList<>();
        EList<EObject> eObjects = loadedEPackage.eContents();
//        EList<EObject> eObjects = bPackage.eContents();
        for (EObject each : eObjects) {
            availableEClassMap.put(((EClassImpl) each).getName(), (EClassImpl) each);
            allrefs.addAll(EMFUtils.findAllReferences((EClass) each));
        }

        //all references are opposite edges, should be 6 in total
//        System.out.println("REFS: " + allrefs.toString());
//        assert allrefs.size() == 6;
        allrefs.forEach(x -> referenceMap.put(x.getName(), x));

//        referenceMap.put("BPlace_BPrnt", bigraphEFactoryInstance.eCrossReferences().get(0));
        //returns an instance of dynamic factory EFactoryImpl since we do not have a generated one
//        bigraphEFactoryInstance = loadedEPackage.getEFactoryInstance();
//        newPackage = EMFUtils.createPackage("newmodel", "newmodel", "http://www.example.com/");
//        loadedEPackage.setNsURI("http:///com.ibm.dynamic.example.bookstore.ecore");
//        loadedEPackage.setName("BigraphMetaModel");
//        loadedEPackage.setNsPrefix("extendedSignature");
//        EMFUtils.serializeMetaModel(loadedEPackage, "bookStore", System.out);
//        EMFUtils.writeEcoreFile(loadedEPackage, "custom2", "http://www.example.com/", System.out);
    }

    //BLEIBT
    private Supplier<String> edgeNameSupplier = new Supplier<String>() {
        private int id = 0;

        @Override
        public String get() {
            return "e" + id++;
        }
    };
    //BLEIBT
    private Supplier<Integer> rootIdxSupplier = new Supplier<Integer>() {
        private int id = 0;

        @Override
        public Integer get() {
            return id++;
        }
    };
    //BLEIBT
    private Supplier<Integer> siteIdxSupplier = new Supplier<Integer>() {
        private int id = 0;

        @Override
        public Integer get() {
            return id++;
        }
    };
}
