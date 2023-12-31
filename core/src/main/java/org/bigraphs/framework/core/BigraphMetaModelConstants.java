package org.bigraphs.framework.core;

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
//    public final static String KIND_SIGNATURE_BASE_MODEL = "/model/kindSignatureBaseModel.ecore";

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
    public static final String CLASS_INDEXABLETYPE = "IndexableType";
    public static final String CLASS_ESTRING2EJAVAOBJECT_MAP = "EStringToEJavaObjectMap";

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
    public final static String REFERENCE_BNODE_ATTRIBUTES = "attributes";
    public final static String REFERENCE_BNODE_ATTRIBUTES_KEY = "key";
    public final static String REFERENCE_BNODE_ATTRIBUTES_VALUE = "value";

    public interface SignaturePackage {
        String SORT_PREFIX = "Sort";
        String EPACKAGE_NAME = "signatureBaseModel";
        String ECLASS_BKINDSIGNATURE = "BKindSignature";
        String ECLASS_BDYNAMICSIGNATURE = "BDynamicSignature";
        String ECLASS_BBASICSIGNATURE = "BBasicSignature";
        String ECLASS_BKINDPLACESORTINGS = "BKindPlaceSorting";
        String ECLASS_BKINDSORTATOMIC = "BKindSortAtomic";
        String ECLASS_KINDSORTNONATOMIC = "BKindSortNonAtomic";
        String ECLASS_BCONTROL = "BControl";
        String ECLASS_BCONTROLSTATUS = "BControlStatus";

        String REFERENCE_BCONTROLS = "bControls";
        String REFERENCE_BSIG = "bSig";
        String REFERENCE_BKINDPLACESORTS = "bKindPlaceSorts"; // reference to the place-sort instances
        String REFERENCE_BPLACESORTING = "bPlaceSorting"; // reference to the container signature
        String REFERENCE_BKINDSORTS = "bCanContain";
        String REFERENCE_BCONTAINEDBY = "bContainedBy";

        // Attributes
        String ATTRIBUTE_NAME = "name";
        String ATTRIBUTE_ARITY = "arity";
        String ATTRIBUTE_STATUS = "status";
    }

}
