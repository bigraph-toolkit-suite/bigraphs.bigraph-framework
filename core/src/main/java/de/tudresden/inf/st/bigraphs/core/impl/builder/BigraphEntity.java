package de.tudresden.inf.st.bigraphs.core.impl.builder;

import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils;
import de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.NameableType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * Wrapper-like classes for the dynamic EMF model of bigraphs. Allowing the user to handle the bigraph entities
 * without getting in touch with Ecore objects.
 * <p>
 * Also, helps to access attributes, references, etc. easier by offering methods.
 * <p>
 * The objects cannot be instatiated with the constructor. Therefore, the builder shall be used.
 *
 * @param <C> type of the controls
 */
public class BigraphEntity<C extends Control> {

    private EObject instance;
    private C control;
    protected BigraphEntityType type;

    private BigraphEntity() {
    }

    private BigraphEntity(@NonNull EObject instance) {
        this(instance, null, null);
    }

    BigraphEntity(@NonNull EObject instance, C control, BigraphEntityType type) {
        this.instance = instance;
        this.control = control;
        this.type = type;
    }

    BigraphEntity(@NonNull EObject instance, BigraphEntityType type) {
        this(instance, null, type);
    }

    /**
     * Copy constructor
     *
     * @param bigraphEntity
     */
    @Deprecated
    BigraphEntity(BigraphEntity<C> bigraphEntity) {
        this.setInstance(bigraphEntity.getInstance());
        this.control = bigraphEntity.getControl();
        this.type = bigraphEntity.getType();
    }

    @NonNull
    public EObject getInstance() {
        return instance;
    }

    BigraphEntity setInstance(EObject instance) {
        this.instance = instance;
        return this;
    }
    
    public C getControl() {
        return control;
    }


    public BigraphEntityType getType() {
        return type;
    }

    EClass eClass() {
        return getInstance().eClass();
    }

    @Override
    public String toString() {
        return "BigraphEntity{" +
                "instance=" + instance.getClass() +
                ", control=" + control +
                ", type=" + type +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BigraphEntity)) return false;
        BigraphEntity<?> that = (BigraphEntity<?>) o;
        return instance.equals(that.instance) &&
                Objects.equals(control, that.control) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(instance, control, type);
    }

    @NonNull
    public static <T extends BigraphEntity> T create(@NonNull EObject param, @NonNull Class<T> tClass) {
        try {
            return tClass.getDeclaredConstructor(EObject.class).newInstance(param);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @NonNull
    public static NodeEntity<? extends Control> createNode(@NonNull EObject param, Control control) {
        return new NodeEntity<>(param, control);
    }

    public static class InnerName extends BigraphEntity {

        InnerName(@NonNull EObject instance) {
            super(instance, null, BigraphEntityType.INNER_NAME);
        }

        public String getName() {
            EAttribute nameAttr = EMFUtils.findAttribute(getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
            Object name = getInstance().eGet(nameAttr);
            return String.valueOf(name);
        }
    }

    public static class OuterName extends BigraphEntity {

        OuterName(@NonNull EObject instance) {
            super(instance, BigraphEntityType.OUTER_NAME);
        }

        public String getName() {
            EAttribute nameAttr = EMFUtils.findAttribute(getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
            Object name = getInstance().eGet(nameAttr);
            return String.valueOf(name);
        }

    }

    public static class Edge extends BigraphEntity {

        Edge(EObject instance) {
            super(instance, BigraphEntityType.EDGE);
        }

        public String getName() {
            EAttribute nameAttr = EMFUtils.findAttribute(getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
            Object name = getInstance().eGet(nameAttr);
            return String.valueOf(name);
        }
    }

    public static class NodeEntity<C extends Control> extends BigraphEntity<C> {

        NodeEntity(@NonNull EObject instance, C control) {
            super(instance, control, BigraphEntityType.NODE);
        }

        public String getName() {
            EAttribute nameAttr = EMFUtils.findAttribute(getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
            Object name = getInstance().eGet(nameAttr);
            return String.valueOf(name);
        }

        public void setName(String nodeName) {
            EAttribute nameAttr = EMFUtils.findAttribute(getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
            getInstance().eSet(nameAttr, nodeName);
        }
    }

    public static class SiteEntity extends BigraphEntity {

        public SiteEntity() {
            super(null, BigraphEntityType.SITE);
        }

        SiteEntity(@NonNull EObject instance) {
            super(instance, BigraphEntityType.SITE);
        }

        public int getIndex() {
            EAttribute nameAttr = EMFUtils.findAttribute(getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX);
            Object name = getInstance().eGet(nameAttr);
            return (Integer) name;
        }

    }

    public static class Port extends BigraphEntity {

        public Port() {
            super(null, BigraphEntityType.PORT);
        }

        Port(@NonNull EObject instance) {
            super(instance, BigraphEntityType.PORT);
        }

        public int getIndex() {
            EAttribute nameAttr = EMFUtils.findAttribute(getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX);
            Object name = getInstance().eGet(nameAttr);
            return (Integer) name;
        }

        public void setIndex(int index) {
            EAttribute idxAttr = EMFUtils.findAttribute(getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX);
            getInstance().eSet(idxAttr, index);
        }

    }


    public static class RootEntity extends BigraphEntity {

        RootEntity() {
            super(null, BigraphEntityType.ROOT);
        }

        RootEntity(@NonNull EObject instance) {
            super(instance, BigraphEntityType.ROOT);
        }

        public int getIndex() {
            EAttribute nameAttr = EMFUtils.findAttribute(getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX);
            Object name = getInstance().eGet(nameAttr);
            return (Integer) name;
        }
    }
}



