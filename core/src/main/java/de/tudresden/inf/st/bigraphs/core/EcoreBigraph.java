package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.EMetaModelData;
import de.tudresden.inf.st.bigraphs.core.utils.auxiliary.MemoryOperations;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Extension interface with standard methods for Ecore-based bigraph classes.
 *
 * @author Dominik Grzelak
 */
public interface EcoreBigraph {
    default boolean isBPort(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_PORT);
    }

    default boolean isBInnerName(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_INNERNAME);
    }

    default boolean isBOuterName(EObject eObject) {
//        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_OUTERNAME);
        return eObject.eClass().getClassifierID() ==
                (((EPackageImpl) getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_OUTERNAME)).getClassifierID() ||
                eObject.eClass().equals(((EPackageImpl) getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_OUTERNAME)) ||
                eObject.eClass().getEAllSuperTypes().contains(((EPackageImpl) getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_OUTERNAME));
    }


    default boolean isBPoint(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_POINT);
    }

    default boolean isBNode(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_NODE);
    }

    default boolean isBSite(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_SITE);
    }

    default boolean isNameable(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_NAMEABLETYPE);
    }

    default boolean isIndexable(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_INDEXABLETYPE);
    }

    default boolean isBRoot(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_ROOT);
    }

    default boolean isBLink(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_LINK);
    }

    default boolean isBEdge(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_EDGE);
    }

    default boolean isBPlace(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_PLACE);
    }

    //works only for elements of the calling class
    default boolean isOfEClass(EObject eObject, String eClassifier) {
        return ((EPackageImpl) getModelPackage()).getEClassifierGen(eClassifier) != null &&
                (
                        eObject.eClass().getName().equals(((EPackageImpl) getModelPackage()).getEClassifierGen(eClassifier).getName()) ||
                                eObject.eClass().equals(((EPackageImpl) getModelPackage()).getEClassifierGen(eClassifier)) ||
//                                eObject.eClass().getEAllSuperTypes().stream().map(ENamedElement::getName).collect(Collectors.toList()).contains(((EPackageImpl) getModelPackage()).getEClassifierGen(eClassifier).getName()) ||
                                eObject.eClass().getEAllSuperTypes().contains(((EPackageImpl) getModelPackage()).getEClassifierGen(eClassifier))
                );
    }

    /**
     * Return the bigraph meta-model, which contains the signature of the current bigraph.
     * <p>
     * It is an meta-model, which extends the base bigraph meta meta-model.
     *
     * @return
     * @see de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.BigraphBaseModelPackage
     */
    EPackage getModelPackage();

    /**
     * Return the bigraph instance model (Ecore)
     *
     * @return the bigraph instance model
     */
    EObject getModel();

    /**
     * Retrieves the meta model data from the bigraph's {@link EPackage}.
     *
     * @return meta model object
     */
    default EMetaModelData getEMetaModelData() {
        EMetaModelData.MetaModelDataBuilder builder = EMetaModelData.builder();
        if (Objects.nonNull(getModelPackage())) {
            builder.setNsPrefix(getModelPackage().getNsPrefix());
            builder.setNsUri(getModelPackage().getNsURI());
            builder.setName(getModelPackage().getName());
        }
        return builder.create();
    }

    /**
     * A lightweight container for a bigraph that holds only the Ecore-relevant objects.
     * <p>
     * Though it provides no higher-order functions to access all its elements, it can be useful in some cases.
     * For example, to copy a {@link de.tudresden.inf.st.bigraphs.core.Bigraph} instance into memory and later
     * retrieving it via a builder.
     * <p>
     * Further, this stub allows the completely copy a bigraph by calling the {@link #clone()} method.
     *
     * @author Dominik Grzelak
     */
    class Stub implements EcoreBigraph {
        EPackage metaModel;
        EObject instanceModel;

        /**
         * Copy constructor
         *
         * @param bigraph an Ecore-based bigraph
         */
        public Stub(EcoreBigraph bigraph) {
            this(bigraph.getModelPackage(), bigraph.getModel());
//            this(bigraph.getModelPackage(), EcoreUtil.copy(bigraph.getModel()));
//            this.metaModel = bigraph.getModelPackage(); //EcoreUtil.copy(bigraph.getModelPackage());
//            this.instanceModel = EcoreUtil.copy(bigraph.getModel());
        }

        private Stub(EPackage metaModel, EObject instanceModel) {
            this.metaModel = metaModel;
            this.instanceModel = instanceModel;
        }

        @Override
        public EPackage getModelPackage() {
            return metaModel;
        }

        @Override
        public EObject getModel() {
            return instanceModel;
        }

        /**
         * This methods returns a copy of the actual bigraph.
         * <p>
         * Therefore, the bigraph is exported into the memory and loaded again.
         *
         * @return a copy of the current bigraph.
         * @throws CloneNotSupportedException
         */
        public Stub clone() throws CloneNotSupportedException {
//            try {
//                DefaultFileSystemManager manager = MemoryOperations.getInstance().createFileSystemManager();
//                final FileObject fo1 = manager.resolveFile("ram:/instance.xmi");
//                final FileObject fo2 = manager.resolveFile("ram:/meta.ecore");
//                OutputStream outputStream = fo1.getContent().getOutputStream();
//                OutputStream outputStream2 = fo2.getContent().getOutputStream();
//                BigraphArtifacts.exportAsInstanceModel(this, outputStream);
//                BigraphArtifacts.exportAsMetaModel(this, outputStream2);
//                outputStream.close();
//                outputStream2.close();
//                InputStream inputStream = fo1.getContent().getInputStream();
//                InputStream inputStream2 = fo2.getContent().getInputStream();
////                EPackage ePackage = BigraphArtifacts.loadBigraphMetaModel(inputStream2);
////                List<EObject> eObjects = BigraphArtifacts.loadBigraphInstanceModel(ePackage, inputStream);
//                List<EObject> eObjects = BigraphArtifacts.loadBigraphInstanceModel(metaModel, inputStream);
//                inputStream.close();
//                inputStream2.close();
//                return new Stub(ePackage, eObjects.get(0));
//                return new Stub(metaModel, eObjects.get(0));
//            } catch (IOException e) {
//                throw new CloneNotSupportedException(e.getMessage());
//            }
            return new Stub(metaModel, EcoreUtil.copy(instanceModel));
        }

        /**
         * Return the actual instance model (*.xmi) of the bigraph as an input stream.
         *
         * @return the bigraph as an input stream.
         */
        public InputStream getInputStreamOfInstanceModel() {
            try {
                DefaultFileSystemManager manager = MemoryOperations.getInstance().createFileSystemManager();
                final FileObject fo1 = manager.resolveFile("ram:/instance.xmi");
                OutputStream outputStream = fo1.getContent().getOutputStream();
                BigraphArtifacts.exportAsInstanceModel(this, outputStream);
                outputStream.close();
                return fo1.getContent().getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
