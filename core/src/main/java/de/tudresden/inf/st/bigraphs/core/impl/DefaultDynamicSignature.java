package de.tudresden.inf.st.bigraphs.core.impl;

import de.tudresden.inf.st.bigraphs.core.AbstractSignature;
import de.tudresden.inf.st.bigraphs.core.ControlKind;
import de.tudresden.inf.st.bigraphs.core.impl.builder.DynamicSignatureBuilder;
import de.tudresden.inf.st.bigraphs.models.signatureBaseModel.BControl;
import de.tudresden.inf.st.bigraphs.models.signatureBaseModel.BSignature;

import java.util.Set;

public final class DefaultDynamicSignature extends AbstractSignature<DefaultDynamicControl> {

    public DefaultDynamicSignature() {
    }

    public DefaultDynamicSignature(Set<DefaultDynamicControl> controls) {
        super(controls);
    }

    public DefaultDynamicSignature(BSignature bSignature) {
        super(bSignature);
        DynamicSignatureBuilder dynamicSignatureBuilder = new DynamicSignatureBuilder();
        for (BControl bControl : bSignature.getBControls()) {
            dynamicSignatureBuilder = dynamicSignatureBuilder
                    .newControl(bControl.getName(), bControl.getArity())
                    .kind(ControlKind.fromString(bControl.getStatus().getName())).assign();
        }
        this.controls = dynamicSignatureBuilder.create().getControls();
    }
}
