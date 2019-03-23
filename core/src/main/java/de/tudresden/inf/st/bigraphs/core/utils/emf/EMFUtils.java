package de.tudresden.inf.st.bigraphs.core.utils.emf;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants.BIGRAPH_BASE_MODEL;

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
