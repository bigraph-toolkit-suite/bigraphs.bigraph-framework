package de.tudresden.inf.st.bigraphs.core;

import de.tudresden.inf.st.bigraphs.core.impl.builder.BigraphEntity;

import java.util.Objects;

public enum BigraphEntityType {
    NODE, ROOT, SITE, EDGE, OUTER_NAME, INNER_NAME, PORT;

    public static boolean isRoot(BigraphEntity entity) {
        if (Objects.isNull(entity)) return false;
        return entity.getType() == BigraphEntityType.ROOT;
    }

    public static boolean isSite(BigraphEntity entity) {
        if (Objects.isNull(entity)) return false;
        return entity.getType() == BigraphEntityType.SITE;
    }

    public static boolean isNode(BigraphEntity entity) {
        if (Objects.isNull(entity)) return false;
        return entity.getType() == BigraphEntityType.NODE;
    }

    public static boolean isPlaceType(BigraphEntity entity) {
        return BigraphEntityType.isPlaceType(entity.getType());
    }

    public static boolean isLinkType(BigraphEntity entity) {
        return BigraphEntityType.isLinkType(entity.getType());
    }

    public static boolean isPointType(BigraphEntity entity) {
        return BigraphEntityType.isPointType(entity.getType());
    }

    public static boolean isPlaceType(BigraphEntityType type) {
        if (Objects.isNull(type)) return false;
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
        if (Objects.isNull(type)) return false;
        switch (type) {
            case INNER_NAME:
            case PORT:
                return true;
            default:
                return false;
        }
    }
}
