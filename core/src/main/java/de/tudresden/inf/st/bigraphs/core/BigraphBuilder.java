package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;
import de.tudresden.inf.st.bigraphs.core.exceptions.InvalidConnectionException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.LinkTypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.TypeNotExistsException;
import de.tudresden.inf.st.bigraphs.core.impl.BigraphEntity;

/**
 * Common bigraph builder interface.
 *
 * @param <S> type of the signature
 * @author Dominik Grzelak
 */
public interface BigraphBuilder<S extends Signature> {

    /**
     * Create a root node under which additional nodes or hierarchies can be place.
     * <p>
     * A hierarchy is returned which contains a collection of nodes in a tree structure.
     * The hierarchy {@link NodeHierarchy} provides additional methods for adding nodes and sites.
     * The hierarchy can only be used with the {@link BigraphBuilder} which created it.
     *
     * @return a hierarchical tree structure
     */
    NodeHierarchy createRoot();

    NodeHierarchy hierarchy(Control control);

    NodeHierarchy hierarchy(String controlIdentifier);

    <B extends Bigraph<S>> B createBigraph();

    void makeGround();

    BigraphBuilder<S> closeAllInnerNames();

    BigraphBuilder<S> closeAllOuterNames();

    /**
     * Spawns a fresh bigraph builder but with exactly the same instance of the extended bigraph metamodel and signature
     * as before.
     *
     * @return a fresn bigraph builder with the same bigraph metamodel and signature metamodel
     */
    BigraphBuilder<S> spawnNewOne();

    /**
     * A {@link NodeHierarchy} contains a collection of nodes in a tree structure and
     * is related to the current {@link BigraphBuilder} instance.
     * <p>
     * It is responsible for keeping nodes together as one atomic unit of the place graph.
     * Several methods are provided for adding nodes and sites.
     * <p>
     * Can only be used with the {@link BigraphBuilder} instance which created it.
     */
    interface NodeHierarchy<S extends Signature> {

        NodeHierarchy addChild(Control control);

        NodeHierarchy addChild(String controlName);

        /**
         * Creates a child node for the current node hierarchy with the given control label and
         * connects it automatically to the given outer name. The outer name is automatically created
         * if it doesn't exists.
         *
         * @param controlName the control of the newly created node.
         * @param outerName   the outer name to connect the newly created node
         * @return the same node hierarchy instance
         */
        NodeHierarchy addChild(String controlName, String outerName) throws InvalidConnectionException, LinkTypeNotExistsException;

        NodeHierarchy addChild(String controlName, BigraphEntity.OuterName outerName) throws InvalidConnectionException, TypeNotExistsException;

        /**
         * Adds a site to the current parent.
         * <p>
         * An {@link ControlIsAtomicException} is thrown if the parent's control is <i>atomic</i>.
         *
         * @return adds a site to the current parent
         * @see ControlIsAtomicException
         */
        NodeHierarchy addSite();

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

        <B extends Bigraph<S>> B createBigraph();

    }

}
