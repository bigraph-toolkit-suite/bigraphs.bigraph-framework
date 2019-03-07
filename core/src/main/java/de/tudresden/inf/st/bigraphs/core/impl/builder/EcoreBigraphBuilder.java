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
public class EcoreBigraphBuilder<C extends Control<? extends NamedType<?>, ? extends FiniteOrdinal<?>>> {

    //is used for every element except nodes: //TODO maybe remove this one?
//    @Deprecated
//    private BigraphBaseModelFactory factory; // = BigraphBaseModelFactory.eINSTANCE;
//    private BigraphBaseModelPackage bPackage; // = BigraphBaseModelPackage.eINSTANCE;
//    @Deprecated
//    private EFactory bigraphEFactoryInstance;

    //    EcorePackage theCorePackage = EcorePackage.eINSTANCE;
    //is used to createNodeOfEClass concrete nodes of a specified control
    private EPackage loadedEPackage;

//    @Deprecated
//    private EPackage newPackage;

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
    private Supplier<String> vSupplier;
    private Supplier<String> rSupplier;

    private final HashMap<String, EObject> availableEdges = new HashMap<>();
    HashMap<String, EObject> availableOuterNames = new HashMap<>();
    HashMap<String, EObject> availableInnerNames = new HashMap<>();
    HashMap<Integer, EObject> availableRoots = new HashMap<>(); //this is my "dynamic bigraph object" which can later be saved. //TODO probably a package??
    HashMap<Integer, EObject> availableSites = new HashMap<>(); //this is my "dynamic bigraph object" which can later be saved. //TODO probably a package??
    private EObject currentRoot;
    private EObject currentNode;
    private BigraphEntity lastCreatedNode; // only for the user

    private EcoreBigraphBuilder(Signature<C> signature, Supplier<String> nodeNameSupplier, Supplier<String> rootNameSupplier) throws BigraphMetaModelLoadingFailedException {
        this.signature = signature;
        this.completed = false;
//        this.bPackage = BigraphBaseModelPackage.eINSTANCE; //factory.getBigraphBaseModelPackage();
//        this.factory = bPackage.getBigraphBaseModelFactory();
//        loadedEPackage = BigraphBaseModelPackage.eINSTANCE;
        bigraphicalSignatureAsTypeGraph();
        this.vSupplier = nodeNameSupplier;
        this.rSupplier = rootNameSupplier;
//        assert loadedEPackage != null;
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
                }, new Supplier<String>() {
                    private int id = 0;

                    @Override
                    public String get() {
                        return "r" + id++;
                    }
                });
    }

    public static <C extends Control<? extends NamedType<?>, ? extends FiniteOrdinal<?>>> EcoreBigraphBuilder start(@NonNull Signature<C> signature,
                                                                                                                    Supplier<String> nodeNameSupplier,
                                                                                                                    Supplier<String> rootNameSupplier)
            throws BigraphMetaModelLoadingFailedException {
        return new EcoreBigraphBuilder<>(signature, nodeNameSupplier, rootNameSupplier);
    }

    private void validate() {
        //TODO: check arity: go through all controls that have arity and check their ports
    }


    public EcoreBigraphBuilder<C> createRoot() {
        //TODO auf index achten! und hochsetzen
        currentRoot = createRootOfEClass();
        currentNode = currentRoot;
        return this;
    }

    //TODO (!) innername kann nicht ohne outername oder edge erstellt werden

    //TODO nodeIsConnectedToInnername
    //TODO step1 => does the node have an outername which is connected to that given innername?
    //TODO step2 => does the node have an edge which is connected to that given innername?


    //TODO closeInnernames / closeLinks:
    //remove innernames, which keeps the edges between nodes and removes the reference from the outername (if connected)
    //set to null

    public EcoreBigraphBuilder<C> createSite() {
        EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(BigraphMetaModelConstants.CLASS_SITE));
        final int ix = siteIdxSupplier.get();
        eObject.eSet(EMFUtils.findAttribute(eObject.eClass(), "index"), ix);
        availableSites.put(ix, eObject);
        return this;
    }

    public BigraphEntity createChild(C control) {
        this.addChild(control);
        return getLastCreatedNode();
    }

    //this implies: added to a parent (see lastCreatedNode)
    public EcoreBigraphBuilder<C> addChild(C control) {
        if (!checkSameSignature(control)) {
            //TODO debug output or something ...
            return this;
        }
        EObject childNode = createNodeOfEClass(control.getNamedType().stringValue(), control);
        ((EList) currentNode.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_CHILD))).add(childNode);
        BigraphEntity entity = new BigraphEntity(childNode, control, BigraphEntityType.NODE);
        updateLatestNode(entity);
        return this;
    }

    private EObject createRootOfEClass() {
        EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(BigraphMetaModelConstants.CLASS_ROOT));
        final int ix = rootIdxSupplier.get();
        eObject.eSet(EMFUtils.findAttribute(eObject.eClass(), "index"), ix);
        availableRoots.put(ix, eObject);
        return eObject;
    }


    public BigraphEntity getLastCreatedNode() {
        return lastCreatedNode;
    }

    private void updateLatestNode(BigraphEntity eObject) {
        lastCreatedNode = eObject;
    }

    //type from control sind immer StringTypedName

    /**
     * Returns the same outer name if it already exists under the same {@code name}
     *
     * @param name the name for the outer name
     * @return a new outer name or an existing one with the same name
     */
    public BigraphEntity createOuterName(String name) {
        BigraphEntity ecoreOuterName = new BigraphEntity(availableOuterNames.get(name), BigraphEntityType.OUTER_NAME);
        if (ecoreOuterName.getInstance() == null) {
            EObject outername = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(BigraphMetaModelConstants.CLASS_OUTERNAME));
            outername.eSet(EMFUtils.findAttribute(outername.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME), name);
            ecoreOuterName.setInstance(outername);
//            ecoreOuterName = bOuterName; //new EcoreOuterName(outername).setName(name);
            availableOuterNames.put(name, ecoreOuterName.getInstance());
        }
        return ecoreOuterName;
    }

    /**
     * Returns the same inner name if it already exists under the same {@code name}
     *
     * @param name the name for the inner name
     * @return a new inner name or an existing one with the same name
     */
    public BigraphEntity createInnerName(String name) {
        BigraphEntity ecoreInnerName = new BigraphEntity(availableInnerNames.get(name), BigraphEntityType.INNER_NAME);
        if (ecoreInnerName.getInstance() == null) {
            EObject bInnerName = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(BigraphMetaModelConstants.CLASS_INNERNAME));
            bInnerName.eSet(EMFUtils.findAttribute(bInnerName.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME), name);
            ecoreInnerName.setInstance(bInnerName);
            availableInnerNames.put(name, bInnerName);
        }
//        newPackage.getEClassifiers().add(ecoreOuterName.getInstance().eClass());
        return ecoreInnerName;
    }

    /**
     * no checks are done here... use {@link EcoreBigraphBuilder#isConnectedWithLink(BigraphEntity, EObject)}
     *
     * @param node
     * @param edge
     * @see EcoreBigraphBuilder#isConnectedWithLink(BigraphEntity, EObject)
     */
    private void connectByEdge(BigraphEntity node, EObject edge) {
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

//    public void connectNewNodesByEdge(C control1, C control2) throws ArityMismatch {
//        //get arity and check number of connections
////        checkIfNodeIsConnectable(node1);
////        checkIfNodeIsConnectable(node2);
////        EObject instance1 = node1.getInstance();
////        EObject instance2 = node2.getInstance();
//        BigraphEntity child1 = createChild(control1);
//        BigraphEntity child2 = createChild(control2);
//
//        //if not already connected; do:
//        if (areNodesConnected(node1, node2)) return;
//        //Create ports
////        EReference linkReference = referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK);
//        EObject edge = createEdgeOfEClass();
//        {
//            connectByEdge(node1, edge);
////            EList<EObject> bPorts1 = (EList<EObject>) instance1.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_PORT));
////            Integer index = bPorts1.size();
////            EcoreNode portObject = createPortWithIndex(index);
////            portObject.getInstance().eSet(linkReference, edge.getInstance()); //add edge reference for port
////            bPorts1.add(portObject.getInstance());
//        }
//        {
//            connectByEdge(node2, edge);
////            EList<EObject> bPorts2 = (EList<EObject>) instance2.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_PORT));
////            Integer index = bPorts2.size();
////            EcoreNode portObject = createPortWithIndex(index);
////            portObject.getInstance().eSet(linkReference, edge.getInstance()); //add edge reference for port
////            bPorts2.add(portObject.getInstance());
//        }
//
//    }

    public void connectByEdge(BigraphEntity node1, BigraphEntity node2) throws ArityMismatch {
        //get arity and check number of connections
        checkIfNodeIsConnectable(node1);
        checkIfNodeIsConnectable(node2);
//        EObject instance1 = node1.getInstance();
//        EObject instance2 = node2.getInstance();

        //if not already connected; do:
        if (areNodesConnected(node1, node2)) return;
        //Create ports
//        EReference linkReference = referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK);
        EObject edge = createEdgeOfEClass();
        {
            connectByEdge(node1, edge);
//            EList<EObject> bPorts1 = (EList<EObject>) instance1.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_PORT));
//            Integer index = bPorts1.size();
//            EcoreNode portObject = createPortWithIndex(index);
//            portObject.getInstance().eSet(linkReference, edge.getInstance()); //add edge reference for port
//            bPorts1.add(portObject.getInstance());
        }
        {
            connectByEdge(node2, edge);
//            EList<EObject> bPorts2 = (EList<EObject>) instance2.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_PORT));
//            Integer index = bPorts2.size();
//            EcoreNode portObject = createPortWithIndex(index);
//            portObject.getInstance().eSet(linkReference, edge.getInstance()); //add edge reference for port
//            bPorts2.add(portObject.getInstance());
        }

    }

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
     * @throws LinkTypeNotExistsException if the inner name doesn't exists (i.e., doesn't belong from this builder
     * @throws ArityMismatch              if the control cannot connect anything (e.g., is atomic, or no open ports left)
     * @throws InvalidConnectionException if the inner name is already connected to an outer name
     */
    public void connectNodeToInnerName(BigraphEntity node1, BigraphEntity innerName) throws LinkTypeNotExistsException, ArityMismatch, InvalidConnectionException {
        //check if outername exists
        if (availableInnerNames.get(innerName.getInstance().eGet(EMFUtils.findAttribute(innerName.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME))) == null) {
            throw new InnerNameNotExistsException();
        }
        checkIfNodeIsConnectable(node1);
        if (isInnerNameConnectedToAnyOuterName(innerName)) throw new InvalidConnectionException();

//        EObject node = node1.getInstance();

        //EDGE can connect many inner names: pick the specific edge of the given inner name
        //check if innerName has an edge (secure: inner name is not connected to an outer name here
        EObject linkOfEdge = (EObject) innerName.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK));
        EClass eClassEdge = availableEClassMap.get(BigraphMetaModelConstants.CLASS_EDGE);
//        EcoreEdge edge = new EcoreEdge(linkOfEdge);

//        BLink edge = innerName.getBLink();
        if (Objects.isNull(linkOfEdge) || !linkOfEdge.eClass().equals(eClassEdge)) { // no edge ...
            //create an edge first
            linkOfEdge = createEdgeOfEClass();
            //and add it to the inner name
            innerName.getInstance().eSet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK), linkOfEdge);
//            innerName.setBLink(edge);
        }
        //perfect, otherwise ...
        //check if node is connected
        if (!isConnectedWithLink(node1, linkOfEdge)) {
            connectByEdge(node1, linkOfEdge);
        }

        //CHECK if node is already connected to that edge
    }

    public Collection<BigraphEntity> nodes() {
        List<BigraphEntity> childs = new ArrayList<>();
        return childs;
    }


    public interface PlaceGenerator<C> {
        PlaceGenerator<C> createSite();

        PlaceGenerator<C> addAndEnterNewLevel(C control);
    }

    public class Hierarchy {
        public BigraphEntity parent;
        List<BigraphEntity> childs = new ArrayList<>();

        public Hierarchy(BigraphEntity parent) {
            this.parent = parent;
        }

        public Hierarchy addChild(C control) {
            if (!checkSameSignature(control)) {
                return this;
            }
            EObject childNode = createNodeOfEClass(control.getNamedType().stringValue(), control);
            ((EList) parent.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_CHILD))).add(childNode);
            BigraphEntity entity = new BigraphEntity(childNode, control, BigraphEntityType.NODE);
//            updateLatestNode(entity);
            return this;
        }

//        public new
    }

    //TODO connect two Innernames (create separate edge)
    public EcoreBigraphBuilder<C> connectInnerNames(BigraphEntity ecoreInnerName1, BigraphEntity ecoreInnerName2) throws InvalidConnectionException {
        assert ecoreInnerName1.getType().equals(BigraphEntityType.INNER_NAME);
        assert ecoreInnerName2.getType().equals(BigraphEntityType.INNER_NAME);
        // throw exception if an innername is already connected to an outername
        if (isInnerNameConnectedToAnyOuterName(ecoreInnerName1) || isInnerNameConnectedToAnyOuterName(ecoreInnerName2)) {
            throw new InnerNameConnectedToOuterNameException();
        }

        //are they already connected?
        if (areInnerNamesConnectedByEdge(ecoreInnerName1, ecoreInnerName2)) return this;

        EObject edgeOfEClass = createEdgeOfEClass();
        EList<EObject> points = (EList<EObject>) edgeOfEClass.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_POINT));
        points.add(ecoreInnerName1.getInstance());
        points.add(ecoreInnerName2.getInstance());
//        edgeOfEClass.getBPoints().add(ecoreInnerName1);
//        edgeOfEClass.getBPoints().add(ecoreInnerName2);
        return this;
    }

    private boolean isInnerNameConnectedToAnyOuterName(BigraphEntity ecoreInnerName1) {
//        BLink link1 = ecoreInnerName1.getBLink();
        EClass eClassOuterName = availableEClassMap.get(BigraphMetaModelConstants.CLASS_OUTERNAME);
        EObject link1 = (EObject) ecoreInnerName1.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK));
        return !Objects.isNull(link1) && link1.eClass().equals(eClassOuterName);
    }

    private boolean isInnerNameConnectedToOuterName(BigraphEntity ecoreInnerName1, BigraphEntity outerName) {
//        BLink link1 = ecoreInnerName1.getBLink();
//        return !Objects.isNull(link1) && link1.equals(outerName);
        EObject link1 = (EObject) ecoreInnerName1.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK));
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

    private boolean areInnerNamesConnectedByEdge(BigraphEntity ecoreInnerName1, BigraphEntity ecoreInnerName2) {
        EClass eClassInnerName = availableEClassMap.get(BigraphMetaModelConstants.CLASS_INNERNAME);
        EClass eClassEdge = availableEClassMap.get(BigraphMetaModelConstants.CLASS_EDGE);

        EObject link1 = getEdgeFromInnerName(ecoreInnerName1); //(EObject) ecoreInnerName1.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK));
        EObject link2 = getEdgeFromInnerName(ecoreInnerName2); //(EObject) ecoreInnerName2.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK));
//        if (Objects.isNull(link1) || !link1.eClass().equals(eClassEdge)) return false;
//        if (Objects.isNull(link2) || !link2.eClass().equals(eClassEdge)) return false;
        if (Objects.isNull(link1) || Objects.isNull(link2)) return false;


        //is an edge
        EList<EObject> bPoints1 = (EList<EObject>) link1.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_POINT));
        EList<EObject> bPoints2 = (EList<EObject>) link2.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_POINT));
//        EList<BPoint> bPoints1 = link1.getBPoints();
//        EList<BPoint> bPoints2 = link2.getBPoints();

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
     * @throws ArityMismatch if control is atomic or the current connections exceed the control's arity
     */
    private void checkIfNodeIsConnectable(BigraphEntity node) throws ArityMismatch {
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
        if (castedArityOfControl == 0) {
            throw new ControlIsAtomicException();
        }
        if (numberOfConnections.compareTo(castedArityOfControl) >= 0) throw new ToManyConnections(); // index >= value
    }

    public void connectNodeToOuterName(BigraphEntity node1, BigraphEntity outerName) throws LinkTypeNotExistsException, ArityMismatch {
        //check if outername exists
        assert outerName.getType().equals(BigraphEntityType.OUTER_NAME);
        if (availableOuterNames.get(outerName.getInstance().eGet(EMFUtils.findAttribute(outerName.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME))) == null) {
            throw new OuterNameNotExistsException();
        }
        //check arity of control
        checkIfNodeIsConnectable(node1);
        EObject instance1 = node1.getInstance();

        boolean connectedWithOuterName = isConnectedWithLink(node1, outerName.getInstance());
        if (!connectedWithOuterName) {
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

    private EObject createPortWithIndex(final int index) {
        EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(BigraphMetaModelConstants.CLASS_PORT));
        eObject.eSet(EMFUtils.findAttribute(eObject.eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX), index);
//        BPortImpl portObject = (BPortImpl) factory.createBPort();
//        portObject.setIndex(index);
//        EcoreNode portObject = createNodeOfEClass(BigraphMetaModelConstants.CLASS_PORT, (C) null);
//        EAttribute attribute = EMFUtils.findAttribute(portObject.getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX);
//        portObject.getInstance().eSet(attribute, index);
        return eObject;
    }

    private boolean isConnectedWithLink(BigraphEntity place1, EObject aLink) {
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

    public boolean areNodesConnected(BigraphEntity place1, BigraphEntity place2) {
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

    private EObject createNodeOfEClass(String name, @NonNull C control) {
        EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(name));
        EAttribute name1 = EMFUtils.findAttribute(eObject.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
        eObject.eSet(name1, vSupplier.get());
//        EcoreUtil.copy(eObject);
//        EcoreNode ecoreNode = new EcoreNode(eObject, control, PlaceType.NODE);
//        ecoreNode.setName(vSupplier.get());
//        eObject.eClass().setInstanceClassName(vSupplier.get());
        return eObject;
    }

//    private EcoreNode createNodeOfEClass(String name, PlaceType type) {
//        EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(name));
//        assert availableEClassMap.get(name).equals(eObject.eClass());
//        EcoreNode ecoreNode = new EcoreNode(eObject, null, type);
//        ecoreNode.setName(vSupplier.get());
//        return ecoreNode; //new EcoreNode(eObject, null, type).setName(vSupplier.get());
//    }

//    //TODO: zusammenfassen: outername und edge superclass, remove createEdgeOfEClass then
//    private void createEntityOfEClass(String name, LinkType linkType) {
//        switch (linkType) {
//            case EDGE:
//                EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(BigraphMetaModelConstants.CLASS_EDGE));
//
////                BEdgeImpl ecoreEdge = (BEdgeImpl) factory.createBEdge(); //new EcoreEdge(eObject).setName(edgeNameSupplier.get());
////                ecoreEdge.setName(edgeNameSupplier.get());
//                availableEdges.put(ecoreEdge.getName(), ecoreEdge);
//                break;
//            case OUTER_NAME:
//                BOuterNameImpl outername = (BOuterNameImpl) factory.createBOuterName();
//                outername.setName(name);
////                EObject outername = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(BigraphMetaModelConstants.CLASS_OUTERNAME));
////                EcoreOuterName ecoreOuterName = new EcoreOuterName(outername).setName(name);
//                availableOuterNames.put(name, outername);
//                break;
//        }
//        throw new NotImplementedException();
//    }

    //    /**
//     * use {@link EcoreBigraphBuilder#createLinkOfEClass(String, LinkType)} in the future
//     *
//     * @return
//     */
//    @Deprecated
    private EObject createEdgeOfEClass() {
        EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(BigraphMetaModelConstants.CLASS_EDGE));
        final String name = edgeNameSupplier.get();
        eObject.eSet(EMFUtils.findAttribute(eObject.eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME), name);
//        EcoreEdge ecoreEdge = new EcoreEdge(eObject).setName(edgeNameSupplier.get());
//        BEdgeImpl bEdge = (BEdgeImpl) factory.createBEdge();
//        bEdge.setName(edgeNameSupplier.get());
        availableEdges.put(name, eObject);
        return eObject;
    }

    private boolean checkSameSignature(C control) {
        return iterableToList(signature.getControls()).contains(control);
    }

    public DynamicEcoreBigraph createBigraph() {
        DynamicEcoreBigraph bigraph = new DynamicEcoreBigraph(null);
        bigraph.setRoot(currentRoot);
        return bigraph;
    }

    // cannot be instantiated from outside
    //container for everything for the user ...
    public class BigraphEntity {


        private EObject instance;
        private C control;
        BigraphEntityType type;

        //        private String name;
//
        BigraphEntity(@NonNull EObject instance, C control, BigraphEntityType type) {
            this.instance = instance;
            this.control = control;
            this.type = type;
        }

        BigraphEntity(@NonNull EObject instance, BigraphEntityType type) {
            this(instance, null, type);
        }

        //
        EObject getInstance() {
            return instance;
        }

        BigraphEntity setInstance(EObject instance) {
            this.instance = instance;
            return this;
        }

        //
        public C getControl() {
            return control;
        }

        //
        public BigraphEntityType getType() {
            return type;
        }

        EClass eClass() {
            return getInstance().eClass();
        }
    }

    private List<C> iterableToList(Iterable<C> controls) {
        List<C> list = new ArrayList<>();
        controls.iterator().forEachRemaining(list::add);
        return list;
    }

    public void WRITE_DEBUG() {
        try {
            List<EObject> allresources = new ArrayList<>();
//            allresources.add(currentRoot.getInstance());
            availableRoots.forEach((s, x) -> allresources.add(x));
            availableOuterNames.forEach((s, x) -> allresources.add(x));
            availableInnerNames.forEach((s, x) -> allresources.add(x));
//            allresources.add(newPackage);
//            allresources.addAll();
            EMFUtils.writeDynamicInstanceModel(allresources, "bookStore", System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
