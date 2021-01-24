package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils;
import de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.BigraphBaseModelPackage;
import de.tudresden.inf.st.bigraphs.models.signatureBaseModel.SignatureBaseModelPackage;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.BasicExtendedMetaData;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMLResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants.BIGRAPH_BASE_MODEL;
import static de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants.SIGNATURE_BASE_MODEL;
import static de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils.*;

/**
 * A simple file utility class to serialize (deserialize) bigraphs to (from) Ecore-based bigraph model files.
 *
 * @author Dominik Grzelak
 */
public class BigraphArtifacts {
    // register packages: https://www.cct.lsu.edu/~rguidry/eclipse-doc36/src-html/org/eclipse/emf/cdo/common/model/EMFUtil.html
    private final static String DEFAULT_ENCODING = "UTF-8";

    public enum Format {
        ECORE, XMI
    }

    private static void enableExtendedMetadata(ResourceSet rs) {
        final ExtendedMetaData extendedMetaData = new BasicExtendedMetaData(rs.getPackageRegistry());
        rs.getLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA, extendedMetaData);
    }

    /**
     * Loads the internal metamodel of a bigraphical signature that is declared in the bigraphMetaModel dependency.
     *
     * @return the base signature metamodel as {@link EPackage}
     * @throws IOException if the model could not be loaded from the bigraphMetaModel dependency
     */
    public static EPackage loadInternalSignatureMetaMetaModel() throws IOException {
        EcorePackage.eINSTANCE.eClass();    // makes sure EMF is up and running, probably not necessary
        SignatureBaseModelPackage.eINSTANCE.eClass();
        ResourceSet resourceSet = new ResourceSetImpl();
        enableExtendedMetadata(resourceSet);

        URL resource1 = EMFUtils.class.getResource(SIGNATURE_BASE_MODEL);
        URI uri = URI.createURI(resource1.toString());

        // Resource factories
        // registerEcoreResourceFactories(Resource.Factory.Registry.INSTANCE);
        registerEcoreResourceFactories(resourceSet.getResourceFactoryRegistry());
        // Register packages
        // EPackage.Registry.INSTANCE.put(SignatureBaseModelPackage.eNS_URI, SignatureBaseModelPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(SignatureBaseModelPackage.eNS_URI, SignatureBaseModelPackage.eINSTANCE);

        Resource resource = resourceSet.createResource(uri);
        resource.load(Collections.EMPTY_MAP);
        EPackage ePackage = (EPackage) resource.getContents().get(0);
        validateModel(ePackage);
        return ePackage;
    }

    /**
     * Loads the internal metamodel of a base bigraph that is declared in the bigraphMetaModel dependency.
     *
     * @return the base bigraph metamodel as {@link EPackage}
     * @throws IOException if the model could not be loaded from the bigraphMetaModel dependency
     */
    public static EPackage loadInternalBigraphMetaMetaModel() throws IOException {
        EcorePackage.eINSTANCE.eClass();    // makes sure EMF is up and running, probably not necessary
        BigraphBaseModelPackage.eINSTANCE.eClass();
        ResourceSet resourceSet = new ResourceSetImpl();

        URL resource1 = EMFUtils.class.getResource(BIGRAPH_BASE_MODEL);
        URI uri = URI.createURI(resource1.toString());

        // Resource factories
        // registerEcoreResourceFactories(Resource.Factory.Registry.INSTANCE);
        registerEcoreResourceFactories(resourceSet.getResourceFactoryRegistry());
        // Register Package
        // EPackage.Registry.INSTANCE.put(BigraphBaseModelPackage.eNS_URI, BigraphBaseModelPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(BigraphBaseModelPackage.eNS_URI, BigraphBaseModelPackage.eINSTANCE);

        // https://wiki.eclipse.org/EMF/FAQ#How_do_I_make_my_EMF_standalone_application_Eclipse-aware.3F
        // resourceSet.getURIConverter().getURIMap().putAll(EcorePlugin.computePlatformURIMap(false));
        Resource resource = resourceSet.createResource(uri);
        resource.load(Collections.EMPTY_MAP);
        EPackage ePackage = (EPackage) resource.getContents().get(0);
        validateModel(ePackage);
        return ePackage;
    }

    public static EPackage loadBigraphMetaModel(String filePath) throws IOException {
        assert filePath != null;
        File file = new File(filePath);
        if (!file.exists()) throw new IOException("File couldn't be found: " + filePath);
        EcorePackage.eINSTANCE.eClass();
        BigraphBaseModelPackage.eINSTANCE.eClass();
        ResourceSet resourceSet = new ResourceSetImpl();

        URI uri = URI.createURI(filePath);

        // Resource factories
        // registerEcoreResourceFactories(Resource.Factory.Registry.INSTANCE);
        registerEcoreResourceFactories(resourceSet.getResourceFactoryRegistry());
        // Register packages
        // EPackage.Registry.INSTANCE.put(BigraphBaseModelPackage.eNS_URI, BigraphBaseModelPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(BigraphBaseModelPackage.eNS_URI, BigraphBaseModelPackage.eINSTANCE);

        Resource resource = resourceSet.createResource(uri);
        resource.load(Collections.EMPTY_MAP);
        EPackage ePackage = (EPackage) resource.getContents().get(0);
        validateModel(ePackage);
        return ePackage;
    }

    public static EPackage loadBigraphMetaModel(InputStream inputStream) throws IOException {
        EcorePackage.eINSTANCE.eClass();
        BigraphBaseModelPackage.eINSTANCE.eClass();
        ResourceSet resourceSet = new ResourceSetImpl();

        // Resource factories
        // registerEcoreResourceFactories(Resource.Factory.Registry.INSTANCE);
        registerEcoreResourceFactories(resourceSet.getResourceFactoryRegistry());
        // Register Packages
        // EPackage.Registry.INSTANCE.put(BigraphBaseModelPackage.eNS_URI, BigraphBaseModelPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(BigraphBaseModelPackage.eNS_URI, BigraphBaseModelPackage.eINSTANCE);

        Resource resource = resourceSet.createResource(URI.createURI("*.xmi"));
        resource.load(inputStream, Collections.EMPTY_MAP);
        EPackage ePackage = (EPackage) resource.getContents().get(0);
        validateModel(ePackage);
        return ePackage;
    }

    /**
     * Loads an instance model and validates it against the given meta-model.
     *
     * @param metaModelPackageWithSignature the meta-model of the instance model
     * @param filePath                      the file path of the instance model
     * @return list of {@link EObject} resources representing the bigraph
     * @throws IOException if the file doesn't exists, or an exception is raised when loading the resource
     * @see #loadBigraphInstanceModel(String)
     * @see #loadBigraphInstanceModel(EPackage, InputStream)
     */
    public static List<EObject> loadBigraphInstanceModel(EPackage metaModelPackageWithSignature, String filePath) throws IOException {
        return loadBigraphInstanceModelByFilePath(metaModelPackageWithSignature, filePath);
    }

    /**
     * Loads an instance model without validating it against its meta-model.
     *
     * @param filePath the file path of the instance model
     * @return list of {@link EObject} resources representing the bigraph
     * @throws IOException if the file doesn't exists, or an exception is raised when loading the resource
     * @see #loadBigraphInstanceModel(EPackage, String)
     * @see #loadBigraphInstanceModel(EPackage, InputStream)
     */
    public static List<EObject> loadBigraphInstanceModel(String filePath) throws IOException {
        return loadBigraphInstanceModelByFilePath(null, filePath);
    }

    /**
     * Loads an instance model and validates it against the given meta-model.
     *
     * @param metaModelPackageWithSignature the meta-model of the instance model
     * @param instanceModelInputStream      the input stream of the instance model
     * @return list of {@link EObject} resources representing the bigraph
     * @throws IOException if the file doesn't exists, or an exception is raised when loading the resource
     * @see #loadBigraphInstanceModel(String)
     * @see #loadBigraphInstanceModel(EPackage, InputStream)
     */
    public static List<EObject> loadBigraphInstanceModel(EPackage metaModelPackageWithSignature, InputStream instanceModelInputStream) throws IOException {
        ResourceSet load_resource = BigraphArtifacts.getResourceSetBigraphInstanceModel(metaModelPackageWithSignature, instanceModelInputStream);
        Resource resource = load_resource.getResources().get(0);
        EList<EObject> contents = resource.getContents();
        validateModel(contents.get(0));
        return contents;
    }


    private static List<EObject> loadBigraphInstanceModelByFilePath(EPackage metaModelPackageWithSignature, String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) throw new IOException("File couldn't be found: " + filePath);

        ResourceSet load_resourceSet = initResourceSet(metaModelPackageWithSignature);
        Resource load_resource = load_resourceSet.createResource(URI.createFileURI(filePath));
        load_resource.load(Collections.EMPTY_MAP);
        EList<EObject> contents = load_resource.getContents();
        validateModel(contents.get(0));
        return contents;
    }

    public static void validateModel(EObject eObject) {
        Diagnostic diagnostic = Diagnostician.INSTANCE.validate(eObject);
        if (diagnostic.getSeverity() != Diagnostic.OK) {
            System.out.println("ERROR in: " + diagnostic.getMessage());
            for (Diagnostic child : diagnostic.getChildren()) {
                System.out.println(child.getMessage());
            }
            throw new RuntimeException("Invalid model loaded.");
        }
    }

    /**
     * Returns the resource set ({@link ResourceSet}) of a loaded bigraph instance model and validates it against the
     * given meta-model.
     *
     * @param metaModelPackageWithSignature the meta-model of the instance model
     * @param instanceModelInputStream      the input stream of the instance model
     * @return the resource set of the bigraph instance model to load
     * @throws IOException
     * @see #loadBigraphInstanceModel(String)
     * @see #loadBigraphInstanceModel(EPackage, String)
     * @see #loadBigraphInstanceModel(EPackage, InputStream)
     */
    public static ResourceSet getResourceSetBigraphInstanceModel(EPackage metaModelPackageWithSignature, InputStream instanceModelInputStream) throws IOException {
        ResourceSet load_resourceSet = initResourceSet(metaModelPackageWithSignature);
        Resource load_resource = load_resourceSet.createResource(URI.createURI("*.xmi"));
        load_resource.load(instanceModelInputStream, Collections.EMPTY_MAP);
        return load_resourceSet;
    }

    /**
     * Prepares a {@link ResourceSet} in order to load a bigraph instance model.
     *
     * @param metaModelPackageWithSignature the meta-model of the bigraph
     * @return an initialized resource set
     */
    private static ResourceSet initResourceSet(EPackage metaModelPackageWithSignature) {
        EcorePackage.eINSTANCE.eClass();
        ResourceSet load_resourceSet = new ResourceSetImpl();

        // Register factories
        // registerResourceFactories(Resource.Factory.Registry.INSTANCE);
        registerXMIResourceFactories(load_resourceSet.getResourceFactoryRegistry());

        if (Objects.nonNull(metaModelPackageWithSignature)) {
            // Register packages
            // EPackage.Registry.INSTANCE.put(BigraphBaseModelPackage.eNS_URI, BigraphBaseModelPackage.eINSTANCE);
            load_resourceSet.getPackageRegistry().put(BigraphBaseModelPackage.eNS_URI, BigraphBaseModelPackage.eINSTANCE);
            if (Objects.nonNull(metaModelPackageWithSignature.getNsURI()) && !metaModelPackageWithSignature.getNsURI().isEmpty()) {
                // EPackage.Registry.INSTANCE.put(metaModelPackageWithSignature.getNsURI(), metaModelPackageWithSignature);
                load_resourceSet.getPackageRegistry().put(metaModelPackageWithSignature.getNsURI(), metaModelPackageWithSignature);
            }
        }
        return load_resourceSet;
    }

    /**
     * Exports the Ecore-based instance model of a bigraph.
     */
    public static void exportAsInstanceModel(EcoreBigraph bigraph, OutputStream outputStream) throws IOException {
        EMFUtils.writeDynamicInstanceModel(bigraph.getModelPackage(), Collections.singleton(bigraph.getModel()), outputStream, null);
    }

    public static void exportAsInstanceModel(EcoreBigraph bigraph, OutputStream outputStream, String newNamespaceLocation) throws IOException {
        EMFUtils.writeDynamicInstanceModel(bigraph.getModelPackage(), Collections.singleton(bigraph.getModel()), outputStream, newNamespaceLocation);
    }

    /**
     * Exports the Ecore-based instance model of a signature.
     */
    public static void exportAsInstanceModel(EcoreSignature signature, OutputStream outputStream) throws IOException {
        EMFUtils.writeDynamicInstanceModel(signature.getModelPackage(), Collections.singleton(signature.getModel()), outputStream, null);
    }

    /**
     * Exports the Ecore-based metamodel of a bigraph
     */
    public static void exportAsMetaModel(EcoreBigraph bigraph, OutputStream outputStream) throws IOException {
        writeDynamicMetaModel(bigraph.getModelPackage(), DEFAULT_ENCODING, outputStream);
    }

    /**
     * Exports the Ecore-based metamodel of a signature
     */
    public static void exportAsMetaModel(EcoreSignature signature, OutputStream outputStream) throws IOException {
        writeDynamicMetaModel(signature.getModelPackage(), DEFAULT_ENCODING, outputStream);
    }
}
