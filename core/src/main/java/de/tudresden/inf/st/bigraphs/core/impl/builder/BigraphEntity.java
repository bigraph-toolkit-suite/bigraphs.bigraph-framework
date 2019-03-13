package de.tudresden.inf.st.bigraphs.core.impl.builder;

import de.tudresden.inf.st.bigraphs.core.BigraphEntityType;
import de.tudresden.inf.st.bigraphs.core.Control;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * Wrapper-like classes for the dynamic EMF model of bigraphs. Allowing the user to handle the bigraph entities
 * without getting in touch with Ecore objects.
 * <p>
 * The objects cannot be instatiated with the constructor. Therefore, the builder shall be used.
 *
 * @param <C> type of the controls
 */
public class BigraphEntity<C extends Control<?, ?>> {


    private EObject instance;
    private C control;
    protected BigraphEntityType type;

    public BigraphEntity() {
    }

    public BigraphEntity(EObject instance) {
        this.instance = instance;
    }

    BigraphEntity(EObject instance, C control, BigraphEntityType type) {
        this.instance = instance;
        this.control = control;
        this.type = type;
    }

    BigraphEntity(EObject instance, BigraphEntityType type) {
        this(instance, null, type);
    }

    BigraphEntity(BigraphEntity bigraphEntity) {
        this.setInstance(bigraphEntity.getInstance());
        this.control = (C) bigraphEntity.getControl();
        this.type = bigraphEntity.getType();
    }

    @Nullable
    EObject getInstance() {
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
        return Objects.isNull(getInstance()) ? null : getInstance().eClass();
    }

    @NonNull
    public static <T extends BigraphEntity> T create(@Nullable EObject param, @NonNull Class<T> tClass) {
        try {
            if (Objects.isNull(param)) {
                return tClass.newInstance();
            } else {
                return tClass.getDeclaredConstructor(EObject.class).newInstance(param);
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    @NonNull
    public static <C extends Control<?, ?>> NodeEntity<C> createNode(@Nullable EObject param, C control) {
        return new NodeEntity<>(param, control);
    }

    public static class InnerName extends BigraphEntity {

        InnerName() {
            super(null, BigraphEntityType.INNER_NAME);
        }

        InnerName(@NonNull EObject instance) {
            super(instance, null, BigraphEntityType.INNER_NAME);
        }

    }

    public static class OuterName extends BigraphEntity {

        OuterName() {
            super(null, BigraphEntityType.OUTER_NAME);
        }

        OuterName(EObject instance) {
            super(instance, BigraphEntityType.OUTER_NAME);
        }

    }

    public static class Edge extends BigraphEntity {

        Edge() {
            super(null, BigraphEntityType.EDGE);
        }

        Edge(EObject instance) {
            super(instance, BigraphEntityType.EDGE);
        }

    }

    public static class NodeEntity<C> extends BigraphEntity {

        public NodeEntity() {
            super(null, BigraphEntityType.NODE);
        }

        NodeEntity(@NonNull EObject instance, C control) {
            super(instance, (Control<?, ?>) control, BigraphEntityType.NODE);
        }

    }

    public static class SiteEntity extends BigraphEntity {

        public SiteEntity() {
            super(null, BigraphEntityType.SITE);
        }

        SiteEntity(@NonNull EObject instance) {
            super(instance, BigraphEntityType.SITE);
        }

    }

    public static class RootEntity extends BigraphEntity {

        RootEntity() {
            super(null, BigraphEntityType.ROOT);
        }

        RootEntity(@NonNull EObject instance) {
            super(instance, BigraphEntityType.ROOT);
        }

    }
}



