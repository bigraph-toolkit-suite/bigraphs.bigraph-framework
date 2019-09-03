package de.tudresden.inf.st.bigraphs.core.impl.builder;

import de.tudresden.inf.st.bigraphs.core.Bigraph;
import de.tudresden.inf.st.bigraphs.core.impl.ecore.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils;
import de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.BigraphBaseModelPackage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants.BIGRAPH_BASE_MODEL;

/**
 * A simple file store to serialize/deserialize bigraph model files.
 * <b>For experimental purposes only.</b>
 *
 * @author Dominik Grzelak
 */
public class BigraphArtifactHelper {

    /**
     * Get all {@link EObject} instances of a bigraph. Necessary when exporting an Ecore model.
     *
     * @param bigraph the bigraphs whos {@link EObject} instances should be collected
     * @return a collection of the {@link EObject} instances for the given bigraph
     */
    public static Collection<EObject> getResourcesFromBigraph(Bigraph<?> bigraph) {
        Collection<EObject> allresources = new ArrayList<>();
        // the child-parent relationship is automatically considered as well as "ports"
        // that is way we don't need to call getNodes() and getSites()
        bigraph.getRoots().forEach((x) -> allresources.add(x.getInstance()));
        bigraph.getOuterNames().forEach((x) -> allresources.add(x.getInstance()));
        bigraph.getInnerNames().forEach((x) -> allresources.add(x.getInstance()));
        bigraph.getEdges().forEach((x) -> allresources.add(x.getInstance()));
        return allresources;
    }

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
}
