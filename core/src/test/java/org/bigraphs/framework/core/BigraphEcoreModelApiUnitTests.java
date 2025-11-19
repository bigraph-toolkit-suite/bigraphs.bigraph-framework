/*
 * Copyright (c) 2019-2024 Bigraph Toolkit Suite Developers
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

import org.bigraphs.model.bigraphBaseModel.*;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.ocl.OCLInput;
import org.eclipse.ocl.ParserException;
import org.eclipse.ocl.ecore.Constraint;
import org.eclipse.ocl.ecore.OCL;
import org.eclipse.ocl.ecore.delegate.OCLDelegateDomain;
import org.eclipse.ocl.ecore.delegate.OCLInvocationDelegateFactory;
import org.eclipse.ocl.ecore.delegate.OCLSettingDelegateFactory;
import org.eclipse.ocl.ecore.delegate.OCLValidationDelegateFactory;
import org.junit.jupiter.api.Test;

/**
 * This test class uses the generated Ecore API of the {@link org.bigraphs.model.bigraphBaseModel.BigraphBaseModelPackage}
 * to create some bigraphs. This is the direct approach without using the Bigraph Framework without further functionality.
 *
 * @author Dominik Grzelak
 */
public class BigraphEcoreModelApiUnitTests {

    @Test
    void no_duplicate_controls() {
        // not working because BControlImpl is abstract ("java.lang.IllegalArgumentException: The class 'BControl' is not a valid classifier"):
        // EObject eObject = SignatureBaseModelFactory.eINSTANCE.create(SignatureBaseModelPackage.eINSTANCE.getBControl());
//        BControl bControl = SignatureBaseModelFactory.eINSTANCE.createBControl();
//        bControl.setName("User");
//        bControl.setArity(1);
//        BControl bControl2 = SignatureBaseModelFactory.eINSTANCE.createBControl();
//        bControl2.setName("User");
//        bControl2.setArity(1);
//        bControl2.setStatus(BControlStatus.ACTIVE);
//
//        BDynamicSignature bSignature = SignatureBaseModelFactory.eINSTANCE.createBDynamicSignature();
//        bSignature.getBControls().add(bControl);
//        bSignature.getBControls().add(bControl2);
//
//        System.out.println(bSignature.getBControls().size());
//        System.out.println(bSignature.getBControls());
//
//
//        // Triggering Validation
//        Diagnostic diagnostic = Diagnostician.INSTANCE.validate(bSignature);
////        System.out.println(diagnostic);
//        if (diagnostic.getSeverity() != Diagnostic.OK) {
//            System.out.println("ERROR in: " + diagnostic.getMessage());
//            for (Diagnostic child : diagnostic.getChildren()) {
//                System.out.println(child.getMessage());
//            }
//        }
    }

    @Test
    void ocl_setup_01() {
        // https://wiki.eclipse.org/OCL/FAQ
        // With OCl in Ecore: Standalone setup for OCL
        // - "Register OCL implementation with EMF"
        // - "Not necessary within Eclipse runtime"
        // - "OCL Validation supports pure reflective EMF"

        String oclDelegateURI = OCLDelegateDomain.OCL_DELEGATE_URI;
        EOperation.Internal.InvocationDelegate.Factory.Registry.INSTANCE.put(oclDelegateURI,
                new OCLInvocationDelegateFactory.Global());
        EStructuralFeature.Internal.SettingDelegate.Factory.Registry.INSTANCE.put(oclDelegateURI,
                new OCLSettingDelegateFactory.Global());
        EValidator.ValidationDelegate.Registry.INSTANCE.put(oclDelegateURI,
                new OCLValidationDelegateFactory.Global());
    }

    @Test
    void ocl_query_02() throws ParserException {

    }

    @Test
    void api_test_01() {
        BBigraph bBigraph = BigraphBaseModelFactory.eINSTANCE.createBBigraph();
        BRoot bRoot = BigraphBaseModelFactory.eINSTANCE.createBRoot();
        bBigraph.getBRoots().add(bRoot);

        System.out.println(bRoot.getBBigraph().hashCode());
        System.out.println(bBigraph.getBRoots().size());
        BBigraph bBigraph2 = BigraphBaseModelFactory.eINSTANCE.createBBigraph();
        bBigraph2.getBRoots().add(bRoot);

        BEdge edge2 = BigraphBaseModelFactory.eINSTANCE.createBEdge();
        BPort port21 = BigraphBaseModelFactory.eINSTANCE.createBPort();
        BPort port22 = BigraphBaseModelFactory.eINSTANCE.createBPort();
        edge2.getBPoints().add(port21);
        edge2.getBPoints().add(port22);
        BNode node2 = BigraphBaseModelFactory.eINSTANCE.createBNode();
        node2.getAttributes().put("key", 1);
        node2.getAttributes().put("key2", "1");
        node2.getBPorts().add(port21);
        node2.getBPorts().add(port22);
        bRoot.getBChild().add(node2);
        node2.setName("A");
//        EcoreUtil.setConstraints();

        System.out.println(bRoot.getBBigraph().hashCode());
        System.out.println(bBigraph.getBRoots().size());
        System.out.println(bBigraph2.getBRoots().size());
//http://de.tudresden.inf.st.bigraphs.models
        try {//bigraphBaseModel
            //forall(...) //filter only those ports that have a link
            // then check size
//            enfants->forall( e | e.age < self.age - 7)
            //->notEmpty() implies e.x < f.x
            OCL ocl = OCL.newInstance();
            ocl.parse(new OCLInput("package bigraphBaseModel " +
                    "context BNode " +
                    "inv name: name <> '' " +
                    "endpackage"));
            for (Constraint constraint : ocl.getConstraints()) {
                System.out.println(constraint);
                boolean check = ocl.check(node2, constraint);
                System.out.println("\t: " + check);
            }
        } catch (ParserException e) {
            e.printStackTrace();
        }

        // Triggering Validation
        Diagnostic diagnostic = Diagnostician.INSTANCE.validate(bBigraph);
        if (diagnostic.getSeverity() != Diagnostic.OK) {
            System.out.println("ERROR in: " + diagnostic.getMessage());
            for (Diagnostic child : diagnostic.getChildren()) {
            }
        }

    }
}
