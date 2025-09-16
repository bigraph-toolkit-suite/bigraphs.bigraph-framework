package GCF;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.URL;

/*
 * The GCF.GXLConverterAPI is a user interface to make the use of the GXL Converter Framework
 * (GCF) as simple as possible. It centralizes the management of any in the parsing process
 * involved classes. So it offers methods to create, return and close  the
 * GCF.GXLConnector/GXLConnector2, the  GCF.GXLDocumentHandler and the ResultObject. These three classes
 * are necessary to make the framework work. For further information on the methods, see
 * below.
 *
 * IMPORTANT NOTE :
 *      Before you can create the GCF.GXLConnector, you must have set the URL and the package name
 *      of your implementation of the abstract classes. Furthermore you must have created the GXLObject
 *      with the method createGXL (Object). This is necessary because these 3 are parameters of the
 *      GCF.GXLConnector's constructor.
 *      This does _not_ apply to the GXLConnector2. This class only needs the GXLObject.
 *      It is furthermore necessary that you have created the GCF.GXLConnector/GXLConnector2 before
 *      you create the GCF.GXLDocumentHandler, because the GCF.GXLConnector/GXLConnector2 is a parameter
 *      of the GCF.GXLDocumentHandler's constructor.
 */
public class GXLConverterAPI extends Object {

    private static Object GXLObject;        // holds the GXL-Construct
    private static GXLConnector connector;          // holds the GCF.GXLConnector/GXLConnector2
    private static GXLDocumentHandler docHandler;   // holds the GCF.GXLDocumentHandler
    private static URL implementationURL = null;   // holds where the implementation of the GCF
    // you want to use is located
    private static String packageName;      // holds the package name of the implementation you
    // want to use

    public static final int CONNECTOR = 0; // some constants for the creation of the GCF.GXLConnector/
    public static final int CONNECTOR2 = 1; // GXLConnector2

    /**
     * Empty Constructor.
     */
    public GXLConverterAPI() {
    }

    /**
     * Creates the GXL-Object.
     */
    public static void createGXL(Object GXL) {
        GXLObject = GXL;
    }

    /**
     * Returns the resulting object after the conversion.
     */
    public static Object getResultObject() {
        return GXLObject;
    }

    /**
     * Closes the GXL-Object.
     */
    public static void closeGXL() {
    }

    /**
     * Method to set the location of the implementation you want to use.
     * <p>
     * Use {@code null} for using the default classloader of the current class.
     */
    public static void setImplementationURL(@Nullable URL location) {
        implementationURL = location;
    }

    /**
     * Method to return the location of the currently used implementation of the GCF.
     */
    public static URL getImplementationURL() {
        return implementationURL;
    }

    /**
     * Method to set the package name of the implementation you want to use.
     */
    public static void setPackageName(String name) {
        packageName = name;
    }

    /**
     * Method to return the package name of the currently used implementation of the GCF.
     */
    public static String getPackageName() {
        return packageName;
    }

    /**
     * Creates the GCF.GXLConnector/GXLConnector2.
     *
     * @param which CONNECTOR -> GCF.GXLConnector, CONNECTOR2 -> GXLConnector2
     */
    public static void createConnector(int which) {
        connector = (which == CONNECTOR) ? new GXLConnector(GXLObject, implementationURL, packageName) :
                new GXLConnector(GXLObject, implementationURL, packageName);
    }

    /**
     * Returns the GXL-Connector.
     */
    public static GXLConnector getConnector() {
        return connector;
    }

    /**
     * Closes the GCF.GXLConnector.
     */
    public static void closeConnector() {
    }

    /**
     * Creates the GCF.GXLDocumentHandler (for parsing of an GXL-document).
     */
    public static void createDocumentHandler() {
        if (connector != null) docHandler = new GXLDocumentHandler(connector);
    }

    /**
     * Returns the GCF.GXLDocumentHandler.
     */
    public static GXLDocumentHandler getDocumentHandler() {
        return docHandler;
    }

    /**
     * Closes the GCF.GXLDocumentHandler.
     */
    public static void closeDocumentHandler() {
    }

    /**
     * Makes the GXLDocumentHanler parse a file.
     *
     * @param fileToParse The file that shall be parsed.
     */
    public static void parse(String fileToParse) {
        try {
            docHandler.doParse(fileToParse);
        } catch (java.lang.Exception e) {
            docHandler.displayText("Error, file " + fileToParse + " not found or could not be opened !");
            docHandler.displayText(e.getMessage());
            e.printStackTrace();
        }
    }

}