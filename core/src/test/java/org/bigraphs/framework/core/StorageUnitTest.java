/*
 * Copyright (c) 2026 Bigraph Toolkit Suite Developers
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

import static org.bigraphs.framework.core.factory.BigraphFactory.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
import org.bigraphs.framework.core.utils.BigraphUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.Test;

/**
 * @author Dominik Grzelak
 */
public class StorageUnitTest {

    public DynamicSignature sig() {
        // (1) Create Sample Signature
        return pureSignatureBuilder()
                .add("A", 0)
                .add("B", 1)
                .add("C", 2)
                .create();
    }

    public PureBigraph big() {
        return pureBuilder(sig()).root()
                .child("A")
                .child("B")
                .child("C")
                .create();
    }

    @Test
    public void save_and_load_signature_model() throws IOException {
        // (0) Create Sample Signature
        DynamicSignature sigOriginal = sig();

        // (1) Store Signature on Filesystem
        // Console output
        BigraphFileModelManagement.Store.exportAsInstanceModel(sigOriginal, System.out);
        // File output
        BigraphFileModelManagement.Store.exportAsInstanceModel(sigOriginal, new FileOutputStream("sigModel.xmi"));
        BigraphFileModelManagement.Store.exportAsMetaModel(sigOriginal, new FileOutputStream("sigMetaModel.ecore"));

        // (2) Load Signature From Filesystem
        List<EObject> eObjectsSig = BigraphFileModelManagement.Load.signatureInstanceModel(
                "sigMetaModel.ecore",
                "sigModel.xmi"
        );
        DynamicSignature sigLoaded = createOrGetSignature(eObjectsSig.getFirst());

        // (3) Compare
        System.out.println(sigOriginal);
        System.out.println(sigLoaded);
    }

    @Test
    public void save_and_load_bigraph_model() throws IOException {
        // (0) Create Sample Bigraph
        PureBigraph bigOriginal = big();

        // (1) Store Bigraph MetaModel on Filesystem
        BigraphFileModelManagement.Store.exportAsMetaModel(bigOriginal, new FileOutputStream("bigraphMetaModel.ecore"));

        // (2) Store the concrete Bigraph Instance Model on Filesystem
        BigraphFileModelManagement.Store.exportAsInstanceModel(bigOriginal, new FileOutputStream("model.xmi"));

        // (3) Load the Bigraph MetaModel from the file system
        EPackage ePackage = BigraphFileModelManagement.Load.bigraphMetaModel("bigraphMetaModel.ecore", false);

        // (3) Or, re-create it like this, since the signature is loaded:
        // EPackage ePackage = createOrGetBigraphMetaModel(sig());

        // (4) Initialize the EMF EPackage registry with the bigraphMetaModel
        EPackage.Registry.INSTANCE.put(ePackage.getNsURI(), ePackage);

        // (5) Load the instance model (do not provide the bigraphMetaModel)
        List<EObject> eObjects = BigraphFileModelManagement.Load.bigraphInstanceModel(
                "model.xmi"
        );

        // (6) Bigraph instance model loaded
        PureBigraph bigraphLoaded = BigraphUtil.toBigraph(ePackage, eObjects.getFirst(), sig());

        // (7) Compare the original and the loaded bigraphLoaded
        bigOriginal.getNodes().forEach(System.out::println);
        bigraphLoaded.getNodes().forEach(System.out::println);
    }
}
