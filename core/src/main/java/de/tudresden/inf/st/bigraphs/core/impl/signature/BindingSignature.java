package de.tudresden.inf.st.bigraphs.core.impl.signature;

import de.tudresden.inf.st.bigraphs.core.AbstractEcoreSignature;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import java.util.Set;

/**
 * <strong>Note:</strong> This class is not yet implemented!
 * <p>
 * Points can be bound in binding bigraphs and are represented by this signature type.
 * <p>
 * According to Milner, a binding implies that for a node it has arity 0 and it is passive.
 * <p>
 * Can only be used within binding bigraphs.
 * <p>
 * A binding signature K is a set of controls. For each K ∈ K it provides a pair of finite ordinals: the binding arity
 * arb(K) = h and the free arity arf(k) = k. We write ar(K) = arb(K) + arf (k).
 *
 * @author Dominik Grzelak
 */
public class BindingSignature extends AbstractEcoreSignature<BindingControl<? extends NamedType, ? extends FiniteOrdinal>> {

    public BindingSignature(Set<BindingControl<? extends NamedType, ? extends FiniteOrdinal>> controls) {
        super(controls);
    }

    public boolean isBindingControl(BindingControl<? extends NamedType, ? extends FiniteOrdinal> control) {
        if (!getControls().contains(control)) return false;
        BindingControl<? extends NamedType, ? extends FiniteOrdinal> controlByName = getControlByName(control.getNamedType().stringValue());
        return controlByName.isBindingControl();
    }

    @Override
    public EPackage getMetaModel() {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public EObject getInstanceModel() {
        throw new RuntimeException("Not implemented yet");
    }

}
