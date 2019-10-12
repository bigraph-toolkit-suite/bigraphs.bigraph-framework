package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils;
import de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.BigraphBaseModelPackage;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants.BIGRAPH_BASE_MODEL;

/**
 * A simple file store to serialize/deserialize bigraph model files.
 * <b>For experimental purposes only.</b>
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
}
