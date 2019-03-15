package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.NamedType;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultControl;

import java.util.Iterator;

//TODO change iterable to set!
public interface Signature<C extends Control<? extends NamedType, ? extends FiniteOrdinal>> {
    Iterable<C> getControls();

    default C getControlByName(String name) {
        C selected = null;
        Iterator<C> iterator = getControls().iterator();
        while (iterator.hasNext()) {
            C next1 = iterator.next();
            if (next1.getNamedType().stringValue().equals(name)) {
                selected = next1;
                break;
            }
        }
        return selected;
    }
}
