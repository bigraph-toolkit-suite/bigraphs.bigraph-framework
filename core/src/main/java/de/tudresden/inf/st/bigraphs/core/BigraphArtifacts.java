package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils;
import de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.BigraphBaseModelPackage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;

import static de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants.BIGRAPH_BASE_MODEL;

/**
 * A simple file utility class to serialize bigraphs and deserialize Ecore-based bigraph model files.
 *
 * @author Dominik Grzelak
 */
public class BigraphArtifacts {

    public static EPackage loadInternalBigraphMetaModel() throws IOException {
        EcorePackage.eINSTANCE.eClass();    // makes sure EMF is up and running, probably not necessary
        BigraphBaseModelPackage.eINSTANCE.eClass();
        ResourceSet resourceSet = new ResourceSetImpl();

        URL resource1 = EMFUtils.class.getResource(BIGRAPH_BASE_MODEL);
        URI uri = URI.createURI(resource1.toString()); //URI.createPlatformResourceURI(resource1.toString(), true);
//        URI uri = URI.createURI(ecoreResource);
        resourceSet.getPackageRegistry().put(BigraphBaseModelPackage.eNS_URI, BigraphBaseModelPackage.eINSTANCE);
        // resource factories
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl()); //probably not necessary


        //https://wiki.eclipse.org/EMF/FAQ#How_do_I_make_my_EMF_standalone_application_Eclipse-aware.3F
//        resourceSet.getURIConverter().getURIMap().putAll(EcorePlugin.computePlatformURIMap(false));
        //TODO throw error if resource is not available!
//        Resource resource = resourceSet.getResource(uri, true);
        Resource resource = resourceSet.createResource(uri);
        try {
            resource.load(Collections.EMPTY_MAP);
//            System.out.println("Model loaded");
            return (EPackage) resource.getContents().get(0);
        } catch (
                IOException e) {
            throw e;
        }
    }

    /**
     * Loads an instance model and validates it against the given meta-model.
     *
     * @param metaModelPackageWithSignature the meta-model of the instance model
     * @param filePath                      the file path of the instance model
     * @return list of {@link EObject} resources
     * @throws IOException if the file doesn't exists, or an exception is raised when loading the resource
     */
    public static List<EObject> loadBigraphInstanceModel(EPackage metaModelPackageWithSignature, String filePath) throws IOException {
        return loadBigraphInstanceModel0(metaModelPackageWithSignature, filePath);
    }

    /**
     * Loads an instance model without validates it against its meta-model.
     *
     * @param filePath the file path of the instance model
     * @return list of {@link EObject} resources
     * @throws IOException if the file doesn't exists, or an exception is raised when loading the resource
     */
    public static List<EObject> loadBigraphInstanceModel(String filePath) throws IOException {
        return loadBigraphInstanceModel0(null, filePath);
    }

    private static List<EObject> loadBigraphInstanceModel0(EPackage metaModelPackageWithSignature, String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) throw new IOException("File couldn't be found: " + filePath);
        EcorePackage.eINSTANCE.eClass();

        ResourceSet load_resourceSet = new ResourceSetImpl();
        load_resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMLResourceFactoryImpl());
        load_resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());

        if (Objects.nonNull(metaModelPackageWithSignature)) {
            // register the dynamic package locally
            load_resourceSet.getPackageRegistry().put(null, metaModelPackageWithSignature);
            load_resourceSet.getPackageRegistry().put(metaModelPackageWithSignature.getNsURI(), metaModelPackageWithSignature);
        }
        URI uri = URI.createFileURI(filePath);
        Resource load_resource = load_resourceSet.createResource(uri);
        load_resource.load(Collections.EMPTY_MAP);
        return load_resource.getContents();
    }

    public static EPackage loadBigraphMetaModel(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) throw new IOException("File couldn't be found: " + filePath);
        EcorePackage.eINSTANCE.eClass();
        BigraphBaseModelPackage.eINSTANCE.eClass();
        ResourceSet resourceSet = new ResourceSetImpl();

//        URL resource1 = EMFUtils.class.getResource(filePath);
        URI uri = URI.createURI(filePath); //resource1.toString()); //URI.createPlatformResourceURI(resource1.toString(), true);
//        URI uri = URI.createURI(ecoreResource);
        resourceSet.getPackageRegistry().put(BigraphBaseModelPackage.eNS_URI, BigraphBaseModelPackage.eINSTANCE);
        // resource factories
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl()); //probably not necessary


        //https://wiki.eclipse.org/EMF/FAQ#How_do_I_make_my_EMF_standalone_application_Eclipse-aware.3F
//        resourceSet.getURIConverter().getURIMap().putAll(EcorePlugin.computePlatformURIMap(false));
        //TODO throw error if resource is not available!
//        Resource resource = resourceSet.getResource(uri, true);

        Resource resource = resourceSet.createResource(uri);
//        Resource resource = resourceSet.getResource(uri, true);
        resource.load(Collections.EMPTY_MAP);
        return (EPackage) resource.getContents().get(0);
    }

    /**
     * Export the instance model of a bigraph.
     *
     * @param bigraph      the bigraph to export
     * @param outputStream the output stream
     * @throws IOException
     */
    public static void exportAsInstanceModel(Bigraph<?> bigraph, OutputStream outputStream) throws IOException {
//        Collection<EObject> allresources = BigraphArtifactHelper.getResourcesFromBigraph(bigraph); // old method
        writeDynamicInstanceModel(bigraph.getModelPackage(), bigraph.getModel(), outputStream);
    }

    /**
     * Meta-model of a bigraph is exported
     *
     * @param bigraph
     * @param outputStream
     * @throws IOException
     */
    public static void exportAsMetaModel(Bigraph<?> bigraph, OutputStream outputStream) throws IOException {
        writeDynamicMetaModel(bigraph.getModelPackage(), outputStream);
    }

    private static void writeDynamicInstanceModel(EPackage metaModelPackage, EObject instanceModel, OutputStream outputStream) throws IOException {
        writeDynamicInstanceModel(metaModelPackage, Collections.singleton(instanceModel), outputStream);
    }

    //TODO: add UTF-8
    private static void writeDynamicInstanceModel(EPackage ePackage, Collection<EObject> objects, OutputStream outputStream) throws IOException {
        EcorePackage.eINSTANCE.eClass();    // makes sure EMF is up and running, probably not necessary
        final ResourceSet resourceSet = new ResourceSetImpl();
//        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceImpl());
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
//        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
        final Resource outputRes = resourceSet.createResource(URI.createFileURI(ePackage.getName() + ".xmi")); //
        // add our new package to resource contents
        objects.forEach(x -> outputRes.getContents().add(x));
//        outputRes.getContents().add(ePackage); //TODO then the meta model is also included in the instance model
        outputRes.getResourceSet().getPackageRegistry().put(ePackage.getNsURI(), ePackage);

        /*
         * Save the resource using OPTION_SCHEMA_LOCATION save option to produce
         * xsi:schemaLocation attribute in the document
         */
//        org.eclipse.emf.ecore.xmi.XMLResource.OPTION_PROCESS_DANGLING_HREF
        Map<String, Object> options = new HashMap<>();
        options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
        options.put(XMLResource.OPTION_PROCESS_DANGLING_HREF, "THROW"); //see: https://books.google.de/books?id=ff-9ZYhvPwwC&pg=PA317&lpg=PA317&dq=emf+OPTION_PROCESS_DANGLING_HREF&source=bl&ots=yBXkH3qSpD&sig=ACfU3U3uEGX_DCnDa2DAnjRboybhyGsKng&hl=en&sa=X&ved=2ahUKEwiCg-vI7_DgAhXDIVAKHU1PAIgQ6AEwBHoECAYQAQ#v=onepage&q=emf%20OPTION_PROCESS_DANGLING_HREF&f=false
        outputRes.save(outputStream, options);

    }

    public static void writeDynamicMetaModel(EPackage ePackage, OutputStream outputStream) throws IOException {
        ResourceSet metaResourceSet = new ResourceSetImpl();
        metaResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new XMLResourceFactoryImpl());

        Resource metaResource = metaResourceSet.createResource(URI.createURI(ePackage.getName() + ".ecore")); //URI.createURI(ePackage.getName())); //URI.createURI(filename + ".ecore"));
        metaResource.getContents().add(ePackage);
        Map options = new HashMap();
        options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
        metaResource.save(outputStream, options);
        outputStream.close();
    }
}