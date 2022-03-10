package de.tudresden.inf.st.bigraphs.core.impl;

import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.BigraphMetaModelConstants;
import de.tudresden.inf.st.bigraphs.core.Control;
import de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils;
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
 * @author Dominik Grzelak
 */
public class BigraphEntity<C extends Control> {

    private EObject instance;
    private C control;
    protected BigraphEntityType type;
    int hashed = -1;

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

    BigraphEntity(EObject instance, BigraphEntityType type) {
        this(instance, null, type);
    }

    /**
     * Copy constructor
     *
     * @param bigraphEntity
     */
    @Deprecated
    BigraphEntity(BigraphEntity<C> bigraphEntity) {
        this.instance = bigraphEntity.getInstance();
        this.control = bigraphEntity.getControl();
        this.type = bigraphEntity.getType();
    }

    @NonNull
    public EObject getInstance() {
        return instance;
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
        if (hashed == -1) {
            hashed = Objects.hash(instance, control, type);
        }
        return hashed;
//        return Objects.hash(instance, control, type);
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
        String toString = null;

        InnerName(@NonNull EObject instance) {
            super(instance, null, BigraphEntityType.INNER_NAME);
        }

        public String getName() {
            EAttribute nameAttr = EMFUtils.findAttribute(getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
            Object name = getInstance().eGet(nameAttr);
            return String.valueOf(name);
        }

        @Override
        public String toString() {
            if (Objects.isNull(toString)) {
                toString = new StringBuilder(getName()).append(":").append("InnerName").toString();
            }
            return toString;
        }
    }

    public static class Link extends BigraphEntity {

        Link(@NonNull EObject instance, BigraphEntityType type) {
            super(instance, type);
        }

        public String getName() {
            EAttribute nameAttr = EMFUtils.findAttribute(getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
            Object name = getInstance().eGet(nameAttr);
            return String.valueOf(name);
        }

    }

    public static class OuterName extends Link {
        String toString = null;

        OuterName(@NonNull EObject instance) {
            super(instance, BigraphEntityType.OUTER_NAME);
        }

        @Override
        public String toString() {
            if (Objects.isNull(toString)) {
                toString = new StringBuilder(getName()).append(":").append("OuterName").toString();
            }
            return toString;
        }
    }

    public static class Edge extends Link {
        String toString = null;

        Edge(EObject instance) {
            super(instance, BigraphEntityType.EDGE);
        }

        @Override
        public String toString() {
            if (Objects.isNull(toString)) {
                toString = new StringBuilder(getName()).append(":").append("Edge").toString();
            }
            return toString;
        }
    }

    public static class NodeEntity<C extends Control> extends BigraphEntity<C> implements Comparable<BigraphEntity.NodeEntity<C>> {
        String toString = null;

        NodeEntity(@NonNull EObject instance, C control) {
            super(instance, control, BigraphEntityType.NODE);
        }

        public String getName() {
            EAttribute nameAttr = EMFUtils.findAttribute(getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
            return nameAttr != null ? String.valueOf(getInstance().eGet(nameAttr)) : "";
        }

        public void setName(String nodeName) {
            EAttribute nameAttr = EMFUtils.findAttribute(getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_NAME);
            getInstance().eSet(nameAttr, nodeName);
        }

        @Override
        public String toString() {
            if (Objects.isNull(toString)) {
                toString = new StringBuilder(getName())
                        .append(":").append("NodeEntity").append("{")
                        .append(getControl().getNamedType().stringValue()).append(":").append(getControl().getArity().getValue())
                        .append("}").toString();
            }
            return toString;
        }

        @Override
        public int compareTo(NodeEntity<C> o) {
            return this.getName().compareTo(o.getName());
        }
    }

    public static class SiteEntity extends BigraphEntity implements Comparable<BigraphEntity.SiteEntity> {
        int index;
        String toString = null;

        public SiteEntity() {
            super(null, BigraphEntityType.SITE);
            EAttribute nameAttr = EMFUtils.findAttribute(getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX);
            Object name = getInstance().eGet(nameAttr);
            index = (Integer) name;
        }

        SiteEntity(@NonNull EObject instance) {
            super(instance, BigraphEntityType.SITE);
            EAttribute nameAttr = EMFUtils.findAttribute(getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX);
            Object name = getInstance().eGet(nameAttr);
            index = (Integer) name;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public String toString() {
            if (Objects.isNull(toString)) {
                toString = new StringBuilder("").append(getIndex())
                        .append(":").append("Site").toString();
            }
            return toString;
        }

        @Override
        public int compareTo(BigraphEntity.SiteEntity otherPort) {
            return Integer.compare(this.getIndex(), otherPort.getIndex());
        }
    }

    public static class Port extends BigraphEntity implements Comparable<BigraphEntity.Port> {
        String toString = null;

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

        @Override
        public String toString() {
            if (Objects.isNull(toString)) {
                toString = new StringBuilder("Port").append(":").append(getIndex()).toString();
            }
            return toString;
        }

        @Override
        public int compareTo(BigraphEntity.Port otherPort) {
            return Integer.compare(this.getIndex(), otherPort.getIndex());
        }
    }


    public static class RootEntity extends BigraphEntity implements Comparable<BigraphEntity.RootEntity> {
        int index;
        String toString = null;

        RootEntity() {
            super(null, BigraphEntityType.ROOT);
            EAttribute nameAttr = EMFUtils.findAttribute(getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX);
            Object name = getInstance().eGet(nameAttr);
            index = (Integer) name;
        }

        RootEntity(@NonNull EObject instance) {
            super(instance, BigraphEntityType.ROOT);
            EAttribute nameAttr = EMFUtils.findAttribute(getInstance().eClass(), BigraphMetaModelConstants.ATTRIBUTE_INDEX);
            Object name = getInstance().eGet(nameAttr);
            index = (Integer) name;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public String toString() {
            if ((toString) == null) {
                toString = new StringBuilder("").append(getIndex()).append(":").append("Root").toString();
            }
            return toString;
        }

        @Override
        public int compareTo(BigraphEntity.RootEntity otherRoot) {
            return Integer.compare(this.getIndex(), otherRoot.getIndex());
        }
    }
}



