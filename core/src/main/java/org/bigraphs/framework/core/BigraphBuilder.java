/*
 * Copyright (c) 2019-2025 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.core;

import org.bigraphs.framework.core.exceptions.ControlIsAtomicException;
import org.bigraphs.framework.core.exceptions.InvalidConnectionException;
import org.bigraphs.framework.core.exceptions.builder.LinkTypeNotExistsException;
import org.bigraphs.framework.core.exceptions.builder.TypeNotExistsException;
import org.bigraphs.framework.core.impl.BigraphEntity;

/**
 * Generic interface for building bigraphs.
 *
 * @param <S> the signature type
 * @author Dominik Grzelak
 */
public interface BigraphBuilder<S extends Signature<?>> {

    /**
     * Create a root node under which additional nodes or hierarchies can be place.
     * <p>
     * A hierarchy is returned which contains a collection of nodes in a tree structure.
     * The hierarchy {@link NodeHierarchy} provides additional methods for adding nodes and sites.
     * The hierarchy can only be used with the {@link BigraphBuilder} which created it.
     *
     * @return a hierarchical tree structure
     */
    NodeHierarchy root();

    NodeHierarchy hierarchy(Control control);

    NodeHierarchy hierarchy(String controlIdentifier);

    <B extends Bigraph<S>> B create();

    void makeGround();

    BigraphBuilder<S> closeInner();

    BigraphBuilder<S> closeOuter();

    /**
     * Spawns a fresh bigraph builder but with exactly the same instance of the extended bigraph metamodel and signature
     * as before.
     *
     * @return a fresn bigraph builder with the same bigraph metamodel and signature metamodel
     */
    BigraphBuilder<S> spawn();

    /**
     * A {@link NodeHierarchy} contains a collection of nodes in a tree structure and
     * is related to the current {@link BigraphBuilder} instance.
     * <p>
     * It is responsible for keeping nodes together as one atomic unit of the place graph.
     * Several methods are provided for adding nodes and sites.
     * <p>
     * Can only be used with the {@link BigraphBuilder} instance which created it.
     */
    interface NodeHierarchy<S extends Signature<? extends Control<?, ?>>> {

        NodeHierarchy child(Control control);

        NodeHierarchy child(String controlName);

        /**
         * Creates a child node for the current node hierarchy with the given control label and
         * connects it automatically to the given outer name. The outer name is automatically created
         * if it doesn't exists.
         *
         * @param controlName the control of the newly created node.
         * @param outerName   the outer name to connect the newly created node
         * @return the same node hierarchy instance
         */
        NodeHierarchy child(String controlName, String outerName) throws InvalidConnectionException, LinkTypeNotExistsException;

        default NodeHierarchy<S> child(Control controlName, String outerName) throws InvalidConnectionException, LinkTypeNotExistsException {
            return child(controlName.getNamedType().stringValue(), outerName);
        }


        NodeHierarchy child(String controlName, BigraphEntity.OuterName outerName) throws InvalidConnectionException, TypeNotExistsException;

        /**
         * Adds a site to the current parent.
         * <p>
         * An {@link ControlIsAtomicException} is thrown if the parent's control is <i>atomic</i>.
         *
         * @return adds a site to the current parent
         * @see ControlIsAtomicException
         */
        NodeHierarchy site();

        /**
         * Creates a new hierarchy builder where the last created node is the parent of this new hierarchy.
         * <p>
         * One can go to the previous hierarchy by calling the {@link NodeHierarchy#up()} method.
         *
         * @return the new hierarchy
         */
        NodeHierarchy down() throws ControlIsAtomicException;

        /**
         * Place the cursor one level up from the current position.
         *
         * @return the same hierarchy
         */
        NodeHierarchy up();

        /**
         * Place the cursor to the top most element of the hierarchy.
         *
         * @return the same hierarchy
         */
        NodeHierarchy top();

        <B extends Bigraph<S>> B create();

    }

}
