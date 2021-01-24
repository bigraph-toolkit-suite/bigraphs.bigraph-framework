package de.tudresden.inf.st.bigraphs.core.impl;

import de.tudresden.inf.st.bigraphs.core.AbstractSignature;
import de.tudresden.inf.st.bigraphs.core.ControlStatus;
import de.tudresden.inf.st.bigraphs.core.EcoreSignature;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.models.signatureBaseModel.BControl;
import de.tudresden.inf.st.bigraphs.models.signatureBaseModel.BSignature;
import de.tudresden.inf.st.bigraphs.models.signatureBaseModel.SignatureBaseModelFactory;
import de.tudresden.inf.st.bigraphs.models.signatureBaseModel.SignatureBaseModelPackage;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import java.util.Set;

public final class DefaultDynamicSignature extends AbstractSignature<DefaultDynamicControl> {
    protected SignatureBaseModelPackage ePackage = SignatureBaseModelPackage.eINSTANCE; //TODO
    protected BSignature instanceModel;

    public DefaultDynamicSignature(BSignature bSignature) { // TODO: change this to EObject
        super();
        //TODO: extend/re-create the metamodel here for the new approach, or maybe allow loading it
        EcoreSignature.validateBSignature(bSignature);
        instanceModel = bSignature;
        DynamicSignatureBuilder dynamicSignatureBuilder = new DynamicSignatureBuilder();
        for (BControl bControl : bSignature.getBControls()) {
            dynamicSignatureBuilder = dynamicSignatureBuilder
                    .newControl(bControl.getName(), bControl.getArity())
                    .status(ControlStatus.fromString(bControl.getStatus().getName())).assign();
        }
        this.controls = dynamicSignatureBuilder.create().getControls();
    }


    public DefaultDynamicSignature(Set<DefaultDynamicControl> controls) {
        super(controls);
        // TODO: the metamodel should be here extended/re-created also first by letting controls inheriting class BControl first
        SignatureBaseModelFactory factory = SignatureBaseModelFactory.eINSTANCE; //TODO: use loadInternal model: otherwise we might face a "A frozen model should not be modified" assertion exception
        instanceModel = factory.createBSignature();
        this.controls.forEach(x -> {
            BControl bControl = factory.createBControl();
            bControl.setArity(x.getArity().getValue().intValue());
            bControl.setName(x.getNamedType().stringValue());
            instanceModel.getBControls().add(bControl);
        });
        EcoreSignature.validateBSignature(instanceModel);
    }

    @Override
    public EPackage getModelPackage() {
        return ePackage;
    }

    @Override
    public EObject getModel() {
        return instanceModel;
    }
}
