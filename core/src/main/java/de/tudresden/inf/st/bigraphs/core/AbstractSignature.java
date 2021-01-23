package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.models.signatureBaseModel.BControl;
import de.tudresden.inf.st.bigraphs.models.signatureBaseModel.BSignature;
import de.tudresden.inf.st.bigraphs.models.signatureBaseModel.SignatureBaseModelFactory;
import de.tudresden.inf.st.bigraphs.models.signatureBaseModel.SignatureBaseModelPackage;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.Diagnostician;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public abstract class AbstractSignature<C extends Control<? extends NamedType, ? extends FiniteOrdinal>>
        implements Signature<C>, EcoreSignature {

    // collection of controls of type C
    protected Set<C> controls;

    protected AbstractSignature() {
        controls = new LinkedHashSet<>();
    }

    protected AbstractSignature(Set<C> controls) {
        this.controls = controls;
    }

    @Override
    public Set<C> getControls() {
        return controls;
    }

    @Override
    public abstract EPackage getModelPackage();

    @Override
    public abstract EObject getModel();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractSignature)) return false;
        AbstractSignature<?> that = (AbstractSignature<?>) o;
        return controls.equals(that.controls);
    }

    @Override
    public int hashCode() {
        return Objects.hash(controls);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "controls=" + controls +
                '}';
    }
}
