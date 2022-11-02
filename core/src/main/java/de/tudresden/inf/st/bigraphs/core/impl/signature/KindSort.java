package de.tudresden.inf.st.bigraphs.core.impl.signature;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

import java.util.Objects;

/**
 * A place-sort for a kind signature.
 *
 * @author Dominik Grzelak
 * @see KindSignature
 */
public class KindSort {
    private DefaultDynamicControl control;
    private MutableList<DefaultDynamicControl> kindsOfControl = Lists.mutable.empty();

    public static KindSort create(DefaultDynamicControl control, MutableList<DefaultDynamicControl> kindsOfControl) {
        return new KindSort(control, kindsOfControl);
    }

    private KindSort(DefaultDynamicControl control, MutableList<DefaultDynamicControl> kindsOfControl) {
        this.control = control;
        this.kindsOfControl = kindsOfControl;
    }

    public DefaultDynamicControl getControl() {
        return control;
    }

    public MutableList<DefaultDynamicControl> getKindsOfControl() {
        return kindsOfControl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KindSort kindSort = (KindSort) o;
        return control.equals(kindSort.control) &&
                kindsOfControl.equals(kindSort.kindsOfControl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(control, kindsOfControl);
    }
}
