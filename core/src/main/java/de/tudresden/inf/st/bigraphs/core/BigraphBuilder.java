package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.exceptions.ControlIsAtomicException;

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

    NodeHierarchy newHierarchy(Control control);

    NodeHierarchy newHierarchy(String controlIdentifier);

    <B extends Bigraph<S>> B createBigraph();

    void makeGround();

    BigraphBuilder<S> closeAllInnerNames();

    BigraphBuilder<S> closeAllOuterNames();

    /**
     * A {@link NodeHierarchy} contains a collection of nodes in a tree structure and
     * is related to the current {@link BigraphBuilder} instance.
     * <p>
     * It is responsible for keeping nodes together as one atomic unit of the place graph.
     * Several methods are provided for adding nodes and sites.
     * <p>
     * Can only be used with the {@link BigraphBuilder} instance which created it.
     */
    interface NodeHierarchy {

        NodeHierarchy addChild(Control control);

        NodeHierarchy addChild(String controlName);

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
         * One can go to the previous hierarchy by calling the {@link NodeHierarchy#goBack()} method.
         *
         * @return the new hierarchy
         */
        NodeHierarchy withNewHierarchy() throws ControlIsAtomicException;

        /**
         * Place the cursor one level up from the current position.
         *
         * @return the same hierarchy
         */
        NodeHierarchy goBack();

        /**
         * Place the cursor to the top most element of the hierarchy.
         *
         * @return the same hierarchy
         */
        NodeHierarchy top();

    }

}
