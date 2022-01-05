package de.tudresden.inf.st.bigraphs.core.impl.builder;

import de.tudresden.inf.st.bigraphs.core.AbstractEcoreSignature;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.datatypes.EMetaModelData;
import de.tudresden.inf.st.bigraphs.core.datatypes.FiniteOrdinal;
import de.tudresden.inf.st.bigraphs.core.datatypes.StringTypedName;
import de.tudresden.inf.st.bigraphs.core.exceptions.builder.ControlNotExistsException;
import de.tudresden.inf.st.bigraphs.core.factory.BigraphFactory;
import de.tudresden.inf.st.bigraphs.core.impl.DefaultDynamicControl;
import de.tudresden.inf.st.bigraphs.core.impl.KindSignature;
import de.tudresden.inf.st.bigraphs.core.impl.KindSort;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Builder class for kind signatures.
 * <p>
 * The builder returns an object of the class {@link de.tudresden.inf.st.bigraphs.core.impl.KindSignature}, which includes
 * both the Ecore-based kind signature metamodel and its instance model.
 * Both the metamodel for kind signatures and the instance model are created internally.
 * The instance model is based on the extended metamodel.
 *
 * @author Dominik Grzelak
 */
public class KindSignatureBuilder extends
        SignatureBuilder<StringTypedName, FiniteOrdinal<Integer>, KindControlBuilder, KindSignatureBuilder> {

    MutableMap<String, KindSort> kindSortsMap;

    @Override
    protected KindControlBuilder createControlBuilder() {
        return new KindControlBuilder();
    }

    public KindControlBuilder newControl(String name, int arity) {
        KindControlBuilder builder = createControlBuilder();
        builder.withControlListBuilder(this);
        return builder.identifier(StringTypedName.of(name)).arity(FiniteOrdinal.ofInteger(arity));
    }

    public KindSignatureBuilder addControl(String name, int arity) {
        KindControlBuilder builder = createControlBuilder();
        builder.withControlListBuilder(this);
        return builder.identifier(StringTypedName.of(name)).arity(FiniteOrdinal.ofInteger(arity)).assign();
    }

    private Optional<DefaultDynamicControl> getControl(String nameOfCtrl) {
        return getControls().stream().filter(x -> x.getNamedType().stringValue().equals(nameOfCtrl))
                .map(x -> (DefaultDynamicControl) x)
                .findFirst();
    }

    /**
     * This methods adds a place-sort for the given control.
     * The status of the control is thus {@code active}, i.e., it is an non-atomic control.
     * <p>
     * If this method is called multiple times with the same argument {@code control}, the new values {@code containingControls}
     * will be used to override the existing sorting for the control {@code control}.
     * <p>
     * If this method is called after, for example, {@link #addPassiveKindSort(String)}, then it will override
     * the previous configuration of being an atomic control.
     *
     * @param control            the control to specify the place-sorts for
     * @param containingControls the controls that can be nested under {@code control}
     * @return the same instance of the kind signature builder
     */
    public KindSignatureBuilder addActiveKindSort(String control, Collection<String> containingControls) throws ControlNotExistsException {
        Optional<DefaultDynamicControl> ctrl = getControl(control);
        if (!ctrl.isPresent()) throw new ControlNotExistsException(control);
        initMapIfRequired();
        List<DefaultDynamicControl> collect = getControls().stream()
                .filter(x -> containingControls.contains(x.getNamedType().stringValue()))
                .map(x -> (DefaultDynamicControl) x)
                .collect(Collectors.toList());
        kindSortsMap.put(control, KindSort.create(ctrl.get(), Lists.mutable.ofAll(collect)));
        return self();
    }

    /**
     * This method adds an empty place-sort for the given control.
     * The status of the control is thus {@code passive}, i.e., it is an atomic control.
     * <p>
     * If this method is called after, for example, {@link #addActiveKindSort(String, Collection)}, then it will override
     * the existing sort and declare the given control as {@code passive}.
     *
     * @param control the control to declare atomic
     * @return
     */
    public KindSignatureBuilder addPassiveKindSort(String control) throws ControlNotExistsException {
        Optional<DefaultDynamicControl> ctrl = getControl(control);
        if (!ctrl.isPresent()) throw new ControlNotExistsException(control);
        initMapIfRequired();
        kindSortsMap.put(control, KindSort.create(ctrl.get(), Lists.mutable.empty()));
        return self();
    }

    private void initMapIfRequired() {
        if (Objects.isNull(kindSortsMap)) {
            kindSortsMap = Maps.mutable.empty();
        }
    }

    @Override
    public KindSignature create() {
        if (Objects.isNull(kindSortsMap) || kindSortsMap.isEmpty())
            return (KindSignature) super.create();
        else
            return this.createWith(getControls(), kindSortsMap.values());
    }

    @Override
    public KindSignature create(EMetaModelData metaModelData) {
        if (Objects.isNull(kindSortsMap) || kindSortsMap.isEmpty())
            return (KindSignature) super.create(metaModelData);
        else
            return this.createWith(getControls(), kindSortsMap.values(), metaModelData);
    }

    @Override
    public KindSignature createWith(Iterable<? extends Control<StringTypedName, FiniteOrdinal<Integer>>> controls) {
        KindSignature sig = new KindSignature((Set<DefaultDynamicControl>) controls);
        BigraphFactory.createOrGetSignatureMetaModel((AbstractEcoreSignature<?>) sig);
        return sig;
    }

    public KindSignature createWith(Iterable<? extends Control<StringTypedName, FiniteOrdinal<Integer>>> controls,
                                    Collection<KindSort> kindSorts) {
        KindSignature sig = new KindSignature((Set<DefaultDynamicControl>) controls, kindSorts);
        BigraphFactory.createOrGetSignatureMetaModel((AbstractEcoreSignature<?>) sig);
        return sig;
    }

    public KindSignature createWith(Iterable<? extends Control<StringTypedName, FiniteOrdinal<Integer>>> controls,
                                    Collection<KindSort> kindSorts, EMetaModelData metaModelData) {
        KindSignature sig = new KindSignature((Set<DefaultDynamicControl>) controls, kindSorts);
        BigraphFactory.createOrGetSignatureMetaModel((AbstractEcoreSignature<?>) sig, metaModelData);
        return sig;
    }

    @Override
    public KindSignature createEmpty() {
        return (KindSignature) super.createEmpty();
    }

    @Override
    protected KindSignature createEmptyStub() {
        return new KindSignature(Collections.emptySet());
    }

}
