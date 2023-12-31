package org.bigraph.model;


import org.bigraphs.framework.core.Control;
import org.bigraphs.framework.core.ControlStatus;
import org.bigraphs.framework.core.Signature;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.StringTypedName;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraph.model.assistants.IObjectIdentifier;
import org.bigraph.model.interfaces.ISignature;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Adapter for our {@link Signature} class to be used with {@link org.bigraph.model.savers.SignatureXMLSaver}.
 * <p>
 * This is an adapter for the signature of type {@link DefaultDynamicSignature}.
 *
 * @author Dominik Grzelak
 */
public class SignatureAdapter extends org.bigraph.model.Signature implements ISignature, IObjectIdentifier.Resolver {

    Signature<? extends Control<StringTypedName, FiniteOrdinal<Integer>>> adaptee;

    public SignatureAdapter(Signature<? extends Control<StringTypedName, FiniteOrdinal<Integer>>> adaptee) {
        super();
        this.adaptee = adaptee;
        initControls();
    }

    public void initControls() {
        Set<? extends Control<StringTypedName, FiniteOrdinal<Integer>>> controls = adaptee.getControls();
        for (Control<StringTypedName, FiniteOrdinal<Integer>> each : controls) {
            org.bigraph.model.Control control = new org.bigraph.model.Control();
            control.setName(each.getNamedType().stringValue());
            control.setKind(translate(each.getControlKind()));
            List<? extends PortSpec> portSpecs = translatePorts(each.getArity());
            portSpecs.forEach(control::addPort);
            addControl(control);
        }
    }

    private List<PortSpec> translatePorts(FiniteOrdinal<Integer> arity) {
        List<PortSpec> ports = new ArrayList<>();
        for (int i = 0; i < arity.getValue().longValue(); i++) {
            PortSpec portSpec = new PortSpec();
            portSpec.setName("" + i);
            ports.add(portSpec);
        }
        return ports;
    }

    private org.bigraph.model.Control.Kind translate(ControlStatus kind) {
        switch (kind) {
            case ATOMIC:
                return org.bigraph.model.Control.Kind.ATOMIC;
            case ACTIVE:
                return org.bigraph.model.Control.Kind.ACTIVE;
            case PASSIVE:
                return org.bigraph.model.Control.Kind.PASSIVE;
            default:
                return org.bigraph.model.Control.Kind.ACTIVE;
        }
    }
}
