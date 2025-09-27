package org.bigraphs.framework.core;

import org.bigraphs.framework.core.impl.BigraphEntity;

import java.util.Objects;

/**
 * Enums for all bigraph element types.
 *
 * @author Dominik Grzelak
 */
public enum BigraphEntityType {
    NODE, ROOT, SITE, EDGE, OUTER_NAME, INNER_NAME, PORT;

    /**
     * If entity is {@code null}, then {@code false} will be returned
     *
     * @param entity the entity to check
     * @return {@code true} if the given entity is a root, otherwise {@code false}.
     */
    public static boolean isRoot(BigraphEntity entity) {
        if ((entity) == null) return false;
        return entity.getType() == BigraphEntityType.ROOT;
    }

    /**
     * If entity is {@code null}, then {@code false} will be returned
     *
     * @param entity the entity to check
     * @return {@code true} if the given entity is a site, otherwise {@code false}.
     */
    public static boolean isSite(BigraphEntity entity) {
        if ((entity) == null) return false;
        return entity.getType() == BigraphEntityType.SITE;
    }

    /**
     * If entity is {@code null}, then {@code false} will be returned
     *
     * @param entity the entity to check
     * @return {@code true} if the given entity is a node, otherwise {@code false}.
     */
    public static boolean isNode(BigraphEntity entity) {
        if (entity == null) return false;
        return entity.getType() == BigraphEntityType.NODE;
    }


    /**
     * If entity is {@code null}, then {@code false} will be returned
     *
     * @param entity the entity to check
     * @return {@code true} if the given entity is a port, otherwise {@code false}.
     */
    public static boolean isPort(BigraphEntity entity) {
        if ((entity) == null) return false;
        return entity.getType() == BigraphEntityType.PORT;
    }

    /**
     * If entity is {@code null}, then {@code false} will be returned
     *
     * @param entity the entity to check
     * @return {@code true} if the given entity is an inner name, otherwise {@code false}.
     */
    public static boolean isInnerName(BigraphEntity entity) {
        if ((entity) == null) return false;
        return entity.getType() == BigraphEntityType.INNER_NAME;
    }

    /**
     * If entity is {@code null}, then {@code false} will be returned
     *
     * @param entity the entity to check
     * @return {@code true} if the given entity is an outer name, otherwise {@code false}.
     */
    public static boolean isOuterName(BigraphEntity entity) {
        if ((entity) == null) return false;
        return entity.getType() == BigraphEntityType.OUTER_NAME;
    }

    /**
     * If entity is {@code null}, then {@code false} will be returned
     *
     * @param entity the entity to check
     * @return {@code true} if the given entity is an edge, otherwise {@code false}.
     */
    public static boolean isEdge(BigraphEntity entity) {
        if ((entity) == null) return false;
        return entity.getType() == BigraphEntityType.EDGE;
    }

    /**
     * A place is a node, root or site.
     * If entity is {@code null}, then {@code false} will be returned
     *
     * @param entity the entity to check
     * @return {@code true} if the given entity is a place, otherwise {@code false}.
     */
    public static boolean isPlaceType(BigraphEntity entity) {
        return BigraphEntityType.isPlaceType(entity.getType());
    }

    /**
     * A link is an edge or outer name.
     * If entity is {@code null}, then {@code false} will be returned
     *
     * @param entity the entity to check
     * @return {@code true} if the given entity is a link, otherwise {@code false}.
     */
    public static boolean isLinkType(BigraphEntity entity) {
        return BigraphEntityType.isLinkType(entity.getType());
    }

    /**
     * A point is a port or an inner name.
     * If entity is {@code null}, then {@code false} will be returned
     *
     * @param entity the entity to check
     * @return {@code true} if the given entity is a point, otherwise {@code false}.
     */
    public static boolean isPointType(BigraphEntity entity) {
        return BigraphEntityType.isPointType(entity.getType());
    }

    /**
     * Checks if the bigraph type is a place type.
     * If entity is {@code null}, then {@code false} will be returned
     *
     * @param type the entity to check
     * @return {@code true} if the given type is a place, otherwise {@code false}.
     */
    public static boolean isPlaceType(BigraphEntityType type) {
        if ((type) == null) return false;
        switch (type) {
            case NODE:
            case SITE:
            case ROOT:
                return true;
            default:
                return false;
        }
    }

    public static boolean isLinkType(BigraphEntityType type) {
        if (Objects.isNull(type)) return false;
        switch (type) {
            case OUTER_NAME:
            case EDGE:
                return true;
            default:
                return false;
        }
    }

    public static boolean isPointType(BigraphEntityType type) {
        if ((type) == null) return false;
        switch (type) {
            case INNER_NAME:
            case PORT:
                return true;
            default:
                return false;
        }
    }
}
