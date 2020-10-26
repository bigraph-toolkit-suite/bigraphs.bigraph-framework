package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.*;
import de.tudresden.inf.st.bigraphs.models.signatureBaseModel.BControl;
import de.tudresden.inf.st.bigraphs.models.signatureBaseModel.BControlStatus;
import de.tudresden.inf.st.bigraphs.models.signatureBaseModel.BSignature;
import de.tudresden.inf.st.bigraphs.models.signatureBaseModel.SignatureBaseModelFactory;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EValidator;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
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
 * This test class uses the generated Ecore API of the {@link de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.BigraphBaseModelPackage}
 * to create some bigraphs. This is the direct approach without using the Bigraph Framework without further functionality.
 *
 * @author Dominik Grzelak
 */
public class BigraphEcoreModelApiUnitTests {

    @Test
    void no_duplicate_controls() {
        BControl bControl = SignatureBaseModelFactory.eINSTANCE.createBControl();
        bControl.setName("User");
        bControl.setArity(1);
        BControl bControl2 = SignatureBaseModelFactory.eINSTANCE.createBControl();
        bControl2.setName("User");
        bControl2.setArity(1);
        bControl2.setStatus(BControlStatus.ACTIVE);

        BSignature bSignature = SignatureBaseModelFactory.eINSTANCE.createBSignature();
        bSignature.getBControls().add(bControl);
        bSignature.getBControls().add(bControl2);

        System.out.println(bSignature.getBControls().size());
        System.out.println(bSignature.getBControls());


        // Triggering Validation
        Diagnostic diagnostic = Diagnostician.INSTANCE.validate(bSignature);
//        System.out.println(diagnostic);
        if (diagnostic.getSeverity() != Diagnostic.OK) {
            System.out.println("ERROR in: " + diagnostic.getMessage());
            for (Diagnostic child : diagnostic.getChildren()) {
                System.out.println(child.getMessage());
            }
        }
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
    void apiestt() {
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
