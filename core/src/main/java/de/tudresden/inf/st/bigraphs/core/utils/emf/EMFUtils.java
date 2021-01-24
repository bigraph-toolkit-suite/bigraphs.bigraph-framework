package de.tudresden.inf.st.bigraphs.core.utils.emf;

import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Maps;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

//https://sdqweb.ipd.kit.edu/wiki/Creating_EMF_Model_instances_programmatically
public class EMFUtils {

    private final static EcoreFactory FACTORY = EcoreFactory.eINSTANCE;

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
        Optional<EAttribute> first = eClass.getEAllAttributes().stream().filter(x -> x.getName().equals(attributeName)).findFirst();
        return first.orElse(null);
    }

    public static List<EReference> findAllReferences(EClass eClass) {
        return eClass.eContents().stream()
                .filter(x -> x instanceof EReference)
                .map(x -> ((EReference) x))
                .collect(Collectors.toList());
    }

    public static Map<String, EReference> findAllReferences2(EClass eClass) {
        MutableMap<String, EReference> refMap = Maps.mutable.empty();
        eClass.getEAllReferences()
                .forEach(x -> {
                    refMap.put(x.getName(), x);
                });
        return refMap;
    }

    public static void addSuperType(EClass anEClass, EPackage aPackage,
                                    String name) {
        final EClass eSuperClass = (EClass) aPackage.getEClassifier(name);
        anEClass.getESuperTypes().add(eSuperClass);
    }

    public static void addSuperType(EClass anEClass, EClass eSuperClass) {
        anEClass.getESuperTypes().add(eSuperClass);
    }

    public static void writeDynamicMetaModel(EPackage ePackage, OutputStream outputStream) throws IOException {
        EMFUtils.writeDynamicMetaModel(ePackage, "UTF-8", outputStream);
    }

    public static void writeDynamicMetaModel(EPackage ePackage, String encoding, OutputStream outputStream) throws IOException {
        ResourceSet metaResourceSet = new ResourceSetImpl();
        registerResourceFactories(Resource.Factory.Registry.INSTANCE);
        registerResourceFactories(metaResourceSet.getResourceFactoryRegistry());
//        metaResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new XMLResourceFactoryImpl());

        Resource metaResource = metaResourceSet.createResource(URI.createURI(ePackage.getName() + ".ecore")); //URI.createURI(ePackage.getName())); //URI.createURI(filename + ".ecore"));
        metaResource.getContents().add(ePackage);
        Map<String, Object> options = new HashMap<>();
        options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
        options.put(XMLResource.OPTION_ENCODING, encoding);
        metaResource.save(outputStream, options);
        outputStream.close();
    }

    public static void registerResourceFactories(Resource.Factory.Registry registry) {
        registry.getExtensionToFactoryMap().put("*", new XMLResourceFactoryImpl());
        registry.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
        registry.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
    }
}
