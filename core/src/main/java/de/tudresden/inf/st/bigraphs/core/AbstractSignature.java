package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public abstract class AbstractSignature<C extends Control<? extends NamedType, ? extends FiniteOrdinal>> implements Signature<C> {
    //collection of controls of type C
    protected Set<C> controls;
//    protected C control;

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
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "controls=" + controls +
                '}';
    }
    
}
