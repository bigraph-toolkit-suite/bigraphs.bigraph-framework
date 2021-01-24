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
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;

import static de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants.*;
import static de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils.registerResourceFactories;
import static de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils.writeDynamicMetaModel;

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

//    public static EPackage loadInternalKindSortingsMetaMetaModel() throws IOException {
//        EcorePackage.eINSTANCE.eClass();    // makes sure EMF is up and running, probably not necessary
//        KindSignatureBaseModelPackage.eINSTANCE.eClass();
//        ResourceSet resourceSet = new ResourceSetImpl();
//        enableExtendedMetadata(resourceSet);
//        URL resource1 = EMFUtils.class.getResource(KIND_SIGNATURE_BASE_MODEL);
//        URI uri = URI.createURI(resource1.toString());
//        EPackage.Registry.INSTANCE.put(KindSignatureBaseModelPackage.eNS_URI, KindSignatureBaseModelPackage.eINSTANCE);
//        resourceSet.getPackageRegistry().put(KindSignatureBaseModelPackage.eNS_URI, KindSignatureBaseModelPackage.eINSTANCE);
//        // resource factories
//        registerResourceFactories(Resource.Factory.Registry.INSTANCE);
//        registerResourceFactories(resourceSet.getResourceFactoryRegistry());
////        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
////        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
//        Resource resource = resourceSet.createResource(uri);
//        resource.load(Collections.EMPTY_MAP);
//        EPackage ePackage = (EPackage) resource.getContents().get(0);
//        validateModel(ePackage);
//        return ePackage;
//    }

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
        EPackage.Registry.INSTANCE.put(SignatureBaseModelPackage.eNS_URI, SignatureBaseModelPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(SignatureBaseModelPackage.eNS_URI, SignatureBaseModelPackage.eINSTANCE);
        // resource factories
        registerResourceFactories(Resource.Factory.Registry.INSTANCE);
        registerResourceFactories(resourceSet.getResourceFactoryRegistry());
//        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
//        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
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
        URI uri = URI.createURI(resource1.toString()); //URI.createPlatformResourceURI(resource1.toString(), true);
//        URI uri = URI.createURI(ecoreResource);
        EPackage.Registry.INSTANCE.put(BigraphBaseModelPackage.eNS_URI, BigraphBaseModelPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(BigraphBaseModelPackage.eNS_URI, BigraphBaseModelPackage.eINSTANCE);
        // resource factories
        registerResourceFactories(Resource.Factory.Registry.INSTANCE);
//        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
//        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl()); //probably not necessary
        registerResourceFactories(resourceSet.getResourceFactoryRegistry());

        //https://wiki.eclipse.org/EMF/FAQ#How_do_I_make_my_EMF_standalone_application_Eclipse-aware.3F
//        resourceSet.getURIConverter().getURIMap().putAll(EcorePlugin.computePlatformURIMap(false));
//        Resource resource = resourceSet.getResource(uri, true);
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

//        URL resource1 = EMFUtils.class.getResource(filePath);
        URI uri = URI.createURI(filePath); //resource1.toString()); //URI.createPlatformResourceURI(resource1.toString(), true);
//        URI uri = URI.createURI(ecoreResource);
        EPackage.Registry.INSTANCE.put(BigraphBaseModelPackage.eNS_URI, BigraphBaseModelPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(BigraphBaseModelPackage.eNS_URI, BigraphBaseModelPackage.eINSTANCE);
        // resource factories
//        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
//        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl()); //probably not necessary
        registerResourceFactories(Resource.Factory.Registry.INSTANCE);
        registerResourceFactories(resourceSet.getResourceFactoryRegistry());

        //https://wiki.eclipse.org/EMF/FAQ#How_do_I_make_my_EMF_standalone_application_Eclipse-aware.3F
//        resourceSet.getURIConverter().getURIMap().putAll(EcorePlugin.computePlatformURIMap(false));
//        Resource resource = resourceSet.getResource(uri, true);

        Resource resource = resourceSet.createResource(uri);
//        Resource resource = resourceSet.getResource(uri, true);
        resource.load(Collections.EMPTY_MAP);
        EPackage ePackage = (EPackage) resource.getContents().get(0);
        validateModel(ePackage);
        return ePackage;
    }

    public static EPackage loadBigraphMetaModel(InputStream inputStream) throws IOException {
        EcorePackage.eINSTANCE.eClass();
        BigraphBaseModelPackage.eINSTANCE.eClass();
        ResourceSet resourceSet = new ResourceSetImpl();

//        URL resource1 = EMFUtils.class.getResource(filePath);
//        URI uri = URI.createURI(filePath); //resource1.toString()); //URI.createPlatformResourceURI(resource1.toString(), true);
//        URI uri = URI.createURI(ecoreResource);
        EPackage.Registry.INSTANCE.put(BigraphBaseModelPackage.eNS_URI, BigraphBaseModelPackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(BigraphBaseModelPackage.eNS_URI, BigraphBaseModelPackage.eINSTANCE);
        // resource factories
//        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
//        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl()); //probably not necessary
        registerResourceFactories(Resource.Factory.Registry.INSTANCE);
        registerResourceFactories(resourceSet.getResourceFactoryRegistry());

        //https://wiki.eclipse.org/EMF/FAQ#How_do_I_make_my_EMF_standalone_application_Eclipse-aware.3F
//        resourceSet.getURIConverter().getURIMap().putAll(EcorePlugin.computePlatformURIMap(false));
//        Resource resource = resourceSet.getResource(uri, true);

//        Resource load_resource = load_resourceSet.createResource(URI.createURI("*.xmi"));
//        load_resource.load(instanceModelInputStream, Collections.EMPTY_MAP);
        Resource resource = resourceSet.createResource(URI.createURI("*.xmi"));
//        Resource resource = resourceSet.getResource(uri, true);
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
//        registerResourceFactories(Resource.Factory.Registry.INSTANCE);
        registerResourceFactories(load_resourceSet.getResourceFactoryRegistry());
//        load_resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMLResourceFactoryImpl());
//        load_resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());

        if (Objects.nonNull(metaModelPackageWithSignature)) {
            // register the dynamic package locally and globally
            EPackage.Registry.INSTANCE.put(BigraphBaseModelPackage.eNS_URI, BigraphBaseModelPackage.eINSTANCE);
            load_resourceSet.getPackageRegistry().put(BigraphBaseModelPackage.eNS_URI, BigraphBaseModelPackage.eINSTANCE);
            if (Objects.nonNull(metaModelPackageWithSignature.getNsURI()) && !metaModelPackageWithSignature.getNsURI().isEmpty()) {
                EPackage.Registry.INSTANCE.put(metaModelPackageWithSignature.getNsURI(), metaModelPackageWithSignature);
                load_resourceSet.getPackageRegistry().put(metaModelPackageWithSignature.getNsURI(), metaModelPackageWithSignature);
            }
        }
        return load_resourceSet;
    }

    /**
     * Export the instance model of a bigraph.
     *
     * @param bigraph      the bigraph to export
     * @param outputStream the output stream
     * @throws IOException
     */
    public static void exportAsInstanceModel(EcoreBigraph bigraph, OutputStream outputStream) throws IOException {
        writeDynamicInstanceModel(bigraph.getModelPackage(), bigraph.getModel(), outputStream, null);
    }

    public static void exportAsInstanceModel(EcoreBigraph bigraph, OutputStream outputStream, String newNamespaceLocation) throws IOException {
        writeDynamicInstanceModel(bigraph.getModelPackage(), bigraph.getModel(), outputStream, newNamespaceLocation);
    }

    /**
     * Meta-model of a bigraph is exported
     *
     * @param bigraph
     * @param outputStream
     * @throws IOException
     */
    public static void exportAsMetaModel(EcoreBigraph bigraph, OutputStream outputStream) throws IOException {
        writeDynamicMetaModel(bigraph.getModelPackage(), DEFAULT_ENCODING, outputStream);
    }

    private static void writeDynamicInstanceModel(EPackage metaModelPackage, EObject instanceModel, OutputStream outputStream, String newNamespaceLocation) throws IOException {
        writeDynamicInstanceModel(metaModelPackage, Collections.singleton(instanceModel), outputStream, newNamespaceLocation);
    }

    private static void writeDynamicInstanceModel(EPackage ePackage, Collection<EObject> objects, OutputStream outputStream, String newNamespaceLocation) throws IOException {
        EcorePackage.eINSTANCE.eClass();    // makes sure EMF is up and running, probably not necessary
        final ResourceSet resourceSet = new ResourceSetImpl();

        registerResourceFactories(Resource.Factory.Registry.INSTANCE);
        registerResourceFactories(resourceSet.getResourceFactoryRegistry());
//        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
//        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
        final Resource outputRes = resourceSet.createResource(URI.createFileURI(ePackage.getName() + ".xmi")); //
        // add our new package to resource contents
        objects.forEach(x -> outputRes.getContents().add(x));
//        outputRes.getContents().add(ePackage); //(!) then the meta model is also included in the instance model
        EPackage.Registry.INSTANCE.put(ePackage.getNsURI(), ePackage);
        outputRes.getResourceSet().getPackageRegistry().put(ePackage.getNsURI(), ePackage);

        Map<String, Object> options = new HashMap<>();
        // Using OPTION_SCHEMA_LOCATION save option to produce "xsi:schemaLocation" attribute in the XMI document
        options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
        options.put(XMLResource.OPTION_PROCESS_DANGLING_HREF, "RECORD"); //see: https://books.google.de/books?id=ff-9ZYhvPwwC&pg=PA317&lpg=PA317&dq=emf+OPTION_PROCESS_DANGLING_HREF&source=bl&ots=yBXkH3qSpD&sig=ACfU3U3uEGX_DCnDa2DAnjRboybhyGsKng&hl=en&sa=X&ved=2ahUKEwiCg-vI7_DgAhXDIVAKHU1PAIgQ6AEwBHoECAYQAQ#v=onepage&q=emf%20OPTION_PROCESS_DANGLING_HREF&f=false
        options.put(XMLResource.OPTION_ENCODING, DEFAULT_ENCODING);
        URI oldURI = Objects.nonNull(ePackage.eResource()) ? ePackage.eResource().getURI() : null;
        if (Objects.nonNull(newNamespaceLocation)) {
            ePackage.eResource().setURI(URI.createURI(newNamespaceLocation));
            EPackage.Registry.INSTANCE.put(ePackage.getNsURI(), ePackage);
            resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
            options.put(XMIResource.SCHEMA_LOCATION, newNamespaceLocation);
        }
        try {
            outputRes.save(outputStream, options);
        } finally {
            if (Objects.nonNull(oldURI) && Objects.nonNull(newNamespaceLocation)) {
                ePackage.eResource().setURI(oldURI);
            }
        }
    }

//    private static void writeDynamicMetaModel(EPackage ePackage, OutputStream outputStream) throws IOException {
//        ResourceSet metaResourceSet = new ResourceSetImpl();
//        registerResourceFactories(Resource.Factory.Registry.INSTANCE);
//        registerResourceFactories(metaResourceSet.getResourceFactoryRegistry());
////        metaResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new XMLResourceFactoryImpl());
//
//        Resource metaResource = metaResourceSet.createResource(URI.createURI(ePackage.getName() + ".ecore")); //URI.createURI(ePackage.getName())); //URI.createURI(filename + ".ecore"));
//        metaResource.getContents().add(ePackage);
//        Map<String, Object> options = new HashMap<>();
//        options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
//        options.put(XMLResource.OPTION_ENCODING, DEFAULT_ENCODING);
//        metaResource.save(outputStream, options);
//        outputStream.close();
//    }
}
