package de.tudresden.inf.st.bigraphs.core.impl;

import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;

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
}
