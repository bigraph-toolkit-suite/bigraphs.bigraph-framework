package de.tudresden.inf.st.bigraphs.store;

import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphArtifactHelper;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.DynamicEcoreBigraph;
import org.eclipse.emf.common.util.EList;
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
import java.util.*;

/**
 * A simple file store to serialize/deserialize bigraph model files.
 * <b>For experimental purposes only.</b>
 */
public class BigraphModelFileStore {

    /**
     * FOR TESTING ONLY - TODO MOVE
     *
     * @param bigraph
     * @throws IOException
     */
    public static void exportBigraph(DynamicEcoreBigraph bigraph) throws IOException {
        BigraphModelFileStore.exportBigraph(bigraph, "sample", System.out);
    }

    public static void exportBigraph(DynamicEcoreBigraph bigraph, String filename, OutputStream outputStream) throws IOException {
        //        bigraph.getRoots().forEach((x) -> allresources.add(x.getInstance()));
//        bigraph.getOuterNames().forEach((x) -> allresources.add(x.getInstance()));
//        bigraph.getInnerNames().forEach((x) -> allresources.add(x.getInstance()));
//        bigraph.getEdges().forEach((x) -> allresources.add(x.getInstance()));
        Collection<EObject> allresources = BigraphArtifactHelper.getResourcesFromBigraph(bigraph);
//            allresources.add(newPackage);
//            allresources.addAll();
        BigraphModelFileStore.writeDynamicInstanceModel(bigraph.getModelPackage(), allresources, filename, outputStream);
    }

    public static void exportMetaModel(DynamicEcoreBigraph bigraph, String filename, OutputStream outputStream) throws IOException {
        BigraphModelFileStore.writeDynamicMetaModel(bigraph.getModelPackage(), filename, outputStream);
    }



    //TODO: add UTF-8
    public static void writeDynamicInstanceModel(EPackage ePackage, Collection<EObject> objects, String name, OutputStream outputStream) throws IOException {
        ResourceSet resourceSet = new ResourceSetImpl();
        EcorePackage.eINSTANCE.eClass();    // makes sure EMF is up and running, probably not necessary
//        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
//                "*", new XMLResourceFactoryImpl());
//        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceImpl());
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
//        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
        Resource outputRes = resourceSet.createResource(URI.createFileURI(name + ".xmi")); //
        // add our new package to resource contents
        objects.forEach(x -> outputRes.getContents().add(x));
//        outputRes.getContents().add(ePackage); //TODO then the meta model is also included in the instance model
        outputRes.getResourceSet().getPackageRegistry().put("http:///com.ibm.dynamic.example.bookstore.ecore", ePackage);

//        outputRes.getContents().add(object);
//        metapackage.setName(name);
//        metapackage.setNsPrefix(name);
//        metapackage.setNsURI(namespace + name);
        // and at last, we save to standard out.  Remove the first argument to save to file specified in pathToOutputFile
        /*
         * Save the resource using OPTION_SCHEMA_LOCATION save option toproduce
         * xsi:schemaLocation attribute in the document
         */
//        org.eclipse.emf.ecore.xmi.XMLResource.OPTION_PROCESS_DANGLING_HREF
        Map options = new HashMap();
        options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
        options.put(XMLResource.OPTION_PROCESS_DANGLING_HREF, "THROW"); //see: https://books.google.de/books?id=ff-9ZYhvPwwC&pg=PA317&lpg=PA317&dq=emf+OPTION_PROCESS_DANGLING_HREF&source=bl&ots=yBXkH3qSpD&sig=ACfU3U3uEGX_DCnDa2DAnjRboybhyGsKng&hl=en&sa=X&ved=2ahUKEwiCg-vI7_DgAhXDIVAKHU1PAIgQ6AEwBHoECAYQAQ#v=onepage&q=emf%20OPTION_PROCESS_DANGLING_HREF&f=false
        outputRes.save(outputStream, options);

    }


    public static void writeDynamicMetaModel(EPackage ePackage, String filename, OutputStream outputStream) throws IOException {
        ResourceSet metaResourceSet = new ResourceSetImpl();
        metaResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new XMLResourceFactoryImpl());
        Resource metaResource = metaResourceSet.createResource(URI.createURI(filename + ".ecore"));
        metaResource.getContents().add(ePackage);
        Map options = new HashMap();
        options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
        metaResource.save(outputStream, options);
    }

    public static EList<EObject> loadInstanceModel(EPackage metaModelPackageWithSignature, java.net.URI file) throws IOException {
        ResourceSet load_resourceSet = new ResourceSetImpl();
        load_resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMLResourceFactoryImpl());
        load_resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());

        // register the dynamic package locally
        load_resourceSet.getPackageRegistry().put(metaModelPackageWithSignature.getNsURI(), metaModelPackageWithSignature);
        //Also the basemetamodel?

        URI uri = URI.createFileURI(new File(file).getAbsolutePath());
        Resource load_resource = load_resourceSet.createResource(uri);
//        Resource load_resource = load_resourceSet.getResource(URI.createURI(filename), true);
        //https://wiki.eclipse.org/EMF/FAQ#How_do_I_make_my_EMF_standalone_application_Eclipse-aware.3F
//        load_resourceSet.getURIConverter().getURIMap().putAll(EcorePlugin.computePlatformURIMap(false));
//        Resource load_resource = load_resourceSet.getResource(URI.createPlatformResourceURI(filename, true), true);

        load_resource.load(Collections.EMPTY_MAP);
        return load_resource.getContents();
    }

    public static EPackage loadEcoreMetaModel(java.net.URI path) throws IOException {
        EcorePackage.eINSTANCE.eClass();    // makes sure EMF is up and running, probably not necessary
        ResourceSet resourceSet = new ResourceSetImpl();

        URI uri = URI.createFileURI(new File(path).getAbsolutePath());

        // resource factories
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());

        Resource resource = resourceSet.createResource(uri);
//        Resource resource = resourceSet.getResource(uri, true);
        resource.load(Collections.EMPTY_MAP);
        return (EPackage) resource.getContents().get(0);
    }
}
