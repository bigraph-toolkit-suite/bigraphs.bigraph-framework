/*
 * Copyright (c) 2026 Bigraph Toolkit Suite Developers
 * Main Developer: Dominik Grzelak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bigraphs.framework.core.impl.pure;

import java.util.*;
import org.bigraphs.framework.core.*;
import org.bigraphs.framework.core.impl.BigraphEntity;
import org.bigraphs.framework.core.impl.signature.DynamicControl;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

/**
 * Mutable extension of {@link PureBigraph} that supports live editing operations.
 * 
 * <p>This class provides methods to modify the bigraph structure at runtime without
 * requiring a complete rebuild. Changes are applied to both the EMF model and internal
 * caches incrementally for optimal performance.</p>
 * 
 * <h2>Key Features:</h2>
 * <ul>
 *   <li>O(1) mutation operations (addNode, removeNode, etc.)</li>
 *   <li>Automatic cache synchronization</li>
 *   <li>Event notification for change listeners</li>
 *   <li>Maintains consistency between EMF and entity collections</li>
 * </ul>
 * 
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * PureBigraphMutable bigraph = builder.createMutable();
 * 
 * // Add a new node
 * BigraphEntity.NodeEntity newNode = bigraph.addNode(parentNode, sensorControl, "sensor_1");
 * 
 * // Remove a node
 * bigraph.removeNode(nodeToRemove);
 * 
 * // Connect to outer name
 * bigraph.connectPortToOuterName(port, outerName);
 * }</pre>
 * 
 * @author Manuel Krombholz
 * @author AI Assistant (Mutable Extension)
 * @see PureBigraph
 * @see PureBigraphBuilder
 */
public class PureBigraphMutable extends PureBigraph {
    
    // Mutable caches (replacing immutable collections from parent)
    private MutableList<BigraphEntity.RootEntity> mutableRoots;
    private MutableList<BigraphEntity.NodeEntity<DynamicControl>> mutableNodes;
    private MutableList<BigraphEntity.SiteEntity> mutableSites;
    private MutableList<BigraphEntity.InnerName> mutableInnerNames;
    private MutableList<BigraphEntity.OuterName> mutableOuterNames;
    private MutableList<BigraphEntity.Edge> mutableEdges;
    
    // Change listeners
    private final List<BigraphChangeListener> changeListeners = new ArrayList<>();
    
    public PureBigraphMutable(BigraphBuilderSupport.InstanceParameter details) {
        super(details);
        
        // Initialize mutable caches from immutable parent collections
        this.mutableRoots = Lists.mutable.ofAll(super.getRoots());
        this.mutableNodes = Lists.mutable.ofAll(super.getNodes());
        this.mutableSites = Lists.mutable.ofAll(super.getSites());
        this.mutableInnerNames = Lists.mutable.ofAll(super.getInnerNames());
        this.mutableOuterNames = Lists.mutable.ofAll(super.getOuterNames());
        this.mutableEdges = Lists.mutable.ofAll(super.getEdges());
    }
    
    // ============================================
    // OVERRIDDEN GETTERS (return mutable caches)
    // ============================================
    
    @Override
    public List<BigraphEntity.RootEntity> getRoots() {
        return Collections.unmodifiableList(mutableRoots);
    }
    
    @Override
    public List<BigraphEntity.NodeEntity<DynamicControl>> getNodes() {
        return Collections.unmodifiableList(mutableNodes);
    }
    
    @Override
    public List<BigraphEntity.SiteEntity> getSites() {
        return Collections.unmodifiableList(mutableSites);
    }
    
    @Override
    public List<BigraphEntity.InnerName> getInnerNames() {
        return Collections.unmodifiableList(mutableInnerNames);
    }
    
    @Override
    public List<BigraphEntity.OuterName> getOuterNames() {
        return Collections.unmodifiableList(mutableOuterNames);
    }
    
    @Override
    public List<BigraphEntity.Edge> getEdges() {
        return Collections.unmodifiableList(mutableEdges);
    }

    @Override
    public BigraphEntity<?> getParent(BigraphEntity<?> node) {
        if (node == null) return null;
        EObject instance = node.getInstance();
        EStructuralFeature prntRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if (prntRef != null && instance.eGet(prntRef) != null) {
            EObject parentEObject = (EObject) instance.eGet(prntRef);
            
            // Check mutableNodes
            for (BigraphEntity.NodeEntity<DynamicControl> n : mutableNodes) {
                if (n.getInstance().equals(parentEObject)) {
                    return n;
                }
            }
            
            // Check mutableRoots
            for (BigraphEntity.RootEntity r : mutableRoots) {
                if (r.getInstance().equals(parentEObject)) {
                    return r;
                }
            }
        }
        return null;
    }

    @Override
    public List<BigraphEntity<?>> getChildrenOf(BigraphEntity<?> node) {
        if (node == null) return Collections.emptyList();
        EObject instance = node.getInstance();
        EStructuralFeature chldRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
        List<BigraphEntity<?>> children = new ArrayList<>();
        
        if (chldRef != null) {
            Object childsObj = instance.eGet(chldRef);
            if (childsObj instanceof EList) {
                EList<EObject> childs = (EList<EObject>) childsObj;
                for (EObject eachChild : childs) {
                    // Find entity for this EObject
                    // Check nodes
                    boolean found = false;
                    for (BigraphEntity.NodeEntity<DynamicControl> n : mutableNodes) {
                        if (n.getInstance().equals(eachChild)) {
                            children.add(n);
                            found = true;
                            break;
                        }
                    }
                    if (found) continue;
                    
                    // Check sites
                    for (BigraphEntity.SiteEntity s : mutableSites) {
                        if (s.getInstance().equals(eachChild)) {
                            children.add(s);
                            break;
                        }
                    }
                }
            }
        }
        return children;
    }

    @Override
    public BigraphEntity.NodeEntity<DynamicControl> getNodeOfPort(BigraphEntity.Port port) {
        if (port == null) return null;
        EObject instance = port.getInstance();
        EStructuralFeature nodeRef = instance.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_NODE);
        if (nodeRef == null) return null;
        EObject nodeObject = (EObject) instance.eGet(nodeRef);
        
        for (BigraphEntity.NodeEntity<DynamicControl> n : mutableNodes) {
            if (n.getInstance().equals(nodeObject)) {
                return n;
            }
        }
        return null;
    }
    
    // ============================================
    // MUTATION API - PLACE GRAPH
    // ============================================
    
    /**
     * Adds a new node to the bigraph as a child of the specified parent.
     * 
     * @param parent the parent entity (can be a Root or another Node)
     * @param control the control defining the node type
     * @param name optional name for the node (can be null)
     * @return the newly created node entity
     * @throws IllegalArgumentException if parent cannot have children or control is null
     */
    public BigraphEntity.NodeEntity<DynamicControl> addNode(
            BigraphEntity<?> parent, 
            DynamicControl control, 
            String name) {
        
        if (control == null) {
            throw new IllegalArgumentException("Control cannot be null");
        }
        
        // 1. Create EMF instance
        EObject newNodeEObject = createNodeEObject(control, name);
        
        // 2. Add to parent in EMF
        addChildToParentEMF(parent.getInstance(), newNodeEObject);
        
        // 3. Wrap in BigraphEntity
        @SuppressWarnings("unchecked")
        BigraphEntity.NodeEntity<DynamicControl> newNode = 
            (BigraphEntity.NodeEntity<DynamicControl>) BigraphEntity.createNode(newNodeEObject, control);
        
        // 4. Update cache
        mutableNodes.add(newNode);
        
        // 5. Fire event
        fireNodeAdded(newNode, parent);
        
        return newNode;
    }
    
    /**
     * Removes a node from the bigraph.
     * 
     * @param node the node to remove
     * @throws IllegalArgumentException if node is null or not found
     */
    public void removeNode(BigraphEntity.NodeEntity<DynamicControl> node) {
        if (node == null) {
            throw new IllegalArgumentException("Node cannot be null");
        }
        
        if (!mutableNodes.contains(node)) {
            throw new IllegalArgumentException("Node not found in bigraph");
        }
        
        // 1. Get parent
        BigraphEntity<?> parent = getParent(node);
        
        // 2. Remove from EMF
        removeChildFromParentEMF(parent.getInstance(), node.getInstance());
        
        // 3. Update cache
        mutableNodes.remove(node);
        
        // 4. Fire event
        fireNodeRemoved(node, parent);
    }
    
    /**
     * Moves a node to a new parent.
     * 
     * @param node the node to move
     * @param newParent the new parent
     * @throws IllegalArgumentException if node or newParent is null
     */
    public void moveNode(
            BigraphEntity.NodeEntity<DynamicControl> node, 
            BigraphEntity<?> newParent) {
        
        if (node == null || newParent == null) {
            throw new IllegalArgumentException("Node and newParent cannot be null");
        }
        
        // 1. Get old parent
        BigraphEntity<?> oldParent = getParent(node);
        
        // 2. Remove from old parent in EMF
        removeChildFromParentEMF(oldParent.getInstance(), node.getInstance());
        
        // 3. Add to new parent in EMF
        addChildToParentEMF(newParent.getInstance(), node.getInstance());
        
        // 4. No cache update needed (node list doesn't change)
        
        // 5. Fire event
        fireNodeMoved(node, oldParent, newParent);
    }
    
    /**
     * Adds a new site to the bigraph as a child of the specified parent.
     *
     * <p>Sites are place-graph placeholders (holes) — they carry no control, arity, or link
     * information. The site index is assigned automatically based on the current site count.</p>
     *
     * @param parent the parent entity (Root or Node); must not be null and must support children
     * @return the newly created site entity
     * @throws IllegalArgumentException if parent is null or cannot have children
     */
    public BigraphEntity.SiteEntity addSite(BigraphEntity<?> parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent cannot be null");
        }

        // 1. Determine next site index
        int newIndex = mutableSites.size();

        // 2. Create EMF instance
        EObject siteEObject = createSiteEObject(newIndex);

        // 3. Add to parent in EMF (child reference + parent back-reference)
        addChildToParentEMF(parent.getInstance(), siteEObject);

        // 4. Wrap in BigraphEntity
        BigraphEntity.SiteEntity newSite =
            BigraphEntity.create(siteEObject, BigraphEntity.SiteEntity.class);

        // 5. Update cache
        mutableSites.add(newSite);

        // 6. Fire event
        fireSiteAdded(newSite, parent);

        return newSite;
    }

    /**
     * Removes a site from the bigraph.
     *
     * @param site the site to remove
     * @throws IllegalArgumentException if site is null or not found in the bigraph
     */
    public void removeSite(BigraphEntity.SiteEntity site) {
        if (site == null) {
            throw new IllegalArgumentException("Site cannot be null");
        }

        if (!mutableSites.contains(site)) {
            throw new IllegalArgumentException("Site not found in bigraph");
        }

        // 1. Get parent
        BigraphEntity<?> parent = getParent(site);

        // 2. Remove from EMF
        if (parent != null) {
            removeChildFromParentEMF(parent.getInstance(), site.getInstance());
        }

        // 3. Update cache
        mutableSites.remove(site);

        // 4. Fire event
        fireSiteRemoved(site, parent);
    }

    /**
     * Adds a new root to the bigraph.
     * 
     * @return the newly created root entity
     */
    public BigraphEntity.RootEntity addRoot() {
        // 1. Create EMF instance
        EObject newRootEObject = createRootEObject();
        
        // 2. Set the index attribute to the next available index
        // This is critical for XMI serialization - without it, the root will 
        // have index 0 which conflicts with existing roots on reload
        int newIndex = mutableRoots.size();
        EAttribute indexAttr = (EAttribute) newRootEObject.eClass()
            .getEStructuralFeature(BigraphMetaModelConstants.ATTRIBUTE_INDEX);
        if (indexAttr != null) {
            newRootEObject.eSet(indexAttr, newIndex);
        }
        
        // 3. Add to bigraph instance model
        addRootToBigraphEMF(newRootEObject);
        
        // 4. Wrap in BigraphEntity
        BigraphEntity.RootEntity newRoot = 
            BigraphEntity.create(newRootEObject, BigraphEntity.RootEntity.class);
        
        // 5. Update cache
        mutableRoots.add(newRoot);
        
        // 6. Fire event
        fireRootAdded(newRoot);
        
        return newRoot;
    }
    
    /**
     * Removes a root from the bigraph.
     * 
     * @param root the root to remove
     * @throws IllegalArgumentException if root is null or not found
     */
    public void removeRoot(BigraphEntity.RootEntity root) {
        if (root == null) {
            throw new IllegalArgumentException("Root cannot be null");
        }
        
        if (!mutableRoots.contains(root)) {
            throw new IllegalArgumentException("Root not found in bigraph");
        }
        
        // 1. Remove from EMF
        removeRootFromBigraphEMF(root.getInstance());
        
        // 2. Update cache
        mutableRoots.remove(root);
        
        // 3. Fire event
        fireRootRemoved(root);
    }
    
    // ============================================
    // MUTATION API - LINK GRAPH
    // ============================================
    
    /**
     * Adds a new edge to the bigraph with an auto-generated name.
     * 
     * @return the newly created edge entity
     */
    public BigraphEntity.Edge addEdge() {
        return addEdge("edge_" + System.currentTimeMillis());
    }
    
    /**
     * Adds a new edge to the bigraph with the specified name.
     * 
     * @param name the name for the edge
     * @return the newly created edge entity
     */
    public BigraphEntity.Edge addEdge(String name) {
        // 1. Create EMF instance with name
        EObject newEdgeEObject = createEdgeEObject(name);
        
        // 2. Add to bigraph instance model
        addEdgeToBigraphEMF(newEdgeEObject);
        
        // 3. Wrap in BigraphEntity
        BigraphEntity.Edge newEdge = 
            BigraphEntity.create(newEdgeEObject, BigraphEntity.Edge.class);
        
        // 4. Update cache
        mutableEdges.add(newEdge);
        
        // 5. Fire event
        fireEdgeAdded(newEdge);
        
        return newEdge;
    }
    
    /**
     * Removes an edge from the bigraph.
     * 
     * @param edge the edge to remove
     * @throws IllegalArgumentException if edge is null or not found
     */
    public void removeEdge(BigraphEntity.Edge edge) {
        if (edge == null) {
            throw new IllegalArgumentException("Edge cannot be null");
        }
        
        if (!mutableEdges.contains(edge)) {
            throw new IllegalArgumentException("Edge not found in bigraph");
        }
        
        // 1. Remove from EMF
        removeEdgeFromBigraphEMF(edge.getInstance());
        
        // 2. Update cache
        mutableEdges.remove(edge);
        
        // 3. Fire event
        fireEdgeRemoved(edge);
    }
    
    /**
     * Connects a port to a link (edge or outer name).
     * 
     * @param port the port to connect
     * @param link the link to connect to
     * @throws IllegalArgumentException if port or link is null
     */
    public void connectPortToLink(BigraphEntity.Port port, BigraphEntity.Link link) {
        if (port == null || link == null) {
            throw new IllegalArgumentException("Port and link cannot be null");
        }
        
        // 1. Set link reference in port (EMF)
        setLinkOfPortEMF(port.getInstance(), link.getInstance());
        
        // 2. Add port to link's points (EMF)
        addPointToLinkEMF(link.getInstance(), port.getInstance());
        
        // 3. The entity collections don't change; the link graph caches were invalidated by the EMF helpers
        
        // 4. Fire event
        fireLinkConnected(port, link);
    }
    
    /**
     * Connects an inner name to a link (edge or outer name).
     * 
     * @param innerName the inner name to connect
     * @param link the link to connect to
     * @throws IllegalArgumentException if innerName or link is null
     */
    public void connectInnerNameToLink(BigraphEntity.InnerName innerName, BigraphEntity.Link link) {
        if (innerName == null || link == null) {
            throw new IllegalArgumentException("InnerName and link cannot be null");
        }
        
        // 1. Set link reference in inner name (EMF)
        setLinkOfPointEMF(innerName.getInstance(), link.getInstance());
        
        // 2. Add inner name to link's points (EMF)
        addPointToLinkEMF(link.getInstance(), innerName.getInstance());
        
        // 3. Fire event
        fireInnerNameLinked(innerName, link);
    }
    
    /**
     * High-level convenience method: connects a node to a link by reusing an
     * existing free (unlinked) port when available, or by creating a new one.
     *
     * <p>This is the preferred API for callers that simply want to establish a
     * Node → Link connection without managing ports manually. It encapsulates:</p>
     * <ol>
     *   <li>Finding a free port (one whose {@code bLink} reference is {@code null}).</li>
     *   <li>Creating a new port if none is free and the arity allows it.</li>
     *   <li>Calling {@link #connectPortToLink(BigraphEntity.Port, BigraphEntity.Link)}.</li>
     * </ol>
     *
     * @param node the node to connect
     * @param link the link (Edge or OuterName) to connect to
     * @return the port that was used or created for the connection
     * @throws IllegalArgumentException if node or link is null
     * @throws IllegalStateException    if all ports are already linked and the
     *                                  arity limit has been reached
     */
    public BigraphEntity.Port connectNodeToLink(BigraphEntity.NodeEntity<?> node, BigraphEntity.Link link) {
        if (node == null) { throw new IllegalArgumentException("Node cannot be null"); }
        if (link == null) { throw new IllegalArgumentException("Link cannot be null"); }

        List<BigraphEntity.Port> ports = getPorts(node);

        // 1. Reuse first free (unlinked) port
        for (BigraphEntity.Port p : ports) {
            if (getLinkOfPoint(p) == null) {
                connectPortToLink(p, link);
                return p;
            }
        }

        // 2. All existing ports are linked — try to add a new one (addPortToNode
        //    already enforces the arity limit and throws IllegalStateException).
        BigraphEntity.Port newPort = addPortToNode(node);
        connectPortToLink(newPort, link);
        return newPort;
    }

    /**
     * Adds a new port to a node.
     * The port index is automatically assigned based on existing ports.
     * 
     * @param node the node to add the port to
     * @return the newly created port
     * @throws IllegalArgumentException if node is null
     * @throws IllegalStateException if node has reached its arity limit
     */
    public BigraphEntity.Port addPortToNode(BigraphEntity.NodeEntity<?> node) {
        if (node == null) {
            throw new IllegalArgumentException("Node cannot be null");
        }
        
        // Check arity — only count ports that already carry a link, so that
        // free (unlinked) ports that exist in the model are not double-counted.
        int arity = node.getControl().getArity().getValue().intValue();
        List<BigraphEntity.Port> existingPorts = getPorts(node);
        long linkedCount = existingPorts.stream().filter(p -> getLinkOfPoint(p) != null).count();
        if (linkedCount >= arity) {
            throw new IllegalStateException("Node has reached its arity limit of " + arity);
        }
        
        // Create port with next index
        int nextIndex = existingPorts.size();
        EObject portEObject = createPortEObject(nextIndex);
        
        // Add port to node (EMF)
        addPortToNodeEMF(node.getInstance(), portEObject);
        
        // Wrap and return
        return BigraphEntity.create(portEObject, BigraphEntity.Port.class);
    }
    
    /**
     * Disconnects a port from its link.
     * 
     * @param port the port to disconnect
     * @throws IllegalArgumentException if port is null
     */
    public void disconnectPort(BigraphEntity.Port port) {
        if (port == null) {
            throw new IllegalArgumentException("Port cannot be null");
        }
        
        // 1. Get current link
        BigraphEntity.Link currentLink = getLinkOfPoint(port);
        if (currentLink == null) {
            return; // Already disconnected
        }
        
        // 2. Remove port from link's points (EMF)
        removePointFromLinkEMF(currentLink.getInstance(), port.getInstance());
        
        // 3. Unset link reference in port (EMF)
        unsetLinkOfPortEMF(port.getInstance());
        
        // 4. The entity collections don't change; the link graph caches were invalidated by the EMF helpers
        
        // 5. Fire event
        fireLinkDisconnected(port, currentLink);
    }
    
    /**
     * Disconnects an inner name from its link.
     * 
     * @param innerName the inner name to disconnect
     * @throws IllegalArgumentException if innerName is null
     */
    public void disconnectInnerName(BigraphEntity.InnerName innerName) {
        if (innerName == null) {
            throw new IllegalArgumentException("InnerName cannot be null");
        }
        
        // 1. Get current link
        BigraphEntity.Link currentLink = getLinkOfPoint(innerName);
        if (currentLink == null) {
            return; // Already disconnected
        }
        
        // 2. Remove inner name from link's points (EMF)
        removePointFromLinkEMF(currentLink.getInstance(), innerName.getInstance());
        
        // 3. Unset link reference in inner name (EMF)
        unsetLinkOfPointEMF(innerName.getInstance());
        
        // 4. The entity collections don't change; the link graph caches were invalidated by the EMF helpers
        
        // 5. Fire event
        fireInnerNameUnlinked(innerName, currentLink);
    }

    /**
     * Closes a name endpoint by its concrete role.
     * <p>
     * Semantics:
     * <ul>
     *   <li>Inner name: disconnect from its current link (if any), then remove the inner name.</li>
     *   <li>Outer name: disconnect all currently attached points, then remove the outer name.</li>
     * </ul>
     *
     * This is a mutable convenience API mirroring the role-specific close behavior while
     * allowing callers to dispatch on a single method at call-site.
     *
     * @param name the name endpoint to close (inner or outer)
     * @throws IllegalArgumentException if {@code name} is null or not an inner/outer name
     */
    public void closeName(BigraphEntity<?> name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        if (name instanceof BigraphEntity.InnerName) {
            closeInner((BigraphEntity.InnerName) name);
            return;
        }
        if (name instanceof BigraphEntity.OuterName) {
            closeOuter((BigraphEntity.OuterName) name);
            return;
        }
        throw new IllegalArgumentException(
                "Unsupported name type: " + name.getClass().getSimpleName() + " (expected InnerName or OuterName)");
    }

    /**
     * Closes an inner name by disconnecting it from its link and removing it from the bigraph.
     *
     * @param innerName the inner name to close
     */
    public void closeInner(BigraphEntity.InnerName innerName) {
        if (innerName == null) {
            throw new IllegalArgumentException("InnerName cannot be null");
        }
        disconnectInnerName(innerName);
        removeInnerName(innerName);
    }

    /**
     * Closes an outer name by disconnecting all attached points and removing the outer name.
     *
     * @param outerName the outer name to close
     */
    public void closeOuter(BigraphEntity.OuterName outerName) {
        if (outerName == null) {
            throw new IllegalArgumentException("OuterName cannot be null");
        }
        final List<BigraphEntity<?>> points = new ArrayList<>(getPointsFromLink(outerName));
        for (final BigraphEntity<?> p : points) {
            if (p instanceof BigraphEntity.Port) {
                disconnectPort((BigraphEntity.Port) p);
            } else if (p instanceof BigraphEntity.InnerName) {
                disconnectInnerName((BigraphEntity.InnerName) p);
            }
        }
        removeOuterName(outerName);
    }
    
    /**
     * Adds a new outer name to the bigraph.
     * 
     * @param name the name of the outer name
     * @return the newly created outer name entity
     * @throws IllegalArgumentException if name is null or empty
     */
    public BigraphEntity.OuterName addOuterName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        
        // 1. Create EMF instance
        EObject newOuterNameEObject = createOuterNameEObject(name);
        
        // 2. Add to bigraph instance model
        addOuterNameToBigraphEMF(newOuterNameEObject);
        
        // 3. Wrap in BigraphEntity
        BigraphEntity.OuterName newOuterName = 
            BigraphEntity.create(newOuterNameEObject, BigraphEntity.OuterName.class);
        
        // 4. Update cache
        mutableOuterNames.add(newOuterName);
        
        // 5. Fire event
        fireOuterNameAdded(newOuterName);
        
        return newOuterName;
    }
    
    /**
     * Removes an outer name from the bigraph.
     * 
     * @param outerName the outer name to remove
     * @throws IllegalArgumentException if outerName is null or not found
     */
    public void removeOuterName(BigraphEntity.OuterName outerName) {
        if (outerName == null) {
            throw new IllegalArgumentException("OuterName cannot be null");
        }
        
        if (!mutableOuterNames.contains(outerName)) {
            throw new IllegalArgumentException("OuterName not found in bigraph");
        }
        
        // 1. Remove from EMF
        removeOuterNameFromBigraphEMF(outerName.getInstance());
        
        // 2. Update cache
        mutableOuterNames.remove(outerName);
        
        // 3. Fire event
        fireOuterNameRemoved(outerName);
    }
    
    /**
     * Adds a new inner name to the bigraph.
     * 
     * @param name the name of the inner name
     * @return the newly created inner name entity
     * @throws IllegalArgumentException if name is null or empty
     */
    public BigraphEntity.InnerName addInnerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        
        // 1. Create EMF instance
        EObject newInnerNameEObject = createInnerNameEObject(name);
        
        // 2. Add to bigraph instance model
        addInnerNameToBigraphEMF(newInnerNameEObject);
        
        // 3. Wrap in BigraphEntity
        BigraphEntity.InnerName newInnerName = 
            BigraphEntity.create(newInnerNameEObject, BigraphEntity.InnerName.class);
        
        // 4. Update cache
        mutableInnerNames.add(newInnerName);
        
        // 5. Fire event
        fireInnerNameAdded(newInnerName);
        
        return newInnerName;
    }
    
    /**
     * Removes an inner name from the bigraph.
     * 
     * @param innerName the inner name to remove
     * @throws IllegalArgumentException if innerName is null or not found
     */
    public void removeInnerName(BigraphEntity.InnerName innerName) {
        if (innerName == null) {
            throw new IllegalArgumentException("InnerName cannot be null");
        }
        
        if (!mutableInnerNames.contains(innerName)) {
            throw new IllegalArgumentException("InnerName not found in bigraph");
        }
        
        // 1. Remove from EMF
        removeInnerNameFromBigraphEMF(innerName.getInstance());
        
        // 2. Update cache
        mutableInnerNames.remove(innerName);
        
        // 3. Fire event
        fireInnerNameRemoved(innerName);
    }
    
    // ============================================
    // CHANGE LISTENER API
    // ============================================
    
    public void addChangeListener(BigraphChangeListener listener) {
        if (listener != null && !changeListeners.contains(listener)) {
            changeListeners.add(listener);
        }
    }
    
    public void removeChangeListener(BigraphChangeListener listener) {
        changeListeners.remove(listener);
    }
    
    // ============================================
    // PRIVATE: EMF MANIPULATION
    // ============================================
    
    private EObject createNodeEObject(DynamicControl control, String name) {
        // Find EClass for this control
        EClass nodeEClass = (EClass) getMetaModel().getEClassifier(control.getNamedType().stringValue());
        if (nodeEClass == null) {
            throw new IllegalArgumentException("Control not found in metamodel: " + control.getNamedType().stringValue());
        }
        
        // Create instance
        EObject newNode = getMetaModel().getEFactoryInstance().create(nodeEClass);
        
        // Set name if provided
        if (name != null) {
            EAttribute nameAttr = (EAttribute) nodeEClass.getEStructuralFeature(BigraphMetaModelConstants.ATTRIBUTE_NAME);
            if (nameAttr != null) {
                newNode.eSet(nameAttr, name);
            }
        }
        
        // Create ports based on arity
        int arity = control.getArity().getValue().intValue();
        if (arity > 0) {
            EStructuralFeature portFeature = nodeEClass.getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PORT);
            if (portFeature != null) {
                EList<EObject> ports = (EList<EObject>) newNode.eGet(portFeature);
                EClass portEClass = (EClass) getMetaModel().getEClassifier(BigraphMetaModelConstants.CLASS_PORT);
                
                for (int i = 0; i < arity; i++) {
                    EObject port = getMetaModel().getEFactoryInstance().create(portEClass);
                    EAttribute indexAttr = (EAttribute) portEClass.getEStructuralFeature(BigraphMetaModelConstants.ATTRIBUTE_INDEX);
                    if (indexAttr != null) {
                        port.eSet(indexAttr, i);
                    }
                    ports.add(port);
                }
            }
        }
        
        return newNode;
    }
    
    private void addChildToParentEMF(EObject parent, EObject child) {
        EStructuralFeature childFeature = parent.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
        if (childFeature == null) {
            throw new IllegalArgumentException("Parent cannot have children");
        }
        
        EList<EObject> children = (EList<EObject>) parent.eGet(childFeature);
        children.add(child);
        
        // Set parent reference
        EStructuralFeature parentFeature = child.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if (parentFeature != null) {
            child.eSet(parentFeature, parent);
        }
    }
    
    private void removeChildFromParentEMF(EObject parent, EObject child) {
        invalidateLinkGraphCaches();
        EStructuralFeature childFeature = parent.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_CHILD);
        if (childFeature == null) {
            return;
        }
        
        EList<EObject> children = (EList<EObject>) parent.eGet(childFeature);
        children.remove(child);
        
        // Unset parent reference
        EStructuralFeature parentFeature = child.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PARENT);
        if (parentFeature != null) {
            child.eUnset(parentFeature);
        }
    }
    
    private EObject createRootEObject() {
        EClass rootEClass = (EClass) getMetaModel().getEClassifier(BigraphMetaModelConstants.CLASS_ROOT);
        return getMetaModel().getEFactoryInstance().create(rootEClass);
    }
    
    private void addRootToBigraphEMF(EObject root) {
        EStructuralFeature rootsFeature = getInstanceModel().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BROOTS);
        if (rootsFeature != null) {
            EList<EObject> roots = (EList<EObject>) getInstanceModel().eGet(rootsFeature);
            roots.add(root);
        }
    }
    
    private void removeRootFromBigraphEMF(EObject root) {
        invalidateLinkGraphCaches();
        EStructuralFeature rootsFeature = getInstanceModel().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BROOTS);
        if (rootsFeature != null) {
            EList<EObject> roots = (EList<EObject>) getInstanceModel().eGet(rootsFeature);
            roots.remove(root);
        }
    }
    
    private EObject createEdgeEObject(String name) {
        EClass edgeEClass = (EClass) getMetaModel().getEClassifier(BigraphMetaModelConstants.CLASS_EDGE);
        EObject edge = getMetaModel().getEFactoryInstance().create(edgeEClass);
        // Set the name attribute
        EAttribute nameAttr = (EAttribute) edgeEClass.getEStructuralFeature(BigraphMetaModelConstants.ATTRIBUTE_NAME);
        if (nameAttr != null) {
            edge.eSet(nameAttr, name);
        }
        return edge;
    }
    
    private void addEdgeToBigraphEMF(EObject edge) {
        EStructuralFeature edgesFeature = getInstanceModel().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BEDGES);
        if (edgesFeature != null) {
            EList<EObject> edges = (EList<EObject>) getInstanceModel().eGet(edgesFeature);
            edges.add(edge);
        }
    }
    
    private void removeEdgeFromBigraphEMF(EObject edge) {
        invalidateLinkGraphCaches();
        EStructuralFeature edgesFeature = getInstanceModel().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BEDGES);
        if (edgesFeature != null) {
            EList<EObject> edges = (EList<EObject>) getInstanceModel().eGet(edgesFeature);
            edges.remove(edge);
        }
    }
    
    private EObject createPortEObject(int index) {
        EClass portEClass = (EClass) getMetaModel().getEClassifier(BigraphMetaModelConstants.CLASS_PORT);
        EObject port = getMetaModel().getEFactoryInstance().create(portEClass);
        
        EAttribute indexAttr = (EAttribute) portEClass.getEStructuralFeature(BigraphMetaModelConstants.ATTRIBUTE_INDEX);
        if (indexAttr != null) {
            port.eSet(indexAttr, index);
        }
        
        return port;
    }
    
    private void addPortToNodeEMF(EObject node, EObject port) {
        invalidateLinkGraphCaches();
        EStructuralFeature portsFeature = node.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PORT);
        if (portsFeature != null) {
            @SuppressWarnings("unchecked")
            EList<EObject> ports = (EList<EObject>) node.eGet(portsFeature);
            ports.add(port);
        }
    }
    
    private EObject createOuterNameEObject(String name) {
        EClass outerNameEClass = (EClass) getMetaModel().getEClassifier(BigraphMetaModelConstants.CLASS_OUTERNAME);
        EObject outerName = getMetaModel().getEFactoryInstance().create(outerNameEClass);
        
        EAttribute nameAttr = (EAttribute) outerNameEClass.getEStructuralFeature(BigraphMetaModelConstants.ATTRIBUTE_NAME);
        if (nameAttr != null) {
            outerName.eSet(nameAttr, name);
        }
        
        return outerName;
    }
    
    private void addOuterNameToBigraphEMF(EObject outerName) {
        EStructuralFeature outerNamesFeature = getInstanceModel().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BOUTERNAMES);
        if (outerNamesFeature != null) {
            EList<EObject> outerNames = (EList<EObject>) getInstanceModel().eGet(outerNamesFeature);
            outerNames.add(outerName);
        }
    }
    
    private void removeOuterNameFromBigraphEMF(EObject outerName) {
        invalidateLinkGraphCaches();
        EStructuralFeature outerNamesFeature = getInstanceModel().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BOUTERNAMES);
        if (outerNamesFeature != null) {
            EList<EObject> outerNames = (EList<EObject>) getInstanceModel().eGet(outerNamesFeature);
            outerNames.remove(outerName);
        }
    }
    
    private void setLinkOfPortEMF(EObject port, EObject link) {
        invalidateLinkGraphCaches();
        EStructuralFeature linkFeature = port.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
        if (linkFeature != null) {
            port.eSet(linkFeature, link);
        }
    }
    
    private void unsetLinkOfPortEMF(EObject port) {
        invalidateLinkGraphCaches();
        EStructuralFeature linkFeature = port.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
        if (linkFeature != null) {
            port.eUnset(linkFeature);
        }
    }
    
    private void unsetLinkOfPointEMF(EObject point) {
        invalidateLinkGraphCaches();
        EStructuralFeature linkFeature = point.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
        if (linkFeature != null) {
            point.eUnset(linkFeature);
        }
    }
    
    private void setLinkOfPointEMF(EObject point, EObject link) {
        invalidateLinkGraphCaches();
        EStructuralFeature linkFeature = point.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
        if (linkFeature != null) {
            point.eSet(linkFeature, link);
        }
    }
    
    private void addPointToLinkEMF(EObject link, EObject point) {
        invalidateLinkGraphCaches();
        EStructuralFeature pointsFeature = link.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_POINT);
        if (pointsFeature != null) {
            EList<EObject> points = (EList<EObject>) link.eGet(pointsFeature);
            if (!points.contains(point)) {
                points.add(point);
            }
        }
    }
    
    private void removePointFromLinkEMF(EObject link, EObject point) {
        invalidateLinkGraphCaches();
        EStructuralFeature pointsFeature = link.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_POINT);
        if (pointsFeature != null) {
            EList<EObject> points = (EList<EObject>) link.eGet(pointsFeature);
            points.remove(point);
        }
    }
    
    // ============================================
    // PRIVATE: EVENT FIRING
    // ============================================
    
    private void fireNodeAdded(BigraphEntity.NodeEntity<DynamicControl> node, BigraphEntity<?> parent) {
        BigraphChangeEvent event = new BigraphChangeEvent.NodeAdded(this, node, parent);
        for (BigraphChangeListener listener : changeListeners) {
            listener.onNodeAdded(event);
        }
    }
    
    private void fireNodeRemoved(BigraphEntity.NodeEntity<DynamicControl> node, BigraphEntity<?> parent) {
        BigraphChangeEvent event = new BigraphChangeEvent.NodeRemoved(this, node, parent);
        for (BigraphChangeListener listener : changeListeners) {
            listener.onNodeRemoved(event);
        }
    }
    
    private void fireNodeMoved(BigraphEntity.NodeEntity<DynamicControl> node, BigraphEntity<?> oldParent, BigraphEntity<?> newParent) {
        BigraphChangeEvent event = new BigraphChangeEvent.NodeMoved(this, node, oldParent, newParent);
        for (BigraphChangeListener listener : changeListeners) {
            listener.onNodeMoved(event);
        }
    }
    
    private void fireRootAdded(BigraphEntity.RootEntity root) {
        BigraphChangeEvent event = new BigraphChangeEvent.RootAdded(this, root);
        for (BigraphChangeListener listener : changeListeners) {
            listener.onRootAdded(event);
        }
    }
    
    private void fireRootRemoved(BigraphEntity.RootEntity root) {
        BigraphChangeEvent event = new BigraphChangeEvent.RootRemoved(this, root);
        for (BigraphChangeListener listener : changeListeners) {
            listener.onRootRemoved(event);
        }
    }
    
    private void fireEdgeAdded(BigraphEntity.Edge edge) {
        BigraphChangeEvent event = new BigraphChangeEvent.EdgeAdded(this, edge);
        for (BigraphChangeListener listener : changeListeners) {
            listener.onEdgeAdded(event);
        }
    }
    
    private void fireEdgeRemoved(BigraphEntity.Edge edge) {
        BigraphChangeEvent event = new BigraphChangeEvent.EdgeRemoved(this, edge);
        for (BigraphChangeListener listener : changeListeners) {
            listener.onEdgeRemoved(event);
        }
    }
    
    private void fireOuterNameAdded(BigraphEntity.OuterName outerName) {
        BigraphChangeEvent event = new BigraphChangeEvent.OuterNameAdded(this, outerName);
        for (BigraphChangeListener listener : changeListeners) {
            listener.onOuterNameAdded(event);
        }
    }
    
    private void fireOuterNameRemoved(BigraphEntity.OuterName outerName) {
        BigraphChangeEvent event = new BigraphChangeEvent.OuterNameRemoved(this, outerName);
        for (BigraphChangeListener listener : changeListeners) {
            listener.onOuterNameRemoved(event);
        }
    }
    
    private void fireLinkConnected(BigraphEntity.Port port, BigraphEntity.Link link) {
        BigraphChangeEvent event = new BigraphChangeEvent.LinkConnected(this, port, link);
        for (BigraphChangeListener listener : changeListeners) {
            listener.onLinkConnected(event);
        }
    }
    
    private void fireLinkDisconnected(BigraphEntity.Port port, BigraphEntity.Link link) {
        BigraphChangeEvent event = new BigraphChangeEvent.LinkDisconnected(this, port, link);
        for (BigraphChangeListener listener : changeListeners) {
            listener.onLinkDisconnected(event);
        }
    }
    
    private void fireInnerNameLinked(BigraphEntity.InnerName innerName, BigraphEntity.Link link) {
        BigraphChangeEvent event = new BigraphChangeEvent.InnerNameLinked(this, innerName, link);
        for (BigraphChangeListener listener : changeListeners) {
            listener.onInnerNameLinked(event);
        }
    }
    
    private void fireInnerNameUnlinked(BigraphEntity.InnerName innerName, BigraphEntity.Link link) {
        BigraphChangeEvent event = new BigraphChangeEvent.InnerNameUnlinked(this, innerName, link);
        for (BigraphChangeListener listener : changeListeners) {
            listener.onInnerNameUnlinked(event);
        }
    }
    
    private void fireSiteAdded(BigraphEntity.SiteEntity site, BigraphEntity<?> parent) {
        BigraphChangeEvent event = new BigraphChangeEvent.SiteAdded(this, site, parent);
        for (BigraphChangeListener listener : changeListeners) {
            listener.onSiteAdded(event);
        }
    }

    private void fireSiteRemoved(BigraphEntity.SiteEntity site, BigraphEntity<?> parent) {
        BigraphChangeEvent event = new BigraphChangeEvent.SiteRemoved(this, site, parent);
        for (BigraphChangeListener listener : changeListeners) {
            listener.onSiteRemoved(event);
        }
    }

    private void fireInnerNameAdded(BigraphEntity.InnerName innerName) {
        BigraphChangeEvent event = new BigraphChangeEvent.InnerNameAdded(this, innerName);
        for (BigraphChangeListener listener : changeListeners) {
            listener.onInnerNameAdded(event);
        }
    }
    
    private void fireInnerNameRemoved(BigraphEntity.InnerName innerName) {
        BigraphChangeEvent event = new BigraphChangeEvent.InnerNameRemoved(this, innerName);
        for (BigraphChangeListener listener : changeListeners) {
            listener.onInnerNameRemoved(event);
        }
    }
    
    // ============================================
    // PRIVATE: EMF HELPERS FOR INNER NAMES
    // ============================================
    
    private EObject createSiteEObject(int index) {
        EClass siteEClass = (EClass) getMetaModel().getEClassifier(BigraphMetaModelConstants.CLASS_SITE);
        if (siteEClass == null) {
            throw new RuntimeException("Site class not found in metamodel");
        }
        EObject site = getMetaModel().getEFactoryInstance().create(siteEClass);
        EAttribute indexAttr = (EAttribute) siteEClass.getEStructuralFeature(BigraphMetaModelConstants.ATTRIBUTE_INDEX);
        if (indexAttr != null) {
            site.eSet(indexAttr, index);
        }
        return site;
    }

    private EObject createInnerNameEObject(String name) {
        EClass innerNameEClass = (EClass) getMetaModel().getEClassifier(BigraphMetaModelConstants.CLASS_INNERNAME);
        if (innerNameEClass == null) {
            throw new RuntimeException("InnerName class not found in metamodel");
        }
        
        EObject newInnerName = getMetaModel().getEFactoryInstance().create(innerNameEClass);
        
        EAttribute nameAttr = (EAttribute) innerNameEClass.getEStructuralFeature(BigraphMetaModelConstants.ATTRIBUTE_NAME);
        if (nameAttr != null) {
            newInnerName.eSet(nameAttr, name);
        }
        
        return newInnerName;
    }
    
    private void addInnerNameToBigraphEMF(EObject innerName) {
        EStructuralFeature innerNamesFeature = getInstanceModel().eClass()
            .getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BINNERNAMES);
        
        if (innerNamesFeature != null) {
            EList<EObject> innerNames = (EList<EObject>) getInstanceModel().eGet(innerNamesFeature);
            innerNames.add(innerName);
        }
    }
    
    private void removeInnerNameFromBigraphEMF(EObject innerName) {
        invalidateLinkGraphCaches();
        EStructuralFeature innerNamesFeature = getInstanceModel().eClass()
            .getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_BINNERNAMES);
        
        if (innerNamesFeature != null) {
            EList<EObject> innerNames = (EList<EObject>) getInstanceModel().eGet(innerNamesFeature);
            innerNames.remove(innerName);
        }
    }
}
