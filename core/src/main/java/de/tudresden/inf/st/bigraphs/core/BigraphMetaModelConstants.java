package de.tudresden.inf.st.bigraphs.core;

/**
 * Some metamodel constants to conveniently refer to for the <i>BigraphBaseMetaModel</i>.
 * This is configured as a dependency here in this project.
 *
 * @author Dominik Grzelak
 */
public final class BigraphMetaModelConstants {

    /**
     * Relative path to the Ecore bigraph meta model inside the included bigraph model library.
     */
    public final static String BIGRAPH_BASE_MODEL = "/model/bigraphBaseModel.ecore";
    public final static String SIGNATURE_BASE_MODEL = "/model/signatureBaseModel.ecore";

    // Attributes
    public final static String ATTRIBUTE_INDEX = "index";
    public final static String ATTRIBUTE_NAME = "name";

    // Node types
    public final static String CLASS_ROOT = "BRoot";
    public final static String CLASS_NODE = "BNode";
    public final static String CLASS_OUTERNAME = "BOuterName";
    public final static String CLASS_INNERNAME = "BInnerName";
    public final static String CLASS_EDGE = "BEdge";
    public final static String CLASS_PORT = "BPort";
    public final static String CLASS_SITE = "BSite";
    public static final String CLASS_PLACE = "BPlace";
    public static final String CLASS_LINK = "BLink";
    public static final String CLASS_POINT = "BPoint";
    public static final String CLASS_BIGRAPH = "BBigraph";
    public static final String CLASS_NAMEABLETYPE = "NameableType";

    // References
    public final static String REFERENCE_CHILD = "bChild";
    public final static String REFERENCE_PARENT = "bPrnt";
    public final static String REFERENCE_LINK = "bLink";
    public final static String REFERENCE_POINT = "bPoints";
    public final static String REFERENCE_PORT = "bPorts";
    public final static String REFERENCE_NODE = "bNode";
    public final static String REFERENCE_BINNERNAMES = "bInnerNames";
    public final static String REFERENCE_BOUTERNAMES = "bOuterNames";
    public final static String REFERENCE_BEDGES = "bEdges";
    public final static String REFERENCE_BROOTS = "bRoots";


}
