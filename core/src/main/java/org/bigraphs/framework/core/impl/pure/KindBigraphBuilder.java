/*
 * Copyright (c) 2023-2024 Bigraph Toolkit Suite Developers
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
package org.bigraphs.framework.core.impl.pure;

import org.bigraphs.framework.core.datatypes.EMetaModelData;
import org.bigraphs.framework.core.exceptions.BigraphMetaModelLoadingFailedException;
import org.bigraphs.framework.core.exceptions.SignatureValidationFailedException;
import org.bigraphs.framework.core.factory.AbstractBigraphFactory;
import org.bigraphs.framework.core.impl.signature.KindSignature;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

public class KindBigraphBuilder extends PureBigraphBuilder<KindSignature> {


    protected KindBigraphBuilder(KindSignature signature) throws BigraphMetaModelLoadingFailedException {
        super(signature);
    }

    protected KindBigraphBuilder(EObject signatureMetaModel) {
        super(signatureMetaModel);
    }

    protected KindBigraphBuilder(KindSignature signature, EMetaModelData metaModelData) throws BigraphMetaModelLoadingFailedException {
        super(signature, metaModelData);
    }

    protected KindBigraphBuilder(EObject signatureMetaModel, EMetaModelData metaModelData) {
        super(signatureMetaModel, metaModelData);
    }

    protected KindBigraphBuilder(KindSignature signature, EPackage metaModel, EObject instanceModel) {
        super(signature, metaModel, instanceModel);
    }

    protected KindBigraphBuilder(EObject signatureMetaModel, EPackage metaModel, EObject instanceModel) {
        super(signatureMetaModel, metaModel, instanceModel);
    }

    protected KindBigraphBuilder(KindSignature signature, String metaModelFilePath, String instanceModelFilePath) throws BigraphMetaModelLoadingFailedException {
        super(signature, metaModelFilePath, instanceModelFilePath);
    }

    protected KindBigraphBuilder(KindSignature signature, EPackage metaModel, String instanceModelFilePath) throws BigraphMetaModelLoadingFailedException {
        super(signature, metaModel, instanceModelFilePath);
    }

    protected KindBigraphBuilder(EObject signatureInstanceModel, String metaModelFilePath, String instanceModelFilePath) {
        super(signatureInstanceModel, metaModelFilePath, instanceModelFilePath);
    }

    protected KindBigraphBuilder(KindSignature signature, String metaModelFilePath) throws BigraphMetaModelLoadingFailedException {
        super(signature, metaModelFilePath);
    }

    protected KindBigraphBuilder(EObject signatureInstanceModel, String metaModelFilePath) {
        super(signatureInstanceModel, metaModelFilePath);
    }


    /**
     * Should not be directly called by the user. Instead use the {@link AbstractBigraphFactory}.
     *
     * @param instanceModelFilePath file path to the instance model
     * @param metaModelFilePath     the file to the Ecore meta model
     * @param signature             the signature for the builder and the generated bigraph
     * @return a configured builder with the bigraph instance loaded
     * @throws BigraphMetaModelLoadingFailedException If the provided signature metamodel does not conform the the
     *                                                builders signature type {@code S}
     */
    public static KindBigraphBuilder create(@NonNull KindSignature signature, String metaModelFilePath, String instanceModelFilePath) throws BigraphMetaModelLoadingFailedException {
        return new KindBigraphBuilder(signature, metaModelFilePath, instanceModelFilePath);
    }

    /**
     * @throws SignatureValidationFailedException If the provided signature instance model does not conform the the
     *                                            builders signature type {@code S}
     */
    public static KindBigraphBuilder create(@NonNull EObject signatureInstance, String metaModelFilePath, String instanceModelFilePath) {
        return new KindBigraphBuilder(signatureInstance, metaModelFilePath, instanceModelFilePath);
    }

    public static KindBigraphBuilder create(
            @NonNull KindSignature signature,
            @NonNull EPackage bigraphMetaModel, String instanceModelFilePath) {
        return new KindBigraphBuilder(signature, bigraphMetaModel, instanceModelFilePath);
    }

    /**
     * @throws BigraphMetaModelLoadingFailedException If the provided bigraph metamodel could not be loaded
     */
    public static KindBigraphBuilder create(@NonNull KindSignature signature, EPackage metaModel, EObject instanceModel) throws BigraphMetaModelLoadingFailedException {
        return new KindBigraphBuilder(signature, metaModel, instanceModel);
    }

    /**
     * @throws SignatureValidationFailedException If the provided signature instance model does not conform the the
     *                                            builders signature type {@code S}
     */
    public static KindBigraphBuilder create(@NonNull EObject signatureInstance, EPackage metaModel, EObject instanceModel) {
        return new KindBigraphBuilder(signatureInstance, metaModel, instanceModel);
    }

    /**
     * Should not be directly called by the user. Instead use the {@link AbstractBigraphFactory}.
     *
     * @param signature the signature for the builder
     * @return a pure bigraph builder with the given signature
     * @throws BigraphMetaModelLoadingFailedException If the provided bigraph metamodel does not conform the the
     *                                                builders signature type {@code S}
     */
    public static KindBigraphBuilder create(@NonNull KindSignature signature)
            throws BigraphMetaModelLoadingFailedException {
        return new KindBigraphBuilder(signature);
    }

    /**
     * @throws SignatureValidationFailedException If the provided signature instance model does not conform the the
     *                                            builders signature type {@code S}
     */
    public static KindBigraphBuilder create(@NonNull EObject signatureInstance)
            throws BigraphMetaModelLoadingFailedException {
        return new KindBigraphBuilder(signatureInstance);
    }

    /**
     * Should not be directly called by the user. Instead use the {@link AbstractBigraphFactory}.
     *
     * @param signature     the signature for the builder and generated bigraph
     * @param metaModelData the meta data to use for the model
     * @return a pure bigraph builder over the given signature
     * @throws BigraphMetaModelLoadingFailedException If the provided bigraph metamodel does not conform the the
     *                                                builders signature type {@code S}
     */
    public static KindBigraphBuilder create(@NonNull KindSignature signature, EMetaModelData metaModelData)
            throws BigraphMetaModelLoadingFailedException {
        return new KindBigraphBuilder(signature, metaModelData);
    }

    /**
     * @throws SignatureValidationFailedException If the provided signature instance model does not conform the the
     *                                            builders signature type {@code S}
     */
    public static KindBigraphBuilder create(@NonNull EObject signatureInstance, EMetaModelData metaModelData)
            throws BigraphMetaModelLoadingFailedException {
        return new KindBigraphBuilder(signatureInstance, metaModelData);
    }

    /**
     * @throws BigraphMetaModelLoadingFailedException If the provided bigraph metamodel does not conform the the
     *                                                builders signature type {@code S}
     */
    public static KindBigraphBuilder create(@NonNull KindSignature signature, String metaModelFileName)
            throws BigraphMetaModelLoadingFailedException {
        return new KindBigraphBuilder(signature, metaModelFileName);
    }

    /**
     * @throws SignatureValidationFailedException If the provided signature instance model does not conform the the
     *                                            builders signature type {@code S}
     */
    public static KindBigraphBuilder create(@NonNull EObject signatureInstance, String metaModelFileName)
            throws BigraphMetaModelLoadingFailedException {
        return new KindBigraphBuilder(signatureInstance, metaModelFileName);
    }
}
