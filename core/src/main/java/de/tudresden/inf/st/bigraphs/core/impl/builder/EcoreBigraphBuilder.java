package de.tudresden.inf.st.bigraphs.core.impl.builder;

import de.tudresden.inf.st.bigraphs.core.*;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.exceptions.*;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.DefaultEcoreBigraph;
import de.tudresden.inf.st.bigraphs.core.model2.Edge;
import de.tudresden.inf.st.bigraphs.core.model2.InnerName;
import de.tudresden.inf.st.bigraphs.core.model2.Link;
import de.tudresden.inf.st.bigraphs.core.model2.Root;
import de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils;
import de.tudresden.inf.st.bigraphs.model.BigraphBaseModel.BRoot;
import de.tudresden.inf.st.bigraphs.model.BigraphBaseModel.BigraphMetaModelFactory;
import de.tudresden.inf.st.bigraphs.model.BigraphBaseModel.BigraphMetaModelPackage;
import de.tudresden.inf.st.bigraphs.model.BigraphBaseModel.impl.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.impl.EClassImpl;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
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
 *
 * @param <C> the type of the control
 */
public class EcoreBigraphBuilder<C extends Control<? extends NamedType<?>, ? extends FiniteOrdinal<?>>> {

    //is used for every element except nodes: //TODO maybe remove this one?
    @Deprecated
    private BigraphMetaModelFactory factory = BigraphMetaModelFactory.eINSTANCE;
    @Deprecated
    private EFactory bigraphEFactoryInstance;

    EcorePackage theCorePackage = EcorePackage.eINSTANCE;
    //is used to createNodeOfEClass concrete nodes of a specified control
    private EPackage loadedEPackage;

//    @Deprecated
//    private EPackage newPackage;

    private boolean completed = false;
    private Signature<C> signature;

    @Deprecated
    private final HashMap<String, EClass> controlMap = new HashMap<>();
    private final HashMap<String, EClass> availableEClassMap = new HashMap<>();
    private final HashMap<String, EReference> referenceMap = new HashMap<>();
    private final HashMap<String, EReference> cntmRefMap = new HashMap<>();

    // Create the VertexFactory so the generator can createNodeOfEClass vertices
    private Supplier<String> vSupplier;
    private Supplier<String> rSupplier;

    private final HashMap<String, EcoreEdge> availableEdges = new HashMap<>();
    HashMap<String, EcoreOuterName> availableOuterNames = new HashMap<>();
    HashMap<String, EcoreInnerName> availableInnerNames = new HashMap<>();
    HashMap<Integer, EcoreRoot> availableRoots = new HashMap<>(); //this is my "dynamic bigraph object" which can later be saved. //TODO probably a package??
    private EcoreRoot currentRoot;
    private EcoreNode currentNode;
    private EcoreNode lastCreatedNode;

    private EcoreBigraphBuilder(Signature<C> signature, Supplier<String> nodeNameSupplier, Supplier<String> rootNameSupplier) throws BigraphMetaModelLoadingFailedException {
        this.signature = signature;
        this.completed = false;
        loadedEPackage = BigraphMetaModelPackage.eINSTANCE;
        bigraphicalSignatureAsTypeGraph();
        this.vSupplier = nodeNameSupplier;
        this.rSupplier = rootNameSupplier;
        assert loadedEPackage != null;
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
        currentNode = new EcoreNode(currentRoot.getInstance(), null, PlaceType.ROOT);
//        updateCurrentNode(currentNode, null, PlaceType.ROOT);
        availableRoots.put(currentRoot.getIndex(), currentRoot);
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
        EcoreNode nodeOfEClass = createNodeOfEClass(BigraphMetaModelConstants.CLASS_SITE, PlaceType.SITE);
        EAttribute index = EMFUtils.findAttribute(nodeOfEClass.getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX);
        nodeOfEClass.getInstance().eSet(index, siteIdxSupplier.get());
        return this;
    }

    public EcoreNode createChild(C control) {
        this.addChild(control);
        return getLastCreatedNode();
    }

    //this implies: added to a parent (see lastCreatedNode)
    public EcoreBigraphBuilder<C> addChild(C control) {
        if (!checkSameSignature(control)) {
            //TODO debug output or something ...
            return this;
        }
        EcoreNode childNode = createNodeOfEClass(control.getNamedType().stringValue(), control);
//        System.out.println(childNode);
        //parent is automatically set (according to the meta-model specification)
        ((EList) currentNode.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_CHILD))).add(childNode.getInstance());
        updateCurrentNode(childNode);
        return this;
    }

    private EcoreRoot createRootOfEClass() {
        EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(BigraphMetaModelConstants.CLASS_ROOT));
//        eObject.eClass().setInstanceClassName(rSupplier.get());
        EAttribute index = EMFUtils.findAttribute(eObject.eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX);
        eObject.eSet(index, rootIdxSupplier.get());
        return new EcoreRoot(eObject);
    }


    public EcoreNode getLastCreatedNode() {
        return lastCreatedNode;
    }

    private void updateCurrentNode(EcoreNode eObject) {
        lastCreatedNode = eObject;
    }

    //type from control sind immer StringTypedName

    /**
     * Returns the same outer name if it already exists under the same {@code name}
     *
     * @param name the name for the outer name
     * @return a new outer name or an existing one with the same name
     */
    public EcoreOuterName createOuterName(String name) {
        EcoreOuterName ecoreOuterName;
        if ((ecoreOuterName = availableOuterNames.get(name)) == null) {
            EObject outername = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(BigraphMetaModelConstants.CLASS_OUTERNAME));
            ecoreOuterName = new EcoreOuterName(outername).setName(name);
            availableOuterNames.put(name, ecoreOuterName);
        }
//        newPackage.getEClassifiers().add(ecoreOuterName.getInstance().eClass());
        return ecoreOuterName;
    }

    /**
     * Returns the same inner name if it already exists under the same {@code name}
     *
     * @param name the name for the inner name
     * @return a new inner name or an existing one with the same name
     */
    public EcoreInnerName createInnerName(String name) {
        EcoreInnerName ecoreInnerName;
        if ((ecoreInnerName = availableInnerNames.get(name)) == null) {
            EObject outername = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(BigraphMetaModelConstants.CLASS_INNERNAME));
            ecoreInnerName = new EcoreInnerName(outername).setName(name);
            availableInnerNames.put(name, ecoreInnerName);
        }
//        newPackage.getEClassifiers().add(ecoreOuterName.getInstance().eClass());
        return ecoreInnerName;
    }

    /**
     * no checks are done here... use {@link EcoreBigraphBuilder#isConnectedWithLink(EcoreNode, Link)}
     *
     * @param node
     * @param edge
     * @see EcoreBigraphBuilder#isConnectedWithLink(EcoreNode, Link)
     */
    private void connectByEdge(EcoreNode node, EcoreEdge edge) {
        EReference linkReference = referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK);
        EList<EObject> bPorts = (EList<EObject>) node.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_PORT));
        Integer index = bPorts.size();
        EcoreNode portObject = createPortWithIndex(index);
        portObject.getInstance().eSet(linkReference, edge.getInstance()); //add edge reference for port
        bPorts.add(portObject.getInstance());
    }

    public void connectByEdge(EcoreNode node1, EcoreNode node2) throws ArityMismatch {
        //get arity and check number of connections
        checkIfNodeIsConnectable(node1);
        checkIfNodeIsConnectable(node2);
        EObject instance1 = node1.getInstance();
        EObject instance2 = node2.getInstance();

        //if not already connected; do:
        if (areNodesConnected(node1, node2)) return;
        //Create ports
//        EReference linkReference = referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK);
        EcoreEdge edge = createEdgeOfEClass();
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

    public void connectInnerToOuterName(EcoreInnerName innerName, EcoreOuterName outerName) throws InvalidConnectionException {
        //TODO: check if from the same set: the "owner" problem

        EObject edgeFromInnerName = getEdgeFromInnerName(innerName);
        if (edgeFromInnerName != null) throw new InnerNameConnectedToEdgeException();

        if (isInnerNameConnectedToOuterName(innerName, outerName)) return;
        if (isInnerNameConnectedToAnyOuterName(innerName)) throw new InnerNameConnectedToOuterNameException();


        EList<EObject> pointsOfOuterName = (EList<EObject>) outerName.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_POINT));
        pointsOfOuterName.add(innerName.getInstance());
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
    public void connectNodeToInnerName(EcoreNode node1, EcoreInnerName innerName) throws LinkTypeNotExistsException, ArityMismatch, InvalidConnectionException {
        //check if outername exists
        if (availableInnerNames.get(innerName.getName()) == null) {
            throw new InnerNameNotExistsException();
        }
        checkIfNodeIsConnectable(node1);
        if (isInnerNameConnectedToAnyOuterName(innerName)) throw new InvalidConnectionException();

//        EObject node = node1.getInstance();

        //EDGE can connect many inner names: pick the specific edge of the given inner name
        //check if innerName has an edge (secure: inner name is not connected to an outer name here
        EObject linkOfEdge = (EObject) innerName.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK));
        EClass eClassEdge = availableEClassMap.get(BigraphMetaModelConstants.CLASS_EDGE);
        EcoreEdge edge = new EcoreEdge(linkOfEdge);
        if (Objects.isNull(linkOfEdge) || !linkOfEdge.eClass().equals(eClassEdge)) { // no edge ...
            //create an edge first
            edge = createEdgeOfEClass();
            //and add it to the inner name
            innerName.getInstance().eSet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK), edge.getInstance());
        }
        //perfect, otherwise ...
        //check if node is connected
        if (!isConnectedWithLink(node1, edge)) {
            connectByEdge(node1, edge);
        }

        //CHECK if node is already connected to that edge
    }

    //TODO connect two Innernames (create separate edge)
    public EcoreBigraphBuilder<C> connectInnerNames(EcoreInnerName ecoreInnerName1, EcoreInnerName ecoreInnerName2) throws InvalidConnectionException {

        // throw exception if an innername is already connected to an outername
        if (isInnerNameConnectedToAnyOuterName(ecoreInnerName1) || isInnerNameConnectedToAnyOuterName(ecoreInnerName2)) {
            throw new InnerNameConnectedToOuterNameException();
        }

        //are they already connected?
        if (areInnerNamesConnectedByEdge(ecoreInnerName1, ecoreInnerName2)) return this;

        EcoreEdge edgeOfEClass = createEdgeOfEClass();
        EList<EObject> points = (EList<EObject>) edgeOfEClass.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_POINT));
        points.add(ecoreInnerName1.getInstance());
        points.add(ecoreInnerName2.getInstance());
        return this;
    }

    private boolean isInnerNameConnectedToAnyOuterName(EcoreInnerName ecoreInnerName1) {
        EClass eClassOuterName = availableEClassMap.get(BigraphMetaModelConstants.CLASS_OUTERNAME);
        EObject link1 = (EObject) ecoreInnerName1.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK));
        return !Objects.isNull(link1) && link1.eClass().equals(eClassOuterName);
    }

    private boolean isInnerNameConnectedToOuterName(EcoreInnerName ecoreInnerName1, EcoreOuterName outerName) {
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
    private EObject getEdgeFromInnerName(EcoreInnerName innerName) {
        EClass eClassEdge = availableEClassMap.get(BigraphMetaModelConstants.CLASS_EDGE);
        EObject link1 = (EObject) innerName.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK));
        if (Objects.isNull(link1) || !link1.eClass().equals(eClassEdge)) return null;
        return link1;
    }

    public boolean areInnerNamesConnectedByEdge(EcoreInnerName ecoreInnerName1, EcoreInnerName ecoreInnerName2) {
        EClass eClassInnerName = availableEClassMap.get(BigraphMetaModelConstants.CLASS_INNERNAME);
//        EClass eClassEdge = availableEClassMap.get(BigraphMetaModelConstants.CLASS_EDGE);

        EObject link1 = getEdgeFromInnerName(ecoreInnerName1); //(EObject) ecoreInnerName1.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK));
        EObject link2 = getEdgeFromInnerName(ecoreInnerName2); //(EObject) ecoreInnerName2.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK));
//        if (Objects.isNull(link1) || !link1.eClass().equals(eClassEdge)) return false;
//        if (Objects.isNull(link2) || !link2.eClass().equals(eClassEdge)) return false;
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
     * @throws ArityMismatch if control is atomic or the current connections exceed the control's arity
     */
    private void checkIfNodeIsConnectable(EcoreNode node) throws ArityMismatch {
        EObject instance1 = node.getInstance();
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

    public void connectNodeToOuterName(EcoreNode node1, EcoreOuterName outerName) throws LinkTypeNotExistsException, ArityMismatch {
        //check if outername exists
        if (availableOuterNames.get(outerName.getName()) == null) {
            throw new OuterNameNotExistsException();
        }
        //check arity of control
        checkIfNodeIsConnectable(node1);
        EObject instance1 = node1.getInstance();

        boolean connectedWithOuterName = isConnectedWithLink(node1, outerName);
        if (!connectedWithOuterName) {
            // connect procedure
            //Create ports
            EList<EObject> bPorts = (EList<EObject>) instance1.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_PORT));
            Integer index = bPorts.size(); // auf langer sicht andere methode finden, um den Index zu bekommen (unabhängig von der liste machen)
            EcoreNode portObject = createPortWithIndex(index);
            bPorts.add(portObject.getInstance());
            portObject.getInstance().eSet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK), outerName.getInstance());
        }
    }

    private EcoreNode createPortWithIndex(int index) {
        EcoreNode portObject = createNodeOfEClass(BigraphMetaModelConstants.CLASS_PORT, (C) null);
        EAttribute attribute = EMFUtils.findAttribute(portObject.getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX);
        portObject.getInstance().eSet(attribute, index);
        return portObject;
    }

    public boolean isConnectedWithLink(EcoreNode place1, Link aLink) {
        EList<EObject> bPorts = (EList<EObject>) place1.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_PORT));
//        EList<BPort> bPorts = place1.getBPorts();
        for (EObject bPort : bPorts) {
            //get link from port to a possible outername
            EObject link = (EObject) bPort.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK));
            if (link.eClass().equals((aLink.getInstance()).eClass())) {
                if (aLink.getInstance().equals(link)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean areNodesConnected(EcoreNode place1, EcoreNode place2) {
        EClass eClassPort = availableEClassMap.get(BigraphMetaModelConstants.CLASS_PORT);
        EList<EObject> bPorts = (EList<EObject>) place1.getInstance().eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_PORT));
        for (EObject bPort : bPorts) {
            //get link from port to a possible outername
            EObject link = (EObject) bPort.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_LINK));
            EList<EObject> bPortsRight = (EList<EObject>) link.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_POINT));
            for (EObject eachRight : bPortsRight) {
                if (eachRight.eClass().equals(eClassPort)) {
                    //Get reference from this port to the node
                    EObject node0 = (EObject) eachRight.eGet(referenceMap.get(BigraphMetaModelConstants.REFERENCE_NODE));
                    if (node0.equals(place2.getInstance())) return true;
                }
            }
        }
        return false;
    }

    private EcoreNode createNodeOfEClass(String name, C control) {
        EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(name));
        assert availableEClassMap.get(name).equals(eObject.eClass());
//        eObject.eClass().setInstanceClassName(vSupplier.get());
        return new EcoreNode(eObject, control, control != null ? PlaceType.NODE : null).setName(vSupplier.get());
    }

    private EcoreNode createNodeOfEClass(String name, PlaceType placeType) {
        EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(name));
        assert availableEClassMap.get(name).equals(eObject.eClass());
        return new EcoreNode(eObject, null, placeType).setName(vSupplier.get());
    }

    //TODO: zusammenfassen: outername und edge superclass, remove createEdgeOfEClass then
    private void createLinkOfEClass(String name, LinkType linkType) {
        switch (linkType) {
            case EDGE:
                EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(BigraphMetaModelConstants.CLASS_EDGE));
                EcoreEdge ecoreEdge = new EcoreEdge(eObject).setName(edgeNameSupplier.get());
                availableEdges.put(ecoreEdge.getName(), ecoreEdge);
                break;
            case OUTER_NAME:
                EObject outername = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(BigraphMetaModelConstants.CLASS_OUTERNAME));
                EcoreOuterName ecoreOuterName = new EcoreOuterName(outername).setName(name);
                availableOuterNames.put(name, ecoreOuterName);
                break;
        }
        throw new NotImplementedException();
    }

    /**
     * use {@link EcoreBigraphBuilder#createLinkOfEClass(String, LinkType)} in the future
     *
     * @return
     */
    @Deprecated
    private EcoreEdge createEdgeOfEClass() {
        EObject eObject = loadedEPackage.getEFactoryInstance().create(availableEClassMap.get(BigraphMetaModelConstants.CLASS_EDGE));
        EcoreEdge ecoreEdge = new EcoreEdge(eObject).setName(edgeNameSupplier.get());
        availableEdges.put(ecoreEdge.getName(), ecoreEdge);
        return ecoreEdge;
    }

    private boolean checkSameSignature(C control) {
        return iterableToList(signature.getControls()).contains(control);
    }

    public DefaultEcoreBigraph createBigraph() {
        DefaultEcoreBigraph bigraph = new DefaultEcoreBigraph(null);
        bigraph.setRoot(currentRoot);
        return bigraph;
    }

    public class EcoreInnerName extends BInnerNameImpl implements InnerName {
        private EObject instance;
        private String name;

        public EcoreInnerName(EObject instance) {
            this.instance = instance;
        }

        EcoreInnerName setName(String name) {
            this.name = name;
            return this;
        }

        public String getName() {
            return name;
        }

        @Override
        public PointType getPointType() {
            return PointType.INNER_NAME;
        }

        public EObject getInstance() {
            return instance;
        }
    }

    // cannot be instantiated from outside
    //"decorator" the keep the instance name
    public class EcoreOuterName extends BOuterNameImpl implements OuterName { //extends BOuterNameImpl
        private EObject instance;
        private String name;

        EcoreOuterName(@NonNull EObject instance) {
            this.instance = instance;
        }

        public EObject getInstance() {
            return instance;
        }

        /**
         * "instance" name of this node
         *
         * @return
         */
        public String getName() {
            return name;
        }

        EcoreOuterName setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public LinkType getLinkType() {
            return LinkType.OUTER_NAME;
        }
    }

    public class EcoreEdge extends BEdgeImpl implements Edge {
        private EObject instance;
        private String name;

        EcoreEdge(@NonNull EObject instance) {
            this.instance = instance;
        }

        public EObject getInstance() {
            return instance;
        }

        /**
         * "instance" name of this node
         *
         * @return
         */
        public String getName() {
            return name;
        }

        EcoreEdge setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public LinkType getLinkType() {
            return LinkType.EDGE;
        }
    }

    // cannot be instantiated from outside
    public class EcoreNode extends BNodeImpl implements Node {
        private EObject instance;
        private C control;
        PlaceType placeType;
        private String name;

        public EcoreNode(@NonNull EObject instance, C control, PlaceType placeType) {
            this.instance = instance;
            this.control = control;
            this.placeType = placeType;
        }

        public EObject getInstance() {
            return instance;
        }

        /**
         * "instance" name of this node
         *
         * @return
         */
        public String getName() {
            return name;
        }

        EcoreNode setName(String name) {
            this.name = name;
            return this;
        }

        public C getControl() {
            return control;
        }

        @Override
        public PlaceType getPlaceType() {
            return placeType;
        }
    }

    public class EcoreRoot extends BRootImpl implements Root {
        private EObject instance;
        private String name;

        public EcoreRoot(@NonNull EObject instance) {
            this.instance = instance;
        }

        public EObject getInstance() {
            return instance;
        }

        /**
         * "instance" name of this node
         *
         * @return
         */
        public String getName() {
            return name;
        }

        EcoreRoot setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public int getIndex() {
            EAttribute attribute = EMFUtils.findAttribute(instance.eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX);
            return (int) this.instance.eGet(attribute);
        }

        @Override
        public PlaceType getPlaceType() {
            return PlaceType.ROOT;
        }
    }

    private List<C> iterableToList(Iterable<C> controls) {
        List<C> list = new ArrayList<>();
        controls.iterator().forEachRemaining(list::add);
        return list;
    }


    public interface PlaceGenerator<C> {
        PlaceGenerator<C> createSite();

        PlaceGenerator<C> addAndEnterNewLevel(C control);
    }

    public void WRITE_DEBUG() {
        try {
            List<EObject> allresources = new ArrayList<>();
//            allresources.add(currentRoot.getInstance());
            availableRoots.forEach((s, x) -> allresources.add(x.getInstance()));
            availableOuterNames.forEach((s, x) -> allresources.add(x.getInstance()));
            availableInnerNames.forEach((s, x) -> allresources.add(x.getInstance()));
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
        try {
            loadedEPackage = EMFUtils.loadEcoreModel("/model/bigraphDomainModel.ecore");
        } catch (IOException e) {
            throw new BigraphMetaModelLoadingFailedException();
        }
        Iterable<C> controls = signature.getControls();
        StreamSupport.stream(controls.spliterator(), false)
                .forEach(x -> {
                    EClass entityClass = (EClass) loadedEPackage.getEClassifier("BNode");
                    String s = x.getNamedType().stringValue();
                    EClass newControlClass = EMFUtils.createEClass(s);
                    controlMap.put(s, newControlClass);
                    loadedEPackage.getEClassifiers().add(newControlClass);
                    EMFUtils.addSuperType(newControlClass, loadedEPackage, entityClass.getName());
                });
        List<EReference> allrefs = new ArrayList<>();
        EList<EObject> eObjects = loadedEPackage.eContents();
        for (EObject each : eObjects) {
            availableEClassMap.put(((EClassImpl) each).getName(), (EClassImpl) each);
            allrefs.addAll(EMFUtils.findAllReferences((EClass) each));
        }

        //all references are opposite edges, should be 6 in total
        System.out.println("REFS: " + allrefs.toString());
        assert allrefs.size() == 6;
        allrefs.forEach(x -> referenceMap.put(x.getName(), x));

//        referenceMap.put("BPlace_BPrnt", bigraphEFactoryInstance.eCrossReferences().get(0));
        //returns an instance of dynamic factory EFactoryImpl since we do not have a generated one
        bigraphEFactoryInstance = loadedEPackage.getEFactoryInstance();
//        newPackage = EMFUtils.createPackage("newmodel", "newmodel", "http://www.example.com/");
        loadedEPackage.setNsURI("http:///com.ibm.dynamic.example.bookstore.ecore");
        loadedEPackage.setName("BigraphMetaModel");
        loadedEPackage.setNsPrefix("extendedSignature");
        EMFUtils.serializeMetaModel(loadedEPackage, "bookStore", System.out);
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
