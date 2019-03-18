package de.tudresden.inf.st.bigraphs.core;

public enum BigraphEntityType {
    NODE, ROOT, SITE, EDGE, OUTER_NAME, INNER_NAME;

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
}
