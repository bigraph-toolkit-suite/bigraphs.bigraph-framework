package org.bigraphs.framework.core.impl.signature;

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
    private DynamicControl control;
    private MutableList<DynamicControl> kindsOfControl = Lists.mutable.empty();

    public static KindSort create(DynamicControl control, MutableList<DynamicControl> kindsOfControl) {
        return new KindSort(control, kindsOfControl);
    }

    private KindSort(DynamicControl control, MutableList<DynamicControl> kindsOfControl) {
        this.control = control;
        this.kindsOfControl = kindsOfControl;
    }

    public DynamicControl getControl() {
        return control;
    }

    public MutableList<DynamicControl> getKindsOfControl() {
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
