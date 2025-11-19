/*
 * Copyright (c) 2019-2025 Bigraph Toolkit Suite Developers
 * Main Developer: Dominik Grzelak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bigraphs.framework.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.bigraphs.framework.core.datatypes.EMetaModelData;
import org.bigraphs.framework.core.exceptions.EcoreBigraphFileSystemException;
import org.bigraphs.framework.core.factory.BigraphFactory;
import org.bigraphs.framework.core.utils.auxiliary.MemoryOperations;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * Interface defining standard methods for all Ecore-based bigraph classes.
 * <p>
 * Technology-specific (i.e., EMF-compliant) counterpart to {@link Bigraph}, similar to {@link EcoreSignature}.
 *
 * @param <S> the Ecore-based signature type
 * @author Dominik Grzelak
 * @see Bigraph
 * @see EcoreSignature
 */
public interface EcoreBigraph<S extends AbstractEcoreSignature<?>>
        extends HasSignature<S>, EcoreBigraphExt {

    @Override
    S getSignature();

    default boolean isBPort(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_PORT);
    }

    default boolean isBInnerName(EObject eObject) {
        return isOfEClass(eObject, BigraphMetaModelConstants.CLASS_INNERNAME);
    }

    default boolean isBOuterName(EObject eObject) {
        return eObject.eClass().getClassifierID() ==
                (((EPackageImpl) getMetaModel()).getEClassifierGen(BigraphMetaModelConstants.CLASS_OUTERNAME)).getClassifierID() ||
                eObject.eClass().equals(((EPackageImpl) getMetaModel()).getEClassifierGen(BigraphMetaModelConstants.CLASS_OUTERNAME)) ||
                eObject.eClass().getEAllSuperTypes().contains(((EPackageImpl) getMetaModel()).getEClassifierGen(BigraphMetaModelConstants.CLASS_OUTERNAME));
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
        return ((EPackageImpl) getMetaModel()).getEClassifierGen(eClassifier) != null &&
                (
                        eObject.eClass().getName().equals(((EPackageImpl) getMetaModel()).getEClassifierGen(eClassifier).getName()) ||
                                eObject.eClass().equals(((EPackageImpl) getMetaModel()).getEClassifierGen(eClassifier)) ||
                                eObject.eClass().getEAllSuperTypes().contains(((EPackageImpl) getMetaModel()).getEClassifierGen(eClassifier))
                );
    }

    /**
     * Retrieves the meta model data from the bigraph's {@link EPackage}.
     *
     * @return meta model object
     */
    default EMetaModelData getEMetaModelData() {
        EMetaModelData.MetaModelDataBuilder builder = EMetaModelData.builder();
        if (Objects.nonNull(getMetaModel())) {
            builder.setNsPrefix(getMetaModel().getNsPrefix());
            builder.setNsUri(getMetaModel().getNsURI());
            builder.setName(getMetaModel().getName());
        }
        return builder.create();
    }

    /**
     * A lightweight container for a bigraph that holds only the Ecore-relevant objects.
     * <p>
     * Though it provides no higher-order functions to access all its elements, it can be useful in some cases.
     * For example, to copy a {@link Bigraph} instance into memory and later
     * retrieving it via a builder.
     * <p>
     * Further, this stub allows the completely copy a bigraph by calling the {@link #clone()} method.
     *
     * @author Dominik Grzelak
     */
    class Stub<S extends AbstractEcoreSignature<?>> implements EcoreBigraph<S> {
        EPackage metaModel;
        EObject instanceModel;

        /**
         * Copy constructor
         *
         * @param bigraph an Ecore-based bigraph
         */
        public Stub(EcoreBigraph<S> bigraph) {
            this(bigraph.getMetaModel(), bigraph.getInstanceModel());
        }

        private Stub(EPackage metaModel, EObject instanceModel) {
            this.metaModel = metaModel;
            this.instanceModel = instanceModel;
        }

        /**
         * Return the respective Ecore-based metamodel.
         *
         * @return the metamodel
         * @see org.bigraphs.model.bigraphBaseModel.BigraphBaseModelPackage
         */
        @Override
        public EPackage getMetaModel() {
            return metaModel;
        }

        @Override
        public EObject getInstanceModel() {
            return instanceModel;
        }

        /**
         * This method returns a copy of the actual bigraph.
         * <p>
         * Therefore, the bigraph is exported into the memory and loaded again.
         *
         * @return a copy of the current bigraph.
         */
        public Stub<S> clone() throws CloneNotSupportedException {
            return new Stub<>(metaModel, EcoreUtil.copy(instanceModel));
        }

        /**
         * Return the actual instance model (*.xmi) of the bigraph as an input stream.
         *
         * @return the bigraph's Ecore instance model as an input stream.
         * @throws EcoreBigraphFileSystemException if the input stream for the model instance could not be created for
         *                                         some reason. See the wrapped Exception to retrieve a more detailed
         *                                         error message.
         */
        public InputStream getInputStreamOfInstanceModel() throws EcoreBigraphFileSystemException {
            try {
                DefaultFileSystemManager manager = MemoryOperations.getInstance().createFileSystemManager();
                final FileObject fo1 = manager.resolveFile("ram:/instance.xmi");
                OutputStream outputStream = fo1.getContent().getOutputStream();
                BigraphFileModelManagement.Store.exportAsInstanceModel(this, outputStream);
                outputStream.close();
                return fo1.getContent().getInputStream();
            } catch (Exception e) {
                throw new EcoreBigraphFileSystemException(e);
            }
        }

        @Override
        public S getSignature() {
            return BigraphFactory.createOrGetSignature(getInstanceModel());
        }

    }
}
