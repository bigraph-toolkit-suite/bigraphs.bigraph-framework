/*
 * Copyright (c) 2022-2025 Bigraph Toolkit Suite Developers
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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

/**
 * Technology-specific interface for <strong>EMF/Ecore-based</strong> bigraph objects (e.g., bigraphs or signatures).
 * <p>
 * Provides two basic methods:
 * <ul>
 *   <li>Access the metamodel ({@link EPackage})</li>
 *   <li>Access the instance model ({@link EObject})</li>
 * </ul>
 *
 * @author Dominik Grzelak
 * @see EcoreBigraph
 * @see EcoreSignature
 */
public interface EcoreBigraphExt {

    /**
     * Return the metamodel of a bigraph object or a signature object.
     * <p>
     * It is a metamodel that either extends the base bigraph metamodel or the base signature metamodel.
     *
     * @return the metamodel in Ecore format
     * @see org.bigraphs.model.bigraphBaseModel.BigraphBaseModelPackage
     * @see org.bigraphs.model.signatureBaseModel.SignatureBaseModelPackage
     */
    EPackage getMetaModel();

    /**
     * Return the Ecore-based instance model of a bigraph object or signature object.
     *
     * @return the instance model in Ecore format
     */
    EObject getInstanceModel();
}
