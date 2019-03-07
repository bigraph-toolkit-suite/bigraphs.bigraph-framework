package de.tudresden.inf.st.bigraphs.core.utils.emf;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

//https://sdqweb.ipd.kit.edu/wiki/Creating_EMF_Model_instances_programmatically
public class EMFUtils {

    private final static EcoreFactory FACTORY = EcoreFactory.eINSTANCE;

    public static EPackage loadEcoreModel(String ecoreResource) throws IOException {
        ResourceSet resourceSet = new ResourceSetImpl();


        URL resource1 = EMFUtils.class.getResource(ecoreResource);
//        java.net.URI uri1 = resource1.toURI();
        URI uri = URI.createURI(resource1.toString());

//        EcorePackage.eINSTANCE.eClass();    // makes sure EMF is up and running, probably not necessary
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
//        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new XMIResourceFactoryImpl()); //probably not necessary
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl()); //probably not necessary
//        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new EcoreResourceFactoryImpl());//probably not necessary

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

    public static void writeEcoreFile(EPackage metapackage, String name, String namespace, OutputStream outputStream) throws IOException {
        ResourceSet resourceSet = new ResourceSetImpl();
        EcorePackage.eINSTANCE.eClass();    // makes sure EMF is up and running, probably not necessary
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new XMIResourceFactoryImpl()); //probably not necessary
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl()); //probably not necessary
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new EcoreResourceFactoryImpl());//probably not necessary
        Resource outputRes = resourceSet.createResource(URI.createFileURI(name + ".ecore"));
        // add our new package to resource contents
        outputRes.getContents().add(metapackage);
        metapackage.setName(name);
        metapackage.setNsPrefix(name);
        metapackage.setNsURI(namespace + name);
        // and at last, we save to standard out.  Remove the first argument to save to file specified in pathToOutputFile
        outputRes.save(outputStream, Collections.emptyMap());

    }


    public static void serializeMetaModel(EPackage ePackage, String filename, OutputStream outputStream) {
        ResourceSet metaResourceSet = new ResourceSetImpl();

        // Register XML Factory implementation to handle .ecore files
        metaResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
                "ecore", new XMLResourceFactoryImpl());
        Resource metaResource = metaResourceSet.createResource(URI.createURI("./" + filename + ".ecore"));
        metaResource.getContents().add(ePackage);

        try {
            metaResource.save(null);
//            ePackage.setNsURI("http://de.tudresden.inf.BigraphBaseModel");
            metaResource.save(outputStream, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //TODO: add UTF-8
    public static void writeDynamicInstanceModel(List<EObject> objects, String name, OutputStream outputStream) throws IOException {
        ResourceSet resourceSet = new ResourceSetImpl();
        EcorePackage.eINSTANCE.eClass();    // makes sure EMF is up and running, probably not necessary
//        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
//                "*", new XMLResourceFactoryImpl());
//        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceImpl());
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
//        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
        Resource outputRes = resourceSet.createResource(URI.createFileURI("./" + name + ".xmi"));
        // add our new package to resource contents
        objects.forEach(x -> outputRes.getContents().add(x));
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

    public static EAttribute addAttribute(EClass eClass, String name,
                                          EDataType type, boolean isId, int lowerBound, int upperBound) {
        final EAttribute attribute = FACTORY.createEAttribute();
        // always add to container first
        eClass.getEStructuralFeatures().add(attribute);
        attribute.setName(name);
        attribute.setEType(type);
        attribute.setID(isId);
        attribute.setChangeable(true);
        attribute.setLowerBound(lowerBound);
        attribute.setUpperBound(upperBound);
        return attribute;
    }

    /**
     * Creates a default attribute with standard settings as in Eclipse (EMF)
     *
     * @param eClass
     * @param name
     * @param type
     * @return
     */
    public static EAttribute addAttribute(EClass eClass, String name, EDataType type) {
        return addAttribute(eClass, name, type, false, true, true, true, 0, 1);
    }

    public static EAttribute addAttribute(EClass eClass, String name,
                                          EDataType type, boolean isId, boolean ordered, boolean unique, boolean changeable, int lowerBound, int upperBound) {
        final EAttribute attribute = FACTORY.createEAttribute();
        // always add to container first
        eClass.getEStructuralFeatures().add(attribute);
        attribute.setName(name);
        attribute.setEType(type);
        attribute.setID(isId);
        attribute.setOrdered(ordered);
        attribute.setUnique(unique);
        attribute.setChangeable(changeable);
        attribute.setLowerBound(lowerBound);
        attribute.setUpperBound(upperBound);
        return attribute;
    }

    public static EReference addReference(EClass eClass, String name,
                                          EClassifier type, int lowerBound, int upperBound) {
        final EReference reference = FACTORY.createEReference();
        // always add to container first
        eClass.getEStructuralFeatures().add(reference);
        reference.setName(name);
        reference.setEType(type);
        reference.setLowerBound(lowerBound);
        reference.setUpperBound(upperBound);
        return reference;
    }

    public static EPackage createPackage(final String name, final String prefix,
                                         final String uri) {
        final EPackage epackage = FACTORY.createEPackage();
        epackage.setName(name);
        epackage.setNsPrefix(prefix);
        epackage.setNsURI(uri);
        return epackage;
    }

    public static EClass createEClass(final String name) {
        final EClass eClass = FACTORY.createEClass();
        eClass.setName(name);
        return eClass;
    }

    public static EAttribute findAttribute(EClass eClass, String attributeName) {
        List<EAttribute> collect = eClass.getEAllAttributes().stream().filter(x -> x.getName().equals(attributeName)).collect(Collectors.toList());
        if (collect.size() == 1) {
            return collect.get(0);
        }
        return null;
    }

    public static List<EReference> findAllReferences(EClass eClass) {
        return eClass.eContents().stream()
                .filter(x -> x instanceof EReference)
                .map(x -> ((EReference) x))
                .collect(Collectors.toList());
    }

    public static void addSuperType(EClass anEClass, EPackage aPackage,
                                    String name) {
        final EClass eSuperClass = (EClass) aPackage.getEClassifier(name);
        anEClass.getESuperTypes().add(eSuperClass);
    }

}
