package org.bigraphs.framework.core;

import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.utils.emf.EMFUtils;
import org.bigraphs.model.bigraphBaseModel.BigraphBaseModelFactory;
import org.bigraphs.model.bigraphBaseModel.BigraphBaseModelPackage;
import org.bigraphs.model.signatureBaseModel.SignatureBaseModelFactory;
import org.bigraphs.model.signatureBaseModel.SignatureBaseModelPackage;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
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

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.bigraphs.framework.core.BigraphMetaModelConstants.BIGRAPH_BASE_MODEL;
import static org.bigraphs.framework.core.BigraphMetaModelConstants.SIGNATURE_BASE_MODEL;

/**
 * File-based utility for managing Ecore/XMI bigraph models.
 * <ul>
 *   <li>Serialize {@link EObject}/{@link EPackage} to {@code *.xmi} and {@code *.ecore}.</li>
 *   <li>Deserialize {@code *.xmi}/{@code *.ecore} into {@link EObject}/{@link EPackage}.</li>
 * </ul>
 * <p>
 * XMI is the XML serialization format for Ecore.
 *
 * @author Dominik Grzelak
 */
public class BigraphFileModelManagement {

    public static boolean VALIDATE = false;

    private final static String DEFAULT_ENCODING = "UTF-8";

    public enum Format {
        ECORE, XMI
    }

    public static class Load {

        /**
         * Loads the internal metamodel of the base bigraphical signature that is declared in the bigraphMetaModel dependency.
         *
         * @return the base signature metamodel as {@link EPackage}
         * @throws IOException if the model could not be loaded from the bigraphMetaModel dependency
         */
        public static EPackage internalSignatureMetaMetaModel() throws IOException {
            ResourceSet resourceSet = initResourceSet(null);
            enableExtendedMetadata(resourceSet);

            URL resource1 = EMFUtils.class.getResource(SIGNATURE_BASE_MODEL);
            URI uri = URI.createURI(Objects.requireNonNull(resource1).toString());

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
        public static EPackage internalBigraphMetaMetaModel() throws IOException {
            ResourceSet resourceSet = initResourceSet(BigraphBaseModelPackage.eINSTANCE);

            URL resource1 = EMFUtils.class.getResource(BIGRAPH_BASE_MODEL);
            URI uri = URI.createURI(resource1.toString());

            // https://wiki.eclipse.org/EMF/FAQ#How_do_I_make_my_EMF_standalone_application_Eclipse-aware.3F
            // resourceSet.getURIConverter().getURIMap().putAll(EcorePlugin.computePlatformURIMap(false));
            Resource resource = resourceSet.createResource(uri);
            resource.load(Collections.EMPTY_MAP);
            EPackage ePackage = (EPackage) resource.getContents().get(0);
            validateModel(ePackage);
            return ePackage;
        }

        public static EPackage signatureMetaModel(String metaModelFilename) throws IOException {
            return signatureMetaModel(new FileInputStream(metaModelFilename));
        }

        public static EPackage signatureMetaModel(InputStream inputStream) throws IOException {
            ResourceSet resourceSet = initResourceSet(null);
            URL resource1 = EMFUtils.class.getResource(SIGNATURE_BASE_MODEL);
            URI uri = URI.createURI(Objects.requireNonNull(resource1).toString());
            Resource resource = resourceSet.createResource(uri);
            resource.load(inputStream, Collections.EMPTY_MAP);
            EPackage ePackage = (EPackage) resource.getContents().get(0);
            validateModel(ePackage);
            return ePackage;
        }

        public static List<EObject> signatureInstanceModel(String filename) throws IOException {
            Objects.requireNonNull(filename);
            File file = new File(filename);
            if (!file.exists()) throw new IOException("File couldn't be found: " + filename);
            ResourceSet load_resourceSet = initResourceSet(internalSignatureMetaMetaModel());
            Resource load_resource = load_resourceSet.createResource(URI.createFileURI(filename));
            load_resource.load(Collections.EMPTY_MAP);
            EList<EObject> contents = load_resource.getContents();
            validateModel(contents.get(0));
            return contents;
        }

        public static List<EObject> signatureInstanceModel(String metamodelFilename, String instanceModelFilename) throws IOException {
            Objects.requireNonNull(instanceModelFilename);
            File file = new File(instanceModelFilename);
            if (!file.exists()) throw new IOException("File couldn't be found: " + instanceModelFilename);
            ResourceSet load_resourceSet = initResourceSet(signatureMetaModel(metamodelFilename));
            Resource load_resource = load_resourceSet.createResource(URI.createFileURI(instanceModelFilename));
            load_resource.load(Collections.EMPTY_MAP);
            EList<EObject> contents = load_resource.getContents();
            validateModel(contents.get(0));
            return contents;
        }

        /**
         * Loads the bigraph metamodel ({@code *.ecore}) by specifying a filename.
         *
         * @param filePath the filename of the bigraph metamodel to load
         * @return the loaded bigraph metamodel
         * @throws IOException if the file does not exist
         */
        public static EPackage bigraphMetaModel(String filePath) throws IOException {
            return bigraphMetaModel(filePath, true);
        }

        public static EPackage bigraphMetaModel(String filePath, boolean validate) throws IOException {
            assert filePath != null;
            File file = new File(filePath);
            if (!file.exists()) throw new IOException("File couldn't be found: " + filePath);
            return bigraphMetaModel(new FileInputStream(file), validate);
        }

        /**
         * Loads the bigraph metamodel ({@code *.ecore}) by specifying an input stream.
         *
         * @param inputStream an input stream of the bigraph metamodel to load
         * @return the loaded bigraph metamodel
         * @throws IOException if the model could not be loaded
         */
        public static EPackage bigraphMetaModel(InputStream inputStream) throws IOException {
            return bigraphMetaModel(inputStream, true);
        }

        public static EPackage bigraphMetaModel(InputStream inputStream, boolean validate) throws IOException {
            ResourceSet resourceSet = initResourceSet(null);
            URL resource1 = EMFUtils.class.getResource(BIGRAPH_BASE_MODEL);
            URI uri = URI.createURI(Objects.requireNonNull(resource1).toString());
            //        Resource resource = resourceSet.createResource(URI.createURI("*.ecore"));
            Resource resource = resourceSet.createResource(uri);
            resource.load(inputStream, Collections.EMPTY_MAP);
            EPackage ePackage = (EPackage) resource.getContents().get(0);
            if (validate) {
                validateModel(ePackage);
            }
            return ePackage;
        }

        /**
         * Loads an instance model without validating it against its meta-model.
         *
         * @param filenameInstancemodel the file path of the instance model
         * @return list of {@link EObject} resources representing the bigraph
         * @throws IOException if the file doesn't exists, or an exception is raised when loading the resource
         * @see #bigraphInstanceModel(EPackage, String)
         * @see #bigraphInstanceModel(EPackage, InputStream)
         */
        public static List<EObject> bigraphInstanceModel(String filenameInstancemodel) throws IOException {
            return bigraphInstanceModel((EPackage) null, filenameInstancemodel);
        }

        /**
         * Loads an instance model and validates it against the given metamodel.
         *
         * @param filenameMetamodel     the filename of the metamodel
         * @param filenameInstancemodel the filename of the instance model
         * @return list of {@link EObject} resources representing the bigraph
         * @throws IOException if the file does not exist, or an exception is raised when loading the resource
         * @see #bigraphInstanceModel(String)
         * @see #bigraphInstanceModel(EPackage, InputStream)
         */
        public static List<EObject> bigraphInstanceModel(String filenameMetamodel, String filenameInstancemodel) throws IOException {
            return bigraphInstanceModel(bigraphMetaModel(filenameMetamodel), filenameInstancemodel);
        }

        /**
         * Loads an instance model and validates it against the given metamodel.
         *
         * @param metaModelPackageWithSignature the metamodel object of the instance model
         * @param filenameInstancemodel         the filename of the instance model
         * @return list of {@link EObject} resources representing the bigraph
         * @throws IOException if the file does not exist, or an exception is raised when loading the resource
         * @see #bigraphInstanceModel(String)
         * @see #bigraphInstanceModel(EPackage, InputStream)
         */
        public static List<EObject> bigraphInstanceModel(EPackage metaModelPackageWithSignature, String filenameInstancemodel) throws IOException {
            ResourceSet load_resource = BigraphFileModelManagement
                    .getResourceSetBigraphInstanceModel(metaModelPackageWithSignature, filenameInstancemodel);
            return delegator(load_resource);
        }

        /**
         * Loads an instance model and validates it against the given meta-model.
         *
         * @param metaModelPackageWithSignature the metamodel of the instance model
         * @param instanceModelInputStream      the input stream of the instance model
         * @return list of {@link EObject} resources representing the bigraph
         * @throws IOException if the file does not exist, or an exception is raised when loading the resource
         * @see #bigraphInstanceModel(String)
         * @see #bigraphInstanceModel(EPackage, InputStream)
         */
        public static List<EObject> bigraphInstanceModel(EPackage metaModelPackageWithSignature, InputStream instanceModelInputStream) throws IOException {
            ResourceSet load_resource = BigraphFileModelManagement.getResourceSetBigraphInstanceModel(metaModelPackageWithSignature, instanceModelInputStream);
            return delegator(load_resource);
        }
    }

    private static List<EObject> delegator(ResourceSet load_resource) {
        Resource resource = load_resource.getResources().get(0);
        EList<EObject> contents = resource.getContents();
        validateModel(contents.get(0));
        return contents;
    }

    public static class Store {
        /**
         * Exports the Ecore-based instance model of a bigraph.
         */
        public static void exportAsInstanceModel(EcoreBigraph bigraph, OutputStream outputStream) throws IOException {
            MutableMap<Object, Object> of = Maps.mutable.of();
            if (bigraph instanceof PureBigraph) {
                ((PureBigraph) bigraph).getNodes().forEach(x -> {
                    //TODO only remove java object attributes
                    of.put(x, x.getAttributes());
                    x.setAttributes(Map.of());
                });
            }
            EMFUtils.writeDynamicInstanceModel(bigraph.getMetaModel(), Collections.singleton(bigraph.getInstanceModel()), outputStream, null);
            if (bigraph instanceof PureBigraph) {
                ((PureBigraph) bigraph).getNodes().forEach(x -> {
                    x.setAttributes((Map<String, Object>) of.get(x));
                });
            }
        }

        public static void exportAsInstanceModel(EcoreBigraph bigraph, OutputStream outputStream, String newNamespaceLocation) throws IOException {
            MutableMap<Object, Object> of = Maps.mutable.of();
            if (bigraph instanceof PureBigraph) {
                ((PureBigraph) bigraph).getNodes().forEach(x -> {
                    //TODO only remove java object attributes
                    of.put(x, x.getAttributes());
                    x.setAttributes(Map.of());
                });
            }
            EMFUtils.writeDynamicInstanceModel(bigraph.getMetaModel(), Collections.singleton(bigraph.getInstanceModel()), outputStream, newNamespaceLocation);
            if (bigraph instanceof PureBigraph) {
                ((PureBigraph) bigraph).getNodes().forEach(x -> {
                    x.setAttributes((Map<String, Object>) of.get(x));
                });
            }
        }

        /**
         * Exports the Ecore-based instance model of a signature.
         */
        public static void exportAsInstanceModel(EcoreSignature signature, OutputStream outputStream) throws IOException {
            EMFUtils.writeDynamicInstanceModel(signature.getMetaModel(), Collections.singleton(signature.getInstanceModel()), outputStream, null);
        }

        public static void exportAsInstanceModel(EcoreSignature signature, OutputStream outputStream, String newNamespaceLocation) throws IOException {
            EMFUtils.writeDynamicInstanceModel(signature.getMetaModel(), Collections.singleton(signature.getInstanceModel()), outputStream, newNamespaceLocation);
        }

        /**
         * Exports the Ecore-based metamodel of a bigraph.
         * The filename must match the name of the EPackage of the given {@code bigraph} argument.
         *
         * @param bigraph      the bigraph's metamodel to export
         * @param outputStream the output stream, e.g., {@link FileOutputStream}
         * @see EcoreBigraph#getEMetaModelData()
         */
        public static void exportAsMetaModel(EcoreBigraph bigraph, OutputStream outputStream) throws IOException {
            EMFUtils.writeDynamicMetaModel(bigraph.getMetaModel(), DEFAULT_ENCODING, outputStream);
        }

        /**
         * Exports the Ecore-based metamodel of a bigraph.
         * The filename is automatically derived and matches the name of the EPackage of the given {@code bigraph} argument.
         *
         * @param bigraph the bigraph's metamodel to export
         * @param folder  the folder where the metamodel should be stored
         */
        public static void exportAsMetaModel(EcoreBigraph bigraph, Path folder) throws IOException {
            String s = Paths.get(folder.toAbsolutePath().toString(), bigraph.getEMetaModelData().getName() + ".ecore").toAbsolutePath().toString();
            EMFUtils.writeDynamicMetaModel(bigraph.getMetaModel(), DEFAULT_ENCODING, new FileOutputStream(s));
        }

        /**
         * Exports the Ecore-based metamodel of a signature
         * The filename must match the name of the EPackage.
         */
        public static void exportAsMetaModel(EcoreSignature signature, OutputStream outputStream) throws IOException {
            EMFUtils.writeDynamicMetaModel(signature.getMetaModel(), DEFAULT_ENCODING, outputStream);
        }
    }

    //    }

    public static void validateModel(EObject eObject) {
        if (VALIDATE) {
            Diagnostic diagnostic = Diagnostician.INSTANCE.validate(eObject);
            if (diagnostic.getSeverity() != Diagnostic.OK) {
                System.out.println("ERROR in: " + diagnostic.getMessage());
                for (Diagnostic child : diagnostic.getChildren()) {
                    System.out.println(child.getMessage());
                }
                throw new RuntimeException("Invalid model loaded.");
            }
        }
    }

    /**
     * Returns the resource set ({@link ResourceSet}) of a loaded bigraph instance model and validates it against the
     * given meta-model.
     *
     * @param metaModelPackageWithSignature the meta-model of the instance model
     * @param filename                      the filename of the instance model ({@code *.xmi})
     * @return the resource set of the bigraph instance model loaded
     * @throws IOException
     * @see Load#bigraphInstanceModel(String)
     * @see Load#bigraphInstanceModel(EPackage, String)
     * @see Load#bigraphInstanceModel(EPackage, InputStream)
     */
    private static ResourceSet getResourceSetBigraphInstanceModel(EPackage metaModelPackageWithSignature, String filename) throws IOException {
        ResourceSet load_resourceSet = initResourceSet(metaModelPackageWithSignature);
        Resource load_resource = load_resourceSet.createResource(URI.createFileURI(filename));
        load_resource.load(Collections.EMPTY_MAP);
        return load_resourceSet;
    }

    /**
     * Returns the resource set ({@link ResourceSet}) of a loaded bigraph instance model and validates it against the
     * given meta-model.
     *
     * @param metaModelPackageWithSignature the meta-model of the instance model
     * @param instanceModelInputStream      the input stream of the instance model
     * @return the resource set of the bigraph instance model loaded
     * @throws IOException
     * @see Load#bigraphInstanceModel(String)
     * @see Load#bigraphInstanceModel(EPackage, String)
     * @see Load#bigraphInstanceModel(EPackage, InputStream)
     */
    private static ResourceSet getResourceSetBigraphInstanceModel(EPackage metaModelPackageWithSignature, InputStream instanceModelInputStream) throws IOException {
        ResourceSet load_resourceSet = initResourceSet(metaModelPackageWithSignature);
        Resource load_resource = load_resourceSet.createResource(URI.createURI("*.xmi"));
        load_resource.load(instanceModelInputStream, Collections.EMPTY_MAP);
        return load_resourceSet;
    }


    /**
     * Prepares a {@link ResourceSet} in order to load a bigraph instance model.
     *
     * @param metaModelPackageWithSignature the base bigraph metamodel; can be {@code null}
     * @return an initialized resource set
     */
    private static ResourceSet initResourceSet(EPackage metaModelPackageWithSignature) {
        EcorePackage.eINSTANCE.eClass(); // Makes sure that EMF is "up and running"
        BigraphBaseModelFactory.eINSTANCE.eClass(); // Makes sure that the Bigraph-Metamodel is "up and running"
//        SignatureBaseModelFactory.eINSTANCE.eClass(); // Makes sure that the Signature-Metamodel is "up and running"
//        BigraphBaseModelPackage.eINSTANCE.eClass();
//        SignatureBaseModelPackage.eINSTANCE.eClass();

        ResourceSet load_resourceSet = new ResourceSetImpl();

        // Register factories
        // registerResourceFactories(Resource.Factory.Registry.INSTANCE);
        EMFUtils.registerXMIResourceFactories(Resource.Factory.Registry.INSTANCE);
        EMFUtils.registerEcoreResourceFactories(Resource.Factory.Registry.INSTANCE);
        EMFUtils.registerXMIResourceFactories(load_resourceSet.getResourceFactoryRegistry());
        EMFUtils.registerEcoreResourceFactories(load_resourceSet.getResourceFactoryRegistry());

        if (metaModelPackageWithSignature != null) {
            // Register packages
            // See also: https://www.cct.lsu.edu/~rguidry/eclipse-doc36/src-html/org/eclipse/emf/cdo/common/model/EMFUtil.html
            // EPackage.Registry.INSTANCE.put(BigraphBaseModelPackage.eNS_URI, BigraphBaseModelPackage.eINSTANCE);
            // load_resourceSet.getPackageRegistry().put(BigraphBaseModelPackage.eNS_URI, BigraphBaseModelPackage.eINSTANCE);
//            EMFUtils.registerPackages(EPackage.Registry.INSTANCE, SignatureBaseModelPackage.eINSTANCE);
            EMFUtils.registerPackages(EPackage.Registry.INSTANCE, BigraphBaseModelPackage.eINSTANCE);
//            EMFUtils.registerPackages(load_resourceSet.getPackageRegistry(), SignatureBaseModelPackage.eINSTANCE);
            EMFUtils.registerPackages(load_resourceSet.getPackageRegistry(), BigraphBaseModelPackage.eINSTANCE);
            if (Objects.nonNull(metaModelPackageWithSignature.getNsURI()) && !metaModelPackageWithSignature.getNsURI().isEmpty()) {
                EMFUtils.registerPackages(EPackage.Registry.INSTANCE, metaModelPackageWithSignature);
                EMFUtils.registerPackages(load_resourceSet.getPackageRegistry(), metaModelPackageWithSignature);
            }
        }
        return load_resourceSet;
    }

    /**
     * Enables the option {@code XMLResource.OPTION_EXTENDED_META_DATA} on a given resource set.
     *
     * @param rs the same resource set as provided
     */
    private static void enableExtendedMetadata(ResourceSet rs) {
        final ExtendedMetaData extendedMetaData = new BasicExtendedMetaData(rs.getPackageRegistry());
        rs.getLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA, extendedMetaData);
    }
}
