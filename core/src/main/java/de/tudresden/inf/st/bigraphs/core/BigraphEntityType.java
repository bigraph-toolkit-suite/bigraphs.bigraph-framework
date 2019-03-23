package de.tudresden.inf.st.bigraphs.core;

public enum BigraphEntityType {
    NODE, ROOT, SITE, EDGE, OUTER_NAME, INNER_NAME, PORT;

    public static boolean isPlaceType(BigraphEntityType type) {
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
        switch (type) {
            case OUTER_NAME:
            case EDGE:
                return true;
            default:
                return false;
        }
    }

    public static boolean isPointType(BigraphEntityType type) {
        switch (type) {
            case INNER_NAME:
            case PORT:
                return true;
            default:
                return false;
        }
    }
}
