package de.tudresden.inf.st.bigraphs.core.factory;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple file utility class to serialize bigraphs and deserialize Ecore-based bigraph model files.
 *
 * @author Dominik Grzelak
 */
public class BigraphModelFileStore {


    /**
     * Exports the instance model (*.xmi) and meta-model (*.ecore) of a bigraph as Ecore models.
     *
     * @param bigraph       the bigraph to export
     * @param modelFilename the base filename of the exported model files
     * @throws IOException
     */
    public static void exportBigraph(Bigraph<?> bigraph, String modelFilename) throws IOException {
//        BigraphModelFileStore.exportBigraph(bigraph, modelFilename, System.out);
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
        BigraphModelFileStore.writeDynamicInstanceModel(bigraph.getModelPackage(), bigraph.getModel(), outputStream);
    }

    /**
     * Meta-model of a bigraph is exported
     *
     * @param bigraph
     * @param outputStream
     * @throws IOException
     */
    public static void exportAsMetaModel(Bigraph<?> bigraph, OutputStream outputStream) throws IOException {
        BigraphModelFileStore.writeDynamicMetaModel(bigraph.getModelPackage(), outputStream);
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
//        Path path = Paths.get("D:/workspace/AmanCV.docx");
        // call getFileName() and get FileName path object
//        Path fileName = path.getFileName();
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
